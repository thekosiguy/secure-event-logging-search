# Implementation Plan: JWT Authentication

## Overview

This plan implements JWT-based authentication and role-based authorization for the Secure Event Logging & Search Platform. Tasks are ordered for incremental progress: dependencies first, then data layer, security components, controllers, configuration, and finally tests. Each task builds on the previous ones, ensuring no orphaned code.

## Tasks

- [x] 1. Add dependencies and configure build
  - [x] 1.1 Add security and JWT dependencies to pom.xml
    - Add `spring-boot-starter-security` dependency
    - Add `jjwt-api` (0.12.6), `jjwt-impl` (0.12.6, runtime), `jjwt-jackson` (0.12.6, runtime) dependencies
    - Add `spring-security-test` (test scope) dependency
    - Add `net.jqwik:jqwik` (1.9.1, test scope) dependency
    - _Requirements: 2.4, 3.1, 5.1_

- [x] 2. Create User entity, Role enum, and UserRepository
  - [x] 2.1 Create Role enum
    - Create `src/main/java/com/secureeventloggingandsearch/model/Role.java`
    - Define enum values: `ROLE_ADMIN`, `ROLE_USER`
    - _Requirements: 1.5, 4.1, 4.2, 4.3_

  - [x] 2.2 Create User entity
    - Create `src/main/java/com/secureeventloggingandsearch/model/User.java`
    - Fields: `id` (UUID, auto-generated), `username` (VARCHAR(50), unique, not null), `password` (VARCHAR(255), not null), `role` (Role enum, EnumType.STRING, not null)
    - JPA annotations: `@Entity`, `@Table(name = "users")`
    - _Requirements: 1.5_

  - [x] 2.3 Create UserRepository interface
    - Create `src/main/java/com/secureeventloggingandsearch/repository/UserRepository.java`
    - Extend `JpaRepository<User, UUID>`
    - Add `Optional<User> findByUsername(String username)`
    - Add `boolean existsByUsername(String username)`
    - _Requirements: 1.1, 1.2_

- [x] 3. Implement JwtProvider
  - [x] 3.1 Create JwtProvider component
    - Create `src/main/java/com/secureeventloggingandsearch/security/JwtProvider.java`
    - Inject `jwt.secret` and `jwt.expiration-ms` from application properties
    - Implement `generateToken(String username, String role)` — signs with HMAC-SHA256, sets subject, role claim, iat, exp
    - Implement `validateToken(String token)` — returns boolean, catches `JwtException`
    - Implement `getUsernameFromToken(String token)` — extracts subject claim
    - Implement `getRoleFromToken(String token)` — extracts role claim
    - _Requirements: 2.3, 2.4, 2.5, 3.1, 5.4, 5.5_

  - [x]* 3.2 Write property test for token claim round-trip
    - **Property 1: Token claim round-trip**
    - Create `src/test/java/com/secureeventloggingandsearch/security/JwtProviderPropertyTest.java`
    - For any valid username and role, generating a token and extracting claims returns the original values
    - **Validates: Requirements 2.3**

  - [x]* 3.3 Write property test for invalid token rejection
    - **Property 9: Invalid tokens are rejected**
    - In `JwtProviderPropertyTest.java`, add test that random strings, tokens signed with different keys, and expired tokens fail validation
    - **Validates: Requirements 3.2, 3.4**

- [x] 4. Implement AuthService
  - [x] 4.1 Create AuthService component
    - Create `src/main/java/com/secureeventloggingandsearch/service/AuthService.java`
    - Inject `UserRepository`, `JwtProvider`, and `PasswordEncoder`
    - Implement `register(RegisterRequest request)`:
      - Check `existsByUsername` → throw `ResponseStatusException(409)` if duplicate
      - Hash password with BCrypt
      - Save User entity
      - Return `RegisterResponse` with username and role
    - Implement `login(LoginRequest request)`:
      - Find user by username → throw `ResponseStatusException(401)` if not found
      - Verify password with BCrypt → throw `ResponseStatusException(401)` if mismatch
      - Generate JWT token via JwtProvider
      - Return `AuthResponse` with token
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2_

  - [x]* 4.2 Write property test for password hashing round-trip
    - **Property 2: Password hashing round-trip**
    - Create `src/test/java/com/secureeventloggingandsearch/security/PasswordHashingPropertyTest.java`
    - For any valid password (8+ chars), BCrypt hash verifies correctly and hash ≠ plaintext
    - **Validates: Requirements 1.3, 1.5**

  - [x]* 4.3 Write property tests for AuthService registration and login
    - **Property 3: Valid registration produces correct response**
    - **Property 4: Duplicate registration is rejected**
    - **Property 5: Invalid registration input is rejected**
    - **Property 6: Valid credentials yield a valid token**
    - **Property 7: Invalid credentials are rejected**
    - Create `src/test/java/com/secureeventloggingandsearch/service/AuthServicePropertyTest.java`
    - **Validates: Requirements 1.1, 1.2, 1.4, 2.1, 2.2**

- [x] 5. Create DTOs for authentication
  - [x] 5.1 Create RegisterRequest, LoginRequest, AuthResponse, and RegisterResponse DTOs
    - Create `src/main/java/com/secureeventloggingandsearch/dto/RegisterRequest.java` with `@NotBlank username`, `@Size(min=8) password`, `@NotNull Role role`
    - Create `src/main/java/com/secureeventloggingandsearch/dto/LoginRequest.java` with `@NotBlank username`, `@NotBlank password`
    - Create `src/main/java/com/secureeventloggingandsearch/dto/AuthResponse.java` with `token` and `type = "Bearer"` fields
    - Create `src/main/java/com/secureeventloggingandsearch/dto/RegisterResponse.java` with `username` and `role` fields
    - _Requirements: 1.1, 1.4, 2.1_

- [x] 6. Create AuthController
  - [x] 6.1 Implement AuthController with register and login endpoints
    - Create `src/main/java/com/secureeventloggingandsearch/controller/AuthController.java`
    - `@RestController` with `@RequestMapping("/api/v1/auth")`
    - `POST /register` → validates `@Valid @RequestBody RegisterRequest`, calls `AuthService.register()`, returns 201
    - `POST /login` → validates `@Valid @RequestBody LoginRequest`, calls `AuthService.login()`, returns 200
    - _Requirements: 1.1, 1.4, 2.1, 2.2_

  - [x]* 6.2 Write unit tests for AuthController
    - Create `src/test/java/com/secureeventloggingandsearch/controller/AuthControllerTest.java`
    - Test successful registration (201), duplicate username (409), validation errors (400)
    - Test successful login (200), invalid credentials (401)
    - Use standalone MockMvc setup with mocked AuthService
    - _Requirements: 1.1, 1.2, 1.4, 2.1, 2.2_

- [x] 7. Checkpoint - Verify core auth components
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Create JwtAuthenticationFilter
  - [x] 8.1 Implement JwtAuthenticationFilter
    - Create `src/main/java/com/secureeventloggingandsearch/security/JwtAuthenticationFilter.java`
    - Extend `OncePerRequestFilter`
    - Extract `Authorization` header, strip `Bearer ` prefix
    - Call `JwtProvider.validateToken()` — if invalid, continue filter chain without setting context
    - Extract username and role from token
    - Create `UsernamePasswordAuthenticationToken` with `SimpleGrantedAuthority` for the role
    - Set `SecurityContextHolder.getContext().setAuthentication()`
    - Continue filter chain
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x]* 8.2 Write property test for filter SecurityContext population
    - **Property 8: Filter populates SecurityContext from valid token**
    - Create `src/test/java/com/secureeventloggingandsearch/security/JwtAuthenticationFilterPropertyTest.java`
    - For any valid token, after filter processes request, SecurityContext contains matching username and role
    - **Validates: Requirements 3.1, 3.5**

- [x] 9. Create SecurityConfig, AuthEntryPoint, and AccessDeniedHandler
  - [x] 9.1 Create AuthEntryPoint
    - Create `src/main/java/com/secureeventloggingandsearch/security/AuthEntryPoint.java`
    - Implement `AuthenticationEntryPoint`
    - Write JSON error response with status 401, error "Unauthorized", message "Authentication required to access this resource", and timestamp
    - _Requirements: 6.1, 6.3_

  - [x] 9.2 Create AccessDeniedHandlerImpl
    - Create `src/main/java/com/secureeventloggingandsearch/security/AccessDeniedHandlerImpl.java`
    - Implement `AccessDeniedHandler`
    - Write JSON error response with status 403, error "Forbidden", message "Access denied: insufficient permissions", and timestamp
    - _Requirements: 6.2, 6.3_

  - [x] 9.3 Create SecurityConfig
    - Create `src/main/java/com/secureeventloggingandsearch/security/SecurityConfig.java`
    - `@Configuration` + `@EnableWebSecurity`
    - Define `SecurityFilterChain` bean:
      - Disable CSRF
      - Set session management to stateless
      - Permit `/api/v1/health`, `/api/v1/auth/**` without authentication
      - Require `ADMIN` role for `POST /api/v1/events`
      - Require `ADMIN` or `USER` role for `GET /api/v1/events` and `GET /api/v1/events/{id}`
      - Deny all other requests (authenticated required)
      - Register `AuthEntryPoint` and `AccessDeniedHandlerImpl`
      - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
    - Define `PasswordEncoder` bean (BCryptPasswordEncoder)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3_

  - [x]* 9.4 Write unit tests for SecurityConfig
    - Create `src/test/java/com/secureeventloggingandsearch/security/SecurityConfigTest.java`
    - Test CSRF disabled, stateless sessions, public endpoints accessible without auth
    - Test protected endpoints return 401 without token
    - Test role-based access (ADMIN can POST events, USER cannot)
    - Use `@SpringBootTest` with `@AutoConfigureMockMvc` or WebMvcTest with security
    - _Requirements: 4.4, 4.5, 5.1, 5.2, 5.3_

  - [ ]* 9.5 Write property tests for SecurityConfig (skipped: requires jqwik-spring module for Spring DI integration; properties 10-12 are covered by SecurityConfigTest JUnit tests)
    - **Property 10: Role-based access control enforcement**
    - **Property 11: Unknown paths are denied**
    - **Property 12: Security error responses are structured JSON**
    - Create `src/test/java/com/secureeventloggingandsearch/security/SecurityConfigPropertyTest.java`
    - **Validates: Requirements 4.1, 4.2, 4.3, 5.3, 6.1, 6.2**

- [x] 10. Update application.properties with JWT configuration
  - [x] 10.1 Add JWT properties to application.properties
    - Add `jwt.secret=${JWT_SECRET:default-dev-secret-key-that-is-at-least-32-bytes-long}`
    - Add `jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}`
    - _Requirements: 5.4, 5.5_

- [x] 11. Update existing EventControllerTest with security annotations
  - [x] 11.1 Add @WithMockUser annotations to EventControllerTest
    - Add `@WithMockUser(roles = "ADMIN")` to POST test methods (createEvent tests)
    - Add `@WithMockUser(roles = "USER")` to GET test methods (getAllEvents, getEventById tests)
    - Add `spring-security-test` import for `@WithMockUser`
    - Verify existing tests still pass with standalone MockMvc setup
    - _Requirements: 7.1, 7.3_

- [x] 12. Write unit tests for JwtProvider
  - [x]* 12.1 Write unit tests for JwtProvider
    - Create `src/test/java/com/secureeventloggingandsearch/security/JwtProviderTest.java`
    - Test token generation produces valid JWT string
    - Test token expiration is configurable
    - Test algorithm is HMAC-SHA256
    - Test expired token fails validation
    - Test tampered token fails validation
    - _Requirements: 2.4, 2.5_

- [x] 13. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The existing `EventServiceTest` requires no changes (Requirement 7.2)
- Surefire argLine (`-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true`) is already configured in pom.xml
- jqwik integrates with JUnit 5 platform — no additional test runner configuration needed
