## S1. User Login

### S1.1. Description

Use case scenario describing logging into the system.

### S1.2. Actors
- Guest User
- Privileged Guest User
- Administrator

### S1.3. Preconditions

The person logging in has an account created in the system or has been given login credentials.

### S1.4. Postconditions

The person logging in gains access to the features of their user account.

### S1.5. Main Flow

1. The person logging in opens the application.
2. The person logging in enters their username and password.
3. The person logging in clicks the "Login" button.
4. The system verifies the credentials.
5. If credetinals are correct, the user is logged in.
6. The system redirects the user to the section with the list of individuals.

### S1.6. Alternative Flows

#### AF.4. The person logging in made an error entering their login information
- AF.4.1. The system displays an error message.
- AF.4.2. The person logging in begins the login procedure from step 2 of the main flow.

### S1.7. Exception Scenarios
None

### S1.8. Non-functional Requirements
None

### S1.9. Notes and Open Questions
Due to the need for unambiguous user identification, the system does not support retrieving forgotten login credentials or resetting passwords.