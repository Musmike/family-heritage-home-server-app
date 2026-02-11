import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import Sidebar from './Sidebar';
import { SidebarProvider } from '../../context/SidebarContext';

describe('Sidebar Component', () => {
  const renderSidebar = () => {
    render(
      <BrowserRouter>
        <SidebarProvider>
          <Sidebar />
        </SidebarProvider>
      </BrowserRouter>
    );
  };

  it('should render all main navigation links', () => {
    renderSidebar();

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

  it('should render the dividers between sections', () => {
    renderSidebar();
    expect(screen.getAllByRole('separator')).toHaveLength(3);
  });
});