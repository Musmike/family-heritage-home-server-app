import { NavLink } from 'react-router-dom';
import { useSidebar } from '../../context/SidebarContext';
import logo from '../../assets/logo-light-mode.png';
import styles from './Sidebar.module.css';

export default function Sidebar() {
  const { isSidebarOpen, screenSize, toggleSidebar } = useSidebar();

  const navItems = [
    { name: 'Panel główny', path: '/', icon: '🏠' },
    null,
    { name: 'Biografie', path: '/biographies', icon: '👤' },
    { name: 'Wydarzenia', path: '/events', icon: '📅' },
    { name: 'Wspomnienia i powiedzenia', path: '/memories', icon: '💬' },
    { name: 'Dziennik', path: '/journal', icon: '📔' },
    null,
    { name: 'Drzewo genealogiczne', path: '/tree', icon: '🌳' },
    { name: 'Statystyki', path: '/statistics', icon: '📊' },
    null,
    { name: 'Galeria', path: '/gallery', icon: '🖼️' },
    { name: 'Dokumenty', path: '/documents', icon: '📄' },
    { name: 'Nagrobki', path: '/gravestones', icon: '🪦' },
    { name: 'Miejsca', path: '/places', icon: '📍' },
    { name: 'Pamiątki', path: '/heirlooms', icon: '🎁' },
  ];

  return (
    <>
      <div
        className={`${styles.sidebarOverlay} ${isSidebarOpen ? styles.sidebarOverlayVisible : ''}`}
        onClick={toggleSidebar}
      />

      <aside className={`${styles.sidebar} ${isSidebarOpen ? styles.sidebarOpen : ''}`}>
        <div className={styles.sidebarHeader}>
          <div className={styles.sidebarHeaderLeft}>
            <button
              onClick={toggleSidebar}
              aria-label="Toggle sidebar"
              className={styles.sidebarHeaderHamburger}
            >
              ☰
            </button>
            <img src={logo} alt="Family Heritage Logo" className={styles.sidebarHeaderLogo} />
          </div>
        </div>

        <nav className={styles.sidebarNav}>
          <ul>
            {navItems.map((item, index) => {
              if (item === null) {
                return <hr key={`divider-${index}`} className={styles.sidebarDivider} role="separator" />;
              }
              return (
                <li key={item.name}>
                  <NavLink
                    to={item.path}
                    end={item.path === '/'}
                    className={({ isActive }) =>
                      `${styles.sidebarNavLink} ${isActive ? styles.sidebarNavLinkActive : ''}`
                    }
                    onClick={() => {
                      if (screenSize === 'mobile' && isSidebarOpen) toggleSidebar();
                    }}
                  >
                    <span className={styles.navIcon}>{item.icon}</span>
                    <span className={styles.navText} style={{ whiteSpace: 'normal' }}>
                      {item.name}
                    </span>
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>
    </>
  );
}