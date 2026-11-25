import { useQuery } from '@apollo/client/react'
import { ArrowUpDown, SearchX, Sparkles } from 'lucide-react'
import { useMemo } from 'react'
import { graphql } from '../gql'
import { useLocalStorage } from '../lib/useLocalStorage'
import { useMerit } from '../workspace/useMerit'
import { MunicipalityPicker } from './MunicipalityPicker'
import { ProgramPicker } from './ProgramPicker'
import { SchoolCard, type SearchResultSchool } from './SchoolCard'
import { SearchField, fieldControlClass } from './SearchField'

const SearchOptionsQuery = graphql(`
  query SearchOptions {
    programs {
      code
      name
      category
    }
    municipalities {
      code
      name
      county
    }
  }
`)

const SearchSchoolsQuery = graphql(`
  query SearchSchools($program: ID!, $municipalities: [ID!]) {
    schools(program: $program, municipalities: $municipalities) {
      code
      ...SchoolCard
    }
  }
`)

type SortOrder = 'name' | 'points-asc' | 'points-desc'


export function SearchPage() {
  const [program, setProgram] = useLocalStorage('search.program', 'TE25')
  const [municipalitiesJson, setMunicipalitiesJson] = useLocalStorage('search.municipalities', '[]')
  const municipalities = parseCodes(municipalitiesJson)
  const { text: meritText, setText: setMeritText, merit, readOnly: meritReadOnly } = useMerit()
  const [sort, setSort] = useLocalStorage('search.sort', 'name')

  const options = useQuery(SearchOptionsQuery)
  const search = useQuery(SearchSchoolsQuery, {
    variables: { program, municipalities },
  })
  const schools = (search.data ?? search.previousData)?.schools

  const programs = options.data?.programs ?? []
  const programName = programs.find((p) => p.code === program)?.name
  const areaDescription = describeArea(municipalities, options.data?.municipalities ?? [])

  const sorted = useMemo(
    () => (schools ? sortSchools(schools, program, sort as SortOrder) : []),
    [schools, program, sort],
  )

  return (
    <>
      {/* ---- Hero ---- */}
      <section className="relative overflow-hidden pt-28 pb-16 sm:pt-36 sm:pb-24">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-0">
          <div className="absolute -top-40 left-1/2 size-[640px] -translate-x-1/2 rounded-full bg-brand-400/30 blur-3xl dark:bg-brand-600/25" />
          <div className="absolute top-20 -right-32 size-[420px] rounded-full bg-fuchsia-300/30 blur-3xl dark:bg-fuchsia-700/20" />
          <div className="absolute top-40 -left-32 size-[380px] rounded-full bg-sky-300/30 blur-3xl dark:bg-sky-700/20" />
        </div>

        <div className="relative mx-auto max-w-5xl px-4 text-center">
          <p className="mx-auto mb-5 inline-flex items-center gap-1.5 rounded-full border border-brand-200 bg-white/70 px-3 py-1 text-sm font-medium text-brand-700 backdrop-blur dark:border-brand-900 dark:bg-brand-950/50 dark:text-brand-300">
            <Sparkles className="size-4" />
            Antagningspoäng och program för hösten 2027
          </p>
          <h1 className="mx-auto max-w-3xl text-4xl font-semibold tracking-tight text-balance sm:text-6xl">
            Hitta gymnasiet som{' '}
            <span className="bg-gradient-to-r from-brand-600 via-fuchsia-500 to-sky-500 bg-clip-text text-transparent dark:from-brand-400 dark:via-fuchsia-400 dark:to-sky-400">
              passar dig
            </span>
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-pretty text-stone-600 dark:text-stone-400">
            Jämför skolor i hela Sverige. Se antagningspoäng, inriktningar och hur stor chans du har att
            komma in med ditt meritvärde.
          </p>

          {/* ---- Sökpanel ---- */}
          <div className="mx-auto mt-10 grid max-w-4xl gap-px overflow-hidden rounded-2xl bg-stone-200 text-left shadow-xl ring-1 shadow-brand-900/10 ring-stone-200 sm:grid-cols-[1.4fr_1.2fr_1fr] dark:bg-stone-800 dark:shadow-black/40 dark:ring-stone-800">
            <ProgramPicker programs={programs} value={program} onChange={setProgram} />
            <MunicipalityPicker
              municipalities={options.data?.municipalities ?? []}
              value={municipalities}
              onChange={(codes) => setMunicipalitiesJson(JSON.stringify(codes))}
            />

            <SearchField icon={<Sparkles />} label="Ditt meritvärde">
              <input
                className={fieldControlClass}
                inputMode="decimal"
                placeholder="t.ex. 232,5"
                value={meritText}
                readOnly={meritReadOnly}
                onChange={(e) => setMeritText(e.target.value)}
              />
            </SearchField>
          </div>
          {meritText.trim() !== '' && merit == null && (
            <p className="mt-3 text-sm text-red-600 dark:text-red-400">Meritvärdet ska vara ett tal mellan 0 och 340.</p>
          )}
        </div>
      </section>

      {/* ---- Resultat ---- */}
      <section className="mx-auto max-w-5xl px-4">
        {search.error && (
          <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-800 dark:border-red-900 dark:bg-red-950/50 dark:text-red-200">
            <p className="font-medium">Kunde inte hämta skolor.</p>
            <p className="mt-1 font-mono text-xs opacity-80">{search.error.message}</p>
          </div>
        )}

        {schools && (
          <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
            <div>
              <h2 className="text-2xl font-semibold tracking-tight">
                {schools.length} {schools.length === 1 ? 'skola' : 'skolor'}
              </h2>
              <p className="text-sm text-stone-500">
                {programName ?? program} · {areaDescription}
                {search.loading && ' · uppdaterar…'}
              </p>
            </div>
            <label className="flex items-center gap-2 rounded-lg border border-stone-200 bg-white px-3 py-1.5 text-sm shadow-sm dark:border-stone-800 dark:bg-stone-900">
              <ArrowUpDown className="size-4 text-stone-400" />
              <select className="bg-transparent outline-none" value={sort} onChange={(e) => setSort(e.target.value)}>
                <option value="name">Namn</option>
                <option value="points-desc">Poäng, högst först</option>
                <option value="points-asc">Poäng, lägst först</option>
              </select>
            </label>
          </div>
        )}

        {!schools && search.loading && <ResultSkeleton />}

        {schools?.length === 0 && (
          <div className="rounded-2xl border border-dashed border-stone-300 px-6 py-16 text-center dark:border-stone-700">
            <SearchX className="mx-auto mb-3 size-8 text-stone-400" />
            <p className="font-medium">Inga skolor hittades</p>
            <p className="mt-1 text-sm text-stone-500">Prova fler kommuner, eller ta bort alla för att söka i hela Sverige.</p>
          </div>
        )}

        <ul className="grid gap-4 md:grid-cols-2">
          {sorted.map((school) => (
            <li key={school.code}>
              <SchoolCard school={school} programCode={program} merit={merit} />
            </li>
          ))}
        </ul>
      </section>
    </>
  )
}

function parseCodes(json: string): string[] {
  try {
    const value: unknown = JSON.parse(json)
    return Array.isArray(value) ? value.filter((v): v is string => typeof v === 'string') : []
  } catch {
    return []
  }
}

/** "hela Sverige", "Täby", "Täby, Sollentuna och Upplands Väsby" eller "Täby, Sollentuna och 3 till". */
function describeArea(codes: readonly string[], all: readonly { code: string; name: string }[]): string {
  const names = codes.map((code) => all.find((m) => m.code === code)?.name ?? code)
  if (names.length === 0) return 'hela Sverige'
  if (names.length === 1) return names[0]
  if (names.length <= 3) return `${names.slice(0, -1).join(', ')} och ${names[names.length - 1]}`
  return `${names.slice(0, 2).join(', ')} och ${names.length - 2} till`
}

function ResultSkeleton() {
  return (
    <ul className="grid gap-4 md:grid-cols-2" aria-label="Laddar skolor">
      {Array.from({ length: 6 }, (_, i) => (
        <li
          key={i}
          className="h-40 animate-pulse rounded-2xl border border-stone-200 bg-white dark:border-stone-800 dark:bg-stone-900"
        />
      ))}
    </ul>
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
