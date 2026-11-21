import { Combobox, ComboboxInput, ComboboxOption, ComboboxOptions } from '@headlessui/react'
import { Check, MapPin, X } from 'lucide-react'
import { useState } from 'react'
import { normalize } from '../lib/text'
import { SearchField, dropdownPanelClass } from './SearchField'

export type PickerMunicipality = { code: string; name: string; county?: string | null }

type Props = {
  municipalities: readonly PickerMunicipality[]
  value: readonly string[]
  onChange: (codes: string[]) => void
}

const MAX_OPTIONS = 50

/** Sökbar flervalslista: skriv för att filtrera, valda kommuner visas som chips. Tomt val = hela Sverige. */
export function MunicipalityPicker({ municipalities, value, onChange }: Props) {
  const [query, setQuery] = useState('')
  const byCode = new Map(municipalities.map((m) => [m.code, m]))
  const selected = value.map((code) => byCode.get(code)).filter((m): m is PickerMunicipality => m != null)

  const q = normalize(query.trim())
  const options = (q ? municipalities.filter((m) => normalize(m.name).includes(q)) : municipalities)
    .toSorted((a, b) => Number(normalize(b.name).startsWith(q)) - Number(normalize(a.name).startsWith(q)))
    .slice(0, MAX_OPTIONS)

  const remove = (code: string) => onChange(value.filter((c) => c !== code))

  return (
    <Combobox
      multiple
      immediate
      value={[...value]}
      onChange={(codes: string[]) => {
        onChange(codes)
        setQuery('')
      }}
      onClose={() => setQuery('')}
    >
      <SearchField icon={<MapPin />} label="Kommuner">
        <span className="flex flex-wrap items-center gap-1.5 py-0.5">
          {selected.map((m) => (
            <span
              key={m.code}
              className="inline-flex items-center gap-1 rounded-full bg-brand-100 py-0.5 pr-1 pl-2.5 text-sm font-medium text-brand-800 dark:bg-brand-900/60 dark:text-brand-100"
            >
              {m.name}
              <button
                type="button"
                aria-label={`Ta bort ${m.name}`}
                onClick={() => remove(m.code)}
                className="rounded-full p-0.5 hover:bg-brand-200 dark:hover:bg-brand-800"
              >
                <X className="size-3.5" />
              </button>
            </span>
          ))}
          <ComboboxInput
            className="min-w-24 flex-1 bg-transparent py-0.5 text-base font-medium text-stone-900 outline-none placeholder:font-normal placeholder:text-stone-400 dark:text-white"
            placeholder={selected.length === 0 ? 'Hela Sverige' : 'Lägg till…'}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Backspace' && query === '' && value.length > 0) {
                remove(value[value.length - 1]) // Backspace i tom ruta tar bort sista chipet
              }
            }}
          />
        </span>
      </SearchField>

      <ComboboxOptions
        anchor="bottom start"
        transition
        className={`${dropdownPanelClass} max-h-80 w-[min(26rem,calc(100vw-2rem))] overflow-y-auto transition duration-150 ease-out empty:invisible data-closed:-translate-y-1 data-closed:opacity-0`}
      >
        {options.length === 0 && <p className="px-3 py-2 text-sm text-stone-500">Ingen kommun matchar “{query}”.</p>}
        {options.map((m) => (
          <ComboboxOption
            key={m.code}
            value={m.code}
            className="group flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 select-none data-focus:bg-brand-50 dark:data-focus:bg-brand-950/50"
          >
            <span className="flex-1">
              <span className="block font-medium">{m.name}</span>
              {m.county && <span className="block text-xs text-stone-500">{m.county}</span>}
            </span>
            <Check className="invisible size-4 text-brand-600 group-data-selected:visible dark:text-brand-400" />
          </ComboboxOption>
        ))}
      </ComboboxOptions>
    </Combobox>
  )
}
