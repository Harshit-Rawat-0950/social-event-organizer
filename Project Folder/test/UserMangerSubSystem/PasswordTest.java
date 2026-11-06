package UserMangerSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import TestSupport.TestEnvironment;

public class PasswordTest {

    @Rule
    public TestEnvironment env = new TestEnvironment();

    private PassWordManager pm;

    @Before
    public void setUp() {
        pm = new PassWordManager(env.path("passwords.txt"));
    }

    @Test
    public void constructorCreatesTheFile() {
        assertTrue(Files.exists(Paths.get(env.path("passwords.txt"))));
    }

    @Test
    public void encryptionIsReversibleAndHidesTheText() throws Exception {
        String original = "SuperSecretPassword123!";
        String encrypted = PassWordManager.encrypt(original);
        assertNotEquals(original, encrypted);
        assertEquals(original, PassWordManager.decrypt(encrypted));
    }

    @Test
    public void passwordsAreNotStoredAsPlainText() throws Exception {
        pm.setPassword(1, "visible");
        assertFalse(env.lines("passwords.txt").get(0).contains("visible"));
    }

    @Test
    public void eachUserGetsTheirOwnPassword() {
        pm.setPassword(101, "MyPass@2024");
        pm.setPassword(102, "HelloWorld99");
        assertEquals("MyPass@2024", pm.getPassWord(101));
        assertEquals("HelloWorld99", pm.getPassWord(102));
    }

    @Test
    public void unknownUserHasNoPassword() {
        assertNull(pm.getPassWord(999));
    }

    @Test
    public void updateChangesThePasswordInPlace() throws Exception {
        pm.setPassword(201, "OldPassword123");
        pm.updatePassword(201, "NewPassword456");
        assertEquals("NewPassword456", pm.getPassWord(201));
        assertEquals(1, env.lines("passwords.txt").size());
    }

    @Test
    public void updatingAnUnknownUserAddsNothing() throws Exception {
        pm.updatePassword(201, "x");
        assertEquals(0, env.lines("passwords.txt").size());
    }

    // Setting a password twice must not leave the old one in effect
    @Test
    public void settingTwiceReplacesTheOldOne() throws Exception {
        pm.setPassword(301, "first");
        pm.setPassword(301, "second");
        assertEquals("second", pm.getPassWord(301));
        assertEquals(1, env.lines("passwords.txt").size());
    }

    @Test
    public void passwordsMayContainSpacesAndCommas() {
        pm.setPassword(1, " a, b ");
        assertEquals(" a, b ", pm.getPassWord(1));
    }

    @Test
    public void corruptEntryGivesNullRatherThanCrashing() throws Exception {
        Files.write(Paths.get(env.path("passwords.txt")), "5,not-valid-base64!!\n".getBytes());
        java.io.PrintStream err = System.err;
        System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        try {
            assertNull(pm.getPassWord(5));
        } finally {
            System.setErr(err);
        }
    }
}
