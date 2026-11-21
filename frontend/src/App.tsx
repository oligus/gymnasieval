import { GraduationCap } from 'lucide-react'
import { SearchPage } from './search/SearchPage'

function App() {
  return (
    <div className="min-h-screen bg-stone-50 font-sans text-stone-900 dark:bg-stone-950 dark:text-stone-100">
      <header className="absolute inset-x-0 top-0 z-10">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-5">
          <a href="/" className="flex items-center gap-2 font-semibold tracking-tight">
            <span className="grid size-8 place-items-center rounded-lg bg-brand-600 text-white shadow-sm shadow-brand-600/30">
              <GraduationCap className="size-5" />
            </span>
            Gymnasieval
          </a>
        </div>
      </header>

      <SearchPage />

      <footer className="mx-auto max-w-5xl px-4 py-10 text-center text-sm text-stone-500">
        Data från Skolverkets öppna API:er. Antagningspoäng avser senaste antagningen.
      </footer>
    </div>
  )
}

export default App
