package cryptoWallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoValuesDto;
import api.dtos.CryptoWalletDto;
import api.dtos.FiatBalanceDto;
import api.dtos.UserDto;
import api.proxies.UsersProxy;
import api.services.CryptoWalletService;

@RestController
public class CryptoWalletServiceImpl implements CryptoWalletService {

	@Autowired
	private CryptoWalletRepository walletRepo;
	
	@Autowired
	private  UsersProxy usersProxy;
	
	@Override
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets() {
		
		List<CryptoWalletModel> models = walletRepo.findAll();
        List<CryptoWalletDto> dtos = models.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}

	//KREIRANJE CRYPTO NOVCANIKA
    //ZAHVALJUJUCI PATH MATCHERS SAMO ADMIN MOZE DA PRISTUPI
	@Override
	public ResponseEntity<?> createWallet(CryptoWalletDto dto) {
		 try {
             // Proveravam da li user postoji
             //UserDto userExists = usersProxy.getUserByEmailFeign(dto.getEmail());
             //if (userExists == null) {
                 //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + dto.getEmail() + " does not exist.");
             //}

             // Proveravam da li user ima novcanik
             CryptoWalletModel existingAccount = walletRepo.findByEmail(dto.getEmail());
             if (existingAccount != null) {
                 return ResponseEntity.badRequest().body("Crypto wallet for user with email " + dto.getEmail() + " already exists.");
             }

             // Kreiram novi novcanik sa 0 balansa za sve valute
             CryptoWalletModel cryptoModel = new CryptoWalletModel(dto.getEmail());
             List<CryptoValuesModel> initialValues = createInitialValues(cryptoModel);
             cryptoModel.setValues(initialValues);
             walletRepo.save(cryptoModel);

             return ResponseEntity.ok(Collections.singletonMap("message", "Crypto wallet created successfully for user: " + dto.getEmail()));

     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating crypto wallet: " + e.getMessage()));
     }
	}
	
	//AZURIRANJE CRYPTO WALLET-A
    //SAMO ADMIN
	@Override
	public ResponseEntity<?> updateWallet(String email, CryptoWalletDto dto) {

		CryptoWalletModel cryptoWallet = walletRepo.findByEmail(email);
	    if (cryptoWallet == null) {
	        return ResponseEntity.notFound().build();
	    }
	    
	    Map<String, CryptoValuesModel> existingValues = cryptoWallet.getValues()
	            .stream()
	            .collect(Collectors.toMap(CryptoValuesModel::getCrypto, value -> value));

	    /*existingValues = {
       "BTC" -> CryptoValuesModel(crypto="BTC", amount=0),
       "ETH" -> CryptoValuesModel(crypto="ETH", amount=0),
       "LTC" -> CryptoValuesModel(crypto="LTC", amount=0)
   		}*/
	    for (CryptoValuesDto dtoValue : dto.getValues()) {
	        if (existingValues.containsKey(dtoValue.getCrypto())) {
	        	existingValues.get(dtoValue.getCrypto()).setAmount(dtoValue.getAmount());
	        } else {
	        	CryptoValuesModel newValues = new CryptoValuesModel(dtoValue.getCrypto(), dtoValue.getAmount());
	        	newValues.setCryptoWallet(cryptoWallet);
	        	cryptoWallet.getValues().add(newValues);
	        }
	    }

	    walletRepo.save(cryptoWallet);

	    return ResponseEntity.ok("Crypto wallet updated successfully");
	}
	
	@Override
	public ResponseEntity<?> deleteWallet(String email, @RequestHeader(value = "X-Internal-Call", required = false) String internalCall) {
		// Provera da li je zahtev internog servisa
	     if (!"true".equals(internalCall)) {
	         return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                              .body("This operation is allowed only via internal service calls.");
	     }

	     CryptoWalletModel cryptoWallet = walletRepo.findByEmail(email);
	     if (cryptoWallet != null) {
	    	 walletRepo.delete(cryptoWallet);
	         return ResponseEntity.ok("Crypto wallet deleted successfully");
	     } else {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                              .body("Crypto wallet not found for email: " + email);
	     }
	}
	
	
	@Override
	public ResponseEntity<?> getWalletByEmail(String email) {
		CryptoWalletModel model = walletRepo.findByEmail(email);
		if (model == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Crypto wallet not found for email: " + email);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(convertToDto(model));
	}

	@Override
	public CryptoWalletDto getWalletForUser(String authorizationHeader) {
		String email = this.getEmailFromAuthHeader(authorizationHeader);
		CryptoWalletModel model = walletRepo.findByEmail(email);
		if (model == null) {
			return null;
		}
		
		return convertToDto(model);
	}


	@Override
	public ResponseEntity<?> updateEmail(String oldEmail, String newEmail) {
		  CryptoWalletModel cryptoWallet = walletRepo.findByEmail(oldEmail);
		  if (cryptoWallet == null) {
		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Crypto wallet not found for user with email: " + oldEmail);
		  }
		  
		  cryptoWallet.setEmail(newEmail);
		  
		  walletRepo.save(cryptoWallet);

		  return ResponseEntity.ok(Collections.singletonMap("message", "Crypto wallet email updated successfully"));
		  //return ResponseEntity.status(HttpStatus.OK).body("Bank account email updated successfully");
	}

	@Override
	public BigDecimal getUserCurrencyAmount(String email, String currencyFrom) {
		CryptoWalletModel cryptoWallet = walletRepo.findByEmail(email);
		if(cryptoWallet == null) {
			return null;
		}
		
		List<CryptoValuesModel> values = cryptoWallet.getValues();
		
		for (CryptoValuesModel accountCurrency : values) {
			if (accountCurrency.getCrypto().equals(currencyFrom)) 
			{
				return accountCurrency.getAmount();
			}
		}
		return null;
	}
	
	private CryptoWalletDto convertToDto(CryptoWalletModel model) {
		CryptoWalletDto dto = new CryptoWalletDto();
	    dto.setEmail(model.getEmail());

	    if (model.getValues() != null) {
	        List<CryptoValuesDto> cryptoValuesDtos = model.getValues().stream()
	                .map(cryptoValuesModel -> {
	                	CryptoValuesDto cryptoValuesDto = new CryptoValuesDto();
	                	cryptoValuesDto.setCrypto(cryptoValuesModel.getCrypto());
	                	cryptoValuesDto.setAmount(cryptoValuesModel.getAmount());
	                    return cryptoValuesDto;
	                })
	                .collect(Collectors.toList());
	        dto.setValues(cryptoValuesDtos);
	    }

	    return dto;
	}
	
	private List<CryptoValuesModel> createInitialValues(CryptoWalletModel wallet) {
	    List<CryptoValuesModel> values = new ArrayList<>();
	    values.add(new CryptoValuesModel("BTC", BigDecimal.ZERO));
	    values.add(new CryptoValuesModel("ETH", BigDecimal.ZERO));
	    values.add(new CryptoValuesModel("LTC", BigDecimal.ZERO));
	    for (CryptoValuesModel value : values) {
	    	value.setCryptoWallet(wallet);
	    }
	    return values;
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
