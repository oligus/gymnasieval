import { useEffect, useState } from 'react'

/**
 * Som useState, men värdet sparas i webbläsarens localStorage och finns kvar
 * efter omladdning. Fungerar även om localStorage är blockerat (privat läge).
 */
export function useLocalStorage(key: string, initialValue: string): [string, (value: string) => void] {
  const [value, setValue] = useState(() => {
    try {
      return localStorage.getItem(key) ?? initialValue
    } catch {
      return initialValue
    }
  })

  useEffect(() => {
    try {
      localStorage.setItem(key, value)
    } catch {
      // Ingen lagring tillgänglig; värdet gäller då bara tills sidan laddas om.
    }
  }, [key, value])

  return [value, setValue]
}
