package zm.cafe.pos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import zm.cafe.pos.domain.Role;

public class StaffForm {
    @NotBlank @Size(max = 80) private String fullName;
    @NotBlank @Size(min = 3, max = 40)
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Use letters, numbers, dots, underscores or hyphens")
    private String username;
    @NotBlank @Size(min = 8, max = 72) private String password;
    @NotNull private Role role = Role.CASHIER;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
