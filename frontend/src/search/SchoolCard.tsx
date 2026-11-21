import { graphql } from '../gql'
import type { OrganizerType, SchoolCardFragment } from '../gql/graphql'
import { admissionChance, formatPoints, type ChanceLevel } from '../lib/chance'

// Fragmentet beskriver exakt vilka fält kortet behöver. Codegen gör om det till en TypeScript-typ.
graphql(`
  fragment SchoolCard on School {
    name
    city
    organizerType
    webUrl
    municipality {
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

const chanceStyles: Record<ChanceLevel, { label: string; className: string }> = {
  SAFE: { label: 'Säker', className: 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-300' },
  LIKELY: { label: 'Trolig', className: 'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300' },
  REACH: { label: 'Chans', className: 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-300' },
}

type Props = {
  school: SearchResultSchool
  programCode: string
  merit: number | null
}

export function SchoolCard({ school, programCode, merit }: Props) {
  const program = school.programs.find((p) => p.program.code === programCode)
  const chance = admissionChance(merit, program?.admissionPointsMin)
  const place = [school.city, school.municipality?.name].filter(Boolean)
  const location = [...new Set(place)].join(', ')

  return (
    <article className="rounded-xl border border-stone-200 bg-white p-4 dark:border-stone-800 dark:bg-stone-900">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h3 className="font-semibold">
            {school.webUrl ? (
              <a href={school.webUrl} target="_blank" rel="noreferrer" className="hover:underline">
                {school.name}
              </a>
            ) : (
              school.name
            )}
          </h3>
          <p className="text-sm text-stone-600 dark:text-stone-400">
            {location}
            {school.organizerType && ` · ${organizerLabels[school.organizerType]}`}
          </p>
        </div>

        <div className="text-right text-sm">
          {program?.admissionPointsMin != null ? (
            <p>
              Lägsta {program.admissionYear}:{' '}
              <span className="font-semibold">{formatPoints(program.admissionPointsMin)}</span>
            </p>
          ) : (
            <p className="text-stone-500">{program?.admissionNote ?? 'Antagningspoäng saknas'}</p>
          )}
          {chance && (
            <span
              className={`mt-1 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${chanceStyles[chance.level].className}`}
            >
              {chanceStyles[chance.level].label} ({chance.margin > 0 ? '+' : ''}
              {formatPoints(chance.margin)})
            </span>
          )}
        </div>
      </div>

      {program && program.studyPaths.length > 0 && (
        <p className="mt-2 text-sm text-stone-600 dark:text-stone-400">
          Inriktningar: {program.studyPaths.map((s) => s.name).join(', ')}
        </p>
      )}
    </article>
  )
}
