package lingvo.app.auth.service;

import lingvo.app.auth.dto.SignUpRequestDto;
import lingvo.app.auth.entity.Role;
import lingvo.app.auth.entity.RoleType;
import lingvo.app.auth.entity.User;
import lingvo.app.auth.repository.RoleRepository;
import lingvo.app.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");

        Role userRole = new Role();
        userRole.setName(RoleType.USER);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        userService.createUser(signUpRequest);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("existinguser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(signUpRequest));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("existing@example.com");
        signUpRequest.setPassword("password123");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(signUpRequest));
    }

    @Test
    void createUser_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("a"); // Too short
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(signUpRequest));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("validuser");
        signUpRequest.setEmail("invalidemail"); // Invalid email format
        signUpRequest.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(signUpRequest));
    }

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        Role userRole = new Role();
        userRole.setName(RoleType.USER);
        user.setRoles(Collections.singleton(userRole));

        when(userRepository.findByUsername("testuser")).thenReturn(user);

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));
    }

    @Test
    void loadUserByUsername_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistinguser")).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistinguser"));
    }

    @Test
    void existsByUsername_WithExistingUsername_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("existinguser");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByUsername_WithNonExistingUsername_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistinguser")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistinguser");

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("existing@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByEmail("nonexisting@example.com")).thenReturn(false);

        // Act
        boolean result = userService.existsByEmail("nonexisting@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void findByUsername_WithExistingUsername_ShouldReturnUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        // Act
        User result = userService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_WithNonExistingUsername_ShouldReturnNull() {
        // Arrange
        when(userRepository.findByUsername("nonexistinguser")).thenReturn(null);

        // Act
        User result = userService.findByUsername("nonexistinguser");

        // Assert
        assertNull(result);
    }
}