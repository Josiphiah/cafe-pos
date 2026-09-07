package zm.cafe.pos.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Security rules for authenticated cafe staff. */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/error").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/staff/**").hasRole("MANAGER")
                        .requestMatchers("/catalog/**").hasRole("MANAGER")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true)
                        .failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
