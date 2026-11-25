import { useQuery } from '@apollo/client/react'
import { ArrowUpDown, Sparkles, Star } from 'lucide-react'
import { useMemo } from 'react'
import { Link } from 'react-router'
import { useLocalStorage } from '../lib/useLocalStorage'
import { useMerit } from '../workspace/useMerit'
import { useWorkspace } from '../workspace/workspaceContext'
import { MyListQuery } from './listDocuments'
import { WatchedSchoolCard, type WatchedSchool } from './WatchedSchoolCard'
import { WeightsPanel } from './WeightsPanel'

type SortOrder = 'total' | 'name' | 'points'

/** Min lista: rangordnade bevakade skolor med betyg, vikter och meritvärde (som originalappen). */
export function MyListPage() {
  const { key, readOnly } = useWorkspace()
  const [sort, setSort] = useLocalStorage('list.sort', 'total')
  const [hideOutText, setHideOutText] = useLocalStorage('list.hideOut', 'false')
  const hideOut = hideOutText === 'true'
  const { merit } = useMerit()

  const { data, loading } = useQuery(MyListQuery, { skip: !key })
  const workspace = key ? data?.workspace : null
  const watchedSchools = workspace?.watchedSchools
  const watched = watchedSchools ?? []

  const sorted = useMemo(
    () => sortWatched(watchedSchools ?? [], sort as SortOrder).filter((w) => !hideOut || w.status !== 'BORTVALD'),
    [watchedSchools, sort, hideOut],
  )
  const outCount = watched.filter((w) => w.status === 'BORTVALD').length

  return (
    <section className="mx-auto max-w-5xl px-4 pt-28 sm:pt-32">
      <h1 className="text-3xl font-semibold tracking-tight sm:text-4xl">Min lista</h1>
      <p className="mt-2 max-w-2xl text-stone-600 dark:text-stone-400">
        {readOnly
          ? 'En delad lista. Du kan titta men inte ändra.'
          : 'Sätt betyg 1–5 på det som är viktigt för dig. Klicka på samma prick igen för att ta bort betyget. Totalpoängen är ett viktat medel av de betyg som är satta.'}
      </p>

      {loading && !data && <p className="mt-8 text-stone-500">Laddar…</p>}

      {!loading && watched.length === 0 && <EmptyList />}

      {workspace && watched.length > 0 && (
        <>
          <div className="mt-8 grid gap-4 lg:grid-cols-[1fr_2fr]">
            <MeritPanel />
            <WeightsPanel weights={workspace.weights} readOnly={readOnly} />
          </div>

          <div className="mt-10 mb-4 flex flex-wrap items-end justify-between gap-3">
            <h2 className="text-2xl font-semibold tracking-tight">
              Rangordning <span className="text-base font-normal text-stone-500">· {watched.length} skolor</span>
            </h2>
            <div className="flex flex-wrap items-center gap-3 text-sm">
              {outCount > 0 && (
                <label className="flex items-center gap-2 text-stone-600 dark:text-stone-400">
                  <input
                    type="checkbox"
                    checked={hideOut}
                    onChange={(e) => setHideOutText(String(e.target.checked))}
                    className="accent-brand-600"
                  />
                  Dölj bortvalda ({outCount})
                </label>
              )}
              <label className="flex items-center gap-2 rounded-lg border border-stone-200 bg-white px-3 py-1.5 shadow-sm dark:border-stone-800 dark:bg-stone-900">
                <ArrowUpDown className="size-4 text-stone-400" />
                <select className="bg-transparent outline-none" value={sort} onChange={(e) => setSort(e.target.value)}>
                  <option value="total">Totalpoäng</option>
                  <option value="points">Antagningspoäng</option>
                  <option value="name">Namn</option>
                </select>
              </label>
            </div>
          </div>

          <ol className="space-y-4">
            {sorted.map((w, i) => (
              <li key={w.school.code}>
                <WatchedSchoolCard watched={w} rank={i + 1} merit={merit} readOnly={readOnly} />
              </li>
            ))}
          </ol>
        </>
      )}
    </section>
  )
}

function MeritPanel() {
  const { text, setText, merit, readOnly } = useMerit()
  const invalid = text.trim() !== '' && merit == null

  return (
    <section className="rounded-2xl border border-stone-200 bg-white p-5 dark:border-stone-800 dark:bg-stone-900">
      <h2 className="flex items-center gap-2 font-semibold">
        <Sparkles className="size-4 text-brand-600 dark:text-brand-400" />
        Meritvärde
      </h2>
      <p className="mt-1 text-xs text-stone-500">
        Max 340. Uppskatta från senaste terminsbetygen (A = 20, B = 17,5, C = 15, D = 12,5, E = 10).
      </p>
      <input
        inputMode="decimal"
        value={text}
        readOnly={readOnly}
        onChange={(e) => setText(e.target.value)}
        placeholder="t.ex. 232,5"
        className="mt-3 w-36 rounded-xl border border-stone-200 bg-stone-50 px-3 py-2 text-lg font-semibold tabular-nums outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-400/20 dark:border-stone-700 dark:bg-stone-950"
      />
      {invalid && <p className="mt-2 text-sm text-red-600 dark:text-red-400">Ett tal mellan 0 och 340.</p>}
      <div className="mt-4 flex flex-wrap gap-1.5 text-xs">
        <span className="rounded-full px-2 py-0.5 text-emerald-700 ring-1 ring-emerald-600/30 dark:text-emerald-300">Säker · minst 10 över</span>
        <span className="rounded-full px-2 py-0.5 text-amber-700 ring-1 ring-amber-600/30 dark:text-amber-300">Trolig · inom ±10</span>
        <span className="rounded-full px-2 py-0.5 text-rose-700 ring-1 ring-rose-600/30 dark:text-rose-300">Chans · mer än 10 under</span>
      </div>
    </section>
  )
}

function EmptyList() {
  return (
    <div className="mt-8 rounded-2xl border border-dashed border-stone-300 px-6 py-16 text-center dark:border-stone-700">
      <Star className="mx-auto mb-3 size-8 text-stone-400" />
      <p className="font-medium">Inga bevakade skolor än</p>
      <p className="mt-1 text-sm text-stone-500">Sök fram skolor och klicka på ☆ för att lägga till dem här.</p>
      <Link to="/" className="mt-5 inline-block rounded-xl bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">
        Till sökningen
      </Link>
    </div>
  )
}

function minPoints(w: WatchedSchool): number | null {
  return w.school.programs.find((p) => p.program.code === w.program.code)?.admissionPointsMin ?? null
}

/** Totalpoäng: högst först, bortvalda och skolor utan betyg sist (som i originalet). */
function sortWatched(list: readonly WatchedSchool[], order: SortOrder): WatchedSchool[] {
  const byName = (a: WatchedSchool, b: WatchedSchool) => a.school.name.localeCompare(b.school.name, 'sv')
  const nullsLast = (a: number | null | undefined, b: number | null | undefined) =>
    a == null ? (b == null ? 0 : 1) : b == null ? -1 : b - a
  return [...list].sort((a, b) => {
    if (order === 'name') return byName(a, b)
    if (order === 'points') return nullsLast(minPoints(a), minPoints(b)) || byName(a, b)
    const outA = a.status === 'BORTVALD' ? 1 : 0
    const outB = b.status === 'BORTVALD' ? 1 : 0
    return outA - outB || nullsLast(a.totalScore, b.totalScore) || byName(a, b)
  })
}
