package cryptoExchange;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoExchangeDto;
import api.services.CryptoExchangeService;
import util.exceptions.CurrencyDoesntExistException;
import util.exceptions.NoDataFoundException;

@RestController
public class CryptoExchangeServiceImpl implements CryptoExchangeService {

	@Autowired
	private CryptoExchangeRepository repo;

	    
	@Override 
	public ResponseEntity<?> getExchange(@RequestParam String from, @RequestParam String to) {
		String missingCurrency = null;
		List<String> validCurrencies = repo.findAllDistinctCurrencies();
		//proveriti da li from parametar odgovara nekoj valuti
		if (!isValidCurrency(from)) missingCurrency= from;
		//proveriti da li to parametar odgovara nekoj valuti
		else if (!isValidCurrency(to)) missingCurrency= to;
		//proveriti da li je missingCurrency razlicit od null i ako jeste bacanje domain exception-a
		if(missingCurrency != null) throw new 
		CurrencyDoesntExistException(String.format("Currency %s does not exist in the database", missingCurrency),
				validCurrencies);
		
		CryptoExchangeModel model = repo.findByFromAndTo(from, to);
		if (model == null) {
			throw new NoDataFoundException(String.format("Requested exchange rate [%s to %s] does not exist", from, to),
					validCurrencies);
        }
        return ResponseEntity.ok(convertModelToDto(model));
	}
	
	public CryptoExchangeDto convertModelToDto(CryptoExchangeModel model) {
        CryptoExchangeDto dto = 
                new CryptoExchangeDto(model.getFrom(), model.getTo(), model.getExchangeRate());
        return dto;
    }
	
	public boolean isValidCurrency(String currency) {
		List<String> currencies = repo.findAllDistinctCurrencies();
		for (String s:currencies) {
			if(s.equalsIgnoreCase(currency))
				return true;
		}
		return false;
	}

}
