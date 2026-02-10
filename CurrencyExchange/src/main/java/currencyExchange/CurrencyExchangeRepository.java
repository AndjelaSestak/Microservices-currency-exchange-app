package currencyExchange;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchangeModel, Integer>{
	
	CurrencyExchangeModel findByFromAndTo(String from, String to);

	@Query(value = """
			SELECT DISTINCT currency_from AS currency from currency_exchange
			UNION
			SELECT DISTINCT currency_to AS currency from currency_exchange
			"""
			,nativeQuery = true)
	List<String> findAllDistinctCurrencies();
}
