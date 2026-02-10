package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface CryptoConversionService {

	@GetMapping("/crypto-conversion-feign")
	ResponseEntity<?>getCryptoConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity,  @RequestHeader("Authorization") String authorizationHeader);
}
