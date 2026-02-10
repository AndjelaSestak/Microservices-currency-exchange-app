package apiGateway.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import api.dtos.UserDto;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {
	
	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
		http
		.csrf(csrf -> csrf.disable())
		.authorizeExchange(exchange -> exchange
				.pathMatchers("/currency-exchange").permitAll()
				.pathMatchers("/currency-conversion").hasRole("USER")
				.pathMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN", "OWNER")
				.pathMatchers(HttpMethod.POST, "/users/newUser").hasAnyRole("OWNER", "ADMIN")
				.pathMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("OWNER")
				.pathMatchers(HttpMethod.PUT, "/users/{id}").hasAnyRole("OWNER", "ADMIN")
				.pathMatchers(HttpMethod.POST, "/bank-accounts").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/bank-accounts").hasRole("ADMIN")
				.pathMatchers(HttpMethod.PUT, "/bank-accounts/{email}").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/bank-accounts/{email}").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/bank-account/user").hasRole("USER")
				.pathMatchers(HttpMethod.DELETE, "/bank-accounts/{email}").denyAll()
				.pathMatchers("/crypto-exchange").permitAll()
				.pathMatchers(HttpMethod.POST, "/crypto-wallets").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/crypto-wallets").hasRole("ADMIN")
				.pathMatchers(HttpMethod.PUT, "/crypto-wallets/{email}").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/crypto-wallets/{email}").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/crypto-wallet/user").hasRole("USER")
				.pathMatchers(HttpMethod.DELETE, "/crypto-wallets/{email}").denyAll()
				.pathMatchers("/crypto-conversion-feign").hasRole("USER")
				.pathMatchers("/trade-service").hasRole("USER")
				).httpBasic(Customizer.withDefaults());
		
		return http.build();
	}
	
	@Bean
	ReactiveUserDetailsService reactiveUserDetailsService(WebClient.Builder webClientBuilder, BCryptPasswordEncoder encoder) {
		//WebClient client = webClientBuilder.baseUrl("http://localhost:8770").build();

		//		Verzija za Docker
		WebClient client = webClientBuilder.baseUrl("http://users-service:8770").build();
		
		return user -> client.get()
				.uri(uriBuilder -> uriBuilder
						.path("/users/email")
						.queryParam("email", user)
						.build()
				)
				.retrieve()
				.bodyToMono(UserDto.class)
				.map(dto -> User.withUsername(dto.getEmail())
						.password(encoder.encode(dto.getPassword()))
						.roles(dto.getRole())
						.build()
				);
		}
		
		/*ResponseEntity<List<UserDto>> response =
				new RestTemplate().exchange("http://users-service:8770/users", HttpMethod.GET,
						null, new ParameterizedTypeReference<List<UserDto>>() {});
		List<UserDetails> users = new ArrayList<UserDetails>();
		for(UserDto user : response.getBody()) {
			users.add(
					User.withUsername(user.getEmail())
					.password(encoder.encode(user.getPassword()))
					.roles(user.getRole())
					.build());
				
		}
		
		return new MapReactiveUserDetailsService(users);
				
		}*/
	
	@Bean
	BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

}