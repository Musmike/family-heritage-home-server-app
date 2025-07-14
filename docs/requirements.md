# App Requirements

## Functional Requiremnts

### 1. User Authentication
- The system allows users to log in using predefined accounts.
- The system must distinguish between Guest User, Privileged Guest User, and Administrator roles after login.

### 2. User Role Management
- The system must restrict access to sensitive data based on user role.
- Only administrators can access adding, editing and deletion functionality.

### 3. Data Import
- The administrator must be able to import data using GEDCOM files to update the family tree data of the application.
- Imported data must be protected from direct modification (read-only).

### 4. Biographies
- Users must be able to browse a list of individuals.
- Users must be able to filter, sort and search for specific individuals based on name or other attributes.
- Users must be able to view a detailed biography page for each person.
- The administrator must be able to manually edit biography pages and additional important photos from the gallery, historical documents, maps and location plans, heirlooms and memorabilia, events and memories.
- The system must automatically add a fragment of the person's family tree, previously tagged vital records, a photo of their gravestone to their biography page and events from a GEDCOM file.

### 5. Family Tree Visualization
- Users must be able to browse and navigate a graphical family tree.
- Users must be able to open a biography page for each person directly from the family tree.

### 6. Vital Records and Historical Documents
- Users must be able to view scanned vital records and other historical documents along with translations.
- Users must be able to search, sort and filter documents by type, person, date or place or other attributes.
- User must be able to view a detailed page for each document.
- The administrator must be able to upload, edit and delete documents and pages.

### 7. Photo & Video Gallery
- Users must be able to view family photos and videos.
- Users must be able to search, sort and filter media by person, place or other attributes.
- User must be able to view information about photos and videos.
- The administrator must be able to upload, edit attributes and remove photos and videos.

### 8. Gravestone & Cemetery Data
- Users must be able to browse gravestone photos and see burial-related information.
- Users must be able to search, sort and filter graves by name or cemetery location.
- User must be able to view a detailed page for each gravestone photo.
- The administrator must be able to upload, edit attributes and remove these photos.

### 9. Maps and Location Plans
- Users must be able to view maps and plans (e.g., homes, yards, towns).
- Users must be able to search, sort and filter maps by type, location or associated individuals.
- User must be able to view a detailed page for each location.
- The administrator must be able to upload and manage these maps.

### 10. Family Heirlooms & Memorabilia
- Users must be able to browse photos and descriptions of family heirlooms (e.g., jewelry, clothing, tools, personal items).
- Users must be able to search, sort and filter them by attributes.
- User must be able to view a detailed page for each object.
- Heirlooms must be linkable to specific individuals, dates, or events.
- The administrator must be able to upload, edit attributes and remove these photos.

### 11. Family Events
- User must be able to browse family events (e.g. births, weddings, deaths) with associated data (date, place, type, people involved).
- Users must be able to search, sort, and filter events by attributes (e.g. type, date, location, participants).
- User must be able to view a detailed page for every family event.
- Each event may be linkable to individuals and locations.
- Administrator must be able to create, edit attributes, and remove family events.
- Events may have attached media (photos, documents) and associated memories.

### 12. Family Memories
- User must be able to browse family memories in narrative form (text, audio, or video).
- Users must be able to search, sort, and filter memories by attributes (e.g. date, theme, related people or places).
- User must be able to view a detailed page for every memory.
- Each memory may be linkable to individuals, locations, family events.
- Administrator must be able to upload, edit attributes, and remove memories.
- Memories may have attached media (photos, documents).

### 13. Administrator’s Private Journal
- The administrator must be able to search and sort entries by date, title, or linked content.
- Only the administrator must have access to the journal feature (view, add, edit, delete).
- The journal must allow creating and editing text entries with a title, date, and rich text content (e.g. markdown or basic formatting).
- Journal entries may include embedded media.

### 14. Statistical Data Visualization
- Users must be able to view graphical representations of family data.
- The system should support various types of charts (e.g., bar, line, pie, maps).
- Users must be able to filter and interact with chart data (e.g., by time period, location, surname).

### 15. Tagging and Categorization
- The administrator must be able to add attributes to documents, individuals, or media for easier organization, searching and filtering.

### 16. Localization and Language Support
- Users must be able to choose between at least Polish and English interfaces.

### 17. Data Export
- Users must be able to export selected data (e.g., family tree, biographies, documents) as PDF, ZIP, video and image formats.


## Non-Functional Requirements

### 1. Performance
- The system must support at least 100 concurrent users with acceptable response times (under 2 seconds for most actions).
- The system should support browsing multimedia (photos/videos) smoothly without excessive loading delays.
- Search and filtering operations across biographies, documents and media must return results within 3 seconds under normal load.

### 2. Scalability
- The system should support growing numbers of individuals, documents, and media files without performance degradation.
- The system should support the future addition of new data types and functions.

### 3. Security
- The system must restrict write/edit permissions to authorized administrator accounts.
- The login system must prevent unauthorized access to sensitive data (e.g., living individuals).
- User passwords must be securely stored using hashing algorithms.
- All sensitive data, including login sessions and uploads, must be transmitted over HTTPS.
- The application must protect against common web vulnerabilities (e.g., XSS, CSRF, SQL Injection).

### 4. Availability and Reliability
- The system must be available 99.5% of the time, excluding scheduled maintenance.
- The system should be reliably available on the internal network with minimal downtime.
- The system must handle multiple users accessing read-only content concurrently without conflict.
- In case of unexpected failure, the system must recover from backups and resume operation within 4 hours.
- All data, especially critical (e.g., biographies, uploaded documents, administrator journal) must be backed up at least daily.

### 5. Maintainability
- The codebase should be modular to allow easy updates (e.g., adding new document types or views).
- Admin interface should support bulk operations (e.g., batch upload of files or metadata).

### 6. Usability
- The interface must be intuitive and easy to use for non-technical users, including older family members.
- Navigation between sections (tree, documents, media, person pages) must be consistent and simple.

### 7. Accessibility
- The application must support:
    - Dark mode
    - Adjustable font size
    - High contrast mode
    - Full keyboard navigation
    - Keyboard shortcuts
    - Screen reader compatibility
    - Alternative text for images

### 8. Portability and Compatibility
- The application must be compatible with modern web browsers (Chrome, Firefox, Safari, Edge) and responsive for desktop, tablet, and mobile devices.
- The system must be deployable on common web servers and cloud infrastructure (e.g., Linux-based environments).

### 9. Logging and Monitoring
- The system must keep logs of user actions (especially administrative changes) for at least 6 months.
- Server performance metrics (CPU, memory, disk usage) and errors must be monitored and alert administrators in case of anomalies.
- All failed login attempts must be logged and reported if exceeding a threshold.

