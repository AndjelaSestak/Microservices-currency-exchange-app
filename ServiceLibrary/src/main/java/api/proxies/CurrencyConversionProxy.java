package api.proxies;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currency-conversion")
public interface CurrencyConversionProxy {

	@GetMapping("/currency-conversion-feign")
	ResponseEntity<?>getConversionFeign(@RequestParam(value = "from") String from, @RequestParam(value = "to") String to, @RequestParam(value = "quantity") BigDecimal quantity,  @RequestHeader("Authorization") String authorizationHeader);
}
