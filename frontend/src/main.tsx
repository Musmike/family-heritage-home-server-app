import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { SidebarProvider } from './context/SidebarContext.tsx';
import './styles/tailwind.css';
import './styles/globals.css'; 

const container = document.getElementById('root');

if (container) {
  createRoot(container).render(
    <StrictMode>
      <BrowserRouter>
        <AuthProvider>
          <SidebarProvider>
            <App />
          </SidebarProvider>
        </AuthProvider>
      </BrowserRouter>
    </StrictMode>
  );
}