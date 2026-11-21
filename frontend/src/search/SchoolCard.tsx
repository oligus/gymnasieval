import { ExternalLink, MapPin, Star } from 'lucide-react'
import { useState } from 'react'
import { graphql } from '../gql'
import type { OrganizerType, SchoolCardFragment } from '../gql/graphql'
import { admissionChance, formatPoints, type ChanceLevel } from '../lib/chance'
import { useWorkspace } from '../workspace/workspaceContext'

// Fragmentet beskriver exakt vilka fält kortet behöver. Codegen gör om det till en TypeScript-typ.
graphql(`
  fragment SchoolCard on School {
    code
    name
    city
    organizerType
    webUrl
    municipality {
      code # nyckelfält för Apollos cache (keyFields i apollo.ts), måste alltid vara med
      name
    }
    programs {
      program {
        code
      }
      admissionPointsMin
      admissionYear
      admissionNote
      studyPaths {
        code
        name
      }
    }
  }
`)

export type SearchResultSchool = SchoolCardFragment

const organizerLabels: Record<OrganizerType, string> = {
  KOMMUNAL: 'Kommunal',
  KOMMUNALFORBUND: 'Kommunalförbund',
  REGION: 'Region',
  FRISTAENDE: 'Fristående',
}

const chanceStyles: Record<ChanceLevel, { label: string; badge: string; dot: string }> = {
  SAFE: {
    label: 'Säker',
    badge: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20 dark:bg-emerald-950/50 dark:text-emerald-300 dark:ring-emerald-400/20',
    dot: 'bg-emerald-500',
  },
  LIKELY: {
    label: 'Trolig',
    badge: 'bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-950/50 dark:text-amber-300 dark:ring-amber-400/20',
    dot: 'bg-amber-500',
  },
  REACH: {
    label: 'Chans',
    badge: 'bg-rose-50 text-rose-700 ring-rose-600/20 dark:bg-rose-950/50 dark:text-rose-300 dark:ring-rose-400/20',
    dot: 'bg-rose-500',
  },
}

type Props = {
  school: SearchResultSchool
  programCode: string
  merit: number | null
}

export function SchoolCard({ school, programCode, merit }: Props) {
  const program = school.programs.find((p) => p.program.code === programCode)
  const chance = admissionChance(merit, program?.admissionPointsMin)
  const location = [...new Set([school.city, school.municipality?.name].filter(Boolean))].join(', ')

  return (
    <article className="group flex h-full flex-col rounded-2xl border border-stone-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-lg hover:shadow-brand-900/5 dark:border-stone-800 dark:bg-stone-900 dark:hover:border-brand-900">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <h3 className="leading-snug font-semibold">
            {school.webUrl ? (
              <a
                href={school.webUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-start gap-1 hover:text-brand-600 dark:hover:text-brand-400"
              >
                {school.name}
                <ExternalLink className="mt-1 size-3.5 shrink-0 opacity-0 transition group-hover:opacity-60" />
              </a>
            ) : (
              school.name
            )}
          </h3>
          <p className="mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-sm text-stone-500">
            {location && (
              <span className="inline-flex items-center gap-1">
                <MapPin className="size-3.5" />
                {location}
              </span>
            )}
            {school.organizerType && (
              <span className="rounded-md bg-stone-100 px-1.5 py-0.5 text-xs font-medium text-stone-600 dark:bg-stone-800 dark:text-stone-300">
                {organizerLabels[school.organizerType]}
              </span>
            )}
          </p>
        </div>

        <div className="flex shrink-0 items-start gap-3">
          <div className="text-right">
          {program?.admissionPointsMin != null ? (
            <>
              <p className="text-2xl font-semibold tracking-tight tabular-nums">
                {formatPoints(program.admissionPointsMin)}
              </p>
              <p className="text-xs text-stone-500">lägsta {program.admissionYear}</p>
            </>
          ) : (
            <p className="max-w-28 text-xs text-stone-500">{program?.admissionNote ?? 'Poäng saknas'}</p>
          )}
          </div>
          <WatchButton schoolCode={school.code} schoolName={school.name} programCode={programCode} />
        </div>
      </div>

      {program && program.studyPaths.length > 0 && (
        <ul className="mt-4 flex flex-wrap gap-1.5">
          {program.studyPaths.map((s) => (
            <li
              key={s.code}
              className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700 dark:bg-brand-950/60 dark:text-brand-300"
            >
              {s.name}
            </li>
          ))}
        </ul>
      )}

      {chance && (
        <div className="mt-auto pt-4">
          <span
            className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset ${chanceStyles[chance.level].badge}`}
          >
            <span className={`size-1.5 rounded-full ${chanceStyles[chance.level].dot}`} />
            {chanceStyles[chance.level].label}
            <span className="tabular-nums opacity-70">
              {chance.margin > 0 ? '+' : ''}
              {formatPoints(chance.margin)} p
            </span>
          </span>
        </div>
      )}
    </article>
  )
}

/** ☆/★ för att bevaka skolan. Första klicket skapar arbetsytan. Döljs med en läslänk. */
function WatchButton({ schoolCode, schoolName, programCode }: { schoolCode: string; schoolName: string; programCode: string }) {
  const { isWatched, toggleWatch, readOnly } = useWorkspace()
  const [busy, setBusy] = useState(false)
  if (readOnly) return null
  const watched = isWatched(schoolCode)

  return (
    <button
      type="button"
      disabled={busy}
      aria-pressed={watched}
      aria-label={watched ? `Sluta bevaka ${schoolName}` : `Bevaka ${schoolName}`}
      title={watched ? 'Bevakad – klicka för att ta bort från Min lista' : 'Bevaka – lägg till i Min lista'}
      onClick={async () => {
        setBusy(true)
        await toggleWatch(schoolCode, programCode)
        setBusy(false)
      }}
      className={`grid size-9 place-items-center rounded-full border transition disabled:opacity-50 ${
        watched
          ? 'border-amber-300 bg-amber-50 text-amber-500 dark:border-amber-700 dark:bg-amber-950/50'
          : 'border-stone-200 text-stone-400 hover:border-amber-300 hover:text-amber-500 dark:border-stone-700'
      }`}
    >
      <Star className={`size-4.5 ${watched ? 'fill-current' : ''}`} />
    </button>
  )
}
