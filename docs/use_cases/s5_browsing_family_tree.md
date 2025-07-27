## S5. Browsing the Family Tree

### S5.1. Description
Use case scenario describing browsing a responsive family tree.

### S5.2. Actors
- Guest User
- Privileged Guest User
- Administrator

### S5.3. Preconditions
- At least one individual exists in the system.

### S5.4. Postconditions
- A dynamic family tree is rendered based on screen size.
- Users can navigate to biographies or re-center the tree.

### S5.5. Main Flow
1. The user accesses the family tree view.
2. The system displays a responsive family tree centered on a default or selected person. The number of visible generations adapts to the screen resolution.
3. The user may re-center the tree using either a search bar or a list of individuals.
4. The user selects a person in the tree.
5. A panel opens with basic information (e.g, name, birth/marriage/death dates and places) and actions: open biography or re-center tree
6. The system performs the selected action.


### S5.6. Alternative Flows
None

### S5.7. Exception Scenarios
SW.1. No tree data available.
Action: The system displays a message: “Family tree not available."

SW.2. No match found in search or list.
Action: The system shows “No matching person found.”


### S5.8. Non-functional Requirements
None

### S5.9. Notes and Open Questions
None