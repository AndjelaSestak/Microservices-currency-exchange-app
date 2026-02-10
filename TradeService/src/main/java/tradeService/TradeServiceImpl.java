package tradeService;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import api.dtos.BankAccountDto;
import api.dtos.CryptoValuesDto;
import api.dtos.CryptoWalletDto;
import api.dtos.FiatBalanceDto;
import api.proxies.BankAccountProxy;
import api.proxies.CryptoWalletProxy;
import api.proxies.CurrencyConversionProxy;
import api.proxies.UsersProxy;
import api.services.TradeService;
import util.exceptions.InvalidQuantityException;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import feign.FeignException;
import java.util.Map;
import java.util.HashMap;

@RestController
public class TradeServiceImpl implements TradeService {

	private static final Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);
	
	@Autowired
	private TradeServiceRepository repository;

	@Autowired
	private UsersProxy userProxy;

	@Autowired
	private BankAccountProxy bankProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;
	
	@Autowired
	private CurrencyConversionProxy currencyConversionProxy;
	
	
	@Override
	public ResponseEntity<?> tradeCurrenices(String from, String to, BigDecimal quantity, String authorizationHeader) {
		// TODO Auto-generated method stub
			
				try {
			                String userEmail = getEmailFromAuthHeader(authorizationHeader);
			                
			                //IZ FIAT U CRYPTO VALUTU
			                if((from.equals("EUR") || from.equals("USD") || from.equals("RSD") || from.equals("CHF") || from.equals("CAD") || from.equals("GBP"))
			                		&& (to.equals("BTC") || to.equals("ETH") || to.equals("LTC"))) {
			                	
			                	if(from.equals("RSD") || from.equals("CHF") || from.equals("CAD") || from.equals("GBP"))
			                	{
			                		ResponseEntity<?> responseEntity = currencyConversionProxy.getConversionFeign(from, "EUR", quantity, authorizationHeader);

			                		// body je mapa
			                		Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();

			                		// izvuci message
			                		String message = (String) responseBody.get("message");

			                		// Parsiranje totalExchanged iz message
			                		String afterFor = message.split("for ")[1]; // npr. "BTC:0.4"
			                		BigDecimal totalExchanged = new BigDecimal(afterFor.split(":")[1]);
			                		
			                		quantity = totalExchanged;
			                		from="EUR";
			                		
			                	}
			                	
			                    TradeServiceModel exchangeRate = getExchange(from, to); //dole ova metoda
			                    
			                    if (exchangeRate == null) {
			                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exchange rate not found.");
			                    }
			                   
			                    ResponseEntity<?> updateAccountResponse;
			                    //AZURIRAM BANKOVNI RACUN
			                    try {
			                    	
			                    	BigDecimal accountCurrencyAmountFrom = bankProxy.getUserCurrencyAmount(userEmail, from);
			                    	
			                        FiatBalanceDto fiatFromDto = new FiatBalanceDto(from, accountCurrencyAmountFrom.subtract(quantity));
			            			
			            			ArrayList<FiatBalanceDto> newList = new ArrayList<FiatBalanceDto>();
			            			
			            			newList.add(fiatFromDto);
			            			
			            			BankAccountDto updateAccount = new BankAccountDto(
			            					userEmail,
			            					newList
			            			);
			            			
			                        updateAccountResponse = bankProxy.updateBankAccount(userEmail, updateAccount);
			                        

			                    } catch (FeignException e) {
			                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
			                    }
			                    
		                        if (!updateAccountResponse.getStatusCode().is2xxSuccessful()) {
		                            log.error("Failed to update bank account: {}", updateAccountResponse.getBody());
		                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
		                        }
			                    
			                    //AZURIRAM CRYPTO WALLET
		                        ResponseEntity<?> updateCryptoWallet;
		                        BigDecimal conversionMultiply = exchangeRate.getConversion();
				                BigDecimal cryptoQuantity = conversionMultiply.multiply(quantity);
		                        
		                        try {
		            	        
		                        	BigDecimal cryptoAmountTo = walletProxy.getUserCurrencyAmount(userEmail, to);
		                        	CryptoValuesDto cryptoToDto =  new CryptoValuesDto(to, cryptoAmountTo.add(cryptoQuantity));
		            			
		                        	ArrayList<CryptoValuesDto> newList = new ArrayList<CryptoValuesDto>();
		            			
		                        	newList.add(cryptoToDto);
		            			
		                        	CryptoWalletDto updatedCryptoWallet = new CryptoWalletDto(
		            					
		            					newList, userEmail
		                        	);
		            					
		                        	updateCryptoWallet = walletProxy.updateWallet(userEmail, updatedCryptoWallet);
		                        } catch (FeignException e){
		                        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update crypto wallet.");
		                        }
		                        
		                        if (!updateCryptoWallet.getStatusCode().is2xxSuccessful()) {
		                            log.error("Failed to update crypto wallet: {}", updateAccountResponse.getBody());
		                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
		                        }
		                        
		            			String message = "Conversion was successfull! " + from + ":" + quantity + " is exchanged for " + to + ":" + cryptoQuantity;
		            			CryptoWalletDto updatedCryptoWallet1 = walletProxy.getWalletForUser(authorizationHeader);
		            			
		            			Map<String, Object> responseBody = new HashMap<>();
		            			responseBody.put("cryptoWallet", updatedCryptoWallet1);
		            			responseBody.put("message", message);

		            			// Vrati kao ResponseEntity
		            			return ResponseEntity.ok(responseBody);
			                 	}
			                
			                //IZ CRYPTO U FIAT
			                else if((to.equals("EUR") || to.equals("USD") || to.equals("RSD") || to.equals("CHF") || to.equals("CAD") || to.equals("GBP"))
			                		&& (from.equals("BTC") || from.equals("ETH") || from.equals("LTC"))) {
			                	
			                	BigDecimal cryptoAmountFrom = walletProxy.getUserCurrencyAmount(userEmail, from);
		                		
		                		if(cryptoAmountFrom.compareTo(quantity) < 0) {
		                			throw new InvalidQuantityException(String.format("Quantity of " + quantity + " is too large, User crypto wallet currency balance: (" + from + ") " + cryptoAmountFrom));
		                		}
		                		
		                		CryptoValuesDto cryptoFromDto = new CryptoValuesDto(from, cryptoAmountFrom.subtract(quantity));
		                		
		                		ArrayList<CryptoValuesDto> newList = new ArrayList<CryptoValuesDto>();
		                		
		                		newList.add(cryptoFromDto);
		                		
		                		CryptoWalletDto updateCryptoWallet = new CryptoWalletDto(
		                				newList,
		                				userEmail
		                				
		                		);
		                		
		                		ResponseEntity<?> updatedCryptoBalances = walletProxy.updateWallet(userEmail, updateCryptoWallet);
			                	
		                		//AKO CRYPTO ZELI DA SE ZAMENI ZA NEKI FIAT KOJI NIJE EUR ILI USD
			                	if(to.equals("RSD") || to.equals("CHF") || to.equals("CAD") || to.equals("GBP"))
			                	{
			                		//MENJAMO PRVO CRYPTO U EUR, DOBIJAM KURS
			                		TradeServiceModel exchangeRate = getExchange(from, "EUR");

			                		if (exchangeRate == null) {
				                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exchange rate not found.");
				                    }
			                		
			                		//cryptoToEUR predstavlja koliko EUR dobija korisnik za 2 BTC.
			                		BigDecimal cryptoToEUR = quantity.multiply(exchangeRate.getConversion());
			                			
			                		//AZURIRAMO BANKOVNI RACUN, DODAJEMO EUR PRE KONVERZIJE EUR TO NEKI FIAT
			                		BigDecimal currentEURBalance= bankProxy.getUserCurrencyAmount(userEmail, "EUR");
			                		FiatBalanceDto fiatEURDto = new FiatBalanceDto("EUR", currentEURBalance.add(cryptoToEUR));
				                    ArrayList<FiatBalanceDto> updatedEURBalance = new ArrayList<FiatBalanceDto>();
				                    updatedEURBalance.add(fiatEURDto);
				                    BankAccountDto updatedEURBank = new BankAccountDto(userEmail,updatedEURBalance);
				                    ResponseEntity<?> updatedBalancesResponse = bankProxy.updateBankAccount(userEmail, updatedEURBank);
				                    
				                    if (!updatedBalancesResponse.getStatusCode().is2xxSuccessful()) {
				                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
				                    }
				                    
			                		//MENJAMO EVRE U TRAZENU VALUTU
			                		ResponseEntity<?> responseEntity = currencyConversionProxy.getConversionFeign("EUR", to, cryptoToEUR, authorizationHeader);
			                		
			                		Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
			                		
			                		// Parsiranje totalExchanged iz message
			                		String message = (String) responseBody.get("message");
			                		String afterFor = message.split("for ")[1]; // npr. "DIN:0.4"
			                		BigDecimal totalExchanged = new BigDecimal(afterFor.split(":")[1]);
			                		
			                		// Iz crypto u npr din
			                		String finalMessage = "Conversion was successfull! " + from + ":" + quantity + " is exchanged for " + to + ":" + totalExchanged;
			                		
			                		BankAccountDto updatedBankAccount = bankProxy.getBankAccountForUser(authorizationHeader);
			            			
			            			Map<String, Object> responseBody1 = new HashMap<>();
			            			responseBody1.put("bankAccount", updatedBankAccount);
			            			responseBody1.put("message", finalMessage);

			            			// Vrati kao ResponseEntity
			            			return ResponseEntity.ok(responseBody1);
			                	
			                	} else if (to.equals("EUR") || to.equals("USD")){
			                		
			                		TradeServiceModel exchangeRate = getExchange(from, to);

			                		if (exchangeRate == null) {
				                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exchange rate not found.");
				                    }
			                		
			                		BigDecimal cryptoToFiat = quantity.multiply(exchangeRate.getConversion());
			                		
			                		//AZURIRAM BANKOVNI RACUN, SMANJUJEM KOLICINU EUR
			                		ResponseEntity<?> updateAccountResponse1;
			                		
			                		BigDecimal accountCurrencyAmountTo = bankProxy.getUserCurrencyAmount(userEmail, to);
			                    	
			                        FiatBalanceDto fiatToDto = new FiatBalanceDto(to, accountCurrencyAmountTo.add(cryptoToFiat));
			            			
			            			ArrayList<FiatBalanceDto> newList1 = new ArrayList<FiatBalanceDto>();
			            			
			            			newList1.add(fiatToDto);
			            			
			            			BankAccountDto updateAccount = new BankAccountDto(
			            					userEmail,
			            					newList1
			            			);
			            			
			                        updateAccountResponse1 = bankProxy.updateBankAccount(userEmail, updateAccount);
			                        
			                        if (!updateAccountResponse1.getStatusCode().is2xxSuccessful()) {
			                            log.error("Failed to update bank account: {}", updateAccountResponse1.getBody());
			                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
			                        }
			                        
			                        //ISPIS REZULTATA
			                		String message = "Conversion was successfull! " + from + ":" + quantity + " is exchanged for " + to + ":" + cryptoToFiat;
			                		BankAccountDto updatedBankAccount = bankProxy.getBankAccountForUser(authorizationHeader);
			            			
			            			Map<String, Object> responseBody = new HashMap<>();
			            			responseBody.put("bankAccount", updatedBankAccount);
			            			responseBody.put("message", message);

			            			return ResponseEntity.ok(responseBody);
			                	}
			                	
			                }
				} catch (Exception e) {
			        log.error("Unexpected error in tradeCurrenices", e);
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
			    }
				
				return null;
			}

	public TradeServiceModel getExchange(String from, String to) {
			TradeServiceModel exchange = repository.findByFromAndToIgnoreCase(from, to);
			return exchange;
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
}

