package zm.cafe.pos.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Backing object for the "Register customer" form on the Customers screen. */
public class CustomerForm {

    @NotBlank
    @Size(max = 80)
    private String name;

    /** Optional. When given it must be unique — checked in the service. */
    @Size(max = 20)
    @Pattern(regexp = "|[0-9+][0-9 -]*", message = "Use digits, spaces, + or -")
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
