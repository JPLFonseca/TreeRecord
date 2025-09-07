# TreeRecord: A Mobile Application for Tree Registration 🌳

### Project Overview 📝
TreeRecord is an Android application created to allow users to document tree species. The app's main goal is to let users pinpoint tree locations on a map and easily access their information whenever they need it. It also includes social features, such as the ability to "like" photos of trees to help identify the most popular images for a specific species.

---

### Core Features ✨
* **Tree Registration**: Users can create a record for a tree, which includes its latitude, longitude, species name, a brief description, and a photo.
* **Map Integration**: The app's central function is to mark and view tree locations on a map.
* **"Likes" System**: This feature allows users to like photos, which helps identify the most popular ones for a particular tree species.
* **Multi-language Support**: The interface can be dynamically switched between Portuguese, English, and Spanish. The chosen language is saved on the user's device, so their preference persists across sessions.

---

### Premium Features (Subscription-Based) 💎
* **Regional Filtering**: Premium users can filter all tree records in the database by five major geographic regions of Portugal: North, Center, South, Azores, and Madeira. This is a paid feature available through a monthly subscription.

---

### Technology Stack 🛠️
* **Language**: Kotlin
* **Database**: Firebase Realtime Database
* **Architecture**: The database is structured with two main top-level nodes: `arvores` (trees) and `premium_users`. This design is optimized for efficient data retrieval and security.

---

### User Feedback and Future Improvements 📈
Usability tests confirmed that the app has an intuitive navigation flow and is easy for users to operate. The project's future development roadmap is based on user feedback and includes several key improvements:
* **Record Management**: Adding features to allow users to edit and delete their tree records.
* **Multimedia Enhancement**: Improving the display and visualization of photos.
* **Advanced Features**: Implementing advanced functionalities like an offline mode and data export.
