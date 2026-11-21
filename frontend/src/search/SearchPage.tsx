import { useQuery } from '@apollo/client/react'
import { useMemo } from 'react'
import { graphql } from '../gql'
import { parseMerit } from '../lib/chance'
import { useLocalStorage } from '../lib/useLocalStorage'
import { SchoolCard, type SearchResultSchool } from './SchoolCard'

const SearchOptionsQuery = graphql(`
  query SearchOptions {
    programs {
      code
      name
    }
    municipalities {
      code
      name
    }
  }
`)

const SearchSchoolsQuery = graphql(`
  query SearchSchools($program: ID!, $municipality: ID) {
    schools(program: $program, municipality: $municipality) {
      code
      ...SchoolCard
    }
  }
`)

type SortOrder = 'name' | 'points-asc' | 'points-desc'

const selectClass =
  'w-full rounded-lg border border-stone-300 bg-white px-3 py-2 dark:border-stone-700 dark:bg-stone-900'

export function SearchPage() {
  const [program, setProgram] = useLocalStorage('search.program', 'TE25')
  const [municipality, setMunicipality] = useLocalStorage('search.municipality', '')
  const [meritText, setMeritText] = useLocalStorage('search.merit', '')
  const [sort, setSort] = useLocalStorage('search.sort', 'name')
  const merit = parseMerit(meritText)

  const options = useQuery(SearchOptionsQuery)
  const search = useQuery(SearchSchoolsQuery, {
    variables: { program, municipality: municipality || null },
  })
  const schools = (search.data ?? search.previousData)?.schools

  const sorted = useMemo(
    () => (schools ? sortSchools(schools, program, sort as SortOrder) : []),
    [schools, program, sort],
  )

  return (
    <div>
      <h2 className="mb-4 text-xl font-semibold">Hitta gymnasieskolor</h2>

      <div className="mb-6 grid gap-3 sm:grid-cols-[2fr_2fr_1fr]">
        <label className="text-sm">
          <span className="mb-1 block text-stone-600 dark:text-stone-400">Program</span>
          <select className={selectClass} value={program} onChange={(e) => setProgram(e.target.value)}>
            {options.data?.programs.map((p) => (
              <option key={p.code} value={p.code}>
                {p.name}
              </option>
            )) ?? <option value={program}>Laddar…</option>}
          </select>
        </label>

        <label className="text-sm">
          <span className="mb-1 block text-stone-600 dark:text-stone-400">Kommun</span>
          <select className={selectClass} value={municipality} onChange={(e) => setMunicipality(e.target.value)}>
            <option value="">Hela Sverige</option>
            {options.data?.municipalities.map((m) => (
              <option key={m.code} value={m.code}>
                {m.name}
              </option>
            ))}
          </select>
        </label>

        <label className="text-sm">
          <span className="mb-1 block text-stone-600 dark:text-stone-400">Ditt meritvärde</span>
          <input
            className={selectClass}
            inputMode="decimal"
            placeholder="t.ex. 232,5"
            value={meritText}
            onChange={(e) => setMeritText(e.target.value)}
          />
        </label>
      </div>

      {search.error && (
        <div className="rounded-lg bg-red-50 p-4 text-red-800 dark:bg-red-950 dark:text-red-200">
          <p>Kunde inte hämta skolor.</p>
          <p className="mt-1 font-mono text-xs opacity-80">{search.error.message}</p>
        </div>
      )}

      {schools && (
        <div className="mb-3 flex items-center justify-between text-sm text-stone-600 dark:text-stone-400">
          <span>
            {schools.length} {schools.length === 1 ? 'skola' : 'skolor'}
            {search.loading && ' · uppdaterar…'}
          </span>
          <label className="flex items-center gap-2">
            Sortera
            <select
              className="rounded-lg border border-stone-300 bg-white px-2 py-1 dark:border-stone-700 dark:bg-stone-900"
              value={sort}
              onChange={(e) => setSort(e.target.value)}
            >
              <option value="name">Namn</option>
              <option value="points-desc">Antagningspoäng, högst först</option>
              <option value="points-asc">Antagningspoäng, lägst först</option>
            </select>
          </label>
        </div>
      )}

      {!schools && search.loading && <p className="text-stone-500">Laddar skolor…</p>}

      <ul className="space-y-3">
        {sorted.map((school) => (
          <li key={school.code}>
            <SchoolCard school={school} programCode={program} merit={merit} />
          </li>
        ))}
      </ul>
    </div>
  )
}

function minPoints(school: SearchResultSchool, programCode: string): number | null {
  return school.programs.find((p) => p.program.code === programCode)?.admissionPointsMin ?? null
}

function sortSchools<T extends SearchResultSchool & { code: string }>(
  schools: readonly T[],
  programCode: string,
  order: SortOrder,
): T[] {
  if (order === 'name') {
    return [...schools] // redan sorterade på namn från servern
  }
  const direction = order === 'points-asc' ? 1 : -1
  return [...schools].sort((a, b) => {
    const pa = minPoints(a, programCode)
    const pb = minPoints(b, programCode)
    if (pa == null) return pb == null ? 0 : 1 // skolor utan poäng sist
    if (pb == null) return -1
    return (pa - pb) * direction
  })
}
