package zm.cafe.pos.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zm.cafe.pos.domain.Staff;
import zm.cafe.pos.repo.StaffRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final StaffRepository staffRepository;

    public DatabaseUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return User.withUsername(staff.getUsername()).password(staff.getPasswordHash())
                .roles(staff.getRole().name()).disabled(!staff.isActive()).build();
    }
}
