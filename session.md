# Session report: finishing the Social Event Organizer

A record of everything done in this working session, written to learn from: what was wrong, why it was wrong, what changed, and how it was checked. **Nothing here has been committed.** All of it is in your working tree.

| | Start of session | End of session |
|---|---|---|
| Compiles? | No | Yes |
| Working features | Login and registration code, but the app could not run | Everything in the README |
| Automated tests | 8 test classes | 42 test classes, **867 test cases**, all passing |
| End-to-end check | None | `scenario.bat`: **661 checks** over 149 real app sessions, all passing |
| Line / branch coverage | Not measured | **94.5% / 94.3%** (JUnit only) |

---

## 1. How the session went

1. **Analyse and complete the app.** Read the code, the UML, and the `Project_Report.pdf` recovered from git history (commit `afebcae`), then finished it.
2. **`run.bat`.** So you could try it.
3. **Plan.** You asked for a cool UI, exhaustive tests and an end-to-end batch file. Analysis showed the third goal needed a new app rule, so you chose four-level visibility and the Neon theme.
4. **Build in order:** make it testable, add the rule, build the UI, write the tests, write the scenario.
5. **Verify the verification:** coverage, deliberately breaking the code, and looking at real console windows.

---

## 2. Bugs found and fixed

Each has a lesson, since that's the useful part.

### Stopping the app from running at all

| # | Bug | Where | Why it was wrong | Fix |
|---|---|---|---|---|
| 1 | Missing `;` | `UserLogin` | One syntax error stops the entire project compiling | Rewrote that section |
| 2 | Pages closed their `Scanner` | Every page and `UserLogin` | Closing a `Scanner` also closes `System.in`, permanently, so the next read crashes | One shared `ConsoleInput` singleton that never closes |
| 3 | Unreachable `UserData` line | `UserLogin` | It sat after the login loop, and `new User.UserData(...)` can't construct a non-static inner class that way | Made `UserData` a `static` nested class, built on successful login |
| 4 | Wrong `Notification` import | `User` | `com.sun.nio.sctp.Notification` is an unrelated JDK class your IDE auto-imported | Imported your own class |
| 5 | Production code imported a test class | `User` imported `EventComponentTest` | The app then needs JUnit just to compile | Removed |
| 6 | `app.delay=500` used as a key | `UserLogin` | The key is `app.delay`, so the lookup returned `null` and parsing threw an exception that was silently swallowed | Used the right key with `getIntProperty` |
| 7 | Page text keys missing | `configData.properties` | `Files.mainpage` and the others didn't exist, so pages couldn't load | Added keys and created `Files/UI/*.txt` |
| 8 | `TextElement.display()` printed nothing | `TextElement` | A `TODO` meant no page text ever appeared | Implemented it |

**Lesson:** an empty `catch` block hides bugs. Bug 6 existed only because the exception vanished silently.

### Wrong data

| # | Bug | Where | Why it was wrong | Fix |
|---|---|---|---|---|
| 9 | Name and email swapped | `UserRegistration` | Passed `(id, email, name, ...)` to a `(id, name, email, ...)` constructor, so "Welcome" printed the email | Fixed the call and swapped the columns in your `Users.txt` |
| 10 | Follows saved to the friends file | `FriendRepo.createRelationship` | Wrong path, so follows looked like friendships | Writes to the follow file |
| 11 | Follows saved with no timestamp | `FriendRepo` | The reader expects a third column, so reading would crash | Saves `LocalDateTime.now()` |
| 12 | Follower and Following mixed up | `FriendRepo.getRelationship` | The direction checks were inverted, and "Following" returned a `Follower` | Defined the file format in a comment and rewrote it from one direction |
| 13 | Closeness lost on read | `getallRelationship` | Always built `new Friend(..., 0)` | Reads the stored value |
| 14 | Unfollowing removed friendships | `deleteRelationship` | It deleted from both files in both directions | Added separate `deleteFriend` and `deleteFollow` |
| 15 | Events ran together on one line | `MainEventManeger.addEvent` | No `\n` when appending | Added it |
| 16 | Varargs create skipped a field | `createBasicEvent(String...)` | Used `args[0,1,2,4,5,6]`, skipping index 3, and required more than 6 arguments | Uses indexes 0 to 5 and checks for at least 6 |
| 17 | Everyone saw everyone's invites | `getInvites(int id)` | The `id` parameter was ignored | Filters by receiver |
| 18 | Notification delete never matched | `NotificationRepo` | Compared strings with `!=`, which compares references, not text | Replaced with a per-user delete |
| 19 | Notifications had no recipient | `NotificationRepo` | The file format stored no user | Format is now `userId,time,text`, text last so it may contain commas |
| 20 | Commas corrupted records | All CSV input | A comma in a name adds a column | Input refuses `, ; \|`, and `validate()` enforces it |
| 21 | Page CSV broke on commas | `TextElement.readFile` | `split(",")` breaks if the content has a comma | Parses from the right-hand end |
| 22 | Setting a password twice kept the old one | `PassWordManager.setPassword` | It appended, and reading returns the first match | Replaces an existing entry |
| 23 | `null` password crash | `UserLogin` | `pm.getPassWord(id).equals(...)` crashes when a user has no password | Reversed to `password.equals(pm.getPassWord(id))`, which is null-safe |
| 24 | News decorator ignored its arguments | `NewsEventDecorator` | The constructor never stored the headline or description | Stores them |
| 25 | Event page read the main menu's text | `EventPage` | Used `Files.mainpage` | Uses `Files.eventpage` |

**Lesson:** in Java, `==` and `!=` on strings compare object identity. Always use `.equals()`.

### Crashes and logic

| # | Bug | Where | Fix |
|---|---|---|---|
| 26 | Answering with no invites crashed | `invites[0]` on an empty array in `InvitesPage` | Checks first and shows a message |
| 27 | Too many failed logins failed silently | `AppStartUp` swallowed the exception | Shows the message |
| 28 | Pages called each other forever | `MainPage` called pages, which called `MainPage`, growing the stack | Pages run in a loop and `return` |
| 29 | `app.mode` trailing space | `ConfigLoader` | Values are trimmed |
| 30 | Windows-only screen clear spawned a process | `AppStartUp.clearConsole` | Replaced with `Terminal.clear()` |

### Found later, by the analysis and tests

| # | Bug | How it was found | Fix |
|---|---|---|---|
| 31 | Editing or cancelling a sub-event notified nobody | Reasoning about the plan: RSVPs sit on the parent | Notifies the main event's attendees |
| 32 | Past dates accepted silently | **Your own data**: your "Happy" event is dated 2025-11-20, so it never showed under Upcoming | Console refuses past dates, while the manager still accepts them for old data |
| 33 | Anyone could read who responded to any event | Plan review | Names are creator-only, counts are for anyone who can see the event |
| 34 | `System.exit` inside input handling | Test design: it would kill JUnit | Throws `InputClosedException` |
| 35 | Symbols would print as `?` in code page 437 | **A failing test.** Java's `IBM437` can't encode `►`, `♫`, `☼` even though the font can draw them | Auto mode checks every glyph and falls back to ASCII |
| 36 | `meter()` drew a block when the maximum was 0 | **A failing test** | Added the `max > 0` check |

**Lesson:** bug 32 came from reading real data, and bug 35 from a test proving an assumption of mine wrong. My plan said those symbols were safe in code page 437, and that was only partly true.

---

## 3. Features added

### Part A: completing the app

- **Events:** create, nest, decorate, edit, cancel with cascade, upcoming list. Stored in `events.txt`, rebuilt into objects by `MainEventManeger.build()`.
- **Decorators** now do something: each adds a tag. They wrap any `EventComponent`, so they stack, and they are stored as `MUSIC|Queen;URGENT|3`.
- **Friends:** new `FriendFacade` with requests, follow, unfollow, lookups.
- **Invites and RSVP:** `Invite` gained `eventId`, `RsvpRepository` and `RsvpStatus` are new. "Maybe" keeps the invite open.
- **Proxy pattern:** `InviteRepo` interface, `InviteRepository` (real), `InviteRepositoryProxy` (loads lazily and hides expired invites).
- **Notifications:** `NotificationFacade` plus read and write workers.
- **Session:** `CurrentApplicationUser` singleton, matching your UML.
- **Pages:** all completed, and a new `ProfilePage`.

### Part B: event visibility

New pure function `EventAccessPolicy.canView()`, with the facts gathered by `EventMangementFacade`.

| | Creator | Invited | Friend | Follower | Anyone else |
|---|---|---|---|---|---|
| **Public** | Yes | Yes | Yes | Yes | Yes |
| **Followers** | Yes | Yes | Yes | Yes | No |
| **Friends** | Yes | Yes | Yes | No | No |
| **Private** | Yes | Yes | No | No | No |

Rules chosen around it: sub-events follow the main event, invites go only to main events, declining gives up the access the invite granted, and a hidden event gives the same "No event with id" message as a missing one so its existence doesn't leak. Old 12-column rows load as Public.

**Lesson:** keeping the rule as a pure function made it possible to test all 80 combinations in a few lines.

### Part C: terminal UI

| New class | Job |
|---|---|
| `Terminal` | Detects colour, Unicode and width, once |
| `Theme` | Neon, Amber and Ocean palettes as 256-colour codes |
| `Glyph` | Every symbol with an ASCII stand-in |
| `Style` | Colour, plus length, padding, truncating and wrapping that ignore invisible colour codes |
| `Widgets` | Logo, title bars, boxes, tables, meters, bars, badges |
| `Screen` | Page frame and held-back messages |
| `EventCardRenderer` | Draws an event as a card |
| `EventView` (events package) | Structured event data, filled in by each Composite and Decorator layer |

Design ideas worth noticing:

- **Colour codes are invisible but count as characters.** `"text".length()` is wrong for coloured text, which is why `Style.visibleLength` exists. Without it, boxes go crooked.
- **Messages wait for the next prompt.** Clearing the screen would otherwise erase "Event created" immediately.
- **Everything degrades.** Pipes and Eclipse's console get the same layout in plain ASCII with no codes.
- **Symbols are written as `\u2554`-style escapes**, so the code compiles even if Eclipse uses another file encoding.
- **Your `TextElement` and `Files/UI` system stayed.** It gained two styles and a `{invites}` placeholder.

---

## 4. Making the code testable

Small changes, each removing one obstacle:

| Change | Obstacle removed |
|---|---|
| `ConfigLoader` honours `-Dapp.config=` | Tests would otherwise write to your real data |
| `ConsoleInput.useInput()` | Tests can type |
| `InputClosedException` instead of `System.exit` | `System.exit` would kill the test runner |
| `AppStartUp` builds its repository on each run | A cached singleton would keep the previous test's data |
| `BasicEvent.setOwnerId` and `setVisibility` | Test setup in other packages |
| Tests moved to `Project Folder/test/` | Clean separation, and `run.bat` no longer filters by filename |

Also deleted 4 stray tracked files (`UUUserdemo.txt`, `UUserdemo.txt`, `Userdemo_del.txt`, `userdemo.txt`), each empty or 2 bytes, left behind by old tests writing to the project folder. Tests now use temp folders.

**Lesson:** code that's hard to test usually has a hidden dependency, such as a fixed path, a global, or `System.exit`.

---

## 5. The tests

### JUnit (`test.bat`)

| Area | Classes | What's notable |
|---|---|---|
| Events | 8 | `EventAccessPolicyTest`: 80 cases. `EventVisibilityMatrixTest`: 320 cases through the real facades and files (16 relationship states × 4 visibilities × 5 invite states) |
| Friends | 3 | `FriendPairStatesTest`: all 16 pair states, checked from both sides |
| Invites | 3 | Proxy, RSVP, capacity, expiry, id prefix collisions |
| Notifications | 3 | Recipient isolation, commas |
| Users | 8 | Login attempts, registration columns, config, whole-app flows |
| Dashboard | 16 | Every key on every page, and box widths in every mode and width |
| Support | 1 | `SuiteCompletenessTest` fails if a test class is left out of `AllTests` |

Support code: `TestEnvironment` (a throwaway world per test), `ConsoleDriver` (types into the app and fails if the script runs short or has leftovers, which catches menu changes), `Fixtures`, `PrettyRunner`.

**Lesson:** expected values in `EventVisibilityMatrixTest` and the scenario are written by hand, not computed by calling the app's own policy. A test that asks the code for the right answer can only ever agree with it.

### Scenario (`scenario.bat`)

Starts the real app as separate programs, types as 8 users, and reads the screen. Uses `build/scenario/`, and fails if your real `Files/` changes.

| Phase | Checks | Proves |
|---|---|---|
| 1. Cast | 12 | Registration, wrong password |
| 2. Relationships | 89 | All 16 states on both sides |
| 3. Events | 11 | 8 kinds × 4 visibilities, plus lifecycle events |
| 4. Who sees what | 40 | Exact lists for each person, plus a printed matrix |
| 5. Display | 164 | Tags, nesting, sorting, cancelled and past events |
| 6. Invites and RSVP | 116 | Accept, decline, maybe, maybe then accept, counts |
| 7. Permissions | 9 | Non-owners refused, hidden stays hidden, file unchanged |
| 8. Live changes | 62 | Unfriend, unfollow, re-friend, tighten visibility |
| 9. Cancel and edit | 55 | Right people notified |
| 10. Notifications and reports | 79 | Exact lists and numbers per person |
| 11. Look | 23 | Colour, every colour switched off again, symbols, all box lines exactly 78 wide, all themes |
| 12. Your data | 1 | Checksum unchanged |

`scenario.bat --explore` then opens the app on that world. It was written but **not run**, because it needs a person typing.

---

## 6. How the checking itself was checked

### Coverage

94.5% of lines and 94.3% of branches, from JUnit alone, measured with JaCoCo in a scratch copy. Most misses are `catch (IOException)` blocks that need a failing disk, plus real-console code paths.

### Deliberately breaking the code

A test suite that always passes proves nothing, so 16 rules were broken one at a time in throwaway copies, running both suites against each. The numbers are how many checks failed, so higher means more of the suite noticed.

| Rule broken on purpose | JUnit failures (of 867) | Scenario failures |
|---|---|---|
| Friends-only events let followers in | 13 | 60 |
| Private events let friends in | 10 | 61 |
| Follower direction swapped | 11 | 11 |
| Declining keeps access | 36 | 12 |
| Anyone can change any event | 6 | 3 |
| Boxes one column too narrow | 2 | 1 |
| Upcoming not sorted | 2 | 1 |
| Decliners still notified | 2 | 2 |
| Hidden events reachable | 1 | 3 |
| Everyone sees the RSVP bar | 2 | 1 |
| Cancelling doesn't cascade | 1 | 2 |
| Registration swaps name and email | 3 | 23 |
| Expired invites shown | 2 | 17 |
| Sub-event edit notifies nobody | 1 | 2 |
| Colour never reset | 1 | 0, then 1 after the fix below |
| Cancelling keeps pending invites | 1 | 2 |

**Result: JUnit caught all 16. The scenario caught 15 and missed one.** It confirmed colour codes were present but never checked that each was switched off. A new check closed that gap, and rerunning that broken version confirmed it now fails, which is why the scenario has 661 checks rather than 660.

**Limit:** these are 16 hand-picked rules, not every possible bug. Several were caught by only one or two tests, so those rules have less of a safety net than the visibility rules. The other 15 mutations were not rerun after the new check, which only adds to the scenario.

### Real console windows

Opened a sample screen in both Windows Terminal and the old console and screenshotted them. Colours, gradient logo, strike-through and box characters rendered in both.

**What this did not cover:** I never typed into the finished app in a real console. My sessions were piped, and the sample screen only displayed. You ran `run.bat` yourself at about 20:58, and your data shows it worked far enough to send a friend request, but I haven't heard how it looked. So **password masking (`Console.readPassword`) is untested by me**, and the live feel is yours to judge.

---

## 7. Mistakes I made along the way

These are in the report on purpose. Each one is a normal way to go wrong.

| Mistake | What happened | Takeaway |
|---|---|---|
| Hand count wrong | The scenario expected Asha to have 39 events, and the app correctly had 38 | When a test fails, the test can be the wrong one. Check before "fixing" the code. |
| Two wrong tests | One counted `\r\n` as `\r`, one searched for text the wrapper had split | Same |
| Assumed code page 437 was safe | Stated in my plan, disproved by a test | Plans contain guesses, and tests find them |
| My test runner hid failure messages | Filtering out `org.junit` lines also removed `org.junit.ComparisonFailure: expected...` | Keep the first line |
| Break-it harness was broken | Git Bash mangled a classpath, so JUnit died at startup and reported "1 failed". That looked like a catch but proved nothing. It was spotted because `0.002s` is impossibly fast. | **A failure is not evidence unless it fails for the right reason.** |
| Scenario blind spot | Breaking colour reset passed all 660 scenario checks. Only JUnit caught it. | This is exactly what deliberately breaking code is for. Added a check. |
| False claim about escapes | I wrote that symbols were `\u` escapes, but the tool writing my files had decoded them, so `Glyph.java` held literal characters. A teammate's Eclipse on Windows-1252 would have shown garbage. Spotted by chance when reading a file back. | Verify what's on disk. Converted to real escapes, and proved it by compiling under Windows-1252. |
| Alarm over your data | `invite.txt` and `notification.txt` had a new timestamp, after I'd promised tests never touch them. It was you using the app: the file holds your friend request. | Check before assuming either way |
| Shell heredoc failed | A large command failed on quoting before running anything | Switched to writing a bundle file and splitting it |
| Screenshot caught the wrong window | The first old-console capture showed your editor (open on `run.bat`), because it was on top. It stayed in my scratch folder. | Captured by window handle instead |
| `FindWindow($null, ...)` found nothing | PowerShell turns `$null` into `""` for string parameters | Use `[NullString]::Value` |

---

## 8. Every file changed

### New in `Project Folder/src`

- **Events:** `EventVisibility`, `EventAccessPolicy`, `EventView`
- **Friends:** `FriendFacade` (a stub of yours, now implemented)
- **Invites:** `InviteRepo`, `InviteRepositoryProxy`, `RsvpRepository`, `RsvpStatus`
- **Notifications:** `NotificationReadWorker`, `NotificationWriteWorker`
- **Dashboard:** `ConsoleInput`, `InputClosedException`, `Glyph`, `Theme`, `Terminal`, `Style`, `Widgets`, `Screen`, `EventCardRenderer`, `ProfilePage`
- **Users:** `CurrentApplicationUser`

### Changed in `Project Folder/src`

| File | Change |
|---|---|
| `BasicEvent` | ids, owner, parent, status, decorations, visibility, CSV round trip, `describe()`, `getInvited()` |
| `CompositeEvent` | Nested details and views, display |
| `EventComponent` | Added `getDetails`, `describe`, `getInvited` |
| `EventDecorator` and 5 decorators | Text and tags, rebuilding from storage, bug 24 |
| `MainEventManeger` | Rewritten: flows, operations, queries, validation, storage. Bugs 15, 16 |
| `EventMangementFacade` | Implemented: actions, visibility queries. Bugs 31, 33 |
| `Follower`, `Following`, `Friend`, `FriendManagement` | Interface methods |
| `FriendRepo` | Bugs 10 to 14 |
| `Invite`, `InviteRepository`, `InviteManagerFacade` | Event invites, RSVP, removal. Bug 17 |
| `Notification`, `NotificationRepo`, `NotificationFacade` | Recipients, time-ago. Bugs 18, 19 |
| `DashboardFacade`, `TextElement`, all `*Page` | UI. Bugs 2, 8, 21, 25, 26, 28 |
| `AppStartUp`, `UserLogin`, `UserRegistration` | Flow, shared input. Bugs 1, 3, 6, 9, 23, 27, 30 |
| `User` | Real `UserData`. Bugs 4, 5 |
| `UserManagementFacade`, `SystemDataService`, `UserRepository`, `ConfigLoader`, `PassWordManager` | Stubs implemented, config override. Bugs 22, 29 |
| `configData.properties` | Page and RSVP paths, `ui.*`, `app.registerDelayMs` |

### Data and other files

| File | Change |
|---|---|
| `Files/UI/*.txt` | New, 7 page text files |
| `Files/Users.txt` | Name and email columns swapped (bug 9). **Also contains the user you registered while trying the app.** |
| `Files/password.txt` | Your test user's entry, from trying the app |
| `Files/invite.txt`, `notification.txt` | Your friend request from `Harhat (101)` to user 10, from trying the app |
| `Files/events.txt` etc. | Created when you ran the app. Untracked. |
| `run.bat`, `test.bat`, `scenario.bat` | New |
| `ReadMe.md` | Structure, running, visibility, terminal settings |
| `.gitignore` | Added `build/` and `lib/` |
| `.classpath` | Added `test` as a source folder. Gitignored, so teammates add it once. |
| 4 stray `*demo*.txt` | Deleted |

### Moved to `Project Folder/test`

`EventComponentTest`, `MainEventManagerTest`, `FriendRepoTest`, `InviteSubSystemTest`, `NotificationRepoTest`, `TextElementTest`, `PasswordTest`, `UserTest`. All except `EventComponentTest` were also extended or rewritten. All other classes under `test/` are new.

---

## 9. Concepts to take away

| Concept | Where to look | The idea |
|---|---|---|
| Facade | `EventMangementFacade` | One simple door into a subsystem |
| Composite | `CompositeEvent`, `addChildren` in the renderer | A group and a single item share one interface, so trees need no special cases |
| Decorator | `EventDecorator.describe()` | Wrap an object to add behaviour, and stack as many as you like |
| Proxy | `InviteRepositoryProxy` | Same interface as the real thing, adding control in front of it |
| Singleton | `ConsoleInput`, `CurrentApplicationUser` | One shared instance, handy but hard on tests, hence `Terminal.reset()` |
| Pure functions | `EventAccessPolicy` | No files or state, so trivially testable |
| Parameterized tests | `EventVisibilityMatrixTest` | One test body, hundreds of inputs |
| Test seams | Section 4 | Small hooks that let tests replace the outside world |
| Mutation testing | Section 6 | Break the code on purpose to test the tests |

---

## 10. Still open

**Needs you:**

1. Try `run.bat` by hand, especially password masking and moving between pages.
2. Before committing, decide whether your test user and data files belong in the repo.
3. `Project_Report.pdf` is out of date: it describes the old stubs, signatures and OOP counts.

**Known limits, left alone:**

- **Weak password storage.** The AES key is hardcoded and `password.txt` is committed, so anyone with the repo can decrypt them. If the repo is public, treat those passwords as exposed.
- **UML still differs.** It draws proxies for users, friends and notifications, but only invites have one, as the README says.
- **Expired invites stay in `invite.txt`,** hidden by the proxy, with their RSVP left as Pending.
- **Cancelled events still show in the report's responses list,** including any "waiting" count, and events with no invites show an empty bar.
- **`password.txt` has two entries for user 10.** The first one wins.
- **Unused:** `Files/configData.properties`, `login.sessionTimeout`, `event.bookingCutoffHours`.
- **Typing non-English characters** can be unreliable in the old console with Java.
- **Dev mode** still waits 5 seconds after login. Set `app.mode` to something other than `dev` to skip it.
