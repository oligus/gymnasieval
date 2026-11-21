import { ApolloProvider } from '@apollo/client/react'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import App from './App.tsx'
import { apolloClient } from './apollo.ts'
import './index.css'
import { WorkspaceProvider } from './workspace/WorkspaceProvider.tsx'
import { initWorkspaceKeyFromUrl } from './workspace/workspaceKey.ts'

// Läs #k=… ur länken innan första anropet till servern, så att rätt nyckel används direkt.
initWorkspaceKeyFromUrl()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ApolloProvider client={apolloClient}>
      <BrowserRouter>
        <WorkspaceProvider>
          <App />
        </WorkspaceProvider>
      </BrowserRouter>
    </ApolloProvider>
  </StrictMode>,
)
