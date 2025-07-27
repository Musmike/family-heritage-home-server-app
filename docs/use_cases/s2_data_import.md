## S2. Data Import

### S2.1. Description

The user imports genealogical data into the system using a GEDCOM (.ged) file. This allows the system to populate or update the family tree with existing data from other genealogy software or platforms without modifying any locally added data (e.g., biographies, media, memories), while allowing the administrator to review and confirm changes, including the removal of individuals and related relationships.

### S2.2. Actors
- Administrator

### S2.3. Preconditions

The administrator has a valid GEDCOM file ready for upload.

### S2.4. Postconditions

- The administrator creates or updates the family tree.
- Local content associated with individuals remains intact.

### S2.5. Main Flow

1. The administrator navigates to the GEDCOM file import page.
2. The administrator clicks the "Import GEDCOM file" button.
3. The administrator selects a GEDCOM file to upload.
4. The system parses the GEDCOM file and compares it to the current dataset.
5. The system identifies and categorizes changes:
    - New individuals to be added.
    - Modified individuals with updated GEDCOM-based attributes (e.g., name, birth/death dates, gender).
    - Individuals previously imported but no longer present in the new file (marked as potentially removed).
    - New, updated, or missing relationships.
6. The system checks for any relationships associated with individuals marked for potential removal.
    - For each such person, the system collects and lists all relationships (e.g., parent-child, spouse) that would also be removed if the person is deleted.
7. The system presents a summary panel of changes, including:
    - List of new individuals to be added.
    - List of individuals to whose GEDCOM-based data will be updated.
    - List of individuals no longer present in the GEDCOM file, each with:
        - Their related relationships listed (e.g., "Spouse of Maria", "Father of Anna").
        - Option for the administrator to keep the person in the system or remove them along with associated relationships.
    - List of new, changed, or removed relationships not directly tied to removed individuals.
8. The administrator reviews the summary panel and confirms whether to proceed.
    - For each "removed" individual, they may choose:
        - ✅ Confirm removal (person + relationships)
        - ❌ Keep the individual in the database
        - Option to cancel or revise import before final confirmation.
9. Upon confirmation, the system applies the changes:
    - Adds new individuals and relationships.
    - Updates GEDCOM-based attributes of existing individuals.
    - Removes individuals and their related relationships if marked for deletion.
    - Leaves all local data (biographies, photos, memories) untouched.
10. The system displays a confirmation message, summarizing:
    - Number of individuals added, updated, or removed.
    - Number of relationships added, updated, or removed.
    - Any errors, skipped entries, or warnings.

### S2.6. Alternative Flows
None

### S2.7. Exception Scenarios
SW.1. Invalid GEDCOM file.

Action: The system aborts the import process and displays an error message informing the administrator that the uploaded GEDCOM file is invalid or corrupted. No changes are applied to the system data.

SW.2. Attempt to remove a person linked to local content.

Action: The system warns the administrator that the person is associated with essential local data (such as biographies, photos, or events) and prompts for confirmation. The administrator may choose to keep the person in the system.

SW.3. Removing a relationship breaks tree integrity.

Action: The system displays a warning indicating that removing the selected relationship will cause structural issues in the family tree (e.g., a child without any parent). The administrator must manually confirm the change before proceeding.

### S2.8. Non-functional Requirements
None

### S2.9. Notes and Open Questions
None