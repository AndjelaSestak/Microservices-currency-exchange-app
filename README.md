Kredencijali 

  ADMIN: admin@uns.ac.rs || password
  
  USER: user@uns.ac.rs || password
  
  OWNER:  owner@uns.ac.rs || password

  # Putanje zahteva 
  
  1. Users service 
  
    http://localhost:8765/users (GET)
    
    http://localhost:8765/users/newUser (POST)
    
    http://localhost:8765/users/{id} (PUT, DELETE)
  
  2. Currency Exchange
  
    http://localhost:8765/currency-exchange?from=EUR&to=RSD
  
  3. Currency Conversion
  
    http://localhost:8765/currency-conversion-feign?from=EUR&to=RSD&quantity=10
  
  4. Bank Account
  
  USER pregleda samo svoj racun

    http://localhost:8765/bank-account/user

  ADMIN pregleda sve bankovne racune
  
    http://localhost:8765/bank-accounts

  ADMIN pregleda jedan racun po email-u 

    http://localhost:8765/bank-accounts/{email}
  
  5. Crypto Wallet

  ADMIN pregleda sve crypto novcanike
  
    http://localhost:8765/crypto-wallets

  Ulogovani USER pregleda svoj crypto novcanik

    http://localhost:8765/crypto-wallet/user

  ADMIN pregleda jedan novcanik po email-u

    http://localhost:8765/crypto-wallets/{email}
  
  6. Crypto Exchange
  
    http://localhost:8765/crypto-exchange?from=BTC&to=LTC
  
  7. Crypto Conversion
  
    http://localhost:8765/crypto-conversion-feign?from=BTC&to=LTC&quantity=100
  
  8. Trade Service
  
    http://localhost:8765/trade-service?from=EUR&to=BTC&quantity=10
