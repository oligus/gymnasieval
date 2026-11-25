import { Disclosure, DisclosureButton, DisclosurePanel, Listbox, ListboxButton, ListboxOption, ListboxOptions } from '@headlessui/react'
import { useMutation } from '@apollo/client/react'
import { Check, ChevronDown, ExternalLink, MapPin, NotebookPen, Star } from 'lucide-react'
import { useEffect, useState } from 'react'
import type { Criterion, MyListWorkspaceFragment, WatchStatus } from '../gql/graphql'
import { admissionChance, formatPoints } from '../lib/chance'
import { useWorkspace } from '../workspace/workspaceContext'
import { CRITERIA, STATUSES, formatScore } from './criteria'
import { SetNoteMutation, SetScoreMutation, SetStatusMutation } from './listDocuments'

export type WatchedSchool = MyListWorkspaceFragment['watchedSchools'][number]

type Props = {
  watched: WatchedSchool
  rank: number
  merit: number | null
  readOnly: boolean
}

const chanceStyles = {
  SAFE: { label: 'Säker', className: 'text-emerald-700 ring-emerald-600/30 dark:text-emerald-300 dark:ring-emerald-400/30' },
  LIKELY: { label: 'Trolig', className: 'text-amber-700 ring-amber-600/30 dark:text-amber-300 dark:ring-amber-400/30' },
  REACH: { label: 'Chans', className: 'text-rose-700 ring-rose-600/30 dark:text-rose-300 dark:ring-rose-400/30' },
} as const

/** Ett kort i Min lista, i originalappens stil: rang, totalpoäng, status, betyg och anteckningar. */
export function WatchedSchoolCard({ watched, rank, merit, readOnly }: Props) {
  const { school, program } = watched
  const offered = school.programs.find((p) => p.program.code === program.code)
  const chance = admissionChance(merit, offered?.admissionPointsMin)
  const location = [...new Set([school.city, school.municipality?.name].filter(Boolean))].join(', ')
  const out = watched.status === 'BORTVALD'

  return (
    <article
      className={`rounded-2xl border border-stone-200 bg-white p-5 shadow-sm transition dark:border-stone-800 dark:bg-stone-900 ${
        out ? 'opacity-55' : ''
      }`}
    >
      {/* Rang, namn och totalpoäng */}
      <div className="grid grid-cols-[2rem_1fr_auto] items-start gap-3 sm:grid-cols-[2.5rem_1fr_auto]">
        <span className="pt-0.5 text-xl font-semibold text-stone-400 tabular-nums">{rank}</span>
        <div className="min-w-0">
          <h3 className="leading-snug font-semibold">
            {school.webUrl ? (
              <a href={school.webUrl} target="_blank" rel="noreferrer" className="group inline-flex items-start gap-1 hover:text-brand-600 dark:hover:text-brand-400">
                {school.name}
                <ExternalLink className="mt-1 size-3.5 shrink-0 opacity-40 group-hover:opacity-80" />
              </a>
            ) : (
              school.name
            )}
          </h3>
          <p className="mt-0.5 flex flex-wrap items-center gap-x-2 text-sm text-stone-500">
            {location && (
              <span className="inline-flex items-center gap-1">
                <MapPin className="size-3.5" />
                {location}
              </span>
            )}
            <span>· {program.name}</span>
          </p>
        </div>
        <TotalScore value={watched.totalScore ?? null} missing={watched.missingScores} />
      </div>

      {/* Status, poäng och inriktningar */}
      <div className="mt-4 flex flex-wrap items-center gap-2 sm:pl-[3.25rem]">
        <StatusSelect schoolCode={school.code} value={watched.status ?? null} readOnly={readOnly} />
        {offered?.admissionPointsMin != null && (
          <span className="rounded-full bg-stone-100 px-2.5 py-1 text-xs font-medium dark:bg-stone-800">
            Lägsta {offered.admissionYear}: <span className="tabular-nums">{formatPoints(offered.admissionPointsMin)}</span>
          </span>
        )}
        {chance && (
          <span className={`rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset ${chanceStyles[chance.level].className}`}>
            {chanceStyles[chance.level].label} {chance.margin > 0 ? '+' : ''}
            {formatPoints(chance.margin)}
          </span>
        )}
        {offered?.studyPaths.map((s) => (
          <span key={s.code} className="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-950/60 dark:text-brand-300">
            {s.name}
          </span>
        ))}
        <span className="ml-auto">
          <UnwatchButton schoolCode={school.code} schoolName={school.name} />
        </span>
      </div>

      {/* Betyg per kriterium */}
      <div className="mt-5 grid grid-cols-2 gap-x-4 gap-y-3 sm:grid-cols-3 sm:pl-[3.25rem] lg:grid-cols-4">
        {CRITERIA.map((c) => (
          <ScoreDots
            key={c.key}
            schoolCode={school.code}
            criterion={c.key}
            label={c.label}
            hint={c.hint}
            value={watched.scores.find((s) => s.criterion === c.key)?.score ?? null}
            readOnly={readOnly}
          />
        ))}
      </div>

      {/* Anteckningar */}
      <Disclosure as="div" className="mt-4 border-t border-stone-100 pt-3 sm:ml-[3.25rem] dark:border-stone-800">
        <DisclosureButton className="group inline-flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400">
          <NotebookPen className="size-4" />
          Anteckningar{watched.note ? ' ·' : ''}
          {watched.note && <span className="max-w-60 truncate font-normal text-stone-500">{watched.note}</span>}
          <ChevronDown className="size-4 transition group-data-open:rotate-180" />
        </DisclosureButton>
        <DisclosurePanel className="mt-2">
          <NoteEditor schoolCode={school.code} initial={watched.note ?? ''} readOnly={readOnly} />
        </DisclosurePanel>
      </Disclosure>
    </article>
  )
}

function TotalScore({ value, missing }: { value: number | null; missing: number }) {
  return (
    <div className="text-right">
      <p className="text-3xl leading-none font-semibold tracking-tight tabular-nums">
        {value != null ? formatScore(value) : '–'}
      </p>
      <p className="mt-1 text-xs text-stone-500">
        av 5{missing > 0 && ` · ${missing} ej satt`}
      </p>
      <div className="mt-1.5 ml-auto h-1.5 w-24 overflow-hidden rounded-full bg-stone-200 dark:bg-stone-800">
        <div
          className="h-full rounded-full bg-gradient-to-r from-brand-500 to-fuchsia-500 transition-[width] duration-300"
          style={{ width: `${((value ?? 0) / 5) * 100}%` }}
        />
      </div>
    </div>
  )
}

function StatusSelect({ schoolCode, value, readOnly }: { schoolCode: string; value: WatchStatus | null; readOnly: boolean }) {
  const [saveStatus] = useMutation(SetStatusMutation)
  const current = STATUSES.find((s) => s.value === value) ?? STATUSES[0]

  return (
    <Listbox value={value} onChange={(status) => void saveStatus({ variables: { school: schoolCode, status } })} disabled={readOnly}>
      <ListboxButton className="inline-flex items-center gap-2 rounded-full border border-stone-200 px-2.5 py-1 text-xs font-medium hover:border-brand-300 data-disabled:cursor-default data-disabled:hover:border-stone-200 dark:border-stone-700">
        <span className={`size-2 rounded-full ${current.dot}`} />
        {current.label}
        {!readOnly && <ChevronDown className="size-3.5 text-stone-400" />}
      </ListboxButton>
      <ListboxOptions
        anchor="bottom start"
        className="z-50 w-44 rounded-xl border border-stone-200 bg-white p-1 shadow-xl [--anchor-gap:6px] focus:outline-none dark:border-stone-800 dark:bg-stone-900"
      >
        {STATUSES.map((s) => (
          <ListboxOption
            key={s.label}
            value={s.value}
            className="group flex cursor-pointer items-center gap-2 rounded-lg px-2.5 py-1.5 text-sm select-none data-focus:bg-stone-100 dark:data-focus:bg-stone-800"
          >
            <span className={`size-2 rounded-full ${s.dot}`} />
            <span className="flex-1">{s.label}</span>
            <Check className="invisible size-4 text-brand-600 group-data-selected:visible" />
          </ListboxOption>
        ))}
      </ListboxOptions>
    </Listbox>
  )
}

function ScoreDots({ schoolCode, criterion, label, hint, value, readOnly }: {
  schoolCode: string
  criterion: Criterion
  label: string
  hint: string
  value: number | null
  readOnly: boolean
}) {
  const [saveScore, { loading }] = useMutation(SetScoreMutation)

  return (
    <div className="min-w-0">
      <p className="truncate text-xs text-stone-500" title={hint}>
        {label}
        {value == null && <span className="text-amber-600 dark:text-amber-400"> · ej satt</span>}
      </p>
      <div className="mt-1 flex gap-1" role="group" aria-label={`${label}, betyg 1–5`}>
        {[1, 2, 3, 4, 5].map((n) => {
          const on = value != null && n <= value
          return (
            <button
              key={n}
              type="button"
              disabled={readOnly || loading}
              aria-label={`${label} ${n} av 5`}
              aria-pressed={value === n}
              title={value === n ? 'Klicka igen för att ta bort betyget' : hint}
              onClick={() => void saveScore({ variables: { school: schoolCode, criterion, score: value === n ? null : n } })}
              className={`grid size-6 place-items-center rounded-full border text-[10px] font-medium tabular-nums transition ${
                on
                  ? 'border-brand-600 bg-brand-600 text-white'
                  : 'border-stone-300 text-stone-400 hover:border-brand-400 hover:text-brand-600 dark:border-stone-700'
              } disabled:cursor-default ${readOnly ? '' : 'disabled:opacity-70'}`}
            >
              {n}
            </button>
          )
        })}
      </div>
    </div>
  )
}

const NOTE_SAVE_DELAY_MS = 800

function NoteEditor({ schoolCode, initial, readOnly }: { schoolCode: string; initial: string; readOnly: boolean }) {
  const [text, setText] = useState(initial)
  const [state, setState] = useState<'idle' | 'saving' | 'saved'>('idle')
  const [saveNote] = useMutation(SetNoteMutation)

  useEffect(() => {
    if (readOnly || text === initial) return
    const timer = setTimeout(() => {
      setState('saving')
      void saveNote({ variables: { school: schoolCode, note: text } }).then(() => setState('saved'))
    }, NOTE_SAVE_DELAY_MS)
    return () => clearTimeout(timer)
  }, [text, initial, readOnly, schoolCode, saveNote])

  return (
    <div>
      <textarea
        value={text}
        readOnly={readOnly}
        onChange={(e) => {
          setText(e.target.value)
          setState('idle')
        }}
        rows={3}
        maxLength={5000}
        placeholder={readOnly ? 'Inga anteckningar.' : 'Intryck, frågor att ställa, vad lärarna sa …'}
        className="w-full resize-y rounded-xl border border-stone-200 bg-stone-50 px-3 py-2 text-sm outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-400/20 dark:border-stone-700 dark:bg-stone-950"
      />
      <p className="mt-1 h-4 text-xs text-stone-500">
        {state === 'saving' && 'Sparar …'}
        {state === 'saved' && 'Sparat.'}
      </p>
    </div>
  )
}

/** Stjärnknapp för att ta bort skolan ur listan. */
function UnwatchButton({ schoolCode, schoolName }: { schoolCode: string; schoolName: string }) {
  const { toggleWatch, readOnly } = useWorkspace()
  if (readOnly) return null
  return (
    <button
      type="button"
      aria-label={`Sluta bevaka ${schoolName}`}
      title="Ta bort från Min lista"
      onClick={() => void toggleWatch(schoolCode, '')}
      className="grid size-8 place-items-center rounded-full border border-amber-300 bg-amber-50 text-amber-500 dark:border-amber-700 dark:bg-amber-950/50"
    >
      <Star className="size-4 fill-current" />
    </button>
  )
}
