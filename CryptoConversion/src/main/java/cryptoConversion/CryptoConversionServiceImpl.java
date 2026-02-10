package cryptoConversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.CryptoConversionDto;
import api.dtos.CryptoExchangeDto;
import api.dtos.CryptoValuesDto;
import api.dtos.CryptoWalletDto;
import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyExchangeDto;
import api.dtos.FiatBalanceDto;
import api.dtos.UserDto;
import api.proxies.CryptoExchangeProxy;
import api.proxies.CryptoWalletProxy;
import api.proxies.UsersProxy;
import api.services.CryptoConversionService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.InvalidQuantityException;

@RestController
public class CryptoConversionServiceImpl implements CryptoConversionService {

	@Autowired
	private CryptoExchangeProxy exchangeProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;

	@Autowired
	private UsersProxy usersProxy;

	
	CryptoExchangeDto response;
	
	Retry retry;
	
	public CryptoConversionServiceImpl(RetryRegistry registry) {
		retry = registry.retry("default");
	}
	
	@Override
	@CircuitBreaker(name = "cb", fallbackMethod = "fallback")
	public ResponseEntity<?> getCryptoConversionFeign(String from, String to, BigDecimal quantity,
			String authorizationHeader) {
		try {
	        UserDto user = usersProxy.getUserByEmailFeign(getEmailFromAuthHeader(authorizationHeader));

	        if (user == null) {
		           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		        }
	        
	        CryptoWalletDto cryptoWallet = walletProxy.getWalletForUser(authorizationHeader);

	        if (cryptoWallet == null) {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Crypto wallet not found for user.");
	        }
	        
	        BigDecimal accountCurrencyAmountTo = walletProxy.getUserCurrencyAmount(user.getEmail(), to);
	        if(accountCurrencyAmountTo == null) {
	        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Currency " + to + " is not valid.");
	        }
	        BigDecimal accountCurrencyAmountFrom = walletProxy.getUserCurrencyAmount(user.getEmail(), from);
	        if(accountCurrencyAmountFrom == null) {
	        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Currency " + from + " is not valid.");
	        }
	        
	        if (accountCurrencyAmountFrom.compareTo(quantity) < 0) {
	        	throw new InvalidQuantityException(String.format("Quantity of " + quantity + " is too large, User crypto wallet currency balance: (" + from + ") " + accountCurrencyAmountFrom));
	        }
	        
	        retry.executeSupplier(()-> response = exchangeProxy.getExchange(from, to).getBody());
	        //u response cuvam kurs
	        //response = exchangeProxy.getExchange(from, to).getBody();
	        
	        if (response == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Crypto exchange service response is null.");
	        }

	        BigDecimal exchangeValue = response.getExchangeRate();
	        BigDecimal totalExchanged = exchangeValue.multiply(quantity);
	        
	        //nova stanja crypto valuta
	        CryptoValuesDto cryptoFromDto = new CryptoValuesDto(from, accountCurrencyAmountFrom.subtract(quantity));
	        CryptoValuesDto cryptoToDto =  new CryptoValuesDto(to, accountCurrencyAmountTo.add(totalExchanged));
			
			ArrayList<CryptoValuesDto> newList = new ArrayList<CryptoValuesDto>();
			
			newList.add(cryptoFromDto);
			newList.add(cryptoToDto);
			
			
			CryptoWalletDto updateCryptoWallet = new CryptoWalletDto(
					
					newList, user.getEmail()
			);
					
			
			ResponseEntity<?> updatedBalances = walletProxy.updateWallet(user.getEmail(), updateCryptoWallet);
			if(updatedBalances.getStatusCode().is4xxClientError()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Updating crypto balances failed!");
			}
			
			
			CryptoWalletDto updatedCryptoWallet = walletProxy.getWalletForUser(authorizationHeader);
			String message = "Conversion was successfull! " + from + ":" + quantity + " is exchanged for " + to + ":" + totalExchanged;
					
			
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("cryptoWallet", updatedCryptoWallet);
			responseBody.put("message", message);

			// Vrati kao ResponseEntity
			return ResponseEntity.ok(responseBody);
			
	 } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An unexpected error occurred: " + ex.getMessage());
	    }
	}
	
	public String getEmailFromAuthHeader(String authorizationHeader) {
		try {
			String encodedCredentials = authorizationHeader.replace("Basic ", "");
			byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());
			String decodedCredentials = new String(decodedBytes);
			String[] credentials = decodedCredentials.split(":");
			String email = credentials[0];
			return email;
		} catch(Exception e) {
			System.out.println("Error while extracting email " + e.getMessage());
			return null;
		}
	}
	
	public ResponseEntity<?> fallback(CallNotPermittedException ex){
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Crypto conversion service is currently unavailbale, Circuit breaker is in OPEN state!");
	}

}
