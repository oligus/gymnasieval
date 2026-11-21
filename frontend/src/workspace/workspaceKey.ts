import { useSyncExternalStore } from 'react'

/**
 * Nyckeln till arbetsytan: sparas i localStorage och kan komma in via länken (#k=…).
 * En liten "store" så att React-komponenter uppdateras när nyckeln byts.
 */

const STORAGE_KEY = 'workspace.key'
const KEY_FORMAT = /^[A-Za-z0-9_-]{22}$/

let currentKey: string | null = readStoredKey()
/** Nyckel från länken som skiljer sig från den sparade; användaren får bekräfta bytet. */
let pendingUrlKey: string | null = null
const listeners = new Set<() => void>()

function readStoredKey(): string | null {
  try {
    const key = localStorage.getItem(STORAGE_KEY)
    return key && KEY_FORMAT.test(key) ? key : null
  } catch {
    return null
  }
}

function notify() {
  listeners.forEach((listener) => listener())
}

export function getWorkspaceKey(): string | null {
  return currentKey
}

export function setWorkspaceKey(key: string | null) {
  currentKey = key
  try {
    if (key) localStorage.setItem(STORAGE_KEY, key)
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Ingen lagring (privat läge): nyckeln gäller tills sidan laddas om.
  }
  notify()
}

export function getPendingUrlKey(): string | null {
  return pendingUrlKey
}

export function resolvePendingUrlKey(accept: boolean) {
  if (accept && pendingUrlKey) {
    setWorkspaceKey(pendingUrlKey)
  }
  pendingUrlKey = null
  notify()
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function useWorkspaceKey(): string | null {
  return useSyncExternalStore(subscribe, getWorkspaceKey)
}

export function usePendingUrlKey(): string | null {
  return useSyncExternalStore(subscribe, getPendingUrlKey)
}

/**
 * Körs en gång innan appen renderas. Läser #k=… ur adressen och tar sedan bort det,
 * så att nyckeln inte råkar delas vidare via skärmdumpar eller kopierade adresser.
 */
export function initWorkspaceKeyFromUrl() {
  const match = window.location.hash.match(/[#&]k=([A-Za-z0-9_-]{22})/)
  if (!match) return
  window.history.replaceState(null, '', window.location.pathname + window.location.search)
  const urlKey = match[1]
  if (!currentKey) {
    setWorkspaceKey(urlKey)
  } else if (urlKey !== currentKey) {
    pendingUrlKey = urlKey
  }
}

export function workspaceLink(key: string): string {
  return `${window.location.origin}/#k=${key}`
}
