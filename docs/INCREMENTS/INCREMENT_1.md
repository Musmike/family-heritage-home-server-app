# Increment from Sprint 1 (Version 1.0.0)

## 1. New User-Facing Features

*   **Login System:**
    *   An Administrator can now log in to the application using their username and password.
    *   The application recognizes the Administrator role, laying the groundwork for future access restrictions.
*   **Genealogy Data Import:**
    *   The admin panel now includes a feature to import data from a file in the GEDCOM format. The system processes the file and saves data about individuals and families to the database.
*   **Data Browsing:**
    *   Any user can now view a public list of all individuals imported into the system.
    *   An initial version of an interactive family tree visualization is available, showing the relationships between individuals.

## 2. Bug Fixes

*   None

## 3. Technical Updates

*   **Project Structure:** An application skeleton has been created based on the chosen technology stack.
*   **Database:** The initial database schema for users, individuals, and families has been designed and implemented.
*   **Environment:** A basic development and testing environment has been configured.

## 4. Non-Functional Requirements Met

*   **Security:** User passwords are now securely stored in the database using a hashing mechanism, preventing them from being read in plain text.

## 5. Documentation

*   A basic `README.md` file has been created, describing how to run the project locally.
*   The database schema has been documented.