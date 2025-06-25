package com.tianqueal.flowjet.backend.config

import com.tianqueal.flowjet.backend.utils.constants.ApiPaths
import com.tianqueal.flowjet.backend.utils.constants.BeanNames
import com.tianqueal.flowjet.backend.utils.constants.PublicEndpoints
import com.tianqueal.flowjet.backend.utils.constants.SecurityConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.util.FileCopyUtils
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

/**
 * Main Spring Security configuration for the application.
 *
 * This class sets up HTTP security, JWT resource server, authentication manager,
 * password encoding, and provides beans for JWT signing and authority mapping.
 *
 * @author Christian A.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    @Value("classpath:certs/private-key.pem")
    private val privateKeyResource: Resource,
) {
    /**
     * Configures the main security filter chain.
     *
     * - Disables CSRF protection.
     * - Permits all requests to public endpoints (such as login, register, etc.).
     * - Requires authentication for all other endpoints.
     * - Enforces stateless session management.
     * - Configures the application as an OAuth2 Resource Server using JWT.
     * - Applies a custom JwtAuthenticationConverter for authority mapping.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val publicV1 = PublicEndpoints.forVersion(ApiPaths.V1)
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(*publicV1.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.sessionManagement { sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
        return http.build()
    }

    /**
     * Exposes the AuthenticationManager as a bean, required for manual authentication flows.
     */
    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    /**
     * Provides a BCrypt password encoder for hashing user passwords.
     */
    @Bean
    fun passwordEncode(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Loads the RSA private key from the configured PEM file for JWT signing.
     *
     * @return PrivateKey used to sign JWT tokens.
     * @throws RuntimeException if the key cannot be loaded or parsed.
     */
    @Bean
    @Qualifier(BeanNames.JWT_SIGNING_PRIVATE_KEY)
    fun jwtSigningPrivateKey(): PrivateKey {
        try {
            privateKeyResource.inputStream.use { inputStream ->
                val keyBytes = FileCopyUtils.copyToByteArray(inputStream)
                val privateKeyPEM =
                    String(keyBytes, StandardCharsets.UTF_8)
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replace(System.lineSeparator(), "")
                        .replace("\n", "")
                        .replace("\r", "")

                val decodedKey = Base64.getDecoder().decode(privateKeyPEM)
                val keySpec = PKCS8EncodedKeySpec(decodedKey)
                val keyFactory = KeyFactory.getInstance("RSA")
                return keyFactory.generatePrivate(keySpec)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to load private key for JWT signing", e)
        }
    }

    /**
     * Configures a JwtAuthenticationConverter bean to map JWT claims (such as roles/authorities)
     * to Spring Security GrantedAuthority objects.
     *
     * The claim name and prefix are configurable via SecurityConstants.
     */
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthoritiesClaimName(SecurityConstants.AUTHORITIES_CLAIM_NAME)
        grantedAuthoritiesConverter.setAuthorityPrefix(SecurityConstants.AUTHORITIES_PREFIX)
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        return jwtAuthenticationConverter
    }
}
