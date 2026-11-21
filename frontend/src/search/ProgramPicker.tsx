import { Disclosure, DisclosureButton, DisclosurePanel, Popover, PopoverButton, PopoverPanel } from '@headlessui/react'
import { ChevronDown, GraduationCap, Search } from 'lucide-react'
import { useState } from 'react'
import type { ProgramCategory } from '../gql/graphql'
import { normalize, shortProgramName } from '../lib/text'
import { SearchField, dropdownPanelClass, fieldControlClass } from './SearchField'

export type PickerProgram = { code: string; name: string; category: ProgramCategory }

type Props = {
  programs: readonly PickerProgram[]
  value: string
  onChange: (code: string) => void
}

/**
 * Programväljare: de 18 nationella programmen som klickbara kort i två grupper,
 * en sökruta som söker bland alla program, och övriga utbildningar ihopfällda.
 */
export function ProgramPicker({ programs, value, onChange }: Props) {
  const [query, setQuery] = useState('')
  const selected = programs.find((p) => p.code === value)

  const byCategory = (category: ProgramCategory) => programs.filter((p) => p.category === category)
  const matches = query.trim() ? programs.filter((p) => normalize(p.name).includes(normalize(query.trim()))) : []

  return (
    <Popover>
      {/* Hela fältet är knappen, så att panelen placeras mot fältet och inte bara mot texten. */}
      <PopoverButton className="block h-full w-full outline-none data-open:[&>span]:bg-brand-50/60 dark:data-open:[&>span]:bg-brand-950/40">
        <SearchField icon={<GraduationCap />} label="Program">
          <span className={`${fieldControlClass} flex items-center justify-between gap-2`}>
            <span className="truncate">{selected?.name ?? 'Välj program'}</span>
            <ChevronDown className="size-4 shrink-0 text-stone-400" />
          </span>
        </SearchField>
      </PopoverButton>

      <PopoverPanel
        anchor="bottom start"
        transition
        className={`${dropdownPanelClass} max-h-(--anchor-max-height) w-[min(44rem,calc(100vw-2rem))] overflow-y-auto p-4 transition duration-150 ease-out data-closed:-translate-y-1 data-closed:opacity-0`}
      >
        {({ close }) => {
          const pick = (code: string) => {
            onChange(code)
            setQuery('')
            close()
          }
          return (
            <>
              <label className="mb-4 flex items-center gap-2 rounded-xl border border-stone-200 px-3 py-2 focus-within:border-brand-400 focus-within:ring-2 focus-within:ring-brand-400/20 dark:border-stone-700">
                <Search className="size-4 text-stone-400" />
                <input
                  autoFocus
                  className="w-full bg-transparent outline-none placeholder:text-stone-400"
                  placeholder="Sök bland alla program och utbildningar…"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                />
              </label>

              {query.trim() ? (
                <ProgramList programs={matches} value={value} onPick={pick} emptyText="Inga program matchar sökningen." />
              ) : (
                <div className="space-y-5">
                  <ProgramGroup title="Högskoleförberedande" programs={byCategory('HOGSKOLEFORBEREDANDE')} value={value} onPick={pick} />
                  <ProgramGroup title="Yrkesprogram" programs={byCategory('YRKES')} value={value} onPick={pick} />
                  <Disclosure>
                    <DisclosureButton className="group flex w-full items-center gap-1.5 text-sm font-medium text-stone-500 hover:text-stone-800 dark:hover:text-stone-200">
                      <ChevronDown className="size-4 -rotate-90 transition group-data-open:rotate-0" />
                      Fler utbildningar ({byCategory('OVRIGA').length})
                      <span className="font-normal">· riksrekryterande, introduktionsprogram, IB</span>
                    </DisclosureButton>
                    <DisclosurePanel className="mt-2 max-h-64 overflow-y-auto">
                      <ProgramList programs={byCategory('OVRIGA')} value={value} onPick={pick} emptyText="" />
                    </DisclosurePanel>
                  </Disclosure>
                </div>
              )}
            </>
          )
        }}
      </PopoverPanel>
    </Popover>
  )
}

function ProgramGroup({ title, programs, value, onPick }: {
  title: string
  programs: readonly PickerProgram[]
  value: string
  onPick: (code: string) => void
}) {
  return (
    <section>
      <h3 className="mb-2 text-xs font-semibold tracking-wide text-stone-500 uppercase">{title}</h3>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
        {programs.map((p) => (
          <button
            key={p.code}
            type="button"
            title={p.name}
            onClick={() => onPick(p.code)}
            className={`rounded-xl border px-3 py-2 text-left text-sm font-medium transition ${
              p.code === value
                ? 'border-brand-500 bg-brand-50 text-brand-700 ring-2 ring-brand-500/20 dark:bg-brand-950/60 dark:text-brand-200'
                : 'border-stone-200 hover:border-brand-300 hover:bg-brand-50/50 dark:border-stone-700 dark:hover:border-brand-700 dark:hover:bg-brand-950/30'
            }`}
          >
            {shortProgramName(p.name)}
          </button>
        ))}
      </div>
    </section>
  )
}

function ProgramList({ programs, value, onPick, emptyText }: {
  programs: readonly PickerProgram[]
  value: string
  onPick: (code: string) => void
  emptyText: string
}) {
  if (programs.length === 0) {
    return <p className="px-2 py-4 text-sm text-stone-500">{emptyText}</p>
  }
  return (
    <ul className="max-h-72 overflow-y-auto">
      {programs.map((p) => (
        <li key={p.code}>
          <button
            type="button"
            onClick={() => onPick(p.code)}
            className={`w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-stone-100 dark:hover:bg-stone-800 ${
              p.code === value ? 'font-semibold text-brand-700 dark:text-brand-300' : ''
            }`}
          >
            {p.name}
          </button>
        </li>
      ))}
    </ul>
  )
}
