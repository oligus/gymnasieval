import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client'
import { loadDevMessages, loadErrorMessages } from '@apollo/client/dev'
import { SetContextLink } from '@apollo/client/link/context'
import { getWorkspaceKey } from './workspace/workspaceKey'

// Apollo visar bara korta felkoder som standard. Under utveckling laddas fullständiga meddelanden.
if (import.meta.env.DEV) {
  loadDevMessages()
  loadErrorMessages()
}

// Lägger arbetsytans nyckel i headern på varje anrop (läses vid anropet, så ny nyckel gäller direkt).
const workspaceKeyLink = new SetContextLink((prevContext) => {
  const key = getWorkspaceKey()
  return key ? { headers: { ...prevContext.headers, 'X-Workspace-Key': key } } : {}
})

export const apolloClient = new ApolloClient({
  // Relativ adress: Vite-proxyn skickar vidare till Spring under utveckling,
  // och i produktion serverar Spring både frontend och /graphql.
  link: workspaceKeyLink.concat(new HttpLink({ uri: '/graphql' })),
  cache: new InMemoryCache({
    typePolicies: {
      // Apollo normaliserar cachen per id. Våra typer använder "code" som id.
      School: { keyFields: ['code'] },
      Program: { keyFields: ['code'] },
      Municipality: { keyFields: ['code'] },
      Workspace: { keyFields: ['id'] },
      // En bevakad skola identifieras av skolans kod. Då slås fält från olika frågor ihop
      // (t.ex. stjärnan i sökningen och betygen i Min lista) i stället för att skriva över varandra.
      WatchedSchool: { keyFields: ['school', ['code']] },
      // Typer utan eget id sparas inbäddade i sin förälder.
      SchoolProgram: { keyFields: false },
      StudyPath: { keyFields: false },
      Weight: { keyFields: false },
      Score: { keyFields: false },
    },
  }),
})
