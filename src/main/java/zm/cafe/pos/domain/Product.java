package zm.cafe.pos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** A sellable menu item with a price in Zambian Kwacha (K). */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Unit price in Kwacha, VAT-inclusive display handled at receipt time. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Soft-delete flag so historical sale lines keep pointing at a real product. */
    @Column(nullable = false)
    private boolean available = true;

    protected Product() {
    }

    public Product(String name, Category category, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
