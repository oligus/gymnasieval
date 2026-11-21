import { Eye, GraduationCap, Share2, Star } from 'lucide-react'
import { Link, NavLink, Route, Routes } from 'react-router'
import { MyListPage } from './list/MyListPage'
import { SearchPage } from './search/SearchPage'
import { useWorkspace } from './workspace/workspaceContext'

function App() {
  return (
    <div className="min-h-screen">
      <Header />
      <Routes>
        <Route path="/" element={<SearchPage />} />
        <Route path="/lista" element={<MyListPage />} />
      </Routes>
      <footer className="mx-auto max-w-5xl px-4 py-10 text-center text-sm text-stone-500">
        Data från Skolverkets öppna API:er. Antagningspoäng avser senaste antagningen.
      </footer>
    </div>
  )
}

function Header() {
  const { workspace, readOnly, openShare, leaveWorkspace } = useWorkspace()
  const count = workspace?.watchedSchools.length ?? 0

  return (
    <header className="absolute inset-x-0 top-0 z-20">
      {readOnly && (
        <div className="bg-amber-100 text-amber-900 dark:bg-amber-950 dark:text-amber-200">
          <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-2 px-4 py-2 text-sm">
            <span className="inline-flex items-center gap-2">
              <Eye className="size-4" />
              Du tittar på en delad lista. Den går inte att ändra.
            </span>
            <button type="button" onClick={leaveWorkspace} className="font-medium underline underline-offset-2">
              Sluta visa listan
            </button>
          </div>
        </div>
      )}
      <div className="mx-auto flex max-w-5xl items-center justify-between gap-3 px-4 py-5">
        <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight">
          <span className="grid size-8 place-items-center rounded-lg bg-brand-600 text-white shadow-sm shadow-brand-600/30">
            <GraduationCap className="size-5" />
          </span>
          Gymnasieval
        </Link>

        <nav className="flex items-center gap-2 text-sm font-medium">
          <NavLink
            to="/lista"
            className={({ isActive }) =>
              `inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 transition ${
                isActive ? 'bg-brand-600 text-white' : 'bg-white/70 backdrop-blur hover:bg-white dark:bg-stone-900/70 dark:hover:bg-stone-900'
              }`
            }
          >
            <Star className="size-4" />
            Min lista
            {count > 0 && <span className="rounded-full bg-brand-100 px-1.5 text-xs text-brand-800 tabular-nums">{count}</span>}
          </NavLink>
          {workspace && !readOnly && (
            <button
              type="button"
              onClick={openShare}
              className="inline-flex items-center gap-1.5 rounded-lg bg-white/70 px-3 py-1.5 backdrop-blur transition hover:bg-white dark:bg-stone-900/70 dark:hover:bg-stone-900"
            >
              <Share2 className="size-4" />
              Dela
            </button>
          )}
        </nav>
      </div>
    </header>
  )
}

export default App
