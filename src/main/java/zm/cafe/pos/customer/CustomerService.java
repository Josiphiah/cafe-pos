package zm.cafe.pos.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.repo.CustomerRepository;

import java.util.Optional;

/**
 * Customer lookup and quick registration.
 *
 * <p>Used by the till for the "name on the cup" flow: enter a phone number, and
 * if it is already on file the existing customer is reused, otherwise a name is
 * requested and a new customer is created on the spot.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return customerRepository.findByPhone(phone.trim());
    }

    /**
     * Return the customer on this phone number, creating one with the given name
     * if the number is not yet on file.
     *
     * @throws IllegalArgumentException if a new customer is needed but no name was given
     */
    @Transactional
    public Customer findOrRegister(String phone, String name) {
        String p = phone == null ? "" : phone.trim();
        if (p.isEmpty()) {
            throw new IllegalArgumentException("A phone number is required");
        }
        return customerRepository.findByPhone(p).orElseGet(() -> {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("A name is required to add a new customer");
            }
            return customerRepository.save(new Customer(name.trim(), p));
        });
    }
}
