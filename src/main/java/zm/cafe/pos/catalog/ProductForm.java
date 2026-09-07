package zm.cafe.pos.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Backing object for the "add menu item" form on {@code GET/POST /catalog}. */
public class ProductForm {

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotNull(message = "Choose a category")
    private Long categoryId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Use at most 2 decimal places")
    private BigDecimal price;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
