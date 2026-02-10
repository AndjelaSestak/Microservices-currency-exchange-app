package api.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.UserDto;

@FeignClient("users-service")
public interface UsersProxy {

	@GetMapping("/users/email")
	UserDto getUserByEmailFeign(@RequestParam(value="email") String email);
	/*
	@GetMapping("/users/by-email/{email}")
	public Boolean getUser(@PathVariable("email") String email);
	
	@GetMapping("/users/by-email-role/{email}")
	public String getUsersRole(@PathVariable("email") String email);
	
	@GetMapping("/users/current-user-role")
	String getCurrentUserRole(@RequestHeader("Authorization") String authorizationHeader);
	
	@GetMapping("/users/current-user-email")
	String getCurrentUserEmail(@RequestHeader("Authorization") String authorizationHeader);*/
	
}