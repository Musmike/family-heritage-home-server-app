# Product Backlog: 'Family Heritage' Home Server App

## 1. Actors (User Roles)

*   **Guest User:** A user with restricted read-only access who does not have access to sensitive or private information. Ideal for general family guests with basic access needs.
*   **Privileged Guest User:** A read-only user with access to public data and private data available only to privileged users. Suitable for trusted family members.
*   **Administrator:** A user with full permissions to manage the system, including adding, editing, and deleting content, as well as managing users.

## 2. Epics and User Stories

### `[ ]` Epic 1: Core Platform & User Management

*   **Description:** Build the application's foundation, including the login system, roles, and basic structure.
*   **User Stories:**
    *   `[ ]` **ID: E-01-US-01**
        *   **Title:** System Login
        *   **Description:** As a user, I want to log in and log out using an username and a password so that I can securely access the system.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I am on the login page, when I enter a valid username and password, then I am redirected to the main dashboard.
            *   Given I am on the login page, when I enter an invalid username or password, then I see an error message and remain on the login page.
            *   Given I am already logged in, when I navigate to the login page, then I am redirected to the main dashboard.
            *   Given I am logged in, when I click the "Log out" button, then my session is terminated.
            *   Given my session is terminated, then I am redirected to the login page.
            *   Given I am logged out, when I try to access a protected page, then I am prompted to log in.
            
    *   `[ ]` **ID: E-01-US-02**
        *   **Title:** Role Distinction After Login
        *   **Description:** As an Administrator, I want the system to distinguish between roles (Guest, Privileged Guest, Administrator), so that I can manage data access.
        *   **Estimate (Story Points):** 3
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given a user is logged in, when the system checks their session/token, then their correct role (Guest, Privileged Guest, or Administrator) is identified.
            *   Given a user logs in, when their user data is fetched, then it must include their assigned role.

    *   `[ ]` **ID: E-01-US-03**
        *   **Title:** Restrict Editing Access
        *   **Description:** As an Administrator, I want only users with my role to be able to add, edit, and delete content, to ensure data integrity.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I am logged in as an Administrator, when I view a page, then I can see and use "Add," "Edit," and "Delete" buttons.
            *   Given I am logged in as a Guest or Privileged Guest, when I view the same page, then the "Add," "Edit," and "Delete" buttons are not visible or are disabled.
            *   Given I am logged in as a non-administrator, when I attempt to directly access a CUD (Create/Update/Delete) API endpoint, then the server returns a 403 Forbidden error.

### `[ ]` Epic 2: Genealogy Data Import & Display

*   **Description:** Implement the core functionality of importing data from GEDCOM files and providing basic visualization.
*   **User Stories:**
    *   `[ ]` **ID: E-02-US-01**
        *   **Title:** Data Import from a GEDCOM File
        *   **Description:** As an Administrator, I want to be able to import a GEDCOM file, so that I can bulk update the family tree data.
        *   **Estimate (Story Points):** 13
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I am on the admin dashboard, when I upload a valid GEDCOM file, then the data is parsed and saved to the database.
            *   Given I upload a file, when the import is complete, then I see a success message with a summary (e.g., "500 individuals and 150 families imported").
            *   Given I upload an invalid or corrupted file, then I see a clear error message explaining the problem.
    *   `[ ]` **ID: E-02-US-02**
        *   **Title:** Protect Imported Data
        *   **Description:** As an Administrator, I want the data from the GEDCOM to be read-only, to prevent accidental modifications to the core genealogical information.
        *   **Estimate (Story Points):** 3
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am viewing an individual's profile, when a field was populated from a GEDCOM import (e.g., birth date), then that field is displayed as read-only text and is not editable.
            *   Given I attempt to update a GEDCOM-sourced field via an API call, then the request is rejected with an error.
    *   `[ ]` **ID: E-02-US-03**
        *   **Title:** Browse List of Individuals
        *   **Description:** As a Guest User, I want to be able to browse a list of all individuals in the database, so that I can quickly find a person of interest.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I navigate to the "Individuals" page, then I see a paginated list of all people, showing at least their full name, birth date, and death date.
            *   Given the list is displayed, when I click on a person's name, then I am taken to their detailed biography page.
    *   `[ ]` **ID: E-02-US-04**
        *   **Title:** Basic Biography Page
        *   **Description:** As a User, I want to be able to open a page with basic biographical data for an individual (name, dates), so that I can learn key information about them.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I am on the "Individuals" list, when I click a person's name, then a dedicated biography page for that person is displayed.
            *   Given I am on a biography page, then I can see key information such as full name, birth/death dates and places, and parents/spouses/children.
    *   `[ ]` **ID: E-02-US-05**
        *   **Title:** Family Tree Visualization
        *   **Description:** As a User, I want to be able to browse a graphical family tree, so that I can understand the relationships between family members.
        *   **Estimate (Story Points):** 13
        *   **Priority:** Highest
        *   **Acceptance Criteria:**
            *   Given I am on the "Family Tree" page, then an interactive graphical tree is displayed.
            *   Given I am viewing the tree, when I click on a person's node, then I can see their basic information in a pop-up or sidebar.
            *   Given I am viewing the tree, then I can pan and zoom to navigate large family structures.
    *   `[ ]` **ID: E-02-US-06**
        *   **Title:** Advanced Individual Search
        *   **Description:** As a User, I want to filter, sort, and search for individuals based on name or other attributes, so that I can find specific people efficiently.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am on the "Individuals" page, when I type a name in the search bar, then the list updates in real-time to show only matching results.
            *   Given I am on the "Individuals" page, when I use a sort option (e.g., "by birth date"), then the list reorders accordingly.
            *   Given I am on the "Individuals" page, when I apply a filter (e.g., "born in 'Warsaw'"), then the list shows only individuals matching that filter.
    *   `[ ]` **ID: E-02-US-07**
        *   **Title:** Navigate from Tree to Biography
        *   **Description:** As a User, I want to open a biography page for a person directly from the family tree, so that I can seamlessly navigate between views.
        *   **Estimate (Story Points):** 3
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am viewing the family tree, when I click on a person's node, then a link or button to their full biography is visible.
            *   Given I click that link/button, then I am navigated to that person's detailed biography page.
    *   `[ ]` **ID: E-02-US-08**
        *   **Title:** Manual Biography Editing
        *   **Description:** As an Administrator, I want to manually edit biography pages to add narratives and link important photos, documents, and other items, so that I can create a rich, curated profile for each person.
        *   **Estimate (Story Points):** 13
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am an Administrator on a biography page, when I click an "Edit" button, then the page fields (that are not from GEDCOM) become editable forms.
            *   Given I am in edit mode, then I can add/edit a rich text narrative about the person.
            *   Given I am in edit mode, then I can select and link existing photos, documents, heirlooms, etc., to the person's profile.
    *   `[ ]` **ID: E-02-US-09**
        *   **Title:** Automatic Biography Enrichment
        *   **Description:** As an Administrator, I want the system to automatically add a family tree fragment, tagged vital records, a gravestone photo, and GEDCOM events to a person's biography page, so that profiles are comprehensive with minimal manual effort.
        *   **Estimate (Story Points):** 21
        *   **Priority:** Medium
        *   **Acceptance Criteria:**
            *   Given a person has associated vital records, when I view their biography, then those records are displayed in a dedicated section.
            *   Given a person has a linked gravestone photo, when I view their biography, then that photo is displayed.
            *   Given I view a biography page, then a mini-tree showing their immediate family (parents, spouse, children) is automatically generated and displayed.

### `[ ]` Epic 3: Multimedia Gallery
*   **Description:** Features related to adding and viewing photos, videos, and other media.
*   **User Stories:**
    *   `[ ]` **ID: E-03-US-01**
        *   **Title:** Browse Photo and Video Gallery
        *   **Description:** As a User, I want to be able to browse the media gallery, so that I can view family photos and videos.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-03-US-02**
        *   **Title:** Upload Photos and Videos
        *   **Description:** As an Administrator, I want to be able to upload photo and video files, so that I can enrich the gallery with new materials.
        *   **Estimate (Story Points):** 5
        *   **Priority:** High
    *   `[ ]` **ID: E-03-US-03**
        *   **Title:** Manage Media Attributes
        *   **Description:** As an Administrator, I want to be able to edit media attributes (e.g., related people, date, place), so that I can facilitate searching and categorization.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium
    *   `[ ]` **ID: E-03-US-04**
        *   **Title:** Search and Filter Media
        *   **Description:** As a User, I want to search, sort, and filter photos and videos by person, place, or other attributes, so that I can easily find specific media.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium

### `[ ]` Epic 4: Document and Records Management
*   **Description:** A module for storing, translating, and viewing scanned historical documents.
*   **User Stories:**
    *   `[ ]` **ID: E-04-US-01**
        *   **Title:** View Historical Documents
        *   **Description:** As a User, I want to be able to view scanned vital records and other documents along with their translations, so that I can access source information about my ancestors.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-04-US-02**
        *   **Title:** Upload and Edit Documents
        *   **Description:** As an Administrator, I want to be able to upload document scans and add descriptions and translations, so that I can share them with other users.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-04-US-03**
        *   **Title:** Search and Filter Documents
        *   **Description:** As a User, I want to search, sort, and filter documents by type, person, date, or place, so that I can locate specific records quickly.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium

### `[ ]` Epic 5: Gravestone & Cemetery Data
*   **Description:** Manage and display photos and information related to graves and cemeteries.
*   **User Stories:**
    *   `[ ]` **ID: E-05-US-01**
        *   **Title:** Browse Gravestone Photos
        *   **Description:** As a User, I want to browse gravestone photos and see burial-related information, so that I can learn where individuals are buried.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Medium
    *   `[ ]` **ID: E-05-US-02**
        *   **Title:** Search and Filter Gravestones
        *   **Description:** As a User, I want to search and filter graves by name or cemetery location, so that I can find a specific person's final resting place.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Low
    *   `[ ]` **ID: E-05-US-03**
        *   **Title:** Manage Gravestone Photos
        *   **Description:** As an Administrator, I want to upload, edit attributes, and remove gravestone photos, so that the information is accurate and relevant.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium

### `[ ]` Epic 6: Maps and Location Plans
*   **Description:** Provide geographical context with maps and plans of significant places.
*   **User Stories:**
    *   `[ ]` **ID: E-06-US-01**
        *   **Title:** View Maps and Plans
        *   **Description:** As a User, I want to view maps and plans (e.g., homes, towns), so that I can understand where my ancestors lived and worked.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Medium
    *   `[ ]` **ID: E-06-US-02**
        *   **Title:** Search and Filter Maps
        *   **Description:** As a User, I want to search and filter maps by type, location, or associated individuals, so that I can find relevant geographical information.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Low
    *   `[ ]` **ID: E-06-US-03**
        *   **Title:** Manage Maps and Plans
        *   **Description:** As an Administrator, I want to upload and manage maps, so that users have access to accurate spatial context.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium

### `[ ]` Epic 7: Family Heirlooms & Memorabilia
*   **Description:** Catalog and display photos and stories of important family objects.
*   **User Stories:**
    *   `[ ]` **ID: E-07-US-01**
        *   **Title:** Browse Heirlooms
        *   **Description:** As a User, I want to browse photos and descriptions of family heirlooms, so that I can learn about items passed down through generations.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Medium
    *   `[ ]` **ID: E-07-US-02**
        *   **Title:** Search and Filter Heirlooms
        *   **Description:** As a User, I want to search and filter heirlooms by attributes, so that I can find specific objects.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Low
    *   `[ ]` **ID: E-07-US-03**
        *   **Title:** Manage Heirlooms
        *   **Description:** As an Administrator, I want to upload, edit, and remove heirloom photos and link them to people or events, so that the collection is organized and meaningful.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium

### `[ ]` Epic 8: Events & Memories
*   **Description:** Create a timeline of family history through structured events and personal narratives.
*   **User Stories:**
    *   `[ ]` **ID: E-08-US-01**
        *   **Title:** Browse Family Events
        *   **Description:** As a User, I want to browse family events (births, weddings) with associated data, so that I can understand the key moments in family history.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-08-US-02**
        *   **Title:** Manage Family Events
        *   **Description:** As an Administrator, I want to create, edit, and delete family events and link them to people, media, and memories, so that the system captures a full history.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-08-US-03**
        *   **Title:** Browse Family Memories
        *   **Description:** As a User, I want to browse family memories in narrative form (text, audio, video), so that I can connect with personal stories from the past.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
    *   `[ ]` **ID: E-08-US-04**
        *   **Title:** Manage Family Memories
        *   **Description:** As an Administrator, I want to upload, edit, and remove memories, so that the narratives remain rich and contextualized.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High

### `[ ]` Epic 9: Administrator's Private Journal
*   **Description:** A secure, private space for the administrator to log research, notes, and reflections.
*   **User Stories:**
    *   `[ ]` **ID: E-09-US-01**
        *   **Title:** Manage Journal Entries
        *   **Description:** As an Administrator, I want to create, edit, and delete private journal entries with rich text and embedded media, so that I can keep a personal record of my research and thoughts.
        *   **Estimate (Story Points):** 13
        *   **Priority:** Medium
    *   `[ ]` **ID: E-09-US-02**
        *   **Title:** Search and Sort Journal
        *   **Description:** As an Administrator, I want to search and sort my journal entries by date, title, or linked content, so that I can quickly find my notes.
        *   **Estimate (Story Points):** 5
        *   **Priority:** Low

### `[ ]` Epic 10: System-Wide Features
*   **Description:** Global features that enhance usability, organization, and accessibility across the application.
*   **User Stories:**
    *   `[ ]` **ID: E-10-US-01**
        *   **Title:** View Statistical Data
        *   **Description:** As a User, I want to view graphical representations of family data (charts, maps), so that I can see patterns and trends in my family history.
        *   **Estimate (Story Points):** 13
        *   **Priority:** Medium
    *   `[ ]` **ID: E-10-US-02**
        *   **Title:** Tagging and Categorization
        *   **Description:** As an Administrator, I want to add tags to documents, individuals, and media, so that I can improve organization, searching, and filtering.
        *   **Estimate (Story Points):** 8
        *   **Priority:** Medium
    *   `[ ]` **ID: E-10-US-03**
        *   **Title:** Language Selection
        *   **Description:** As a User, I want to choose between Polish and English interfaces, so that I can use the application in the language I am most comfortable with.
        *   **Estimate (Story Points):** 13
        *   **Priority:** High
    *   `[ ]` **ID: E-10-US-04**
        *   **Title:** Data Export
        *   **Description:** As a User, I want to export selected data (biographies, documents) as PDF, ZIP, or image formats, so that I can save or share it offline.
        *   **Estimate (Story Points):** 21
        *   **Priority:** High
    *   `[ ]` **ID: E-10-US-05**
        *   **Title:** Accessibility Controls
        *   **Description:** As a User, I want to use dark mode, adjust font size, and use a high contrast mode, so that I can interact with the application comfortably.
        *   **Estimate (Story Points):** 13
        *   **Priority:** High

### `[ ]` Epic 11: User Dashboard
*   **Description:** A central, personalized hub for all users to get a quick overview of the family archive, see relevant recent activity, and track important dates based on their access level.
*   **Business Value:** High
*   **Priority:** High
*   **User Stories:**
    *   `[ ]` **ID: E-11-US-01**
        *   **Title:** View Key Statistics on Dashboard
        *   **Description:** As a user, I want to see key summary statistics on the dashboard, so that I can quickly assess the overall size and scope of the family archive.
        *   **Estimate (Story Points):** 5
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am a logged-in user (any role), then I see summary cards displaying the total count of: Family Members, Documents, Photos & Videos, Events, Memories & Quotes, and Generations.
            *   Given the statistics are displayed, then the counts are accurate and reflect the current state of the database.
            *   Given I am an Administrator and I add a new document, then the "Documents" count on the dashboard updates for all users upon their next visit or page refresh.
    *   `[ ]` **ID: E-11-US-02**
        *   **Title:** View Role-Based Recent Activity Feed
        *   **Description:** As a user, I want to see a "Recent Activity" feed on the dashboard relevant to my access level, so that I can monitor recent changes in the archive.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am logged in as an **Administrator**, then the feed displays all system events, including administrative actions ("Photo deleted") and security events ("Login activity" with IP address).
            *   Given I am logged in as a **Privileged Guest User**, then the feed displays public and privileged activity (e.g., "New document added"), but excludes purely administrative or security events (e.g., "Login activity").
            *   Given I am logged in as a **Guest User**, then the feed displays only activity related to publicly available content (e.g., updates to a biography of a deceased person).
            *   The "View All Activity" button navigates to a detailed log page that also respects the user's role-based permissions.
    *   `[ ]` **ID: E-11-US-03**
        *   **Title:** View Role-Based Upcoming Anniversaries
        *   **Description:** As a user, I want to see a list of upcoming anniversaries that I am permitted to view, so that I am aware of important dates.
        *   **Estimate (Story Points):** 8
        *   **Priority:** High
        *   **Acceptance Criteria:**
            *   Given I am logged in as an **Administrator** or **Privileged Guest User**, then the list displays all upcoming anniversaries, including those for living individuals.
            *   Given I am logged in as a **Guest User**, then the list displays upcoming anniversaries *only* for deceased individuals, to protect the privacy of the living.
            *   Each entry must clearly state the type of event (e.g., "55th birthday"), the person's name, and the date of the anniversary.
            *   The "View All" button navigates to a more detailed calendar or events page that also respects the user's role-based permissions.

## 4. Non-Functional Requirements

*   `[ ]` **ID: NFR-01**
    *   **Title:** Security - Password Encryption
    *   **Description:** User passwords must be stored in the database using strong, one-way hashing algorithms (e.g., Argon2, bcrypt).
    *   **Estimate:** 3
    *   **Priority:** Highest
*   `[ ]` **ID: NFR-02**
    *   **Title:** Security - HTTPS
    *   **Description:** All communication with the application must be transmitted over the encrypted HTTPS protocol.
    *   **Estimate:** 3
    *   **Priority:** Highest
*   `[ ]` **ID: NFR-03**
    *   **Title:** Performance - Response Time
    *   **Description:** The response time for most user actions should not exceed 2 seconds under normal load.
    *   **Estimate:** 3
    *   **Priority:** High
*   `[ ]` **ID: NFR-04**
    *   **Title:** Usability - Responsive UI
    *   **Description:** The application must be fully responsive and display correctly on mobile devices, tablets, and desktops.
    *   **Estimate:** 3
    *   **Priority:** High
*   `[ ]` **ID: NFR-05**
    *   **Title:** Availability & Backup
    *   **Description:** The system must implement daily backups and a recovery plan to meet the 99.5% availability and 4-hour recovery time objective.
    *   **Estimate:** 3
    *   **Priority:** Highest
*   `[ ]` **ID: NFR-06**
    *   **Title:** Security - Web Vulnerabilities
    *   **Description:** Protect against common web vulnerabilities like XSS, CSRF, and SQL Injection through best practices and security audits.
    *   **Estimate:** 3
    *   **Priority:** Highest
*   `[ ]` **ID: NFR-07**
    *   **Title:** Accessibility - Full Support
    *   **Description:** Implement full keyboard navigation, shortcuts, screen reader compatibility, and alternative text for all images.
    *   **Estimate:** 3
    *   **Priority:** Medium

## 5. Technical Tasks

*   `[ ]` **ID: TECH-01**
    *   **Title:** Setup environments, project structure, and CI/CD
    *   **Description:** Create backend/frontend structure, configure dev/staging/prod environments, integrate GitOps with ArgoCD, and implement CI/CD pipeline.
    *   **Estimate:** 5
    *   **Priority:** Highest

*   `[ ]` **ID: TECH-02**
    *   **Title:** Selection and implementation of a library for family tree visualization
    *   **Description:** Research available solutions (e.g., D3.js) and integrate the chosen library into the application.
    *   **Estimate:** 3
    *   **Priority:** High

*   `[ ]` **ID: TECH-03**
    *   **Title:** Implement Logging and Monitoring
    *   **Description:** Integrate a logging framework to capture user actions and system errors, and set up monitoring for server performance with alerts.
    *   **Estimate:** 3
    *   **Priority:** High

*   `[ ]` **ID: TECH-04**
    *   **Title:** Modular Codebase Refactoring
    *   **Description:** Ensure the application architecture is modular to support easy updates and the addition of new data types in the future.
    *   **Estimate:** 3
    *   **Priority:** Medium

*   `[ ]` **ID: TECH-05**
    *   **Title:** Create Admin Bulk Operations Interface
    *   **Description:** Design and build UI components for administrators to perform bulk operations, such as batch uploading files or metadata.
    *   **Estimate:** 3
    *   **Priority:** Medium

## 6. Bugs and Fixes

*   `[ ]` **ID: BUG-01**
    *   **Title:** GEDCOM import fails for files with non-standard date formats.
    *   **Description:** When an admin uploads a GEDCOM file where a date is formatted as `15 MAY 1950` instead of `15 May 1950` (case sensitive), the import process crashes. The parser should be case-insensitive for month names.
    *   **Estimate:** 3
    *   **Priority:** High
*   `[ ]` **ID: BUG-02**
    *   **Title:** Login button is misaligned on Safari mobile.
    *   **Description:** On Safari for iOS, the login button on the main login page is slightly lower than the input fields, creating a minor visual glitch. This does not affect functionality.
    *   **Estimate:** 1
    *   **Priority:** Low


## 7. Comments and Notes

*   **ID: NOTE-01**
    *   **Title:** Discussion on Privacy for Living Individuals
    *   **Description:** We need a team discussion on the default privacy settings for living individuals. Should they be hidden from Guest Users by default? How does a Privileged Guest User get access? We must define these rules before implementing stories related to private data.
*   **ID: NOTE-02**
    *   **Title:** Performance of the Family Tree Visualization Library
    *   **Description:** The chosen library for the family tree might have performance issues with trees containing over 10,000 individuals. We should plan for performance testing and possible optimization strategies (like lazy loading branches) in later sprints.

## 8. Attachments and Links
*   **ID: LINK-01**
    *   **Title:** UI Wireframes of the App
    *   **Description:** Figma designs for the app.
    *   **Link:** `[Figma Wireframes](https://www.figma.com/proto/zmdGaPSVU3GeOVStUzT4pI/FamilyHeritage?t=WzyKlztSVZS84J3O-1&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1&node-id=8-4004)`
*   **ID: LINK-02**
    *   **Title:** Database Schema Diagram
    *   **Description:** The current version of the database entity-relationship diagram (ERD) created in Miro. This should be consulted when making changes to data models.
    *   **Link:** `[ERD Diagram on Miro](https://miro.com/app/board/examplelink)`