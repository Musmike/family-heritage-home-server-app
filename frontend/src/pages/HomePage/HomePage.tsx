import { useAuth } from "../../context/AuthContext";

export default function HomePage() {
  const { user} = useAuth();

  if (!user) {
    return <div>Błąd: Nie znaleziono użytkownika.</div>;
  }
  
  return (
    <div>
      <h1>Witaj, {user.username}!</h1>
      <p>Jesteś zalogowany jako: {user.roles.join(', ')}</p>
    </div>
  );
}