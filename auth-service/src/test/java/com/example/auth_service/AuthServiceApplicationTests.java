package com.example.auth_service;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceApplicationTests {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthService authService;

	@Test
	void registerUserSuccessfully() {

		RegisterRequest request = new RegisterRequest();
		request.setName("Sara");
		request.setEmail("sara@gmail.com");
		request.setPassword("sara123");

		when(userRepository.existsByEmail("sara@gmail.com"))
				.thenReturn(false);

		when(passwordEncoder.encode("sara123"))
				.thenReturn("hashed-password");

		String result = authService.register(request);

		assertEquals("User registered successfully", result);

		verify(userRepository).save(argThat(user ->
				user.getName().equals("Sara")
						&& user.getEmail().equals("sara@gmail.com")
						&& user.getPassword().equals("hashed-password")
						&& user.getRole() == Role.USER
		));
	}

	@Test
	void registerWithExistingEmail() {

		RegisterRequest request = new RegisterRequest();
		request.setName("Sara");
		request.setEmail("sara@gmail.com");
		request.setPassword("sara123");

		when(userRepository.existsByEmail("sara@gmail.com"))
				.thenReturn(true);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> authService.register(request)
		);

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void loginSuccessfully() {

		LoginRequest request = new LoginRequest();
		request.setEmail("sara@gmail.com");
		request.setPassword("sara123");

		User user = new User();
		user.setEmail("sara@gmail.com");
		user.setPassword("hashed-password");
		user.setRole(Role.USER);

		when(userRepository.findByEmail("sara@gmail.com"))
				.thenReturn(java.util.Optional.of(user));

		when(passwordEncoder.matches("sara123", "hashed-password"))
				.thenReturn(true);

		when(jwtService.generateToken(user))
				.thenReturn("sample-jwt-token");

		String result = authService.login(request);

		assertEquals("sample-jwt-token", result);

		verify(jwtService).generateToken(user);
	}

	@Test
	void loginWithWrongPassword() {

		LoginRequest request = new LoginRequest();
		request.setEmail("sara@gmail.com");
		request.setPassword("wrong-password");

		User user = new User();
		user.setEmail("sara@gmail.com");
		user.setPassword("hashed-password");

		when(userRepository.findByEmail("sara@gmail.com"))
				.thenReturn(java.util.Optional.of(user));

		when(passwordEncoder.matches("wrong-password", "hashed-password"))
				.thenReturn(false);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> authService.login(request)
		);

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

		verify(jwtService, never()).generateToken(any(User.class));
	}

}
