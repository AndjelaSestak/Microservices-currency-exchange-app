package util.exceptions;

import java.util.List;

public class NoDataFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<String> currencies;
	
	public NoDataFoundException() {
		
	}
	
	public NoDataFoundException(String message) {
		super(message);
	}

	
	public NoDataFoundException(String message, List<String> currencies) {
		super(message);
		this.currencies=currencies;
	}

	public List<String> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(List<String> currencies) {
		this.currencies = currencies;
	}
	
	

}
