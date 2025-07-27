## S3. Browsing Biographies

### S3.1. Description
The user searches for, filter, sort, and view individual biographies stored in the system, with access permissions based on user role.

### S3.2. Actors
- Guest User
- Privileged Guest User
- Administrator

### S3.3. Preconditions
- The system contains individual records with associated biographies.

### S3.4. Postconditions
- The user successfully views one or more biographies and optionally navigates to related individuals.
- No data is modified during this process unless the user is an administrator with edit permissions.

### S3.5. Main Flow
1. The user navigates to the "Biographies" section from the main menu.
2. The system displays a searchable and filterable list of individuals, showing basic information such as:
    - Individual ID"
    - Thumbnail photo (if available)
    - First name
    - Last name
    - Birth year
    - Death year
    - Date added
3. The user may perform zero, one or more of the following actions:
    - Enters a search query (name)
    - Applies filters (gender, date of birth range, date of death range, place of residence)
    - Sorts the list in ascending and descending order (by ID, first name, last name, birth year, death year and date added)
4. The system updates the displayed list based on the selected filters, search query, and sorting criteria.
5. The user selects an individual from the list.
6. The system opens the biography page for that person, showing:
    - Personal information (name, dates, places, siblings, children)
    - Profile photo (video format allowed)
    - Family tree fragment
    - Related vital records
    - Gravestone photo (if available)
    - Any local content added by the administrator:
        - Additional information (place of residence, honors and awards, occupation, religion)
        - Biography text
        - Photos
        - Documents
        - Events
        - Memories
        - Heirlooms
        - Locations
    - Note: Content that the user is not authorized to access is silently omitted (e.g., restricted media, sensitive data for living individuals). No message is displayed to indicate that anything is hidden.
7. The user may navigate to related individuals, events, documents, and other related items by clicking a hyperlink.
8. Optional: If the user is an administator, they may access options to edit the biography.


### S3.6. Alternative Flows
None

### S3.7. Exception Scenarios
**SW.1. No individuals match the search or filters.**  
**Action:** The system displays a message such as “No results found. Try adjusting your search criteria.”

**SW.2. Media content on the biography page fails to load.**  
**Action:** The system displays placeholders and an error message indicating that the content could not be loaded.

### S3.8. Non-functional Requirements
None

### S3.9. Notes and Open Questions
None