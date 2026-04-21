package UserMangerSubSystem;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
public class PassWordManager {

	private static final String ALGORITHM = "AES";

    // Must be 16 characters for AES-128
    private static final String SECRET_KEY = "MySecretKey12345";
    private final Path passWordfilepath;
    
    public PassWordManager(String filePath) {
        this.passWordfilepath = Paths.get(filePath);
        createFileIfNotExists();
    }
    
    public void createFileIfNotExists() {
        try {
            if (passWordfilepath.getParent() != null) {
                Files.createDirectories(passWordfilepath.getParent());
            }
            if (!Files.exists(passWordfilepath)) {
                Files.createFile(passWordfilepath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static SecretKeySpec getKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    
    protected String getPassWord(int id) {
        try {
            List<String> lines = Files.readAllLines(passWordfilepath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (Integer.parseInt(parts[0]) == id) {
                        // The exception thrown here will be caught below
                        return decrypt(parts[1]); 
                    }
                }
            }
        } catch (Exception e) {
            // Handle the error (e.g., log it)
            System.err.println("Error decrypting password: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Return null or a default value if not found or if an error occurs
    }
    
    protected void setPassword(int id, String s)
    {
    	try {
    	String encp = encrypt(s);
    	 try {
             Files.write(passWordfilepath,(String.valueOf(id) + "," + encp + "\n").getBytes(),StandardOpenOption.APPEND
             );
         } catch (IOException e) {
             e.printStackTrace();
         }
    	}
    	 catch (Exception e) {
             // Handle the error (e.g., log it)
             System.err.println("Error setting password: " + e.getMessage());
             e.printStackTrace();
    }
    

}
    protected void updatePassword(int id, String s) {
        try {
            List<String> lines = Files.readAllLines(passWordfilepath);
            boolean isUpdated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (Integer.parseInt(parts[0]) == id) {
                        // 1. Encrypt the new password
                        String encryptedPassword = encrypt(s);
                        
                        // 2. Replace the old line with the new id and encrypted password
                        lines.set(i, String.valueOf(id) + "," + encryptedPassword);
                        isUpdated = true;
                        
                        break; // Stop searching once we've found and updated the user
                    }
                }
            }

            // 3. If an update occurred, overwrite the file completely with the new lines
            if (isUpdated) {
                Files.write(passWordfilepath, lines);
            }

        } catch (Exception e) {
            // Handle the error (e.g., log it)
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
