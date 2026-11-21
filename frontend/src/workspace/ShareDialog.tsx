import { Dialog, DialogBackdrop, DialogPanel, DialogTitle } from '@headlessui/react'
import { Check, Copy, Eye, Pencil, X } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { workspaceLink } from './workspaceKey'

type Props = {
  open: boolean
  onClose: () => void
  editKey: string
  viewKey: string | null | undefined
}

/** Visar länkarna till arbetsytan: en för att redigera (t.ex. egen mobil) och en för att bara titta. */
export function ShareDialog({ open, onClose, editKey, viewKey }: Props) {
  return (
    <Dialog open={open} onClose={onClose} className="relative z-50">
      <DialogBackdrop
        transition
        className="fixed inset-0 bg-stone-950/40 backdrop-blur-sm transition duration-200 data-closed:opacity-0"
      />
      <div className="fixed inset-0 flex items-end justify-center p-4 sm:items-center">
        <DialogPanel
          transition
          className="w-full max-w-lg rounded-2xl border border-stone-200 bg-white p-6 shadow-2xl transition duration-200 data-closed:translate-y-2 data-closed:opacity-0 dark:border-stone-800 dark:bg-stone-900"
        >
          <div className="flex items-start justify-between gap-4">
            <div>
              <DialogTitle className="text-lg font-semibold">Din lista är sparad</DialogTitle>
              <p className="mt-1 text-sm text-stone-600 dark:text-stone-400">
                Det finns inget konto. Länkarna är det enda sättet att nå listan från en annan enhet,
                så spara dem, t.ex. som bokmärke eller i ett meddelande till dig själv.
              </p>
            </div>
            <button
              type="button"
              onClick={onClose}
              aria-label="Stäng"
              className="rounded-lg p-1 text-stone-400 hover:bg-stone-100 hover:text-stone-700 dark:hover:bg-stone-800"
            >
              <X className="size-5" />
            </button>
          </div>

          <div className="mt-5 space-y-4">
            <LinkRow
              icon={<Pencil />}
              title="Redigeringslänk"
              description="Full åtkomst. Använd på din egen mobil eller dator."
              link={workspaceLink(editKey)}
            />
            {viewKey && (
              <LinkRow
                icon={<Eye />}
                title="Läslänk"
                description="Bara läsa. Bra att skicka till föräldrar eller syskon."
                link={workspaceLink(viewKey)}
              />
            )}
          </div>

          <button
            type="button"
            onClick={onClose}
            className="mt-6 w-full rounded-xl bg-brand-600 px-4 py-2.5 font-medium text-white shadow-sm hover:bg-brand-700"
          >
            Klart
          </button>
        </DialogPanel>
      </div>
    </Dialog>
  )
}

function LinkRow({ icon, title, description, link }: { icon: ReactNode; title: string; description: string; link: string }) {
  const [copied, setCopied] = useState(false)

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(link)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // Urklipp blockerat: användaren kan markera texten själv.
    }
  }

  return (
    <div className="rounded-xl border border-stone-200 p-3 dark:border-stone-800">
      <div className="flex items-center gap-2 text-sm font-medium">
        <span className="text-brand-600 dark:text-brand-400 [&>svg]:size-4">{icon}</span>
        {title}
        <span className="font-normal text-stone-500">· {description}</span>
      </div>
      <div className="mt-2 flex items-center gap-2">
        <input
          readOnly
          value={link}
          onFocus={(e) => e.target.select()}
          className="min-w-0 flex-1 rounded-lg bg-stone-100 px-3 py-2 font-mono text-xs text-stone-700 outline-none dark:bg-stone-800 dark:text-stone-300"
        />
        <button
          type="button"
          onClick={() => void copy()}
          className="inline-flex shrink-0 items-center gap-1.5 rounded-lg border border-stone-200 px-3 py-2 text-sm font-medium hover:border-brand-300 hover:bg-brand-50 dark:border-stone-700 dark:hover:bg-brand-950/40"
        >
          {copied ? <Check className="size-4 text-emerald-600" /> : <Copy className="size-4" />}
          {copied ? 'Kopierad' : 'Kopiera'}
        </button>
      </div>
    </div>
  )
}
