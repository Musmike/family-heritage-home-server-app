## S4. Editing Biography

### S4.1. Description
A use case scenario describing how the administrator edits the biography of a selected individual.

### S4.2. Actors
- Administrator

### S4.3. Preconditions
- The biography to be edited exists.
- The user has permission to modify biography content.

### S4.4. Postconditions
- The biography is updated and saved in the system.
- Changes are reflected in the biography page.
- Read-only imported content remains unchanged.


### S4.5. Main Flow
1. The administrator navigates to the individual's biography page.
2. The administrator selects the "Edit Biography" button.
3. The system opens an editable form for biography narrative and attached content.
4. The administrator updates text, uploads or links media (photos, documents, etc.) and organizes content sections.
5. The administrator saves the changes.
6. The system confirms success and displays the updated biography.

### S4.6. Alternative Flows
None

### S4.7. Exception Scenarios
**SW.1. Unauthorized user attempts to edit.**  
**Action:** The edit option is hidden or access is denied.

**SW.2. Media upload fails (e.g., file too large, unsupported format).**  
**Action:** The system shows a validation message and prevents saving until fixed.

### S4.8. Non-functional Requirements
None

### S4.9. Notes and Open Questions
None