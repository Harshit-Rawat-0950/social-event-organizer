# Social Media Event Organizer

Java application for **event organization + social networking** using **Facade, Proxy, Decorator, Singleton** patterns.

## 🎯 Features

- **User Management**: Login/register/search users/View and update user profile/Maintain a friend list
- **Event System**: Create/decorate/nest events (Composite+Decorator)/Cancel/Edit/Display Upcoming
- **Friends Network**: Requests/followers/following/friend/invites
- **Notifications**: RSVP Tracking with accept/decline/maybe/Display count of responses/Notify event updates

## Structure

```
Project Folder/
├── Files/                        # Data files (created on first use) and Files/UI page text
├── src/
│   ├── EventManagementSubSystem/ # Decorator + Composite, event visibility
│   ├── InviteMangerSubSystem/    # Proxy, RSVP tracking
│   ├── FriendSubSystem/          # Friends, followers, following
│   ├── NotificationSubSystem/    # Read/Write workers
│   ├── ReportDashboardSubSystem/ # Console pages (Singletons), terminal look, reports
│   └── UserMangerSubSystem/      # Singletons + Auth
└── test/                         # JUnit tests in the same package names, plus the scenario
```

## Running

| Double-click | What it does |
|---|---|
| `run.bat` | Compiles and starts the app with your real data in `Files/` |
| `test.bat` | Runs every JUnit test. `test.bat EventFacadeTest` runs one class. |
| `scenario.bat` | Types into the real app as 8 users and checks who sees what. `scenario.bat --explore` then opens the app on that sample world. |

`test.bat` and `scenario.bat` work in throwaway folders and never touch `Files/`. Enter an id that does not exist yet at the login screen to register.

In Eclipse, run `UserMangerSubSystem.AppStartUp`. To run the tests there, add `test` as a source folder (right-click it, then Build Path, then Use as Source Folder), then right-click it and choose Run As, then JUnit Test.

## Event visibility

| | Creator | Invited | Friend | Follower of creator | Anyone else |
|---|---|---|---|---|---|
| **Public** | Yes | Yes | Yes | Yes | Yes |
| **Followers** | Yes | Yes | Yes | Yes | No |
| **Friends** | Yes | Yes | Yes | No | No |
| **Private** | Yes | Yes | No | No | No |

Sub-events follow their main event. Declining an invite gives up the access it granted. The creator following you grants nothing.

## Terminal look

Set in `src/UserMangerSubSystem/configData.properties`:

| Setting | Values |
|---|---|
| `ui.theme` | `neon`, `amber` or `ocean` |
| `ui.color`, `ui.unicode` | `auto`, `on` or `off`. Auto uses colour and symbols only in a real console. |
| `ui.width` | Screen width in columns (40 to 300) |
