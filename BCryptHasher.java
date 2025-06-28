import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Minimal utility to generate BCrypt hash for admin123
 * Run with: java -cp "path/to/spring-security-crypto.jar" BCryptHasher
 */
public class BCryptHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Verification: " + encoder.matches(password, hash));
        
        // Test against your existing hash
        String existingHash = "$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.";
        System.out.println("\nTesting existing hash:");
        System.out.println("Existing Hash: " + existingHash);
        System.out.println("Matches admin123: " + encoder.matches(password, existingHash));
    }
}
