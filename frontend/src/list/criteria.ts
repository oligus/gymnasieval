import type { Criterion, WatchStatus } from '../gql/graphql'

/** Kriterierna i den ordning de visas, med texter från originalappen. */
export const CRITERIA: readonly { key: Criterion; label: string; hint: string }[] = [
  { key: 'RESTID', label: 'Restid', hint: 'Dörr till dörr, antal byten' },
  { key: 'INRIKTNING', label: 'Inriktningar', hint: 'Finns det du vill läsa?' },
  { key: 'POANG', label: 'Antagningspoäng', hint: 'Studiemiljö och konkurrens' },
  { key: 'RYKTE', label: 'Rykte & resultat', hint: 'Examen, betyg, vidare till högskola' },
  { key: 'PROFIL', label: 'Profil', hint: 'Robotik, spel, spets, fjärde år …' },
  { key: 'MILJO', label: 'Skolmiljö', hint: 'Lokaler, labb, storlek, stämning' },
  { key: 'BESOK', label: 'Intryck öppet hus', hint: 'Fyll i efter besöket' },
]

export const STATUSES: readonly { value: WatchStatus | null; label: string; dot: string }[] = [
  { value: null, label: 'Ej vald', dot: 'bg-stone-300 dark:bg-stone-600' },
  { value: 'INTRESSANT', label: 'Intressant', dot: 'bg-sky-500' },
  { value: 'BESOK', label: 'Ska besöka', dot: 'bg-brand-500' },
  { value: 'BESOKT', label: 'Besökt', dot: 'bg-emerald-500' },
  { value: 'BORTVALD', label: 'Bortvald', dot: 'bg-rose-500' },
]

export function formatScore(value: number): string {
  return value.toLocaleString('sv-SE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })
}
