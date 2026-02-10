// frontend/src/layouts/components/__tests__/Sidebar.test.tsx

import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import Sidebar from './Sidebar';
import { SidebarProvider } from '../../context/SidebarContext';

describe('Sidebar Component', () => {
  const renderSidebar = (isOpen: boolean) => {
    render(
      <BrowserRouter>
        <SidebarProvider> {/* Sidebar używa kontekstu, więc musimy go dostarczyć */}
          <Sidebar />
        </SidebarProvider>
      </BrowserRouter>
    );
  };

  it('should render all main navigation links', () => {
    renderSidebar(true);

    // Szukamy linków po ich nazwach
    expect(screen.getByRole('link', { name: /Panel główny/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Biografie/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Wydarzenia/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Wspomnienia i powiedzenia/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Dziennik/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Drzewo genealogiczne/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Statystyki/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Galeria/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Dokumenty/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Nagrobki/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Miejsca/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Pamiątki/i })).toBeInTheDocument();
  });

  // Dodatkowy test na separatory, szukamy po 'role="separator"'
  it('should render the dividers between sections', () => {
    renderSidebar(true);
    expect(screen.getAllByRole('separator')).toHaveLength(3);
  });
});