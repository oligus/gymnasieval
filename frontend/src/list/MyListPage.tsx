import { useQuery } from '@apollo/client/react'
import { Star } from 'lucide-react'
import { Link } from 'react-router'
import { graphql } from '../gql'
import { parseMerit } from '../lib/chance'
import { useLocalStorage } from '../lib/useLocalStorage'
import { SchoolCard } from '../search/SchoolCard'
import { useWorkspace } from '../workspace/workspaceContext'

const MyListQuery = graphql(`
  query MyList {
    workspace {
      id
      watchedSchools {
        program {
          code
          name
        }
        school {
          code
          ...SchoolCard
        }
      }
    }
  }
`)

/** Min lista (del A): de bevakade skolorna. Betyg, vikter och totalpoäng kommer i nästa steg. */
export function MyListPage() {
  const { key, readOnly } = useWorkspace()
  const [meritText] = useLocalStorage('search.merit', '')
  const merit = parseMerit(meritText)
  const { data, loading } = useQuery(MyListQuery, { skip: !key })
  const watched = key ? (data?.workspace?.watchedSchools ?? []) : []

  return (
    <section className="mx-auto max-w-5xl px-4 pt-28 sm:pt-32">
      <h1 className="text-3xl font-semibold tracking-tight sm:text-4xl">Min lista</h1>
      <p className="mt-2 text-stone-600 dark:text-stone-400">
        {readOnly ? 'En delad lista.' : 'Skolorna du bevakar.'}{' '}
        {watched.length > 0 && `${watched.length} ${watched.length === 1 ? 'skola' : 'skolor'}.`}
      </p>

      <div className="mt-8">
        {loading && !data && <p className="text-stone-500">Laddar…</p>}

        {!loading && watched.length === 0 && (
          <div className="rounded-2xl border border-dashed border-stone-300 px-6 py-16 text-center dark:border-stone-700">
            <Star className="mx-auto mb-3 size-8 text-stone-400" />
            <p className="font-medium">Inga bevakade skolor än</p>
            <p className="mt-1 text-sm text-stone-500">
              Sök fram skolor och klicka på ☆ för att lägga till dem här.
            </p>
            <Link
              to="/"
              className="mt-5 inline-block rounded-xl bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700"
            >
              Till sökningen
            </Link>
          </div>
        )}

        <ul className="grid gap-4 md:grid-cols-2">
          {watched.map((w) => (
            <li key={w.school.code}>
              <p className="mb-1.5 text-xs font-medium tracking-wide text-stone-500 uppercase">{w.program.name}</p>
              <SchoolCard school={w.school} programCode={w.program.code} merit={merit} />
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}
