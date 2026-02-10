// /mnt/onedrive-data/kubuntu_pc_files/Documents/projects/github_projects/family-heritage-home-server-app/frontend/src/pages/LoginPage/LoginPage.tsx
import { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import api from '../../api/axiosInstance'; // Use configured instance
import { AuthContext } from '../../context/AuthContext';
import logo from '../../assets/logo-light-mode.png';
import styles from './LoginPage.module.css';
import Loader from '../../components/Loader/Loader';

// Definicja typu dla odpowiedzi o błędzie z backendu
interface ApiErrorResponse {
  errorCode: string;
  message: string;
}

// Funkcja pomocnicza do mapowania kodów błędów na wiadomości dla użytkownika
const getLoginErrorMessage = (errorData: ApiErrorResponse | unknown): string => {
  if (typeof errorData === 'object' && errorData !== null && 'errorCode' in errorData) {
    const code = (errorData as ApiErrorResponse).errorCode;
    switch (code) {
      case 'BAD_CREDENTIALS':
        return 'Nieprawidłowa nazwa użytkownika lub hasło.';
      case 'ACCOUNT_LOCKED':
        return 'Twoje konto zostało zablokowane. Skontaktuj się z administratorem.';
      case 'ACCOUNT_DISABLED':
        return 'Twoje konto jest nieaktywne. Skontaktuj się z administratorem.';
      case 'AUTHENTICATION_FAILED':
        return 'Uwierzytelnianie nie powiodło się. Spróbuj ponownie.';
      case 'INTERNAL_SERVER_ERROR':
        return 'Wystąpił wewnętrzny błąd serwera. Spróbuj ponownie później.';
      default:
        return 'Wystąpił nieoczekiwany błąd. Spróbuj ponownie.';
    }
  }
  return 'Wystąpił nieznany błąd odpowiedzi serwera.';
};


export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    try {
      await api.post(
        '/auth/login',
        { username, password }
      );
      await auth?.login();
      navigate('/');
    } catch (err) {
      console.error("Login failed", err);
      
      if (axios.isAxiosError(err)) {
        const axiosError = err as AxiosError<ApiErrorResponse>;
        if (axiosError.response) {
          // Serwer odpowiedział statusem błędu (4xx, 5xx)
          setError(getLoginErrorMessage(axiosError.response.data));
        } else if (axiosError.request) {
          // Żądanie zostało wysłane, ale nie otrzymano odpowiedzi (błąd sieci)
          setError('Błąd sieci. Sprawdź połączenie z internetem i spróbuj ponownie.');
        } else {
          // Inny błąd związany z konfiguracją axios
          setError('Wystąpił błąd podczas wysyłania żądania.');
        }
      } else {
        // Błąd niepochodzący z axios (np. błąd w kodzie)
        setError('Wystąpił nieoczekiwany błąd aplikacji.');
      }
    }
  };

  return (

    <div className={styles.loginContainer}>

      <header className={styles.loginHeader}>
        <img src={logo} alt="Family Heritage Logo" className={styles.logo} />
      </header>

      <p className={styles.loginMessage}>
        Witaj! Zaloguj się, aby uzyskać dostęp do rodzinnego archiwum.
      </p>

      <form className={styles.loginForm} onSubmit={handleSubmit}>

        {error && (
          <div className={styles.errorMessage}>
            {error}
          </div>
        )}

        <div className={styles.formGroup}>
          <label htmlFor="username">Nazwa użytkownika</label>
          <input
            data-testid="login-username-input"
            type="text"
            id="username"
            required
            value={username}
            autoCapitalize="none"
            onChange={(e) => setUsername(e.target.value)}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="password">Hasło</label>
          <input
            data-testid="login-password-input"
            type="password"
            id="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button
          type="submit"
          className={styles.loginButton}
          disabled={auth?.isLoading}
          data-testid="login-submit-button"
        >
          {auth?.isLoading ? (
            <Loader size={24} />
          ) : (
            "Zaloguj się"
          )}
        </button>


      </form>
    </div>
  );
}