import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Header from './Header';
import { AuthContext } from '../../context/AuthContext';
import { SidebarProvider } from '../../context/SidebarContext';

describe('Header Component', () => {
  it('should call logout function when logout button is clicked', () => {
    // GIVEN
    const mockLogout = vi.fn();
    const mockAuthContext = {
      user: { username: 'testuser', roles: ['ADMIN'] },
      isLoading: false,
      login: vi.fn(),
      logout: mockLogout,
    };

    render(
      <AuthContext value={mockAuthContext}>
        <SidebarProvider>
          <Header />
        </SidebarProvider>
      </AuthContext>
    );

    // WHEN
    const logoutButton = screen.getByTestId('logout-button');
    fireEvent.click(logoutButton);

    // THEN
    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('should not render logout button when user is not authenticated', () => {
    // GIVEN
    const mockAuthContext = {
      user: null,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    };

    render(
      <AuthContext value={mockAuthContext}>
        <SidebarProvider>
          <Header />
        </SidebarProvider>
      </AuthContext>
    );

    // THEN
    const logoutButton = screen.queryByTestId('logout-button');
    expect(logoutButton).not.toBeInTheDocument();
  });
});