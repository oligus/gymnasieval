import { useApolloClient, useMutation, useQuery } from '@apollo/client/react'
import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { graphql } from '../gql'
import { parseMerit } from '../lib/chance'
import { ShareDialog } from './ShareDialog'
import { SwitchWorkspaceDialog } from './SwitchWorkspaceDialog'
import { WorkspaceContext, type WorkspaceState } from './workspaceContext'
import { LOCAL_MERIT_KEY, SetMeritMutation } from './useMerit'
import { resolvePendingUrlKey, setWorkspaceKey, usePendingUrlKey, useWorkspaceKey } from './workspaceKey'

function readLocal(key: string): string {
  try {
    return localStorage.getItem(key) ?? ''
  } catch {
    return ''
  }
}

graphql(`
  fragment WorkspaceFields on Workspace {
    id
    access
    viewKey
    meritValue
    watchedSchools {
      school {
        code
      }
      program {
        code
      }
    }
  }
`)

const WorkspaceQuery = graphql(`
  query Workspace {
    workspace {
      ...WorkspaceFields
    }
  }
`)

const CreateWorkspaceMutation = graphql(`
  mutation CreateWorkspace {
    createWorkspace {
      editKey
    }
  }
`)

const WatchSchoolMutation = graphql(`
  mutation WatchSchool($school: ID!, $program: ID!) {
    watchSchool(school: $school, program: $program) {
      ...WorkspaceFields
    }
  }
`)

const UnwatchSchoolMutation = graphql(`
  mutation UnwatchSchool($school: ID!) {
    unwatchSchool(school: $school) {
      ...WorkspaceFields
    }
  }
`)

export function WorkspaceProvider({ children }: { children: ReactNode }) {
  const client = useApolloClient()
  const key = useWorkspaceKey()
  const pendingKey = usePendingUrlKey()
  const [shareOpen, setShareOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const { data, loading } = useQuery(WorkspaceQuery, { skip: !key })
  const workspace = key ? (data?.workspace ?? null) : null

  const [createWorkspace] = useMutation(CreateWorkspaceMutation)
  const [watchSchool] = useMutation(WatchSchoolMutation)
  const [unwatchSchool] = useMutation(UnwatchSchoolMutation)
  const [saveMerit] = useMutation(SetMeritMutation)

  // En länk till samma arbetsyta (t.ex. läslänken till den egna listan) behöver ingen fråga.
  const pendingIsSameWorkspace = pendingKey != null && workspace?.viewKey === pendingKey
  const storedKeyIsInvalid = pendingKey != null && key != null && !loading && workspace == null
  useEffect(() => {
    if (pendingIsSameWorkspace) resolvePendingUrlKey(false)
    else if (storedKeyIsInvalid) resolvePendingUrlKey(true) // sparad nyckel är ogiltig: byt utan att fråga
  }, [pendingIsSameWorkspace, storedKeyIsInvalid])

  const switchTo = async (accept: boolean) => {
    resolvePendingUrlKey(accept)
    if (accept) await client.resetStore() // glöm den gamla arbetsytans data
  }

  const state = useMemo<WorkspaceState>(() => {
    const watched = new Set(workspace?.watchedSchools.map((w) => w.school.code))
    return {
      workspace,
      key,
      readOnly: workspace?.access === 'VIEW',
      isWatched: (code) => watched.has(code),
      toggleWatch: async (schoolCode, programCode) => {
        setError(null)
        try {
          if (watched.has(schoolCode)) {
            await unwatchSchool({ variables: { school: schoolCode } })
            return
          }
          const isNew = !key
          if (isNew) {
            const created = await createWorkspace()
            setWorkspaceKey(created.data!.createWorkspace.editKey)
          }
          await watchSchool({ variables: { school: schoolCode, program: programCode } })
          if (isNew) {
            // Meritvärdet som skrevs in innan listan fanns följer med in i arbetsytan.
            const localMerit = parseMerit(readLocal(LOCAL_MERIT_KEY))
            if (localMerit != null) await saveMerit({ variables: { merit: localMerit } })
            setShareOpen(true) // visa länkarna en gång, direkt när listan skapats
          }
        } catch (e) {
          setError(e instanceof Error ? e.message : 'Något gick fel.')
        }
      },
      openShare: () => setShareOpen(true),
      leaveWorkspace: () => {
        setWorkspaceKey(null)
        void client.resetStore()
      },
    }
  }, [workspace, key, client, createWorkspace, watchSchool, unwatchSchool, saveMerit])

  return (
    <WorkspaceContext.Provider value={state}>
      {children}
      {workspace && key && workspace.access === 'EDIT' && (
        <ShareDialog open={shareOpen} onClose={() => setShareOpen(false)} editKey={key} viewKey={workspace.viewKey} />
      )}
      <SwitchWorkspaceDialog
        open={pendingKey != null && !pendingIsSameWorkspace && !storedKeyIsInvalid && !loading}
        onAnswer={(accept) => void switchTo(accept)}
      />
      {error && (
        <div
          role="alert"
          className="fixed inset-x-4 bottom-4 z-50 mx-auto max-w-md rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800 shadow-lg dark:border-red-900 dark:bg-red-950 dark:text-red-200"
        >
          <div className="flex items-start justify-between gap-3">
            <span>Kunde inte spara: {error}</span>
            <button type="button" className="font-medium underline" onClick={() => setError(null)}>
              Stäng
            </button>
          </div>
        </div>
      )}
    </WorkspaceContext.Provider>
  )
}
