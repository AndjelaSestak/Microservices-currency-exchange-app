# Service-Oriented Architecture of Systems (Project Assignment)

## Technologies Used for Project Development
- Maven
- Java programming language
- Docker
- H2 in-memory database

## The application consists of the following microservices and their roles:

### 1. Naming Server
Microservice that serves as the Eureka server; all microservices within the application must register with this server.  
Recommended port on which the microservice runs: **8761**

### 2. Users Service
Microservice that communicates with the H2 database where application user data is stored. For each user, the following is recorded: ID, email address, password, and role. Each user can have only one of the following roles: OWNER, ADMIN, USER.  
The permissions for these roles are specified within each microservice.  
This microservice should also provide functionality for adding new users as well as updating and deleting existing users.

**Authorization:**
- OWNER can add, update, and delete all users
- ADMIN can add and update users with the USER role
- USER has no access to this service

Only one user with the OWNER role can exist in the system.  
Recommended port on which the microservice runs: **8770**

### 3. Currency Exchange
Microservice that communicates with the H2 database containing fiat currency exchange rates. The database must include exchange rates for the following currencies: EUR (euro), USD (US dollar), GBP (British pound), CHF (Swiss franc), and RSD (Serbian dinar). Exchange rates for every currency pair are mandatory. Rate values can be arbitrary.

**Authorization:**
- Users with any role can access this service

Recommended port on which the microservice runs: **8000**

### 4. Currency Conversion
Microservice that serves as the endpoint for user requests for fiat currency exchange. Upon a user request, this microservice identifies the amount of currency to exchange and checks the user's funds on the corresponding bank account – if the user has sufficient funds, the exchange is performed based on the current rate obtained by communication with the currency-exchange microservice.  
The result of a successful execution is the display of the corresponding user's bank account balance after the exchange, along with a String describing the transaction result (e.g., "Successfully exchanged EUR: 100 for RSD: 11700").

**Authorization:**
- OWNER cannot access this service
- ADMIN cannot access this service
- USER is authorized to use this service

Recommended port on which the microservice runs: **8100**

### 5. Bank Account
Microservice that communicates with the H2 database containing user bank account data – each account holds information about the amount of each fiat currency the account owner possesses. Every account must contain an ID, the amount of each fiat currency defined for the currency exchange microservice, and an email address linked to the individual account.  
Bank accounts are allowed only for users with the USER role. The email address must be unique and must match the user's email from the database. Each user can have only one bank account.  
When a new user with the USER role is added in the Users service, a bank account with zero balance for all currencies is automatically created for them. Deleting a user in the Users service must automatically delete the corresponding user's account.  
This microservice must provide functionality for viewing, adding, updating, and deleting accounts.

**Authorization:**
- OWNER is not authorized to use this service
- ADMIN can add, update, and view all bank accounts
- USER can view only their own account

- If during application usage a USER with a bank account is deleted, the corresponding bank account with that email is also deleted.

Recommended port on which the microservice runs: **8200**

### 6. Crypto Wallet
Microservice that communicates with the H2 database containing user crypto wallet data – each wallet holds information about the amount of each cryptocurrency the wallet owner possesses. Every wallet must contain an ID, the amount of each cryptocurrency defined for the crypto exchange microservice, and an email address linked to the individual wallet.  
Crypto wallets are allowed only for users with the USER role. The wallet email must be unique and must match the user's email from the database. Each user can have only one wallet.  
When a new user with the USER role is added in the Users service, a wallet with zero balance for all cryptocurrencies is automatically created for them. Deleting a user in the Users service must automatically delete the corresponding user's wallet.  
This microservice must provide functionality for viewing, adding, updating, and deleting wallets.

**Authorization:**
- OWNER is not authorized to use this service
- ADMIN can add, update, and view all user wallets
- USER can view only their own wallet

Recommended port on which the microservice runs: **8300**

### 7. Crypto Exchange
Microservice that communicates with the H2 database containing exchange rates between cryptocurrencies. The database must contain exchange rates for 3 cryptocurrencies of free choice.

**Authorization:**
- Users with any role can access this service

Recommended port on which the microservice runs: **8400**

### 8. Crypto Conversion
Microservice that serves as the endpoint for user requests for cryptocurrency exchange. Upon a user request, this microservice identifies the amount of currency to exchange and checks the user's funds in the corresponding crypto wallet – if the user has sufficient funds, the exchange is performed based on the current rate obtained by communication with the crypto-exchange microservice.  
The result of a successful execution is the display of the corresponding user's wallet balance after the exchange, along with a String describing the transaction result (e.g., "Successfully exchanged BTC: 1 for EUR: 40000").

**Authorization:**
- OWNER cannot access this service
- ADMIN cannot access this service
- USER is authorized to use this service

Recommended port on which the microservice runs: **8500**

### 9. Trade Service
Microservice that provides functionality for exchanging regular (fiat) and cryptocurrency.

**Description of functionality:**

- **Fiat to Crypto exchange** – The corresponding amount of fiat currency is deducted from the bank account, after which, based on the exchange rate between fiat and crypto currency, the desired crypto amount is increased in the wallet. Cryptocurrencies can be purchased only with USD (US dollar) and EUR (euro). If the request contains another currency, the provided currency is first exchanged to dollar or euro, and then the mentioned exchange to crypto is performed.
- **Crypto to Fiat exchange** – The corresponding amount of crypto currency is deducted from the wallet, after which, based on the exchange rate between fiat and crypto currency, the desired fiat amount is added to the bank account. Cryptocurrencies can be exchanged only for USD (US dollar) and EUR (euro). If the request contains another fiat currency, the provided crypto is first exchanged to dollar or euro, and then the mentioned exchange to the desired fiat currency is performed.

For successful operation of this microservice, a database containing crypto-to-USD/EUR and reverse exchange rates is required.  
The result of a successful execution is the display of:
- User's bank account balance if crypto is converted to fiat, along with a String representing the transaction report (similar to crypto-conversion and currency-conversion microservices).
- User's crypto wallet balance if fiat is converted to crypto, along with a String representing the transaction report (similar to crypto-conversion and currency-conversion microservices).

**Authorization:**
- OWNER cannot access this service
- ADMIN cannot access this service
- USER is authorized to use this service

Recommended port on which the microservice runs: **8600**

### 10. API Gateway
Microservice that serves as the entry point of the application; all user requests are sent to it.  
API Gateway must run on port **8765**.

User requests to specific microservices must have the following form:
- localhost:8765/currency-conversion?from=X&to=Y&quantity=Q  
  – Request for fiat currency exchange, Q represents the amount of X currency to be exchanged for Y currency
- localhost:8765/crypto-conversion/?from=X&to=Y&quantity=Q  
  – Request for cryptocurrency exchange, Q represents the amount of X currency to be exchanged for Y currency
- localhost:8765/trade-service?from=X&to=Y&quantity=Q  
  – Request for currency exchange, Q represents the amount of X currency to be exchanged for Y currency.

## Docker
- Docker images have been created for all microservices and pushed to the repository belonging to the user account on Docker Hub.
- A `docker-compose.yaml` file has been created through which the complete application can be started in Docker.

**Used commands:**  
Example for Naming Server (navigate to the microservice folder)

- docker build -t image naming-server-1.0.0.jar .
- docker tag naming-server-1.0.0.jar:latest brankazaric/naming-server:latest
- docker login
- docker push brankazaric/naming-server:latest

## Credentials

ADMIN: admin@uns.ac.rs || password  
USER: user@uns.ac.rs || password  
OWNER: owner@uns.ac.rs || password

## API Request Paths

1. Users service  
   http://localhost:8765/users (GET)  
   http://localhost:8765/users/newUser (POST)  
   http://localhost:8765/users/{id} (PUT, DELETE)

2. Currency Exchange  
   http://localhost:8765/currency-exchange?from=EUR&to=RSD

3. Currency Conversion  
   http://localhost:8765/currency-conversion-feign?from=EUR&to=RSD&quantity=10

4. Bank Account  
   USER views only their own account  
   http://localhost:8765/bank-account/user  
   ADMIN views all bank accounts  
   http://localhost:8765/bank-accounts  
   ADMIN views one account by email  
   http://localhost:8765/bank-accounts/{email}

5. Crypto Wallet  
   ADMIN views all crypto wallets  
   http://localhost:8765/crypto-wallets  
   Logged-in USER views their own crypto wallet  
   http://localhost:8765/crypto-wallet/user  
   ADMIN views one wallet by email  
   http://localhost:8765/crypto-wallets/{email}

6. Crypto Exchange  
   http://localhost:8765/crypto-exchange?from=BTC&to=LTC

7. Crypto Conversion  
   http://localhost:8765/crypto-conversion-feign?from=BTC&to=LTC&quantity=100

8. Trade Service  
   http://localhost:8765/trade-service?from=EUR&to=BTC&quantity=10
