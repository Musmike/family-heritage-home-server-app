 
# 📘 Actors Specification

### Guest User  
A user with restricted read-only access. Can browse the list of individuals, search, filter, and view detailed pages, but does not have access to sensitive or private information (e.g., certain documents or living persons' personal data). Ideal for general family guests with basic access needs.

### Privileged Guest User
A read-only user with access to public data and private data available only to privileged users. Can explore biographies, vital records, photos, videos, documents and statistics, but still cannot make any changes to the data. Suitable for trusted family members.

### Administrator  
A user with full permissions, responsible for managing the database of individuals, editing and deleting data, and adding new biographies, vital records, videos, photos and documents. Has access to all data, including administrator-only and system-level data.

---

# ✅ User Stories

### Guest User

- As a guest user, I want to log in using a shared username and password so that I can securely access the system.  
- As a guest user, I want to browse the family tree so that I can explore relationships and connections between people.  
- As a guest user, I want to browse and search for individuals from the family tree so that I can quickly find specific relatives.  
- As a guest user, I want to view detailed information about each person in the family tree so that I can learn more about their life and history.  
- As a guest user, I want to browse historical documents along with their translations so that I can understand their content and learn what information they reveal about the people and events they describe. 
- As a guest user, I want to browse family photos and videos so that I can get a more personal and visual understanding of the people and moments from the past.  
- As a guest user, I want to browse photos of gravestones from different places so that I can learn where certain individuals are buried and explore their memorials.  
- As a guest user, I want to browse maps and location plans, such as homes, yards, or towns, so that I can better understand where and how people from the family tree lived.  
- As a guest user, I want to browse photos and descriptions of family heirlooms so that I can learn about personal and historical items passed down through generations.
- As a guest user, I want to browse and search through family events with details like date, location, and people involved so that I can understand the key moments in our family history.
- As a guest user, I want to view and explore family memories in various formats (text, audio, video) so that I can connect with the past on a personal level.
- As a guest user, I want to view statistical data on charts and maps so that I can better understand patterns and trends within the family history.  
- As a guest user, I want to export selected data like family trees, biographies, and media so that I can save or share it with others in a suitable format (PDF, ZIP, image, video).
- As a guest user, I want to choose between Polish and English so that I can use the application in the language I’m most comfortable with.  
- As a guest user, I want to adjust accessibility settings such as dark mode, high contrast mode, font size, and use the system via keyboard or screen reader, so that I can navigate, read, and interact with the application comfortably regardless of my physical, visual, or cognitive abilities.

### Privileged Guest User

- As a privileged guest user, I want to log in, so that I can access public data and private data available only to privileged users.  

### Administrator

- As an administrator, I want to log in so that only authorized people can access sensitive features like adding, editing and deleting data.   
- As an administrator, I want to log in, so that I can access all data, including administrator-only and system-level data.
- As an administrator, I want to restrict access to specific features and data based on user roles so that sensitive content is protected appropriately.
- As an administator, I want to upload a GEDCOM file in order to provide up-to-date data about individuals in the family tree.
- As an administrator, I want to edit the detailed information about each person — excluding data imported from the GEDCOM file — so that biographies, facts, and life events stay correct and up to date.
- As an administrator, I want the system to automatically enrich biography pages with GEDCOM data, family tree segments, gravestone photos, and tagged records so that the page is comprehensive with minimal manual effort.
- As an administrator, I want to upload, edit, and remove historical documents and their translations so that the archive reflects verified and relevant content. 
- As an administrator, I want to upload, edit, and remove family photos and videos so that the media collection remains personal, relevant, and appropriate. 
- As an administrator, I want to manage photos of gravestones so that I can ensure they are properly labeled, relevant, and linked to the correct individuals.  
- As an administrator, I want to upload and manage maps and location plans so that users have access to accurate and meaningful spatial context.  
- As an administrator, I want to manage heirlooms and memorabilia by uploading, editing, and removing items and linking them to relevant people and events so that the collection stays meaningful and organized.
- As an administrator, I want to create, edit, and delete family events and link them to individuals, locations, and media so that the system captures the full family history.
- As an administrator, I want to create, edit, and delete family memories, including uploading related media and linking them to people or places, so that narratives remain rich and contextualized.
- As an administrator, I want to maintain a private journal with formatted entries, embedded media, and links so that I can record daily events, personal reflections, and important notes to keep track of what happened each day.
- As an administrator, I want to assign custom labels to data so that I can categorize and organize information more effectively.
