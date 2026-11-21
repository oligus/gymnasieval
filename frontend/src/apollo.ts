import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client'

export const apolloClient = new ApolloClient({
  // Relativ adress: Vite-proxyn skickar vidare till Spring under utveckling,
  // och i produktion serverar Spring både frontend och /graphql.
  link: new HttpLink({ uri: '/graphql' }),
  cache: new InMemoryCache({
    typePolicies: {
      // Apollo normaliserar cachen per id. Våra typer använder "code" som id.
      School: { keyFields: ['code'] },
      Program: { keyFields: ['code'] },
      Municipality: { keyFields: ['code'] },
      // Ett program på en skola har inget eget id; spara det inbäddat i skolan.
      SchoolProgram: { keyFields: false },
      StudyPath: { keyFields: false },
    },
  }),
})
