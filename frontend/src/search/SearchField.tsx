import type { ReactNode } from 'react'

/**
 * Ett fält i sökpanelen: ikon till vänster, liten etikett ovanför innehållet.
 * Bara span-element, så att fältet kan ligga inuti en knapp (t.ex. programväljaren).
 */
export function SearchField({ icon, label, children }: { icon: ReactNode; label: string; children: ReactNode }) {
  return (
    <span className="relative flex h-full w-full items-center gap-3 bg-white px-4 py-3 text-left transition-colors focus-within:bg-brand-50/60 hover:bg-stone-50 dark:bg-stone-900 dark:focus-within:bg-brand-950/40 dark:hover:bg-stone-900/70">
      <span className="text-brand-600 dark:text-brand-400 [&>svg]:size-5">{icon}</span>
      <span className="block min-w-0 flex-1">
        <span className="block text-xs font-medium tracking-wide text-stone-500 uppercase">{label}</span>
        {children}
      </span>
    </span>
  )
}

/** Gemensam stil för textrutor och knappar inne i ett SearchField. */
export const fieldControlClass =
  'w-full appearance-none bg-transparent py-1 text-left text-base font-medium text-stone-900 outline-none placeholder:font-normal placeholder:text-stone-400 dark:text-white'

/** Gemensam stil för paneler som öppnas från sökfälten. */
export const dropdownPanelClass =
  'z-50 rounded-2xl border border-stone-200 bg-white/95 p-2 shadow-2xl shadow-brand-950/10 backdrop-blur-xl [--anchor-gap:10px] focus:outline-none dark:border-stone-800 dark:bg-stone-900/95 dark:shadow-black/50'
