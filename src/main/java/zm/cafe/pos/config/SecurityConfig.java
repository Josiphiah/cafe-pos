package zm.cafe.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SKELETON — shared scaffold on main.
 *
 * <p>Right now every request is permitted so the other vertical slices can be
 * built and demoed independently. The <b>auth / staff-login</b> slice
 * (owner: Salifyanji) replaces the filter chain below with real form login,
 * a {@code UserDetailsService} backed by {@code StaffRepository}, role-based
 * rules (MANAGER vs CASHIER) and logout. Do not build login pages against this
 * placeholder — wait for that slice to land on main.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // H2 console
        return http.build();
    }

    /** Shared bean — used by the seeder and by the auth slice. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
