import { useMutation } from '@apollo/client/react'
import { useEffect, useState } from 'react'
import { graphql } from '../gql'
import { parseMerit } from '../lib/chance'
import { useLocalStorage } from '../lib/useLocalStorage'
import { useWorkspace } from './workspaceContext'

export const SetMeritMutation = graphql(`
  mutation SetMerit($merit: Float) {
    setMerit(merit: $merit) {
      id
      meritValue
    }
  }
`)

/** localStorage-nyckeln för meritvärdet innan någon arbetsyta finns. */
export const LOCAL_MERIT_KEY = 'search.merit'

const SAVE_DELAY_MS = 600

/** "232,5" eller "240" för visning i en textruta. */
export function formatMeritInput(value: number): string {
  return Number.isInteger(value) ? String(value) : String(value).replace('.', ',')
}

/**
 * Meritvärdet: sparas i arbetsytan när en sådan finns (så att det följer med till andra
 * enheter och till läslänken), annars bara i webbläsaren. Sparas en kort stund efter
 * att användaren slutat skriva.
 */
export function useMerit() {
  const { workspace, readOnly } = useWorkspace()
  const [localText, setLocalText] = useLocalStorage(LOCAL_MERIT_KEY, '')
  const [draft, setDraft] = useState<string | null>(null)
  const [saveMerit] = useMutation(SetMeritMutation)

  const serverText = workspace?.meritValue != null ? formatMeritInput(workspace.meritValue) : ''
  const text = workspace ? (draft ?? serverText) : localText

  useEffect(() => {
    if (draft == null || readOnly) return
    const parsed = parseMerit(draft)
    if (draft.trim() !== '' && parsed == null) return // ogiltigt: vänta på bättre inmatning
    const timer = setTimeout(() => {
      void saveMerit({ variables: { merit: parsed } }).then(() => setDraft(null))
    }, SAVE_DELAY_MS)
    return () => clearTimeout(timer)
  }, [draft, readOnly, saveMerit])

  // Arbetsytor som skapades innan meritvärdet sparades där: flytta över det lokala värdet en gång.
  const canAdoptLocal = workspace != null && !readOnly && workspace.meritValue == null && draft == null
  const localMerit = parseMerit(localText)
  useEffect(() => {
    if (canAdoptLocal && localMerit != null) {
      void saveMerit({ variables: { merit: localMerit } })
    }
  }, [canAdoptLocal, localMerit, saveMerit])

  const setText = (value: string) => {
    if (readOnly) return
    setLocalText(value) // spegla lokalt, så att värdet finns kvar även utan arbetsyta
    if (workspace) setDraft(value)
  }

  return { text, setText, merit: parseMerit(text), readOnly }
}
