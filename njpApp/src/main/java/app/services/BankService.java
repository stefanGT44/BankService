package app.services;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import app.BNK.Bank;
import app.BNK.BankBranch;
import app.CORE.Message;
import app.CORE.Response;
import app.CORE.Severity;
import app.repositories.BankBranchRepo;
import app.repositories.BankRepo;
import app.utils.LocalDateTimeSerializer;
import app.utils.UtilsMethods;

@Service
public class BankService {

	private BankRepo bankRepo;
	private BankBranchRepo bankBranchRepo;

	public BankService(BankRepo bankRepo, BankBranchRepo bankBranchRepo) {
		this.bankRepo = bankRepo;
		this.bankBranchRepo = bankBranchRepo;
		UtilsMethods.preloadDatabase(bankRepo, bankBranchRepo);
	}

	public Response add(Bank bank) {
		Response resp = new Response();

		if (!UtilsMethods.checkBankFields(bank, resp))
			return resp;

		if (bank.getSwiftCode().length() != 8 && bank.getSwiftCode().length() != 11) {
			resp.addMessage(
					new Message("invalid", "[Swift code] must have a length of 8 or 11 characters.", Severity.ERROR));
		} else {
			List<Bank> lista = bankRepo.findAllBySwiftCode(bank.getSwiftCode());
			for (Bank bnk : lista) {
				if (UtilsMethods.overlapingPeriods(bank, bnk)) {
					if (bnk.getValidTo() != null)
						resp.addMessage(new Message(
								"invalid", "[Swift code] " + bank.getSwiftCode() + " is already being used "
										+ "in between " + bnk.getValidFrom() + " and " + bnk.getValidTo(),
								Severity.ERROR));
					else
						resp.addMessage(new Message("invalid", "[Swift code]: " + bank.getSwiftCode()
								+ " is already being used " + " from " + bnk.getValidFrom() + ".", Severity.ERROR));
					break;
				}
			}
			List<BankBranch> lista2 = bankBranchRepo.findAllBySwiftCode(bank.getSwiftCode());
			if (lista2.size() > 0)
				resp.addMessage(new Message("invalid",
						"[Swift code] " + bank.getSwiftCode() + " is already being used by a bank branch.",
						Severity.ERROR));
		}

		// bank code mora biti jedinstven u odredjenom vremenskom intervalu, ne sme da
		// ima preklapanja
		List<Bank> lista = bankRepo.findAllByBankCode(bank.getBankCode());
		for (Bank bnk : lista) {
			if (UtilsMethods.overlapingPeriods(bank, bnk)) {
				if (bnk.getValidTo() != null)
					resp.addMessage(
							new Message(
									"invalid", "[Bank code]: " + bank.getBankCode() + " is already being used "
											+ " in between " + bnk.getValidFrom() + " and " + bnk.getValidTo(),
									Severity.ERROR));
				else
					resp.addMessage(new Message("invalid", "[Bank code]: " + bank.getBankCode()
							+ " is already being used " + " from " + bnk.getValidFrom() + ".", Severity.ERROR));
				break;
			}
		}

		// provera da li je valid from
		if (!UtilsMethods.CheckDate(bank.getValidFrom())) {
			resp.addMessage(new Message("invalid", "[Valid from] cannot be before today.", Severity.ERROR));
		}

		// provera da li je valid to posle valid from
		if (bank.getValidTo() != null) {
			if (!bank.getValidTo().isAfter(bank.getValidFrom())) {
				resp.addMessage(new Message("invalid", "[Valid to] must be after [Valid from]", Severity.ERROR));
			} else
				// setovati da valid to bude do kraja specificiranog datuma
				bank.setValidTo(UtilsMethods.setValidTo(bank.getValidTo()));
		}

		// sve provere su prosle
		if (!resp.containsError()) {
			bankRepo.save(bank);
			System.out.println("Bank successfully added to database.");
			resp.addMessage(new Message("added", "Bank successfully added to database.", Severity.INFO));
		}

		return resp;
	}

	public Response search(HttpServletRequest request) {
		Response resp = new Response();
		List<Bank> banks;
		if (request.getParameterMap().containsKey("valid_on") && request.getParameterMap().containsKey("identifier")) {

			String validOn = request.getParameter("valid_on");
			LocalDateTime date = UtilsMethods.parseDate(validOn);

			if (date == null) {
				resp.addMessage(new Message("format error",
						"Date format error. (Excpected format: yyyy-MM-dd HH:mm:ss)", Severity.ERROR));
				return resp;
			}
			String identifier = request.getParameter("identifier");
			identifier = "%" + identifier + "%";
			banks = bankRepo.fullSearch(identifier, date);

		} else if (request.getParameterMap().containsKey("valid_on")) {

			String validOn = request.getParameter("valid_on");
			System.out.println("validOn = " + validOn);
			LocalDateTime date = UtilsMethods.parseDate(validOn);

			if (date == null) {
				resp.addMessage(new Message("format error",
						"Date format error. (Excpected format: yyyy-MM-dd HH:mm:ss)", Severity.ERROR));
				return resp;
			}
			banks = bankRepo.searchByValidDate(date);

		} else if (request.getParameterMap().containsKey("identifier")) {
			String identifier = request.getParameter("identifier");
			identifier = "%" + identifier + "%";
			banks = bankRepo.searchByIdentifier(identifier);
		} else {
			banks = bankRepo.findAll();
		}

		if (banks == null || banks.isEmpty())
			resp.addMessage(new Message("search-empty", "No matching banks found.", Severity.INFO));
		else {
			Gson gson = UtilsMethods.createGson();
			resp.setData(gson.toJson(banks));
			resp.addMessage(new Message("search", "Sent search results", Severity.INFO));
		}

		return resp;
	}

	public Response update(Bank bank) {
		Response resp = new Response();

		if (!UtilsMethods.checkUpdateFields(bank, resp))
			return resp;

		Bank original = bankRepo.findById(bank.getId());
		
		if (original == null) {
			resp.addMessage(new Message("no selection", "Specified bank does not exist.", Severity.ERROR));
			return resp;
		}
		
		//ako nema promena
		if (!UtilsMethods.changed(bank, original)) {
			resp.addMessage(new Message("no changes", "There are no changes to the bank.", Severity.ERROR));
			return resp;
		}
		
		//ako je jedina promena validTo koji ce se svesti na vec postojecu vretnost - te nema promena
		if (bank.getValidTo() != null) {
			Bank test = new Bank();
			test.setName(bank.getName());
			test.setValidFrom(bank.getValidFrom());
			test.setValidTo(UtilsMethods.setValidTo(bank.getValidTo()));
			if (!UtilsMethods.changed(test, original)) {
				resp.addMessage(new Message("no changes", "No changes. Entered value for [Valid to] would be set to already existing value,"
						+ " no update required.", Severity.ERROR));
				return resp;
			}
		}

		Date today = new Date();

		boolean validFrom = true, validTo = true, validFromChanged = false, validToChanged = false;
		
		validFromChanged = !bank.getValidFrom().equals(original.getValidFrom());
		validToChanged = UtilsMethods.changedValidTo(bank, original);
		
		//ako je TRENUTNA vrednost valid_from polja u proslisti
		if (validFromChanged && UtilsMethods.localDateTimeToDate(original.getValidFrom()).before(today)) {
			resp.addMessage(new Message("invalid", "Field [Valid from] cannot be updated if its value is in the past.", Severity.ERROR));
			validFrom = false;
		}

		//ako je TRENUTNA vrednost valid_to polja u proslosti
		if (validToChanged && original.getValidTo() != null && UtilsMethods.localDateTimeToDate(original.getValidTo()).before(today)) {
			resp.addMessage(new Message("invalid", "Field [Valid to] cannot be updated if its value is in the past.", Severity.ERROR));
			validTo = false;
		}

		// provera da li je UNETA vrednost za valid_from u proslosti
		if (validFromChanged && !UtilsMethods.CheckDate(bank.getValidFrom())) {
			resp.addMessage(new Message("invalid", "Submitted [Valid from] value cannot be before today.", Severity.ERROR));
			validFrom = false;
		}

		// provera da li je UNETA valid_to vrednost pre UNETE valid_from vrednosti
		if (bank.getValidTo() != null) {
			if (!bank.getValidTo().isAfter(bank.getValidFrom())) {
				resp.addMessage(new Message("invalid", "Submitted [Valid to] value must be after [Valid from]", Severity.ERROR));
				validTo = false;
			} else
				// setovanje UNETE valid_to vrednosti na kraj dana
				bank.setValidTo(UtilsMethods.setValidTo(bank.getValidTo()));
		}

		// ako je valid_from promenjen i prosao testove ILI ako je valid_to promenjen i prosao testove
		if ((validFromChanged && validFrom) || (validToChanged && validTo)) {
			checkOverlappingBankCode(resp, bank, original.getBankCode());
			checkOverlappingSwiftCode(resp, bank, original.getSwiftCode());
		}
		
		//upisivanje u bazu
		if (!resp.containsError()) {
			bankRepo.save(bank);
			System.out.println("Bank successfully updated.");
			resp.addMessage(new Message("updated", "Bank successfully updated.", Severity.INFO));
		}

		return resp;
	}

	private boolean checkOverlappingBankCode(Response resp, Bank bank, String bankCode) {
		List<Bank> lista = bankRepo.findAllByBankCode(bankCode);
		for (Bank bnk : lista) {
			if (bnk.getId() == bank.getId())
				continue;
			if (UtilsMethods.overlapingPeriods(bank, bnk)) {
				if (bnk.getValidTo() != null)
					resp.addMessage(
							new Message(
									"invalid", "[Bank code]: " + bankCode + " is already being used "
											+ " in between " + bnk.getValidFrom() + " and " + bnk.getValidTo(),
									Severity.ERROR));
				else
					resp.addMessage(new Message("invalid", "[Bank code]: " + bankCode
							+ " is already being used " + " from " + bnk.getValidFrom() + ".", Severity.ERROR));
				return true;
			}
		}
		return false;
	}

	private boolean checkOverlappingSwiftCode(Response resp, Bank bank, String swiftCode) {
		List<Bank> lista = bankRepo.findAllBySwiftCode(swiftCode);
		for (Bank bnk : lista) {
			if (bnk.getId() == bank.getId())
				continue;
			if (UtilsMethods.overlapingPeriods(bank, bnk)) {
				if (bnk.getValidTo() != null)
					resp.addMessage(
							new Message(
									"invalid", "[Swift code] " + swiftCode + " is already being used "
											+ "in between " + bnk.getValidFrom() + " and " + bnk.getValidTo(),
									Severity.ERROR));
				else
					resp.addMessage(new Message("invalid", "[Swift code]: " + swiftCode
							+ " is already being used " + " from " + bnk.getValidFrom() + ".", Severity.ERROR));
				return true;
			}
		}
		return false;
	}
	
	public Response delete(Bank bank) {
		Response resp = new Response();
		
		if (bank.getId() <= 0) {
			resp.addMessage(new Message("invalid", "No bank selected.", Severity.ERROR));
			return resp;
		}
		
		//za svaki slucaj
		bank = bankRepo.findById(bank.getId());
		
		Date now = new Date();
		
		if (bank.getValidTo() != null && (!UtilsMethods.localDateTimeToDate(bank.getValidTo()).after(now) || bank.getValidTo().isEqual(bank.getValidFrom().plusSeconds(1)))) {
			resp.addMessage(new Message("invalid", "Bank already invalid.", Severity.ERROR));
			return resp;
		}
		
		if (UtilsMethods.localDateTimeToDate(bank.getValidFrom()).after(now)) {
			//setuj da valid to bude 1s nakon validFrom
			bank.setValidTo(bank.getValidFrom().plusSeconds(1));
		} else {
			//valid to setovati da bude sadasnji trenutak
			bank.setValidTo(UtilsMethods.dateToLocalDateTime(now));
		}
		
		//pre ovoga moraju da se invalidiraju sve produznice
		List<BankBranch> branches = bankBranchRepo.findAllByBank(bank);
		
		boolean branchDeleteTest = false;
		
		for (BankBranch branch:branches) {
			if (branch.getValidTo() == null || UtilsMethods.localDateTimeToDate(branch.getValidTo()).after(now)) {
				branchDeleteTest = true;
				if (UtilsMethods.localDateTimeToDate(branch.getValidFrom()).after(now)) {
					branch.setValidTo(branch.getValidFrom().plusSeconds(1));
				} else {
					branch.setValidTo(UtilsMethods.dateToLocalDateTime(now));
				}
				bankBranchRepo.save(branch);
				System.out.println("Branch[" + branch.getName() + "] successfully invalidated");
			}
		}
		
		bankRepo.save(bank);
		System.out.println("Bank [" + bank.getName() + "] successfully invalidated.");
		if (!branchDeleteTest)
			resp.addMessage(new Message("success", "Bank [" + bank.getName() + "] successfully invalidated.", Severity.INFO));
		else
			resp.addMessage(new Message("success", "Bank [" + bank.getName() + "] and all of its valid branches successfully invalidated.", Severity.INFO));
		return resp;
	}
	
	public Response getBank(HttpServletRequest request) {
		Response resp = new Response();
		if (!request.getParameterMap().containsKey("id")) {
			resp.addMessage(new Message("invalid", "Bad request. Please provide a bank ID.", Severity.ERROR));
			return resp;
		}
		
		long id = Long.parseLong(request.getParameter("id"));
		Bank bank = bankRepo.findById(id);
		
		if (bank == null) {
			resp.addMessage(new Message("invalid", "Bank with the id = " + id + " does not exist.", Severity.ERROR));
			return resp;
		}
		
		Gson gson = UtilsMethods.createGson();
		resp.setData(gson.toJson(bank));
		resp.addMessage(new Message("success", "Sent selected bank.", Severity.INFO));
		return resp;
	}

}
