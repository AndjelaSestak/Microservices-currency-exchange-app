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

import api.dtos.BankAccountDto;
import feign.Headers;

@Service
public interface BankAccountService {

	@GetMapping("/bank-accounts")
	ResponseEntity<List<BankAccountDto>> getAllAccounts();
	
	//vraca za pojedinacnog korisnika podtake o bankovnom racunu, samo admin
	@GetMapping("/bank-accounts/{email}")
	ResponseEntity<?> getBankAccountByEmail(@PathVariable("email") String email);
	
	//korisnik pregleda samo svoj racun
	@GetMapping("/bank-account/user")
	BankAccountDto getBankAccountForUser(@RequestHeader("Authorization") String authorizationHeader);

	//kreiranje bankovnog racuna, samo admin
    @PostMapping("/bank-accounts")
    ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto);

    //azuriranje bankovnog racuna, samo admin
    @PutMapping("/bank-accounts/{email}")
    ResponseEntity<?> updateBankAccount(@PathVariable String email, @RequestBody BankAccountDto dto);

    //brisanje racuna, poziva se automatski preko proxy-ja iz userService
    @DeleteMapping("/bank-accounts/{email}")
    ResponseEntity<?> deleteAccount(@PathVariable String email, @RequestHeader(value = "X-Internal-Call", required = false) String internalCall);
    
    @PutMapping("/bank-accounts/new-email")
    ResponseEntity<?> updateEmail( @RequestParam String oldEmail,  @RequestParam String newEmail);
    
    @GetMapping("/bank-account/{email}/{currencyFrom}")
	public BigDecimal getUserCurrencyAmount(@PathVariable("email") String email, @PathVariable("currencyFrom") String currencyFrom);

    
}
