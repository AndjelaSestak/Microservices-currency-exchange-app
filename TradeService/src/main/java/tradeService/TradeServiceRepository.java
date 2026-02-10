package tradeService;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeServiceRepository extends JpaRepository<TradeServiceModel,Long>{

	TradeServiceModel findByFromAndToIgnoreCase(String from, String to);

}