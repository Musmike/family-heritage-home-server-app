import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import MainLayout from './MainLayout';
import { SidebarProvider } from '../../context/SidebarContext';
import { AuthContext } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../../pages/HomePage/HomePage', () => ({
  default: () => <div>Mocked Home Page</div>
}));

describe('MainLayout Component', () => {
  it('should render header, sidebar, footer and the main page content', () => {
    // GIVEN
    const mockAuthContext = {
      user: { username: 'testuser', roles: ['ADMIN'] },
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthContext.Provider value={mockAuthContext}>
          <SidebarProvider>
            <MainLayout />
          </SidebarProvider>
        </AuthContext.Provider>
      </MemoryRouter>
    );

    // THEN
    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByRole('complementary')).toBeInTheDocument();
    expect(screen.getByRole('contentinfo')).toBeInTheDocument();
    
    expect(screen.getByText('Mocked Home Page')).toBeInTheDocument();
  });
});