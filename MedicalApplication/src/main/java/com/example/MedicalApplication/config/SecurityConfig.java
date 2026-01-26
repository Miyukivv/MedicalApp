package com.example.MedicalApplication.config;

import com.example.MedicalApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login","/send-email", "/perform_login", "/register", "/css/**", "/img/**", "/api/auth/register", "api/auth/login","/api/medications/taken").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        // ✅ po udanym logowaniu ustaw loggedIn=true
                        .successHandler((request, response, authentication) -> {
                            String email = authentication.getName();
                            userRepository.findByEmail(email).ifPresent(u -> {
                                u.setLoggedIn(true);
                                userRepository.save(u);
                            });
                            response.sendRedirect(request.getContextPath() + "/home");
                        })

                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")

                        // ✅ przy wylogowaniu ustaw loggedIn=false
                        .addLogoutHandler((request, response, authentication) -> {
                            if (authentication != null) {
                                String email = authentication.getName();
                                userRepository.findByEmail(email).ifPresent(u -> {
                                    u.setLoggedIn(false);
                                    userRepository.save(u);
                                });
                            }
                        })

                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return (String email) -> userRepository.findByEmail(email)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        .password(u.getPassword())
                        .roles(u.getRole().toString())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("Nie ma usera: " + email));
    }

   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }



}
