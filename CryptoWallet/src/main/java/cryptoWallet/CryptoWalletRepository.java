package cryptoWallet;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CryptoWalletRepository extends JpaRepository<CryptoWalletModel, Integer> {

	CryptoWalletModel findByEmail(String email);
}
