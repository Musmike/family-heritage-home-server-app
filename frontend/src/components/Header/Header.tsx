import logo from '../../assets/logo-light-mode.png'; 
import styles from './Header.module.css';
import { useAuth } from '../../context/AuthContext';
import { useSidebar } from '../../context/SidebarContext';

export default function Header() {
    const sidebar = useSidebar();
    const { user, logout } = useAuth();

    const handleLogout = () => {
        logout();
    };

    return (
        <header className={styles.appHeader}>
            <div className={styles.headerLeft}>
                    <button 
                        type="button"
                        onClick={sidebar.toggleSidebar} 
                        aria-label="Toggle sidebar" 
                        className={styles.hamburgerBtn}
                    >
                        ☰
                    </button>
                <img src={logo} alt="Family Heritage Logo" className={styles.headerLogo} />
            </div>

            <div className={styles.headerRight}>
                <button type="button" aria-label="Accessibility settings" className={styles.accessibilityBtn}>♿</button>

                {user && (
                    <button 
                        type="button"
                        onClick={handleLogout} 
                        className={styles.logoutBtn}
                        data-testid="logout-button"
                    >
                        <span className={styles.logoutBtnIcon}>🚪</span>
                        <span className={styles.logoutBtnText}>Wyloguj się</span>
                    </button>
                )}
            </div>
        </header>
    );
}