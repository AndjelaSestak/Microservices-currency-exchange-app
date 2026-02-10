package api.dtos;

import java.math.BigDecimal;

public class CryptoConversionDto {

	private CryptoExchangeDto exchange;
	private BigDecimal quantity;
	
	
	public CryptoConversionDto() {
		
	}

	public CryptoConversionDto(CryptoExchangeDto exchange, BigDecimal quantity) {
		super();
		this.exchange = exchange;
		this.quantity = quantity;
	}

	public CryptoExchangeDto getExchange() {
		return exchange;
	}


	public void setExchange(CryptoExchangeDto exchange) {
		this.exchange = exchange;
	}


	public BigDecimal getQuantity() {
		return quantity;
	}


	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	
}
