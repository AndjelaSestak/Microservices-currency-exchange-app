package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface TradeService {

	@GetMapping("/trade-service")
	public ResponseEntity<?> tradeCurrenices(@RequestParam("from") String from, 
            @RequestParam("to") String to,
            @RequestParam("quantity") BigDecimal quantity, 
            @RequestHeader("Authorization") String authorizationHeader);
}
