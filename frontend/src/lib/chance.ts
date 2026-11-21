export type ChanceLevel = 'SAFE' | 'LIKELY' | 'REACH'

export type Chance = {
  level: ChanceLevel
  /** Meritvärde minus lägsta antagningspoäng. Positivt = över gränsen. */
  margin: number
}

/** Hur långt från gränsen som räknas som "trolig" i båda riktningar. */
const LIKELY_RANGE = 10

/**
 * Chansen att komma in, utifrån förra årets lägsta antagningspoäng:
 * minst 10 över = säker, inom ±10 = trolig, mer än 10 under = chans.
 * Räknas i frontend så att den uppdateras direkt när meritvärdet ändras.
 */
export function admissionChance(merit: number | null, minPoints: number | null | undefined): Chance | null {
  if (merit == null || minPoints == null) {
    return null
  }
  const margin = merit - minPoints
  const level: ChanceLevel = margin >= LIKELY_RANGE ? 'SAFE' : margin >= -LIKELY_RANGE ? 'LIKELY' : 'REACH'
  return { level, margin }
}

/** Tolkar ett meritvärde som användaren skrivit, med punkt eller komma. */
export function parseMerit(text: string): number | null {
  const value = Number(text.trim().replace(',', '.'))
  if (text.trim() === '' || Number.isNaN(value) || value < 0 || value > 340) {
    return null
  }
  return value
}

export function formatPoints(points: number): string {
  return points.toLocaleString('sv-SE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })
}
