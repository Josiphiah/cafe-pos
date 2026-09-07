package zm.cafe.pos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SKELETON — shared scaffold on main. Seeds a realistic Zambian café menu, two
 * staff logins and a couple of demo customers on first run. Idempotent: does
 * nothing if data already exists. Each slice can extend its own section.
 *
 * <p>Demo logins (password shown):
 * <ul>
 *   <li><b>manager</b> / manager123  (role MANAGER)</li>
 *   <li><b>cashier</b> / cashier123  (role CASHIER)</li>
 * </ul>
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(StaffRepository staffRepo,
                           CategoryRepository categoryRepo,
                           ProductRepository productRepo,
                           CustomerRepository customerRepo,
                           PasswordEncoder encoder) {
        return args -> {
            if (staffRepo.count() == 0) {
                staffRepo.save(new Staff("manager", encoder.encode("manager123"), "Café Manager", Role.MANAGER));
                staffRepo.save(new Staff("cashier", encoder.encode("cashier123"), "Front Till", Role.CASHIER));
            }

            if (productRepo.count() == 0) {
                Category coffee = categoryRepo.save(new Category("Coffee"));
                Category tea = categoryRepo.save(new Category("Tea"));
                Category pastries = categoryRepo.save(new Category("Pastries"));
                Category food = categoryRepo.save(new Category("Food"));
                Category cold = categoryRepo.save(new Category("Cold Drinks"));

                Map<Category, List<Object[]>> menu = Map.of(
                        coffee, List.of(
                                new Object[]{"Espresso", "18.00"},
                                new Object[]{"Americano", "22.00"},
                                new Object[]{"Cappuccino", "28.00"},
                                new Object[]{"Café Latte", "30.00"}),
                        tea, List.of(
                                new Object[]{"Rooibos Tea", "15.00"},
                                new Object[]{"English Breakfast Tea", "15.00"},
                                new Object[]{"Ginger & Lemon Tea", "18.00"}),
                        pastries, List.of(
                                new Object[]{"Butter Croissant", "20.00"},
                                new Object[]{"Chocolate Muffin", "22.00"},
                                new Object[]{"Scone with Jam", "18.00"}),
                        food, List.of(
                                new Object[]{"Chicken Mayo Sandwich", "45.00"},
                                new Object[]{"Beef Burger & Chips", "85.00"},
                                new Object[]{"Vegetable Samosa (2)", "25.00"}),
                        cold, List.of(
                                new Object[]{"Bottled Water 500ml", "10.00"},
                                new Object[]{"Coca-Cola 300ml", "15.00"},
                                new Object[]{"Fresh Orange Juice", "35.00"}));

                menu.forEach((category, items) -> items.forEach(item ->
                        productRepo.save(new Product((String) item[0], category, new BigDecimal((String) item[1])))));
            }

            if (customerRepo.count() == 0) {
                customerRepo.save(new Customer("Chanda Mwale", "0977123456"));
                customerRepo.save(new Customer("Natasha Phiri", "0966987654"));
            }
        };
    }
}
