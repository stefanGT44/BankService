package app.services;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import app.BNK.Bank;
import app.BNK.BankBranch;
import app.CORE.Message;
import app.CORE.Response;
import app.CORE.Severity;
import app.repositories.BankBranchRepo;
import app.repositories.BankRepo;
import app.utils.UtilsMethods;

@Service
public class BankBranchService {

	@Autowired
	private BankRepo bankRepo;

	@Autowired
	private BankBranchRepo bankBranchRepo;

	public Response add(BankBranch branch) {
		Response resp = new Response();

		if (!UtilsMethods.checkBranchFields(branch, resp))
			return resp;

		if (branch.getSwiftCode().length() != 8 && branch.getSwiftCode().length() != 11) {
			resp.addMessage(
					new Message("invalid", "[Swift code] must have a length of 8 or 11 characters.", Severity.ERROR));
		} else {
			List<Bank> banke = bankRepo.findAllBySwiftCodeAndIdNot(branch.getSwiftCode(), branch.getBank().getId());
			Bank bank = bankRepo.findById(branch.getBank().getId());
			if (banke.size() > 0) {
				resp.addMessage(
						new Message("invalid", "[Swift code] is already being used by another bank.", Severity.ERROR));
			}

			List<BankBranch> branches = bankBranchRepo.findAllBySwiftCodeAndBankNot(branch.getSwiftCode(), bank);
			if (branches.size() > 0) {
				resp.addMessage(new Message("invalid",
						"[Swift code] is already being used by a branch from another bank", Severity.ERROR));
			}
		}

		List<BankBranch> branches = bankBranchRepo.findAllByBranchCode(branch.getBranchCode());
		for (BankBranch b : branches) {
			if (UtilsMethods.overlapingPeriods(branch, b)) {
				if (b.getValidTo() != null)
					resp.addMessage(
							new Message("invalid",
									"[Branch code]: " + branch.getBranchCode() + " is already being used "
											+ " in between " + b.getValidFrom() + " and " + b.getValidTo(),
									Severity.ERROR));
				else
					resp.addMessage(new Message("invalid", "[Branch code]: " + branch.getBranchCode()
							+ " is already being used " + " from " + b.getValidFrom() + ".", Severity.ERROR));
				break;
			}
		}

		// provera da li je valid from
		if (!UtilsMethods.CheckDate(branch.getValidFrom())) {
			resp.addMessage(new Message("invalid", "[Valid from] cannot be before today.", Severity.ERROR));
		}

		if (branch.getValidTo() != null) {
			if (!branch.getValidTo().isAfter(branch.getValidFrom())) {
				resp.addMessage(new Message("invalid", "[Valid to] must be after [Valid from]", Severity.ERROR));
			} else
				// setovati da valid to bude do kraja specificiranog datuma
				branch.setValidTo(UtilsMethods.setValidTo(branch.getValidTo()));
		}

		if (!resp.containsError()) {
			bankBranchRepo.save(branch);
			System.out.println("Branch successfully added to database.");
			resp.addMessage(new Message("added", "Branch successfully added to database.", Severity.INFO));
		}

		return resp;
	}

	public Response search(HttpServletRequest request) {
		Response resp = new Response();
		List<BankBranch> branches;
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
			branches = bankBranchRepo.fullSearch(identifier, date);

		} else if (request.getParameterMap().containsKey("valid_on")) {

			String validOn = request.getParameter("valid_on");
			LocalDateTime date = UtilsMethods.parseDate(validOn);

			if (date == null) {
				resp.addMessage(new Message("format error",
						"Date format error. (Excpected format: yyyy-MM-dd HH:mm:ss)", Severity.ERROR));
				return resp;
			}
			branches = bankBranchRepo.searchByValidDate(date);

		} else if (request.getParameterMap().containsKey("identifier")) {
			String identifier = request.getParameter("identifier");
			identifier = "%" + identifier + "%";
			branches = bankBranchRepo.searchByIdentifier(identifier);
		} else {
			branches = bankBranchRepo.findAll();
		}

		if (branches == null || branches.isEmpty())
			resp.addMessage(new Message("search-empty", "No matching bank branches found.", Severity.INFO));
		else {

			for (BankBranch b : branches)
				System.out.println(b.getName() + " branchID: " + b.getId() + " bankID: " + b.getBank().getId());

			Gson gson = UtilsMethods.createGson();
			resp.setData(gson.toJson(branches));
			resp.addMessage(new Message("search", "Sent search results", Severity.INFO));
		}

		return resp;
	}

	public Response update(BankBranch branch) {
		Response resp = new Response();

		if (!UtilsMethods.checkBranchUpdateFields(branch, resp))
			return resp;

		BankBranch original = bankBranchRepo.findById(branch.getId());

		if (original == null) {
			resp.addMessage(new Message("no selection", "Specified bank branch does not exist.", Severity.ERROR));
			return resp;
		}

		if (!UtilsMethods.branchChanged(branch, original)) {
			resp.addMessage(new Message("no changes", "There are no changes detected.", Severity.ERROR));
			return resp;
		}

		if (branch.getValidTo() != null) {
			BankBranch test = new BankBranch();
			test.setName(branch.getName());
			test.setValidFrom(branch.getValidFrom());
			test.setValidTo(UtilsMethods.setValidTo(branch.getValidTo()));
			test.setBank(branch.getBank());
			if (!UtilsMethods.branchChanged(test, original)) {
				resp.addMessage(new Message("no changes",
						"No changes. Entered value for [Valid to] would be set to already existing value,"
								+ " no update required.",
						Severity.ERROR));
				return resp;
			}
		}

		Date today = new Date();

		boolean validFrom = true, validTo = true, validFromChanged = false, validToChanged = false;

		validFromChanged = !branch.getValidFrom().equals(original.getValidFrom());
		validToChanged = UtilsMethods.changedValidTo(branch, original);

		// ako je TRENUTNA vrednost valid_from polja u proslisti

		System.out.println(original.getValidFrom());
		System.out.println(today);
		System.out.println(UtilsMethods.localDateTimeToDate(original.getValidFrom()).before(today));

		if (validFromChanged && UtilsMethods.localDateTimeToDate(original.getValidFrom()).before(today)) {
			resp.addMessage(new Message("invalid", "Field [Valid from] cannot be updated if its value is in the past.",
					Severity.ERROR));
			validFrom = false;
		}

		// ako je TRENUTNA vrednost valid_to polja u proslosti
		if (validToChanged && original.getValidTo() != null
				&& UtilsMethods.localDateTimeToDate(original.getValidTo()).before(today)) {
			resp.addMessage(new Message("invalid", "Field [Valid to] cannot be updated if its value is in the past.",
					Severity.ERROR));
			validTo = false;
		}

		// provera da li je UNETA vrednost za valid_from u proslosti
		if (validFromChanged && !UtilsMethods.CheckDate(branch.getValidFrom())) {
			resp.addMessage(
					new Message("invalid", "Submitted [Valid from] value cannot be before today.", Severity.ERROR));
			validFrom = false;
		}

		// provera da li je UNETA valid_to vrednost pre UNETE valid_from vrednosti
		if (branch.getValidTo() != null) {
			if (!branch.getValidTo().isAfter(branch.getValidFrom())) {
				resp.addMessage(new Message("invalid", "Submitted [Valid to] value must be after [Valid from]",
						Severity.ERROR));
				validTo = false;
			} else
				// setovanje UNETE valid_to vrednosti na kraj dana
				branch.setValidTo(UtilsMethods.setValidTo(branch.getValidTo()));
		}

		if (branch.getBank().getId() != original.getBank().getId()) {
			List<Bank> banks = bankRepo.findAllBySwiftCodeAndIdNot(original.getSwiftCode(), branch.getBank().getId());
			Bank bank = bankRepo.findById(branch.getBank().getId());
			if (banks.size() > 0) {
				resp.addMessage(new Message("invalid", "Cannot change branch owner to bank [" + bank.getName()
						+ "], [Swift code] is already being used by another bank.", Severity.ERROR));
			}

			List<BankBranch> branches = bankBranchRepo.findAllBySwiftCodeAndBankNot(original.getSwiftCode(), bank);
			if (branches.size() > 1) {
				resp.addMessage(new Message("invalid",
						"Cannot change branch owner to bank [" + bank.getName()
								+ "], [Swift code] is already being used by a branch from another bank.",
						Severity.ERROR));
			}
		}

		if ((validFromChanged && validFrom) || (validToChanged && validTo)) {
			checkOverlappingBranchCodes(resp, branch, original.getBranchCode());
		}

		if (!resp.containsError()) {
			bankBranchRepo.save(branch);
			System.out.println("Bank branch successfully updated.");
			resp.addMessage(new Message("updated", "Bank branch successfully updated.", Severity.INFO));
		}

		return resp;
	}

	private boolean checkOverlappingBranchCodes(Response response, BankBranch branch, String branchCode) {
		List<BankBranch> branches = bankBranchRepo.findAllByBranchCode(branchCode);
		for (BankBranch b : branches) {
			if (b.getId() == branch.getId())
				continue;
			if (UtilsMethods.overlapingPeriods(branch, b)) {
				if (b.getValidTo() != null)
					response.addMessage(
							new Message(
									"invalid", "[Branch code]: " + branchCode + " is already being used "
											+ " in between " + b.getValidFrom() + " and " + b.getValidTo(),
									Severity.ERROR));
				else
					response.addMessage(new Message("invalid", "[Branch code]: " + branchCode
							+ " is already being used " + "from " + b.getValidFrom() + ".", Severity.ERROR));
				return true;
			}
		}
		return false;
	}

	public Response delete(BankBranch branch) {
		Response resp = new Response();

		if (branch.getId() <= 0) {
			resp.addMessage(new Message("invalid", "No bank branch selected.", Severity.ERROR));
			return resp;
		}

		// za svaki slucaj
		branch = bankBranchRepo.findById(branch.getId());

		Date now = new Date();

		if (branch.getValidTo() != null && (!UtilsMethods.localDateTimeToDate(branch.getValidTo()).after(now) || branch.getValidTo().isEqual(branch.getValidFrom().plusSeconds(1)))) {
			resp.addMessage(new Message("invalid", "Bank branch already invalid.", Severity.ERROR));
			return resp;
		}

		if (UtilsMethods.localDateTimeToDate(branch.getValidFrom()).after(now)) {
			// setuj da valid to bude 1s nakon validFrom
			branch.setValidTo(branch.getValidFrom().plusSeconds(1));
		} else {
			// valid to setovati da bude sadasnji trenutak
			branch.setValidTo(UtilsMethods.dateToLocalDateTime(now));
		}

		bankBranchRepo.save(branch);
		System.out.println("Bank branch [" + branch.getName() + "] successfully invalidated.");
		resp.addMessage(new Message("success", "Bank branch [" + branch.getName() + "] successfully invalidated.", Severity.INFO));
		return resp;
	}
	
	public Response getBranch(HttpServletRequest request) {
		Response resp = new Response();
		
		if (!request.getParameterMap().containsKey("id")) {
			resp.addMessage(new Message("invalid", "Bad request. Please provide a branch ID.", Severity.ERROR));
			return resp;
		}
		
		long id = Long.parseLong(request.getParameter("id"));
		BankBranch branch = bankBranchRepo.findById(id);
		
		if (branch == null) {
			resp.addMessage(new Message("invalid", "Branch with the id = " + id + " does not exist.", Severity.ERROR));
			return resp;
		}
		
		Gson gson = UtilsMethods.createGson();
		resp.setData(gson.toJson(branch));
		resp.addMessage(new Message("success", "Sent selected branch.", Severity.INFO));
		return resp;
	}

}
