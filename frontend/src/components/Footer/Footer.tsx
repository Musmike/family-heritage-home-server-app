import styles from './Footer.module.css';

export default function Footer() {
  const currentYear = new Date().getFullYear();
  const appVersion = "0.1.0-alpha";

  return (
    <footer className={styles.appFooter}>
      <span>{`v${appVersion}`}</span>
      <span>|</span>
      <span>&copy; {currentYear} Michał Muzyka</span>
    </footer>
  );
}