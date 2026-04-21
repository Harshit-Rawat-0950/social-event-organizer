package UserMangerSubSystem;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PasswordTest {

    private final String TEST_FILE_PATH = "TestPasswordDB.txt";
    private PassWordManager pm;

    // Sets up a fresh PassWordManager and file before each test runs
    @Before
    public void setUp() {
        pm = new PassWordManager(TEST_FILE_PATH);
    }

    // Cleans up the test file after each test finishes
    @After
    public void tearDown() {
        try {
            Path path = Paths.get(TEST_FILE_PATH);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tests the file creation logic in the constructor/createFileIfNotExists
    @Test
    public void testCreateFileIfNotExists() {
        Path path = Paths.get(TEST_FILE_PATH);
        assertTrue("Password file should be created by the constructor", Files.exists(path));
    }

    // Tests the static encryption and decryption methods to ensure they are reversible
    @Test
    public void testEncryptAndDecrypt() {
        try {
            String originalPassword = "SuperSecretPassword123!";
            
            // Test Encryption
            String encryptedPassword = PassWordManager.encrypt(originalPassword);
            assertNotNull("Encrypted string should not be null", encryptedPassword);
            assertNotEquals("Encrypted string should differ from plain text", originalPassword, encryptedPassword);
            
            // Test Decryption
            String decryptedPassword = PassWordManager.decrypt(encryptedPassword);
            assertEquals("Decrypted string should match the original plain text", originalPassword, decryptedPassword);
            
        } catch (Exception e) {
            fail("Encryption/Decryption threw an unexpected exception: " + e.getMessage());
        }
    }

    // Tests setting a new password and retrieving it
    @Test
    public void testSetAndGetPassword() {
        int userId1 = 101;
        int userId2 = 102;
        String pass1 = "MyPass@2024";
        String pass2 = "HelloWorld99";

        pm.setPassword(userId1, pass1);
        pm.setPassword(userId2, pass2);

        // Verify we can fetch the correct decrypted passwords back
        assertEquals("Should retrieve correct password for user 101", pass1, pm.getPassWord(userId1));
        assertEquals("Should retrieve correct password for user 102", pass2, pm.getPassWord(userId2));
        
        // Verify behavior for a non-existent user
        assertNull("Should return null for a non-existent user ID", pm.getPassWord(999));
    }

    // Tests updating an existing password
    @Test
    public void testUpdatePassword() {
        int userId = 201;
        String oldPass = "OldPassword123";
        String newPass = "NewPassword456";

        // Set initial password
        pm.setPassword(userId, oldPass);
        assertEquals(oldPass, pm.getPassWord(userId));

        // Update the password
        pm.updatePassword(userId, newPass);

        /* * Note: This assertion might fail with your current PassWordManager.java implementation 
         * because updatePassword() currently appends the unencrypted string instead of encrypting 
         * it, and getPassWord() returns the first match it finds (the old password). 
         * This test correctly identifies that bug!
         */
        assertEquals("Should retrieve the newly updated password", newPass, pm.getPassWord(userId));
    }
}