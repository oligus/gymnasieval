import { createContext, useContext } from 'react'
import type { WorkspaceFieldsFragment } from '../gql/graphql'

export type WorkspaceState = {
  /** Arbetsytan, eller null om ingen finns än (eller nyckeln är ogiltig). */
  workspace: WorkspaceFieldsFragment | null
  /** Nyckeln som används just nu (redigering eller läsning). */
  key: string | null
  /** true med en läslänk: inget får ändras. */
  readOnly: boolean
  isWatched: (schoolCode: string) => boolean
  /** Bevakar eller slutar bevaka. Skapar arbetsytan vid första bevakningen. */
  toggleWatch: (schoolCode: string, programCode: string) => Promise<void>
  openShare: () => void
  /** Sluta använda den delade listan (t.ex. efter en läslänk). */
  leaveWorkspace: () => void
}

export const WorkspaceContext = createContext<WorkspaceState | null>(null)

export function useWorkspace(): WorkspaceState {
  const state = useContext(WorkspaceContext)
  if (!state) throw new Error('useWorkspace måste användas inuti <WorkspaceProvider>')
  return state
}
