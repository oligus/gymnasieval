import { useMutation } from '@apollo/client/react'
import { useEffect, useState } from 'react'
import type { Criterion } from '../gql/graphql'
import { CRITERIA } from './criteria'
import { SetWeightMutation } from './listDocuments'

type Props = {
  weights: readonly { criterion: Criterion; weight: number }[]
  readOnly: boolean
}

/** Reglage 0–5 per kriterium: hur mycket varje kriterium väger i totalpoängen. */
export function WeightsPanel({ weights, readOnly }: Props) {
  return (
    <section className="rounded-2xl border border-stone-200 bg-white p-5 dark:border-stone-800 dark:bg-stone-900">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-semibold">Vikter</h2>
        <p className="text-xs text-stone-500">0 = spelar ingen roll, 5 = viktigast</p>
      </div>
      <div className="mt-4 grid gap-x-6 gap-y-4 sm:grid-cols-2">
        {CRITERIA.map((c) => (
          <WeightSlider
            key={c.key}
            criterion={c.key}
            label={c.label}
            hint={c.hint}
            value={weights.find((w) => w.criterion === c.key)?.weight ?? 0}
            readOnly={readOnly}
          />
        ))}
      </div>
    </section>
  )
}

const SAVE_DELAY_MS = 350

function WeightSlider({ criterion, label, hint, value, readOnly }: {
  criterion: Criterion
  label: string
  hint: string
  value: number
  readOnly: boolean
}) {
  const [draft, setDraft] = useState<number | null>(null)
  const [saveWeight] = useMutation(SetWeightMutation)
  const shown = draft ?? value

  // Spara en kort stund efter att reglaget släppts, inte för varje steg medan man drar.
  useEffect(() => {
    if (draft == null || draft === value) return
    const timer = setTimeout(() => {
      void saveWeight({ variables: { criterion, weight: draft } }).then(() => setDraft(null))
    }, SAVE_DELAY_MS)
    return () => clearTimeout(timer)
  }, [draft, value, criterion, saveWeight])

  return (
    <label className="block">
      <span className="flex items-baseline justify-between text-sm font-medium">
        {label}
        <span className="font-semibold text-brand-600 tabular-nums dark:text-brand-400">{shown}</span>
      </span>
      <input
        type="range"
        min={0}
        max={5}
        step={1}
        value={shown}
        disabled={readOnly}
        onChange={(e) => setDraft(Number(e.target.value))}
        className="mt-1 w-full accent-brand-600 disabled:opacity-60"
      />
      <span className="block text-xs text-stone-500">{hint}</span>
    </label>
  )
}
