package api.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoWalletDto;

@Service
public interface CryptoWalletService {

	@GetMapping("/crypto-wallets")
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets();
	
	//vraca za pojedinacnog korisnika podtake o novcaniku, samo admin
	@GetMapping("/crypto-wallets/{email}")
	ResponseEntity<?> getWalletByEmail(@PathVariable("email") String email);
		
	//korisnik pregleda samo svoj wallet
	@GetMapping("/crypto-wallet/user")
	CryptoWalletDto getWalletForUser(@RequestHeader("Authorization") String authorizationHeader);

	//kreiranje novcanika, samo admin
	@PostMapping("/crypto-wallets")
	ResponseEntity<?> createWallet(@RequestBody CryptoWalletDto dto);

	//azuriranje novcanika, samo admin
	@PutMapping("/crypto-wallets/{email}")
	ResponseEntity<?> updateWallet(@PathVariable String email, @RequestBody CryptoWalletDto dto);

	//brisanje novcanika, poziva se automatski preko proxy-ja iz userService
	@DeleteMapping("/crypto-wallets/{email}")
	ResponseEntity<?> deleteWallet(@PathVariable String email, @RequestHeader(value = "X-Internal-Call", required = false) String internalCall);
	    
	@PutMapping("/crypto-wallets/new-email")
	ResponseEntity<?> updateEmail( @RequestParam String oldEmail,  @RequestParam String newEmail);
	    
	@GetMapping("/crypto-wallet/{email}/{currencyFrom}")
	public BigDecimal getUserCurrencyAmount(@PathVariable("email") String email, @PathVariable("currencyFrom") String currencyFrom);

}
