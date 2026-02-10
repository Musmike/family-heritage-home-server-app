import Footer from '../../components/Footer/Footer';
import LoginPage from '../../pages/LoginPage/LoginPage';
import styles from './AuthLayout.module.css';

export default function AuthLayout() {
  return (
    <div className={styles.loginPageWrapper}>
      <div className={styles.loginContainerWrapper} data-testid="login-page-container">
        <LoginPage />
      </div>
      <Footer />
    </div>
  );
}