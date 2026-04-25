package UserMangerSubSystem;

public class User {
    private int id;
    private String name;
    private String email;
    private String role;
    private String status;

    public User(int id, String name, String email, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }  
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return id + "," + name + "," + email + "," + role + "," + status;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        return new User(Integer.parseInt(parts[0]),parts[1],parts[2],parts[3],parts[4]);
    }
}