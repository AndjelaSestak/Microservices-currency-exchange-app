package usersService;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.util.encoders.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.CryptoWalletDto;
import api.dtos.UserDto;
import api.proxies.BankAccountProxy;
import api.proxies.CryptoWalletProxy;
import api.services.UsersService;

@RestController
public class UserServiceImpl implements UsersService {

	@Autowired
	private UserRepository repo;

	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;

	@Override
	public List<UserDto> getUsers() {
		List<UserModel> models = repo.findAll();
		List<UserDto> dtos = new ArrayList<UserDto>();
		for (UserModel model : models) {
			dtos.add(convertModelToDto(model));
		}
		return dtos;
	}

	// KREIRANJE NOVOG KORISNIKA
	@Override
	public ResponseEntity<?> createUser(UserDto dto, @RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);

		UserModel user = convertDtoToModel(dto);

		try {
			switch (role) {
			case "ADMIN":
				// Admin moze da dodaje i azurira samo korisnike sa ulogom user
				if (!"USER".equals(dto.getRole())) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body("Admin can only create users with role 'USER'.");
					// throw new ServiceUnavailableException("Admin can only create users with role
					// 'USER'.");
				}

				// Provera da li user vec postoji
				if (repo.findByEmail(dto.getEmail()) != null) {
					String errorMessage = "User with username " + user.getEmail() + " already exists.";
					return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
					// throw new NoDataFoundException("User with ID " + user.getId() + " already
					// exists.");
				}

				// Cuvam usera u bazi
				UserModel createdUser = repo.save(user);

				// Automatsko kreiranje bankovnog racuna pri kreiranju korisnika
				BankAccountDto bankAccountDto = new BankAccountDto();
				bankAccountDto.setEmail(dto.getEmail());
				ResponseEntity<?> bankAccountResponse = bankAccountProxy.createBankAccount(bankAccountDto);
				if (!bankAccountResponse.getStatusCode().is2xxSuccessful()) {
					repo.delete(createdUser); // Rollback user creation if bank account creation fails
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body("Failed to create bank account for user.");
					// throw new ServiceUnavailableException("Failed to create bank account for
					// user.");
				}
				
				  //Automatko kreiranje crypto novcanika pri kreiranju korisnika
				CryptoWalletDto cryptoWalletDto = new CryptoWalletDto();
                cryptoWalletDto.setEmail(dto.getEmail());
                ResponseEntity<?> cryptoWalletResponse = cryptoWalletProxy.createWallet(cryptoWalletDto);
                if (!cryptoWalletResponse.getStatusCode().is2xxSuccessful()) {
                    repo.delete(createdUser); // Rollback user creation if crypto wallet creation fails
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create crypto wallet for user.");
                    //throw new ServiceUnavailableException("Failed to create crypto wallet for user.");
                }
				 

				return ResponseEntity.ok(createdUser);

			case "OWNER":
				// Owner moze da dodaje, azurira i brise sve korisnike
				// Ako je korisnik ulogovan kao owner, to znaci da ne mozemo da kreiramo jos
				// jednog ownera
				if ("OWNER".equals(user.getRole())) {
					return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
					// throw new NoDataFoundException("A user with role 'OWNER' already exists.");
				}

				UserModel checkUser = repo.findByEmail(dto.getEmail());

				// Provera da li user postoji
				if (checkUser != null) {

					if (checkUser.getRole().equals("USER")) {
						String errorMessage = "User with username " + user.getEmail() + " already exists.";
						return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
						// throw new NoDataFoundException("User with ID " + user.getId() + " already
						// exists.");
					}
					if (checkUser.getRole().equals("ADMIN")) {
						String errorMessage = "Admin with username " + user.getEmail() + " already exists.";
						return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
						// throw new NoDataFoundException("User with ID " + user.getId() + " already
						// exists.");
					}
				}

				// Cuvam usera u bazi
				createdUser = repo.save(user);
				// AUTOMATSKO kreiranje racuna ako je owner kreirao USER
				if (createdUser.getRole().equals("USER")) {
					BankAccountDto bankAccountDto1 = new BankAccountDto();
					bankAccountDto1.setEmail(dto.getEmail());
					ResponseEntity<?> bankAccountResponse1 = bankAccountProxy.createBankAccount(bankAccountDto1);
					if (!bankAccountResponse1.getStatusCode().is2xxSuccessful()) {
						repo.delete(createdUser); // Rollback user creation if bank account creation fails
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.body("Failed to create bank account for user.");
						// throw new ServiceUnavailableException("Failed to create bank account for
						// user.");
					}
					
					 //Automatko kreiranje crypto novcanika pri kreiranju korisnika ako je owner kreirao user
					CryptoWalletDto cryptoWalletDto1 = new CryptoWalletDto();
	                cryptoWalletDto1.setEmail(dto.getEmail());
	                ResponseEntity<?> cryptoWalletResponse1 = cryptoWalletProxy.createWallet(cryptoWalletDto1);
	                if (!cryptoWalletResponse1.getStatusCode().is2xxSuccessful()) {
	                    repo.delete(createdUser); // Rollback user creation if crypto wallet creation fails
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create crypto wallet for user.");
	                    //throw new ServiceUnavailableException("Failed to create crypto wallet for user.");
	                }
				}
				return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

			default:
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role or unauthorized access.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error creating user: " + e.getMessage());
			// throw new ServiceUnavailableException("Error creating user: " +
			// e.getMessage());
		}
	}

	// BRISANJE KORISNIKA
	@Override
	public ResponseEntity<?> deleteUser(@PathVariable int id) {

		UserModel user = repo.findById(id).orElse(null);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found.");
		}

		repo.deleteById(id);

		if(user.getRole().equals("USER")){
			bankAccountProxy.deleteBankAccount(user.getEmail(), "true");
		cryptoWalletProxy.deleteWallet(user.getEmail(),"true");
		}
		
		return ResponseEntity.ok("User with ID " + id + " has been deleted.");

	}

	// AZURIRANJE KORISNIKA
	@Override
	public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UserDto dto,
			@RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);

		UserModel user = repo.findById(id).orElse(null);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found.");
		}

		String userEmail = user.getEmail();
		
		// Provera da li novi email već postoji kod drugog korisnika
		UserModel userWithNewEmail = repo.findByEmail(dto.getEmail());
		if (userWithNewEmail != null && userWithNewEmail.getId() != id) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Email " + dto.getEmail() + " is already taken by another user.");
		}

		if ("ADMIN".equals(role)) {
			if ("USER".equals(user.getRole())) {
				if ("OWNER".equals(dto.getRole()) && repo.existsByRole("OWNER")) {
					return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
				}
				//ako se menja uloga usera i vise nije user, brisemo racun i crypto wallet 
				if(!dto.getRole().equals("USER")) {
					bankAccountProxy.deleteBankAccount(user.getEmail(),"true" );
					cryptoWalletProxy.deleteWallet(user.getEmail(), "true");
				} else {
					bankAccountProxy.updateEmail(user.getEmail(), dto.getEmail());
					cryptoWalletProxy.updateEmail(user.getEmail(), dto.getEmail());
				}
				user.setEmail(dto.getEmail());
				user.setPassword(dto.getPassword());
				user.setRole(dto.getRole());
				repo.save(user);

				return ResponseEntity.ok(convertModelToDto(user));
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("Admin can only update users with role 'USER'.");
			}
		} else if ("OWNER".equals(role)) {

			if (user.getRole().equals("OWNER") && dto.getRole().equals("OWNER")) {
				// Dozvoljeno je menjati email i password
				user.setEmail(dto.getEmail());
				user.setPassword(dto.getPassword());
				repo.save(user);
				return ResponseEntity.ok(convertModelToDto(user));
			}
			// Ako OWNER menja nekog drugog korisnika u OWNER
			else if ("OWNER".equals(dto.getRole()) && !user.getRole().equals("OWNER") && repo.existsByRole("OWNER")) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
			} else {
				if ("USER".equals(user.getRole())) {
					//ako menja ulogu usera u nesto drugo
					
					if(!dto.getRole().equals("USER")) {
						bankAccountProxy.deleteBankAccount(userEmail,"true" );
						cryptoWalletProxy.deleteWallet(userEmail, "true");
					} else {
						bankAccountProxy.updateEmail(userEmail, dto.getEmail());
						cryptoWalletProxy.updateEmail(userEmail, dto.getEmail());
					}
				}
				if ("ADMIN".equals(user.getRole())) {
					//ako menja ulogu admina u usera 
					if(dto.getRole().equals("USER")) {
						//kreira se bankovni racun i crypto wallet 
						BankAccountDto bankAccountDto1 = new BankAccountDto();
						bankAccountDto1.setEmail(dto.getEmail());
						ResponseEntity<?> bankAccountResponse1 = bankAccountProxy.createBankAccount(bankAccountDto1);
						if (!bankAccountResponse1.getStatusCode().is2xxSuccessful()) {
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
									.body("Failed to create bank account for user.");
						}
						 //Automatko kreiranje crypto novcanika pri kreiranju korisnika 
						CryptoWalletDto cryptoWalletDto1 = new CryptoWalletDto();
		                cryptoWalletDto1.setEmail(dto.getEmail());
		                ResponseEntity<?> cryptoWalletResponse1 = cryptoWalletProxy.createWallet(cryptoWalletDto1);
		                if (!cryptoWalletResponse1.getStatusCode().is2xxSuccessful()) {
		                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create crypto wallet for user.");
		                }
					}
				}
				user.setEmail(dto.getEmail());
				user.setPassword(dto.getPassword());
				user.setRole(dto.getRole());
				repo.save(user);

				return ResponseEntity.ok(convertModelToDto(user));
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role.");
		}
	}

	public String extractRoleFromAuthorizationHeader(String authorizationHeader) {
		try {
			String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
			byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());// string se dekodira nazad u originalni
																				// oblik (username:password
			String decodedCredentials = new String(decodedBytes); // "user@example.com:password"
			String[] credentials = decodedCredentials.split(":");
			String email = credentials[0]; // prvo se unosi email kao username korisnika
			UserModel user = repo.findByEmail(email);
			if (user != null) {
				return user.getRole();
			} else {
				System.out.println("User not found for email: " + email);
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error extracting role: " + e.getMessage());
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////

	@Override
	public UserDto getUserByEmail(String email) {
		if (repo.findByEmail(email) != null) {
			return convertModelToDto(repo.findByEmail(email));
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<?> createAdmin(UserDto dto) {
		if (repo.findByEmail(dto.getEmail()) == null) {
			dto.setRole("ADMIN");
			UserModel model = convertDtoToModel(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(model));
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin with passed email already exist");

		}
	}

	/*
	 * @Override public ResponseEntity<?> createUser(UserDto dto) {
	 * if(repo.findByEmail(dto.getEmail()) == null) { dto.setRole("USER"); UserModel
	 * model= convertDtoToModel(dto); return
	 * ResponseEntity.status(HttpStatus.CREATED).body(repo.save(model)); }else {
	 * return ResponseEntity.status(HttpStatus.CONFLICT).
	 * body("User with passed email already exist");
	 * 
	 * } }
	 */

	/*
	 * @Override public ResponseEntity<?> updateUser(UserDto dto) {
	 * if(repo.findByEmail(dto.getEmail()) != null) {
	 * 
	 * repo.updateUser(dto.getEmail(), dto.getPassword(), dto.getRole()); return
	 * ResponseEntity.status(HttpStatus.OK).body(dto); }else { return
	 * ResponseEntity.status(HttpStatus.CONFLICT).
	 * body("User with passed email doesnt exist");
	 * 
	 * } }
	 */

	public UserDto convertModelToDto(UserModel model) {
		return new UserDto(model.getEmail(), model.getPassword(), model.getRole());

	}

	public UserModel convertDtoToModel(UserDto dto) {
		return new UserModel(dto.getEmail(), dto.getPassword(), dto.getRole());

	}

}