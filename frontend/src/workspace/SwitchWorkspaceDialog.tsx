import { Dialog, DialogBackdrop, DialogPanel, DialogTitle } from '@headlessui/react'

type Props = {
  open: boolean
  onAnswer: (accept: boolean) => void
}

/** Frågar innan webbläsaren byter till en annan lista än den som redan är sparad. */
export function SwitchWorkspaceDialog({ open, onAnswer }: Props) {
  return (
    <Dialog open={open} onClose={() => onAnswer(false)} className="relative z-50">
      <DialogBackdrop className="fixed inset-0 bg-stone-950/40 backdrop-blur-sm" />
      <div className="fixed inset-0 flex items-end justify-center p-4 sm:items-center">
        <DialogPanel className="w-full max-w-md rounded-2xl border border-stone-200 bg-white p-6 shadow-2xl dark:border-stone-800 dark:bg-stone-900">
          <DialogTitle className="text-lg font-semibold">Byta till den här listan?</DialogTitle>
          <p className="mt-2 text-sm text-stone-600 dark:text-stone-400">
            Länken du öppnade leder till en annan lista än den som är sparad i den här webbläsaren.
            Byter du sparas ingenting om, men du behöver länken till den gamla listan för att komma tillbaka till den.
          </p>
          <div className="mt-6 flex gap-3">
            <button
              type="button"
              onClick={() => onAnswer(false)}
              className="flex-1 rounded-xl border border-stone-200 px-4 py-2.5 font-medium hover:bg-stone-50 dark:border-stone-700 dark:hover:bg-stone-800"
            >
              Behåll min lista
            </button>
            <button
              type="button"
              onClick={() => onAnswer(true)}
              className="flex-1 rounded-xl bg-brand-600 px-4 py-2.5 font-medium text-white hover:bg-brand-700"
            >
              Byt lista
            </button>
          </div>
        </DialogPanel>
      </div>
    </Dialog>
  )
}
