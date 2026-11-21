/** Jämför text utan hänsyn till versaler och accenter (så att "vasby" hittar "Väsby"). */
export function normalize(text: string): string {
  return text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

/**
 * Kortare programnamn: "Teknikprogrammet" → "Teknik",
 * "Naturvetenskapsprogrammet" → "Naturvetenskap", "Vård- och omsorgsprogrammet" → "Vård och omsorg".
 */
export function shortProgramName(name: string): string {
  if (!name.endsWith('programmet')) {
    return name
  }
  return name
    .slice(0, -'programmet'.length)
    .replace(/s$/, '') // fogemorfem: "Naturvetenskaps" → "Naturvetenskap"
    .replace(/s?- och /, ' och ') // "Försäljnings- och service" → "Försäljning och service"
    .trim() // "Industritekniska programmet" (två ord) → "Industritekniska"
}
