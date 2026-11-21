import { SearchPage } from './search/SearchPage'

function App() {
  return (
    <div className="min-h-screen bg-stone-50 text-stone-900 dark:bg-stone-950 dark:text-stone-100">
      <header className="border-b border-stone-200 bg-white dark:border-stone-800 dark:bg-stone-900">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-3">
          <h1 className="text-xl font-bold text-blue-600 dark:text-blue-400">Gymnasieval</h1>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-4 py-8">
        <SearchPage />
      </main>
    </div>
  )
}

export default App
