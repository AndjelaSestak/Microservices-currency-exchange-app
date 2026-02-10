package api.proxies;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoWalletDto;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {
	
	@GetMapping("/crypto-wallets")
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets();
	
	//vraca za pojedinacnog korisnika podtake o novcaniku, samo admin
	@GetMapping("/crypto-wallets/{email}")
	CryptoWalletDto getWalletByEmail(@PathVariable(value="email") String email);
		
	//korisnik pregleda samo svoj wallet
	@GetMapping("/crypto-wallet/user")
	CryptoWalletDto getWalletForUser(@RequestHeader("Authorization") String authorizationHeader);

	//kreiranje novcanika, samo admin
	@PostMapping("/crypto-wallets")
	ResponseEntity<?> createWallet(@RequestBody CryptoWalletDto dto);

	//azuriranje novcanika, samo admin
	@PutMapping("/crypto-wallets/{email}")
	ResponseEntity<String> updateWallet(@PathVariable(value ="email") String email, @RequestBody CryptoWalletDto dto);

	//brisanje novcanika, poziva se automatski preko proxy-ja iz userService
	@DeleteMapping("/crypto-wallets/{email}")
	ResponseEntity<String> deleteWallet(@PathVariable(value ="email") String email, @RequestHeader(value = "X-Internal-Call", required = false) String internalCall);
	    
	@PutMapping("/crypto-wallets/new-email")
	ResponseEntity<?> updateEmail( @RequestParam(value ="oldEmail") String oldEmail,  @RequestParam(value ="newEmail") String newEmail);
	    
	@GetMapping("/crypto-wallet/{email}/{currencyFrom}")
	public BigDecimal getUserCurrencyAmount(@PathVariable(value="email") String email, @PathVariable(value="currencyFrom") String currencyFrom);

}
