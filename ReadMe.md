# Social Media Event Organizer

Java application for **event organization + social networking** using **Facade, Proxy, Decorator, Singleton** patterns.

## 🎯 Features

- **User Management**: Login/register/search users/View and update user profile/Maintain a friend list
- **Event System**: Create/decorate/nest events (Composite+Decorator)/Cancel/Edit/Display Upcoming
- **Friends Network**: Requests/followers/following/friend/invites
- **Notifications**: RSVP Tracking with accept/decline/maybe/Display count of responses/Notify event updates

Structure

src/
├── EventMangementSubSystem/ # Decorator+Composite
├── InviteSubSystem/ # Proxy
├── FriendSubSystem/ #
├── NotificationSubSystem/ # Read/Write workers
└── UserMangementSubSystem/ # Singletons+Auth
