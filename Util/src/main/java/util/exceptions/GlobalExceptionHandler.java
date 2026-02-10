package util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<?> handleHttpClientException(HttpClientErrorException ex){
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ExceptionModel(ex.getMessage(), "Requested currencies not found", HttpStatus.NOT_FOUND));
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingRequestParam(MissingServletRequestParameterException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ExceptionModel(ex.getMessage(),"Make sure to enter all request parameters",HttpStatus.BAD_REQUEST));
	}
	
	@ExceptionHandler(NoDataFoundException.class)
	public ResponseEntity<?> handleInvalidExchangeRate(NoDataFoundException ex){
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ExceptionModel(ex.getMessage()
						, String.format("Please make sure to enter currency from the list: %s", ex.getCurrencies()),
						HttpStatus.NOT_FOUND));
	}
	
	@ExceptionHandler(CurrencyDoesntExistException.class)
	public ResponseEntity<?> handleInvalidCurrency(CurrencyDoesntExistException ex){
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ExceptionModel(ex.getMessage()
						, String.format("Please make sure to enter currency from the list: %s", ex.getCurrencies()),
						HttpStatus.NOT_FOUND));
	}
	
	@ExceptionHandler(InvalidQuantityException.class)
	public ResponseEntity<?> handleInvalidQuantity(InvalidQuantityException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ExceptionModel(ex.getMessage(),"You can exchange up to 300 units of a single currency",
						HttpStatus.BAD_REQUEST));
	}
	
//	public String fineTuneMessage(String message) {
//		String[] partsOfTheMessage = message.split("\"");
//		return partsOfTheMessage[4];
//	}
}
