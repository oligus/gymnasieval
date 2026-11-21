import type { CodegenConfig } from '@graphql-codegen/cli'

// Genererar TypeScript-typer från backendens GraphQL-schema och frågorna i src/.
// Kör: npm run codegen
const config: CodegenConfig = {
  schema: '../src/main/resources/graphql/schema.graphqls',
  documents: ['src/**/*.{ts,tsx}', '!src/gql/**/*'],
  ignoreNoDocuments: true,
  generates: {
    './src/gql/': {
      preset: 'client',
      // Låt föräldrakomponenter läsa fälten i barnens fragments (t.ex. för sortering).
      presetConfig: { fragmentMasking: false },
      config: {
        useTypeImports: true, // krävs av verbatimModuleSyntax
        enumsAsTypes: true, // krävs av erasableSyntaxOnly (TypeScript-enums tillåts inte)
      },
    },
  },
}

export default config
