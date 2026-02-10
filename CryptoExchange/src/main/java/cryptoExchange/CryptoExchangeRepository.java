package cryptoExchange;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CryptoExchangeRepository extends JpaRepository<CryptoExchangeModel, Integer>{

	CryptoExchangeModel findByFromAndTo(String from, String to);
	
	@Query(value = """
			SELECT DISTINCT crypto_from AS currency from crypto_exchange
			UNION
			SELECT DISTINCT crypto_to AS currency from crypto_exchange
			"""
			,nativeQuery = true)
	List<String> findAllDistinctCurrencies();
	
}