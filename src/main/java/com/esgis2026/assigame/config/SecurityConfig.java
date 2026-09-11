package com.esgis2026.assigame.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.esgis2026.assigame.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/pages/**",
                                "/services/**",
                                "/styles/**",
                                "/utils/**",
                                "/images/**",
                                "/*.html",
                                "/*.css",
                                "/*.js")
                        .permitAll()
                        /* Navigation client (catalogue, fiche produit, catégories) :
                           toujours accessible SANS authentification. Seules les routes
                           de gestion (créer/modifier/supprimer un produit, tableau de
                           bord vendeur/admin, utilisateurs, stats...) restent protégées
                           plus bas par ".requestMatchers("/api/**").authenticated()". */
                        .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promo-banner").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/produits", "/api/produits/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorieproduit/list").permitAll()
                        /* Passage de commande : un client n'est jamais obligé de créer
                           un compte pour acheter (commande "invité"). S'il est connecté,
                           le token est quand même envoyé et pris en compte côté service. */
                        .requestMatchers(HttpMethod.POST, "/api/commandes").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        /* Credentials + "*" origins : OK avec allowedOriginPatterns (Spring).
           En prod, restreindre aux domaines réels via CORS_ORIGINS. */
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
