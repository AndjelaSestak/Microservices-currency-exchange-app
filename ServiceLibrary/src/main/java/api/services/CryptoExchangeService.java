package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoExchangeDto;

@Service
public interface CryptoExchangeService {

	 @GetMapping("/crypto-exchange")
	 ResponseEntity<?> getExchange(@RequestParam String from, @RequestParam String to);
	 
}