package ScenarioSubSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
 * The end-to-end check behind scenario.bat. It starts the real application as a
 * separate program, types into it like a person would, and reads the screen. It
 * works in build/scenario, which is wiped on every run, and verifies at the end
 * that the real data files were not changed.
 *
 * What SHOULD happen is written out by hand in this file, never asked of the app.
 */
public final class ScenarioRunner {

    private static final Path SANDBOX = Paths.get("build", "scenario");
    private static final Path TRANSCRIPTS = SANDBOX.resolve("transcripts");
    private static final Path REAL_DATA = Paths.get("Files");
    private static final String CLASSES = Paths.get("build", "scenario-classes").toString();
    private static final LocalDate BASE_DATE = LocalDate.of(2099, 1, 1);

    private static final int ASHA = 1, BEN = 2, CHLOE = 3, DEV = 4, ESHA = 5, FARHAN = 6, GITA = 7, HARI = 8;
    private static final String[] NAMES = {"", "Asha", "Ben", "Chloe", "Dev", "Esha", "Farhan", "Gita", "Hari"};
    private static final int[] EVERYONE = {ASHA, BEN, CHLOE, DEV, ESHA, FARHAN, GITA, HARI};

    private static final int NONE = 0, REQUEST_A_TO_B = 1, REQUEST_B_TO_A = 2, FRIENDS = 3;

    // user a, user b, friendship, a follows b, b follows a. All 16 states appear.
    private static final int[][] RELATIONSHIPS = {
        {ASHA, BEN, FRIENDS, 0, 0},
        {ASHA, CHLOE, FRIENDS, 1, 1},
        {ASHA, DEV, NONE, 0, 1},
        {ASHA, ESHA, NONE, 1, 0},
        {ASHA, FARHAN, NONE, 0, 0},
        {ASHA, GITA, NONE, 0, 0},
        {ASHA, HARI, REQUEST_B_TO_A, 0, 0},
        {BEN, CHLOE, NONE, 1, 1},
        {BEN, HARI, REQUEST_A_TO_B, 0, 0},
        {CHLOE, HARI, REQUEST_A_TO_B, 1, 0},
        {DEV, HARI, REQUEST_A_TO_B, 0, 1},
        {ESHA, HARI, REQUEST_A_TO_B, 1, 1},
        {CHLOE, GITA, REQUEST_B_TO_A, 1, 0},
        {CHLOE, FARHAN, REQUEST_B_TO_A, 0, 1},
        {CHLOE, ESHA, REQUEST_B_TO_A, 1, 1},
        {DEV, ESHA, FRIENDS, 1, 0},
        {DEV, FARHAN, FRIENDS, 0, 1},
    };

    // Who may see Asha's events by visibility, relative to Asha. Written out from the agreed table.
    private static final Map<String, int[]> AUDIENCE = new LinkedHashMap<>();
    static {
        AUDIENCE.put("Public", new int[] {ASHA, BEN, CHLOE, DEV, ESHA, FARHAN, GITA, HARI});
        AUDIENCE.put("Followers", new int[] {ASHA, BEN, CHLOE, DEV});
        AUDIENCE.put("Friends", new int[] {ASHA, BEN, CHLOE});
        AUDIENCE.put("Private", new int[] {ASHA});
    }
    private static final Map<String, String> VISIBILITY_KEY = new HashMap<>();
    static {
        VISIBILITY_KEY.put("Public", "p");
        VISIBILITY_KEY.put("Followers", "f");
        VISIBILITY_KEY.put("Friends", "i");
        VISIBILITY_KEY.put("Private", "v");
    }
    private static final String[] KINDS = {"Basic", "Composite", "Music", "News", "Outing", "Urgent", "Business", "Stacked"};

    private final Map<Integer, List<String>> expectedNotes = new HashMap<>();
    private final Map<String, Integer> ids = new LinkedHashMap<>();
    private final Map<String, String> dates = new HashMap<>();
    private final Map<String, Set<Integer>> visibleUpcoming = new LinkedHashMap<>();
    private final Map<String, Set<Integer>> visibleAll = new LinkedHashMap<>();
    private int nextId = 1;
    private int sessionCount = 0;
    private int passed = 0;
    private int failed = 0;
    private int phasePassed;
    private int phaseFailed;
    private String phaseName;
    private final boolean color = System.console() != null;
    private String config;

    public static void main(String[] args) throws Exception {
        ScenarioRunner runner = new ScenarioRunner();
        int result = runner.run();
        if (Arrays.asList(args).contains("--explore")) {
            runner.explore();
        }
        System.exit(result);
    }

    private int run() throws Exception {
        if (color) {
            try {
                new ProcessBuilder("cmd", "/c", "").inheritIO().start().waitFor();
            } catch (IOException e) {
                // Colours may show as raw codes in very old consoles
            }
        }
        for (int user : EVERYONE) {
            expectedNotes.put(user, new ArrayList<>());
        }
        print(paint("1;38;5;201", "=== End-to-end scenario: real app, real keystrokes, sandboxed data ==="));
        String before = checksumOfRealData();
        prepareSandbox();

        phase("1. Cast registers through the login screen");
        registerCast();
        endPhase();

        phase("2. Every relationship state is set up and shows on both sides");
        setUpRelationships();
        checkRelationships();
        endPhase();

        phase("3. Every kind of event at every visibility is created");
        createEvents();
        endPhase();

        phase("4. Each person sees exactly the right events");
        checkWhoSeesWhat("before any invites");
        printMatrix();
        endPhase();

        phase("5. Events are displayed correctly");
        checkDisplay();
        endPhase();

        phase("6. Invites and RSVP");
        checkInvitesAndRsvp();
        endPhase();

        phase("7. Permissions");
        checkPermissions();
        endPhase();

        phase("8. Access follows live changes");
        checkLiveChanges();
        endPhase();

        phase("9. Cancelling and editing notify the right people");
        checkCancelAndEdit();
        endPhase();

        phase("10. Notifications, reports and search");
        checkNotifications();
        checkReports();
        checkSearch();
        endPhase();

        phase("11. The screen looks right with and without colour");
        checkLook();
        endPhase();

        phase("12. Your real data was left alone");
        check("Files folder is unchanged", before.equals(checksumOfRealData()), "A file under Project Folder/Files changed during the run");
        endPhase();

        print("");
        String summary = passed + " checks passed, " + failed + " failed, across " + sessionCount + " app sessions";
        print(paint(failed == 0 ? "1;38;5;48" : "1;38;5;197", summary));
        print("Transcripts of every session: " + TRANSCRIPTS.toAbsolutePath());
        return failed == 0 ? 0 : 1;
    }

    /* ---------- Phase 1 ---------- */

    private void registerCast() throws Exception {
        List<String> keys = new ArrayList<>();
        for (int user : EVERYONE) {
            keys.addAll(Arrays.asList(String.valueOf(user), "y", String.valueOf(user), password(user),
                    NAMES[user].toLowerCase() + "@ev.test", NAMES[user], "Tester"));
        }
        keys.add("0");
        String out = app("register-all", config, keys);
        for (int user : EVERYONE) {
            check(NAMES[user] + " registered", out.contains("User Registration Successful"), out);
        }
        check("All 8 are stored", countLines(SANDBOX.resolve("users.txt")) == 8, "users.txt has " + countLines(SANDBOX.resolve("users.txt")) + " lines");
        check("Wrong password is refused", session(ASHA, "wrong-password-check", false, "nope", password(ASHA), "0").contains("Invalid login details"), "");
    }

    /* ---------- Phase 2 ---------- */

    private void setUpRelationships() throws Exception {
        Set<String> states = new TreeSet<>();
        for (int[] r : RELATIONSHIPS) {
            states.add(r[2] + "/" + r[3] + "/" + r[4]);
        }
        check("The table covers all 16 relationship states", states.size() == 16, "Only " + states.size() + " states in the table");

        for (int[] r : RELATIONSHIPS) {
            if (r[2] == FRIENDS) {
                sendRequest(r[0], r[1]);
                String out = login(r[1], "accept-" + NAMES[r[0]] + "-" + NAMES[r[1]], "2", "y", "r");
                check(NAMES[r[1]] + " accepts " + NAMES[r[0]], out.contains("Invite accepted."), out);
                note(r[0], NAMES[r[1]] + " (" + r[1] + ") accepted your friend request.");
            }
        }
        for (int[] r : RELATIONSHIPS) {
            if (r[2] == REQUEST_A_TO_B) {
                sendRequest(r[0], r[1]);
            } else if (r[2] == REQUEST_B_TO_A) {
                sendRequest(r[1], r[0]);
            }
        }
        for (int[] r : RELATIONSHIPS) {
            if (r[3] == 1) {
                follow(r[0], r[1]);
            }
            if (r[4] == 1) {
                follow(r[1], r[0]);
            }
        }
    }

    private void sendRequest(int from, int to) throws Exception {
        String out = login(from, "request-" + NAMES[from] + "-" + NAMES[to], "5", "a", String.valueOf(to), "r");
        check(NAMES[from] + " sends " + NAMES[to] + " a friend request", out.contains("Friend request sent."), out);
        note(to, "New friend request from " + NAMES[from] + " (" + from + ").");
    }

    private void follow(int from, int to) throws Exception {
        String out = login(from, "follow-" + NAMES[from] + "-" + NAMES[to], "5", "f", String.valueOf(to), "r");
        check(NAMES[from] + " follows " + NAMES[to], out.contains("Now following."), out);
        note(to, NAMES[from] + " started following you.");
    }

    private void checkRelationships() throws Exception {
        for (int user : EVERYONE) {
            Set<String> expected = new TreeSet<>();
            int pendingRequests = 0;
            for (int[] r : RELATIONSHIPS) {
                if (r[0] != user && r[1] != user) {
                    continue;
                }
                boolean isA = r[0] == user;
                String other = NAMES[isA ? r[1] : r[0]];
                if (r[2] == FRIENDS) {
                    expected.add("FRIEND:" + other);
                }
                if ((isA && r[3] == 1) || (!isA && r[4] == 1)) {
                    expected.add("FOLLOWING:" + other);
                }
                if ((isA && r[4] == 1) || (!isA && r[3] == 1)) {
                    expected.add("FOLLOWER:" + other);
                }
                if ((isA && r[2] == REQUEST_B_TO_A) || (!isA && r[2] == REQUEST_A_TO_B)) {
                    pendingRequests++;
                }
            }
            String out = login(user, "friends-" + NAMES[user], "5", "r", "2", "r");
            String friendsPage = between(out, "FRIENDS", "(s) search");
            Set<String> actual = new TreeSet<>();
            Matcher matcher = Pattern.compile("\\[(FRIEND|FOLLOWER|FOLLOWING)\\]\\s+(\\S+)\\s+\\d+").matcher(friendsPage);
            while (matcher.find()) {
                actual.add(matcher.group(1) + ":" + matcher.group(2));
            }
            check(NAMES[user] + "'s connections", expected.equals(actual), "expected " + expected + " but saw " + actual);
            int shown = countOccurrences(between(out, "INVITES", "Answer the first invite"), "Friend request from");
            check(NAMES[user] + " has " + pendingRequests + " pending friend request(s)", shown == pendingRequests, "saw " + shown);
        }
    }

    /* ---------- Phase 3 ---------- */

    private void createEvents() throws Exception {
        // Past events cannot be typed in, so they are placed directly. Past-Public uses the old 12 column format.
        List<String> seeded = new ArrayList<>();
        seeded.add("1,1,0,ACTIVE,BASIC,Past-Public,Desc,2000-01-01,10:00,Hall,Party,");
        seeded.add("2,1,0,ACTIVE,BASIC,Past-Friends,Desc,2000-01-02,10:00,Hall,Party,,FRIENDS");
        Files.write(SANDBOX.resolve("events.txt"), seeded);
        // An expired friend request that must never be shown to Gita
        Files.write(SANDBOX.resolve("invite.txt"), Collections.singletonList("1,7,2000-01-01T10:00:00,0"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        ids.put("Past-Public", 1);
        ids.put("Past-Friends", 2);
        dates.put("Past-Public", "2000-01-01");
        dates.put("Past-Friends", "2000-01-02");
        allOnly("Past-Public", AUDIENCE.get("Public"));
        allOnly("Past-Friends", AUDIENCE.get("Friends"));
        nextId = 3;

        List<String> keys = new ArrayList<>();
        keys.add("1");
        // Created latest date first, so a correct listing has to sort them
        int offset = KINDS.length * AUDIENCE.size() + 10;
        for (String kind : KINDS) {
            for (String visibility : AUDIENCE.keySet()) {
                String name = kind + "-" + visibility;
                addTop(keys, name, kind.equals("Composite"), visibility, offset--);
                visible(name, AUDIENCE.get(visibility));
                decorationKeys(keys, kind, ids.get(name));
                if (kind.equals("Composite")) {
                    addSub(keys, ids.get(name), name + "-Day", true);
                    addSub(keys, ids.get(name + "-Day"), name + "-Talk", false);
                }
            }
        }
        // Lifecycle events, all public
        addTop(keys, "Cancelled-Public", false, "Public", offset--);
        allOnly("Cancelled-Public", AUDIENCE.get("Public"));
        keys.addAll(Arrays.asList("x", String.valueOf(ids.get("Cancelled-Public"))));
        addTop(keys, "Partial-Public", true, "Public", offset--);
        visible("Partial-Public", AUDIENCE.get("Public"));
        addSub(keys, ids.get("Partial-Public"), "Partial-Kept", false);
        addSub(keys, ids.get("Partial-Public"), "Partial-Dropped", false);
        keys.addAll(Arrays.asList("x", String.valueOf(ids.get("Partial-Dropped"))));
        addTop(keys, "Edited-Public", false, "Public", offset--);
        visible("Edited-Public", AUDIENCE.get("Public"));
        keys.addAll(Arrays.asList("e", String.valueOf(ids.get("Edited-Public")), "l", "Rooftop"));
        keys.add("r");
        String out = login(ASHA, "asha-creates-events", keys.toArray(new String[0]));
        check("All of Asha's events were created", countOccurrences(out, "Created event #") == KINDS.length * AUDIENCE.size() + 3,
                "saw " + countOccurrences(out, "Created event #"));
        check("All sub-events were created", countOccurrences(out, "Created sub-event #") == AUDIENCE.size() * 2 + 2, "saw " + countOccurrences(out, "Created sub-event #"));

        // Events from other people, where the same viewers stand differently
        hostEvent(BEN, "Ben-Friends", "Friends", new int[] {BEN, ASHA});
        hostEvent(BEN, "Ben-Private", "Private", new int[] {BEN});
        hostEvent(DEV, "Dev-Friends", "Friends", new int[] {DEV, ESHA, FARHAN});
        hostEvent(DEV, "Dev-Followers", "Followers", new int[] {DEV, ESHA, FARHAN, HARI});
    }

    private void hostEvent(int host, String name, String visibility, int[] audience) throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("1");
        addTop(keys, name, false, visibility, 500 + nextId);
        visible(name, audience);
        keys.add("r");
        String out = login(host, "host-" + name, keys.toArray(new String[0]));
        check(NAMES[host] + " creates " + name, out.contains("Created event #" + ids.get(name)), out);
    }

    private void addTop(List<String> keys, String name, boolean composite, String visibility, int dayOffset) {
        String date = BASE_DATE.plusDays(dayOffset).toString();
        keys.addAll(Arrays.asList("c", composite ? "y" : "n", name, "Desc", date, "10:00", "Hall", "Party", VISIBILITY_KEY.get(visibility)));
        ids.put(name, nextId++);
        dates.put(name, date);
    }

    private void addSub(List<String> keys, int parent, String name, boolean composite) {
        keys.addAll(Arrays.asList("s", String.valueOf(parent), composite ? "y" : "n", name, "Desc", BASE_DATE.toString(), "11:00", "Room", "Talk"));
        ids.put(name, nextId++);
        dates.put(name, BASE_DATE.toString());
    }

    private void decorationKeys(List<String> keys, String kind, int id) {
        String key = String.valueOf(id);
        boolean all = kind.equals("Stacked");
        if (kind.equals("Music") || all) {
            keys.addAll(Arrays.asList("d", key, "m", "Queen"));
        }
        if (kind.equals("News") || all) {
            keys.addAll(Arrays.asList("d", key, "n", "Big", "Very big"));
        }
        if (kind.equals("Outing") || all) {
            keys.addAll(Arrays.asList("d", key, "o", "4", "Taj"));
        }
        if (kind.equals("Urgent") || all) {
            keys.addAll(Arrays.asList("d", key, "u", "3"));
        }
        if (kind.equals("Business") || all) {
            keys.addAll(Arrays.asList("d", key, "b", "Sales"));
        }
    }

    private void visible(String name, int[] audience) {
        Set<Integer> who = new TreeSet<>();
        for (int user : audience) {
            who.add(user);
        }
        visibleUpcoming.put(name, who);
        visibleAll.put(name, new TreeSet<>(who));
    }

    // Past and cancelled events appear under All but not Upcoming
    private void allOnly(String name, int[] audience) {
        visible(name, audience);
        visibleUpcoming.put(name, new TreeSet<>());
    }

    /* ---------- Phase 4 ---------- */

    private void checkWhoSeesWhat(String when) throws Exception {
        for (int user : EVERYONE) {
            String out = login(user, "lists-" + NAMES[user] + "-" + when.replace(' ', '-'), "1", "a", "r");
            checkList(user, "Upcoming", between(out, "Upcoming events (", "(c) create"), visibleUpcoming, when);
            checkList(user, "All", between(out, "All events (", "(c) create"), visibleAll, when);
        }
    }

    private void checkList(int user, String label, String segment, Map<String, Set<Integer>> table, String when) {
        Set<String> expected = new TreeSet<>();
        for (Map.Entry<String, Set<Integer>> entry : table.entrySet()) {
            if (entry.getValue().contains(user)) {
                expected.add(entry.getKey());
            }
        }
        Set<String> actual = new TreeSet<>(topLevelNames(segment));
        String difference = "missing " + difference(expected, actual) + ", should not see " + difference(actual, expected);
        check(NAMES[user] + " " + label + " list (" + when + ")", expected.equals(actual), difference);
        Matcher count = Pattern.compile("^(\\d+)\\)").matcher(segment);
        check(NAMES[user] + " " + label + " count", count.find() && Integer.parseInt(count.group(1)) == expected.size(), "heading does not say " + expected.size());
    }

    private void printMatrix() {
        StringBuilder header = new StringBuilder(String.format("    %-24s", "Event"));
        for (int user : EVERYONE) {
            header.append(" ").append(NAMES[user].charAt(0));
        }
        print(paint("38;5;245", header.toString()));
        for (Map.Entry<String, Set<Integer>> entry : visibleAll.entrySet()) {
            StringBuilder row = new StringBuilder(String.format("    %-24s", entry.getKey()));
            for (int user : EVERYONE) {
                row.append(" ").append(entry.getValue().contains(user) ? paint("38;5;48", "Y") : paint("38;5;245", "-"));
            }
            print(row.toString());
        }
    }

    private static Set<String> difference(Set<String> a, Set<String> b) {
        Set<String> result = new TreeSet<>(a);
        result.removeAll(b);
        return result;
    }

    /* ---------- Phase 5 ---------- */

    private void checkDisplay() throws Exception {
        String out = login(ASHA, "display", "1", "a", "r");
        String upcoming = between(out, "Upcoming events (", "(c) create");
        String all = between(out, "All events (", "(c) create");

        // Soonest first
        List<String> order = topLevelNames(upcoming);
        List<String> sorted = new ArrayList<>(order);
        sorted.sort(Comparator.comparing(dates::get));
        check("Upcoming is sorted soonest first", order.equals(sorted), "order was " + order);

        String[][] tags = {
            {"Music", "[Band: Queen]"},
            {"News", "[News: Big - Very big]"},
            {"Outing", "[Outing: Taj - 4 star]"},
            {"Urgent", "[Urgency: 3/5]"},
            {"Business", "[Business: Sales]"},
        };
        for (String visibility : AUDIENCE.keySet()) {
            for (String[] tag : tags) {
                String kind = tag[0];
                String card = card(upcoming, kind + "-" + visibility);
                check(kind + "-" + visibility + " shows its decoration", card.contains(tag[1]), card);
                for (String[] other : tags) {
                    if (!other[0].equals(kind)) {
                        check(kind + "-" + visibility + " has no " + other[0] + " decoration", !card.contains(other[1]), card);
                    }
                }
            }
            String stacked = card(upcoming, "Stacked-" + visibility);
            for (String[] tag : tags) {
                check("Stacked-" + visibility + " shows " + tag[0], stacked.contains(tag[1]), stacked);
            }
            String basic = card(upcoming, "Basic-" + visibility);
            check("Basic-" + visibility + " has no decorations", !basic.contains("[Band") && !basic.contains("[News") && !basic.contains("[Outing") && !basic.contains("[Urgency") && !basic.contains("[Business"), basic);
            check("Basic-" + visibility + " shows its visibility", basic.split("\\R")[0].contains(visibility), basic);
            check("Basic-" + visibility + " shows its date, time and place", basic.contains(dates.get("Basic-" + visibility) + " 10:00 @ Hall"), basic);
            check("Basic-" + visibility + " shows a countdown", basic.split("\\R")[0].contains("days"), basic);

            String composite = card(upcoming, "Composite-" + visibility);
            int day = ids.get("Composite-" + visibility + "-Day");
            int talk = ids.get("Composite-" + visibility + "-Talk");
            check("Composite-" + visibility + " shows its sub-event", composite.contains("`- #" + day + " Composite-" + visibility + "-Day"), composite);
            check("Composite-" + visibility + " nests the deeper sub-event", composite.contains("   `- #" + talk + " Composite-" + visibility + "-Talk"), composite);
            check("Sub-events are not listed on their own", !topLevelNames(upcoming).contains("Composite-" + visibility + "-Day"), "");
        }

        check("Cancelled event is not upcoming", !topLevelNames(upcoming).contains("Cancelled-Public"), "");
        String cancelled = card(all, "Cancelled-Public");
        check("Cancelled event is marked under All", cancelled.split("\\R")[0].contains("CANCELLED"), cancelled);
        String partial = card(upcoming, "Partial-Public");
        check("A cancelled sub-event is marked", partial.contains("#" + ids.get("Partial-Dropped") + " Partial-Dropped [CANCELLED]"), partial);
        check("Its sibling is not marked", !partial.contains("Partial-Kept [CANCELLED]"), partial);
        check("The main event stays active", !partial.split("\\R")[0].contains("CANCELLED"), partial);
        check("Edited event shows the new place", card(upcoming, "Edited-Public").contains("@ Rooftop"), "");
        check("Past event is not upcoming", !topLevelNames(upcoming).contains("Past-Public"), "");
        check("Past event is under All with its age", card(all, "Past-Public").split("\\R")[0].contains("days ago"), card(all, "Past-Public"));
        check("Old format event loads as Public", card(all, "Past-Public").split("\\R")[0].contains("Public"), card(all, "Past-Public"));

        String mine = between(login(BEN, "ben-mine", "1", "m", "r"), "My events (", "(c) create");
        check("Mine lists only Ben's own events", new TreeSet<>(topLevelNames(mine)).equals(new TreeSet<>(Arrays.asList("Ben-Friends", "Ben-Private"))), mine);

        String guestView = between(login(BEN, "ben-sees-card", "1", "r"), "Upcoming events (", "(c) create");
        check("A viewer sees the same decorations", card(guestView, "Stacked-Friends").contains("[Band: Queen]"), "");
        check("A viewer sees the same nesting", card(guestView, "Composite-Friends").contains("-Talk"), "");
    }

    /* ---------- Phase 6 ---------- */

    private void checkInvitesAndRsvp() throws Exception {
        int event = ids.get("Basic-Private");
        String key = String.valueOf(event);
        String out = login(ASHA, "asha-invites", "1", "i", key, str(FARHAN), "i", key, str(GITA), "i", key, str(ESHA), "i", key, str(DEV),
                "i", key, str(DEV), "i", key, str(ASHA), "i", key, "99",
                "i", String.valueOf(ids.get("Composite-Private-Day")), "v", key, "r");
        check("Four invites were sent", countOccurrences(out, "Invite sent.") == 4, out);
        check("Inviting twice is refused", out.contains("Dev (4) has already been invited."), out);
        check("Inviting yourself is refused", out.contains("does not need an invite"), out);
        check("Inviting a stranger id is refused", out.contains("No user with id 99."), out);
        check("Inviting to a sub-event is refused", out.contains("Invite people to the main event"), out);
        check("Four are waiting", out.contains("Accepted: 0, Declined: 0, Maybe: 0, Pending: 4"), out);
        for (int guest : new int[] {FARHAN, GITA, ESHA, DEV}) {
            note(guest, "Asha (1) invited you to Basic-Private.");
            visibleUpcoming.get("Basic-Private").add(guest);
            visibleAll.get("Basic-Private").add(guest);
        }
        checkWhoSeesWhat("while invites are pending");

        String gitaInvites = between(login(GITA, "gita-invites", "2", "r"), "INVITES", "Answer the first invite");
        check("Gita's expired request is hidden", !gitaInvites.contains("Friend request"), gitaInvites);
        check("Gita sees the event invite", gitaInvites.contains("Event invite from Asha (1): #" + event + " Basic-Private"), gitaInvites);

        answer(FARHAN, "y", "Invite accepted.");
        note(ASHA, "Farhan (6) responded ACCEPTED to Basic-Private.");
        answer(GITA, "n", "Invite declined.");
        note(ASHA, "Gita (7) responded DECLINED to Basic-Private.");
        visibleUpcoming.get("Basic-Private").remove(GITA);
        visibleAll.get("Basic-Private").remove(GITA);
        answer(ESHA, "m", "Marked as maybe.");
        note(ASHA, "Esha (5) responded MAYBE to Basic-Private.");
        answer(DEV, "m", "Marked as maybe.");
        note(ASHA, "Dev (4) responded MAYBE to Basic-Private.");
        String devAgain = login(DEV, "dev-answers-again", "2", "y", "r");
        check("Dev's maybe stays open and can become yes", devAgain.contains("(you said maybe)") && devAgain.contains("Invite accepted."), devAgain);
        note(ASHA, "Dev (4) responded ACCEPTED to Basic-Private.");

        checkWhoSeesWhat("after everyone answered");

        out = login(ASHA, "asha-responses", "1", "v", key, "r");
        check("Counts are right", out.contains("Accepted: 2, Declined: 1, Maybe: 1, Pending: 0"), out);
        for (String line : new String[] {"Farhan (6): ACCEPTED", "Gita (7): DECLINED", "Esha (5): MAYBE", "Dev (4): ACCEPTED"}) {
            check("Creator sees " + line, out.contains(line), out);
        }
        check("Asha's card shows the numbers", card(between(out, "Upcoming events (", "(c) create"), "Basic-Private").contains("2 going - 1 maybe - 0 waiting - 1 declined"), out);
        out = login(FARHAN, "farhan-responses", "1", "v", key, "r");
        check("A guest sees the counts", out.contains("Accepted: 2, Declined: 1, Maybe: 1, Pending: 0"), out);
        check("A guest does not see who answered", !out.contains("Gita (7): DECLINED"), out);
        check("A guest's card has no response bar", !card(between(out, "Upcoming events (", "(c) create"), "Basic-Private").contains("going"), out);

        int friendsOnly = ids.get("News-Friends");
        out = login(ASHA, "asha-invites-friends", "1", "i", String.valueOf(friendsOnly), "0", "i", String.valueOf(friendsOnly), "0", "r");
        check("Inviting all friends reaches both", out.contains("Sent 2 invite(s)."), out);
        check("Inviting all friends again finds nobody new", out.contains("Sent 0 invite(s)."), out);
        note(BEN, "Asha (1) invited you to News-Friends.");
        note(CHLOE, "Asha (1) invited you to News-Friends.");
        answer(BEN, "y", "Invite accepted.");
        note(ASHA, "Ben (2) responded ACCEPTED to News-Friends.");
    }

    private void answer(int user, String key, String message) throws Exception {
        String out = login(user, "answer-" + NAMES[user] + "-" + key, "2", key, "r");
        check(NAMES[user] + " answers " + key, out.contains(message), out);
    }

    /* ---------- Phase 7 ---------- */

    private void checkPermissions() throws Exception {
        byte[] before = Files.readAllBytes(SANDBOX.resolve("events.txt"));
        String open = String.valueOf(ids.get("Composite-Public"));
        String out = login(BEN, "ben-not-owner", "1", "e", open, "d", open, "x", open, "s", open, "i", open, "r");
        check("Non-owner told four times they cannot change it", countOccurrences(out, "Only the creator of an event can change it.") == 4, out);
        check("Non-owner cannot invite", out.contains("You can only invite people to your own events."), out);

        String hidden = String.valueOf(ids.get("Stacked-Private"));
        out = login(GITA, "gita-hidden", "1", "e", hidden, "d", hidden, "x", hidden, "s", hidden, "i", hidden, "v", hidden, "r");
        check("A hidden event looks like it does not exist", countOccurrences(out, "No event with id " + hidden + ".") == 6, out);
        check("Nothing about it leaks", !out.contains("Stacked-Private"), out);

        String sub = String.valueOf(ids.get("Composite-Private-Talk"));
        out = login(HARI, "hari-hidden-sub", "1", "v", sub, "r");
        check("Sub-events of hidden events are hidden too", out.contains("No event with id " + sub + "."), out);

        check("The events file did not change", Arrays.equals(before, Files.readAllBytes(SANDBOX.resolve("events.txt"))), "events.txt was modified");
    }

    /* ---------- Phase 8 ---------- */

    private void checkLiveChanges() throws Exception {
        // Ben stops being Asha's friend. His accepted invite still counts for News-Friends.
        login(BEN, "ben-unfriends", "5", "d", str(ASHA), "r");
        Set<String> ben = new TreeSet<>(topLevelNames(between(login(BEN, "ben-after-unfriend", "1", "r"), "Upcoming events (", "(c) create")));
        check("Unfriending removes Friends events", !ben.contains("Basic-Friends") && !ben.contains("Music-Friends"), ben.toString());
        check("Unfriending removes Followers events", !ben.contains("Basic-Followers"), ben.toString());
        check("An accepted invite still counts", ben.contains("News-Friends"), ben.toString());
        check("Public events remain", ben.contains("Basic-Public"), ben.toString());

        login(ASHA, "asha-refriends", "5", "a", str(BEN), "r");
        note(BEN, "New friend request from Asha (1).");
        login(BEN, "ben-accepts-again", "2", "y", "r");
        note(ASHA, "Ben (2) accepted your friend request.");
        ben = new TreeSet<>(topLevelNames(between(login(BEN, "ben-after-refriend", "1", "r"), "Upcoming events (", "(c) create")));
        check("Becoming friends again restores access", ben.contains("Basic-Friends") && ben.contains("Basic-Followers"), ben.toString());

        // Dev stops following Asha
        login(DEV, "dev-unfollows", "5", "u", str(ASHA), "r");
        Set<String> dev = new TreeSet<>(topLevelNames(between(login(DEV, "dev-after-unfollow", "1", "r"), "Upcoming events (", "(c) create")));
        check("Unfollowing removes Followers events", !dev.contains("Basic-Followers"), dev.toString());
        login(DEV, "dev-refollows", "5", "f", str(ASHA), "r");
        note(ASHA, "Dev started following you.");
        dev = new TreeSet<>(topLevelNames(between(login(DEV, "dev-after-refollow", "1", "r"), "Upcoming events (", "(c) create")));
        check("Following again restores access", dev.contains("Basic-Followers"), dev.toString());

        // Asha tightens and relaxes a visibility setting
        String id = String.valueOf(ids.get("Basic-Friends"));
        login(ASHA, "asha-tightens", "1", "e", id, "v", "v", "r");
        check("Tightening visibility takes effect at once", !topLevelNames(between(login(CHLOE, "chloe-after-tighten", "1", "r"), "Upcoming events (", "(c) create")).contains("Basic-Friends"), "");
        login(ASHA, "asha-relaxes", "1", "e", id, "v", "i", "r");
        check("Relaxing visibility takes effect at once", topLevelNames(between(login(CHLOE, "chloe-after-relax", "1", "r"), "Upcoming events (", "(c) create")).contains("Basic-Friends"), "");

        checkWhoSeesWhat("after live changes were undone");
    }

    /* ---------- Phase 9 ---------- */

    private void checkCancelAndEdit() throws Exception {
        // Editing notifies everyone invited, except the one who declined
        int event = ids.get("Basic-Private");
        login(ASHA, "asha-edits-private", "1", "e", str(event), "l", "Garden", "r");
        String updated = "Event updated: #" + event + " Basic-Private (Party) - " + dates.get("Basic-Private") + " 10:00 @ Garden - Desc";
        note(FARHAN, updated);
        note(ESHA, updated);
        note(DEV, updated);

        // Editing a sub-event notifies those invited to its main event
        int partial = ids.get("Partial-Public");
        login(ASHA, "asha-invites-partial", "1", "i", str(partial), str(FARHAN), "r");
        note(FARHAN, "Asha (1) invited you to Partial-Public.");
        answer(FARHAN, "y", "Invite accepted.");
        note(ASHA, "Farhan (6) responded ACCEPTED to Partial-Public.");
        int kept = ids.get("Partial-Kept");
        login(ASHA, "asha-edits-sub", "1", "e", str(kept), "l", "Attic", "r");
        note(FARHAN, "Event updated: #" + kept + " Partial-Kept (Talk) - " + dates.get("Partial-Kept") + " 11:00 @ Attic - Desc");

        // Cancelling
        List<String> keys = new ArrayList<>(Arrays.asList("1"));
        int offset = 900;
        addTop(keys, "Temp-Public", false, "Public", offset);
        visible("Temp-Public", AUDIENCE.get("Public"));
        int temp = ids.get("Temp-Public");
        keys.addAll(Arrays.asList("i", str(temp), str(BEN), "i", str(temp), str(GITA), "r"));
        login(ASHA, "asha-temp", keys.toArray(new String[0]));
        note(BEN, "Asha (1) invited you to Temp-Public.");
        note(GITA, "Asha (1) invited you to Temp-Public.");
        answer(GITA, "n", "Invite declined.");
        note(ASHA, "Gita (7) responded DECLINED to Temp-Public.");
        login(ASHA, "asha-cancels-temp", "1", "x", str(temp), "r");
        note(BEN, "Event cancelled: Temp-Public (#" + temp + ")");
        allOnly("Temp-Public", AUDIENCE.get("Public"));
        String benInvites = between(login(BEN, "ben-after-cancel", "2", "r"), "INVITES", "Answer the first invite");
        check("Cancelling withdraws the pending invite", benInvites.contains("(no pending invites)"), benInvites);

        // Cancelling a sub-event only
        login(ASHA, "asha-cancels-sub", "1", "x", str(kept), "r");
        note(FARHAN, "Event cancelled: Partial-Kept (#" + kept + ")");

        // Cancelling a whole composite takes every sub-event with it
        int composite = ids.get("Composite-Followers");
        String out = login(ASHA, "asha-cancels-composite", "1", "x", str(composite), "a", "r");
        String card = card(between(out, "All events (", "(c) create"), "Composite-Followers");
        check("Cancelling a composite cancels the sub-event", card.contains("Composite-Followers-Day [CANCELLED]"), card);
        check("Cancelling a composite cancels the deeper sub-event", card.contains("Composite-Followers-Talk [CANCELLED]"), card);
        visibleUpcoming.get("Composite-Followers").clear();
        checkWhoSeesWhat("after cancelling");
    }

    /* ---------- Phase 10 ---------- */

    private void checkNotifications() throws Exception {
        for (int user : EVERYONE) {
            String page = between(login(user, "notifications-" + NAMES[user], "3", "r"), "NOTIFICATIONS", "(c) clear");
            List<String> actual = new ArrayList<>();
            Matcher matcher = Pattern.compile("(?m)^   - (.*?)\\s*$").matcher(page);
            while (matcher.find()) {
                actual.add(matcher.group(1));
            }
            List<String> expected = new ArrayList<>(expectedNotes.get(user));
            Collections.sort(expected);
            Collections.sort(actual);
            List<String> missing = new ArrayList<>(expected);
            actual.forEach(missing::remove);
            List<String> extra = new ArrayList<>(actual);
            expected.forEach(extra::remove);
            check(NAMES[user] + " has exactly the right " + expected.size() + " notifications", missing.isEmpty() && extra.isEmpty(), "missing " + missing + ", unexpected " + extra);
        }
    }

    private void checkReports() throws Exception {
        // user: events created, pending invites
        int[][] numbers = {
            {ASHA, 38, 1},
            {BEN, 2, 0},
            {CHLOE, 0, 4},
            {DEV, 2, 0},
            {ESHA, 0, 1},
            {FARHAN, 0, 0},
            {GITA, 0, 0},
            {HARI, 0, 4},
        };
        for (int[] row : numbers) {
            int user = row[0];
            int friends = 0, followers = 0, following = 0;
            for (int[] r : RELATIONSHIPS) {
                if (r[0] != user && r[1] != user) {
                    continue;
                }
                boolean isA = r[0] == user;
                friends += r[2] == FRIENDS ? 1 : 0;
                following += isA ? r[3] : r[4];
                followers += isA ? r[4] : r[3];
            }
            String report = login(user, "report-" + NAMES[user], "4", "");
            checkStat(user, report, "Events created", row[1]);
            checkStat(user, report, "Pending invites", row[2]);
            checkStat(user, report, "Notifications", expectedNotes.get(user).size());
            checkStat(user, report, "Friends", friends);
            checkStat(user, report, "Followers", followers);
            checkStat(user, report, "Following", following);
        }
        String report = login(ASHA, "report-asha-responses", "4", "");
        check("Report lists responses per event", report.contains("2 going - 1 maybe - 0 waiting - 1 declined"), report);
        check("Report leaves out sub-events", !report.contains("-Talk"), report);
    }

    private void checkStat(int user, String report, String label, int value) {
        check(NAMES[user] + " report: " + label + " = " + value, Pattern.compile(label + "\\s+" + value + "\\s").matcher(report).find(), between(report, "Report for", "Press Enter"));
    }

    private void checkSearch() throws Exception {
        String out = login(GITA, "search", "5", "s", "asha", "s", "EV.TEST", "s", "nobody-here", "r");
        check("Search by name finds one", out.contains("Found 1 user(s):") && out.contains("Asha (1) - asha@ev.test"), out);
        check("Search by email ignores case and finds all", out.contains("Found 8 user(s):"), out);
        check("Search with no match says so", out.contains("No users found."), out);
    }

    /* ---------- Phase 11 ---------- */

    private void checkLook() throws Exception {
        String plain = login(ASHA, "look-plain", "1", "r", "5", "r", "4", "");
        check("Plain mode has no escape codes", !plain.contains("\033"), "");

        String fancyConfig = writeConfig("fancy.properties", "on", "on", "78", "0");
        List<String> keys = Arrays.asList(str(ASHA), password(ASHA), "1", "a", "r", "2", "r", "3", "r", "4", "", "5", "r", "6", "r", "0");
        String fancy = app("look-fancy", fancyConfig, keys);
        check("Colour mode uses escape codes", fancy.contains("\033[38;5;"), "");
        check("Colour mode clears the screen", fancy.contains("\033[2J"), "");
        // A colour left switched on would bleed into everything printed after it
        List<String> unreset = new ArrayList<>();
        for (String line : fancy.split("\\R")) {
            int lastColour = line.lastIndexOf("\033[");
            while (lastColour >= 0 && line.startsWith("\033[2J", lastColour) || lastColour >= 0 && line.startsWith("\033[3J", lastColour) || lastColour >= 0 && line.startsWith("\033[H", lastColour)) {
                lastColour = line.lastIndexOf("\033[", lastColour - 1);
            }
            if (lastColour >= 0 && !line.startsWith("\033[0m", lastColour)) {
                unreset.add(line);
            }
        }
        check("Every colour is switched off again", unreset.isEmpty(), unreset.size() + " lines end with a colour still on");
        check("The logo is shown", fancy.contains("\u2588\u2588\u2588"), "");
        for (String[] symbol : new String[][] {{"\u266B", "music"}, {"\u00A7", "news"}, {"\u263C", "outing"}, {"\u25B2", "urgent"}, {"\u2666", "business"}, {"\u2554", "title frame"}, {"\u250C", "card frame"}, {"\u251C", "tree branch"}, {"\u25BA", "prompt"}}) {
            check("Shows the " + symbol[1] + " symbol", fancy.contains(symbol[0]), "");
        }
        String stripped = fancy.replaceAll("\033\\[[0-9;]*[A-Za-z]", "");
        int boxLines = 0;
        List<String> crooked = new ArrayList<>();
        for (String line : stripped.split("\\R")) {
            if (!line.isEmpty() && "\u2554\u2551\u255A\u250C\u2502\u2514".indexOf(line.charAt(0)) >= 0) {
                boxLines++;
                if (line.length() != 78) {
                    crooked.add(line.length() + ": " + line);
                }
            }
        }
        check("Checked a real number of box lines (" + boxLines + ")", boxLines > 100, "only " + boxLines);
        check("Every box line is exactly 78 wide", crooked.isEmpty(), crooked.toString());
        check("No raw question marks replaced symbols", !stripped.contains("?? "), "");

        for (String theme : new String[] {"amber", "ocean"}) {
            String themed = writeConfig(theme + ".properties", "on", "on", "78", "0");
            Path file = Paths.get(themed);
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).replace("ui.theme=neon", "ui.theme=" + theme);
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
            String out = app("look-" + theme, themed, Arrays.asList(str(ASHA), password(ASHA), "0"));
            check("Theme " + theme + " runs", out.contains("Logged out. Goodbye!") && out.contains("\033[38;5;"), out);
        }
    }

    /* ---------- Running the application ---------- */

    private String str(int value) {
        return String.valueOf(value);
    }

    private static String password(int user) {
        return "secret-" + user;
    }

    private String login(int user, String name, String... keys) throws Exception {
        return session(user, name, true, keys);
    }

    // Logs in with the keys typed from the main menu. With correct=false the keys include the password stage.
    private String session(int user, String name, boolean correct, String... keys) throws Exception {
        List<String> all = new ArrayList<>();
        all.add(str(user));
        if (correct) {
            all.add(password(user));
        }
        all.addAll(Arrays.asList(keys));
        all.add("0");
        return app(name, config, all);
    }

    private String app(String name, String configFile, List<String> keys) throws Exception {
        sessionCount++;
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-D" + "app.config=" + configFile, "-Dstdout.encoding=UTF-8", "-cp", CLASSES, "UserMangerSubSystem.AppStartUp")
                .redirectErrorStream(true).start();
        Thread writer = new Thread(() -> {
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write((String.join("\n", keys) + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                // The app ended before reading everything. The check below reports that.
            }
        });
        writer.start();
        byte[] bytes;
        try (InputStream stdout = process.getInputStream()) {
            bytes = stdout.readAllBytes();
        }
        boolean ended = process.waitFor(60, TimeUnit.SECONDS);
        if (!ended) {
            process.destroyForcibly();
        }
        writer.join();
        String output = new String(bytes, StandardCharsets.UTF_8);
        String file = String.format("%03d-%s.txt", sessionCount, name.replaceAll("[^A-Za-z0-9-]", "_"));
        Files.write(TRANSCRIPTS.resolve(file), output.getBytes(StandardCharsets.UTF_8));
        boolean clean = ended && process.exitValue() == 0 && !output.contains("Input closed") && !output.contains("Exception")
                && output.contains("Goodbye!");
        check("Session " + file + " ran and ended cleanly", clean, tail(output));
        return output;
    }

    private static String tail(String text) {
        return text.length() <= 600 ? text : "..." + text.substring(text.length() - 600);
    }

    private void explore() throws Exception {
        print("");
        print(paint("1;38;5;51", "Opening the app on this scenario's data. Your real data is not used."));
        print("Log in with any id below:");
        for (int user : EVERYONE) {
            print("  id " + user + "  " + String.format("%-8s", NAMES[user]) + " password " + password(user));
        }
        String exploreConfig = writeConfig("explore.properties", "auto", "auto", "78", "0");
        new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(), "-Dapp.config=" + exploreConfig, "-Dstdout.encoding=UTF-8", "-cp", CLASSES, "UserMangerSubSystem.AppStartUp")
                .inheritIO().start().waitFor();
    }

    /* ---------- Reading the screen ---------- */

    // Names of top level cards, in the order shown. Their top border starts at the left edge.
    private static List<String> topLevelNames(String segment) {
        List<String> names = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?m)^\\+ #\\d+ (\\S+) -").matcher(segment);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private String card(String segment, String name) {
        Matcher matcher = Pattern.compile("(?ms)^\\+ #\\d+ " + Pattern.quote(name) + " -.*?^\\+-").matcher(segment);
        return matcher.find() ? matcher.group() : "(no card for " + name + ")";
    }

    private static String between(String text, String start, String end) {
        int from = text.indexOf(start);
        if (from < 0) {
            return "(did not find '" + start + "')";
        }
        from += start.length();
        int to = text.indexOf(end, from);
        return to < 0 ? text.substring(from) : text.substring(from, to);
    }

    private static int countOccurrences(String text, String part) {
        int count = 0;
        int position = text.indexOf(part);
        while (position >= 0) {
            count++;
            position = text.indexOf(part, position + part.length());
        }
        return count;
    }

    private static long countLines(Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.filter(line -> !line.trim().isEmpty()).count();
        }
    }

    private void note(int user, String text) {
        expectedNotes.get(user).add(text);
    }

    /* ---------- Sandbox ---------- */

    private void prepareSandbox() throws Exception {
        if (Files.exists(SANDBOX)) {
            try (Stream<Path> files = Files.walk(SANDBOX)) {
                List<Path> all = new ArrayList<>();
                files.sorted(Comparator.reverseOrder()).forEach(all::add);
                for (Path file : all) {
                    Files.delete(file);
                }
            }
        }
        Files.createDirectories(TRANSCRIPTS);
        config = writeConfig("config.properties", "off", "off", "300", "0");
    }

    private String writeConfig(String name, String colorSetting, String unicodeSetting, String width, String delay) throws IOException {
        String root = SANDBOX.toAbsolutePath().toString().replace('\\', '/');
        List<String> lines = new ArrayList<>(Arrays.asList(
                "app.name=Event Management System", "app.version=1.0", "app.mode=dev",
                "app.delay=" + delay, "app.devStartupDelayMs=" + delay, "app.registerDelayMs=" + delay,
                "login.maxAttempts=3", "event.defaultType=Public", "event.maxCapacity=500", "event.allowCancellation=true",
                "ui.theme=neon", "ui.color=" + colorSetting, "ui.unicode=" + unicodeSetting, "ui.width=" + width,
                "Files.users=" + root + "/users.txt", "Files.events=" + root + "/events.txt", "Files.invites=" + root + "/invite.txt",
                "Files.friends=" + root + "/friends.txt", "Files.follow=" + root + "/follow.txt", "Files.password=" + root + "/password.txt",
                "Files.notification=" + root + "/notification.txt", "Files.rsvp=" + root + "/rsvp.txt",
                "Files.mainpage=Files/UI/mainpage.txt", "Files.eventpage=Files/UI/eventpage.txt", "Files.invitepage=Files/UI/invitepage.txt",
                "Files.notificationpage=Files/UI/notificationpage.txt", "Files.reportpage=Files/UI/reportpage.txt",
                "Files.friendpage=Files/UI/friendpage.txt", "Files.profilepage=Files/UI/profilepage.txt"));
        Path file = SANDBOX.resolve(name);
        Files.write(file, lines);
        return file.toAbsolutePath().toString();
    }

    private static String checksumOfRealData() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(REAL_DATA)) {
            walk.filter(Files::isRegularFile).sorted().forEach(files::add);
        }
        for (Path file : files) {
            digest.update(file.toString().getBytes(StandardCharsets.UTF_8));
            digest.update(Files.readAllBytes(file));
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /* ---------- Reporting ---------- */

    private void phase(String name) {
        phaseName = name;
        phasePassed = 0;
        phaseFailed = 0;
        print("");
        print(paint("1;38;5;51", name));
    }

    private void endPhase() {
        String mark = phaseFailed == 0 ? paint("38;5;48", "PASS") : paint("38;5;197", "FAIL");
        print("  " + mark + "  " + phasePassed + " of " + (phasePassed + phaseFailed) + " checks");
    }

    private void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            phasePassed++;
            return;
        }
        failed++;
        phaseFailed++;
        print("  " + paint("1;38;5;197", "FAILED") + " [" + phaseName + "] " + name);
        if (!detail.isEmpty()) {
            print("         " + detail.replace("\n", "\n         "));
        }
    }

    private String paint(String code, String text) {
        return color ? "\033[" + code + "m" + text + "\033[0m" : text;
    }

    private static void print(String text) {
        System.out.println(text);
    }
}
