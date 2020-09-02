package app.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import app.BNK.Bank;
import app.BNK.BankBranch;
import app.BNK.BankType;
import app.BNK.FEntity;
import app.CORE.Message;
import app.CORE.Response;
import app.CORE.Severity;
import app.repositories.BankBranchRepo;
import app.repositories.BankRepo;

public class UtilsMethods {
	
	public static boolean checkFEntityFields(FEntity fentity, Response response) {
		boolean test = true;
		if (fentity.getName() == null || fentity.getName().trim().equals("")) {
			response.addMessage(new Message("missing", "[Name] field is required.", Severity.ERROR));
			test = false;
		}
		if (fentity.getValidFrom() == null) {
			response.addMessage(new Message("missing", "[Valid from] field is required.", Severity.ERROR));
			test = false;
		}
		if (fentity.getSwiftCode() == null || fentity.getSwiftCode().trim().equals("")) {
			response.addMessage(new Message("missing", "[Swift code] field is required.", Severity.ERROR));
			test = false;
		}
		return test;
	}
	
	public static boolean checkBankFields(Bank bank, Response response) {
		boolean test = true;
		test = checkFEntityFields(bank, response);
		
		if (bank.getBankType() == null) {
			response.addMessage(new Message("missing", "[Bank type] field is required.", Severity.ERROR));
			test = false;
		}
		if (bank.getBankCode() == null || bank.getBankCode().trim().equals("")) {
			response.addMessage(new Message("missing", "[Bank code] field is required.", Severity.ERROR));
			test = false;
		}
		return test;
	}
	
	public static boolean checkBranchFields(BankBranch branch, Response response) {
		boolean test = true;
		test = checkFEntityFields(branch, response);
		
		if (branch.getBank() == null) {
			response.addMessage(new Message("missing", "[Bank] must be specified.", Severity.ERROR));
			test = false;
		}
		if (branch.getBranchCode() == null || branch.getBranchCode().trim().equals("")) {
			response.addMessage(new Message("missing", "[Branch code] field is required.", Severity.ERROR));
			test = false;
		}
		
		return test;
	}

	public static boolean CheckDate(LocalDateTime date1) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		Date today = c.getTime();

		Date dateSpecified = Date.from(date1.atZone(ZoneId.systemDefault()).toInstant());

		if (dateSpecified.before(today)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static LocalDateTime setValidTo(LocalDateTime date) {
		date = date.plusSeconds(60 - date.getSecond() - 1);
		date = date.plusMinutes(60 - date.getMinute() - 1);
		date = date.plusHours(24 - date.getHour() - 1);
		return date;
	}
	
	public static void preloadDatabase(BankRepo bankRepo, BankBranchRepo bankBranchRepo) {
		LocalDateTime validFrom = LocalDateTime.now();
		validFrom = validFrom.plusHours(3);
		
		LocalDateTime validTo = LocalDateTime.now();
		validTo = validTo.plusHours(5);
		
		Random r = new Random();
		
		
		for (int i = 0; i < 20; i ++) {
			Bank temp = new Bank("Bank" + i, BankType.FOREIGN, validFrom, validTo, "12312312" + i, "32132132" + i);
			bankRepo.save(temp);
			if (i % 2 == 0) {
				for (int j = 0; j < r.nextInt(4) + 1; j++) {
					BankBranch branch = new BankBranch(temp, "Branch-"+i+"-"+j, "1231231"+i+j, "321321"+i+j, validFrom, validTo);
					bankBranchRepo.save(branch);
				}
			}
		}
		
		validFrom = validFrom.plusDays(2);
		validTo = validTo.plusDays(3);
		Bank temp = new Bank("Bankk", BankType.FOREIGN, validFrom, validTo, "123123120", "321321320");
		bankRepo.save(temp);
		
		validTo = validTo.plusDays(20);
		validFrom = validFrom.plusDays(15);
		Bank temp2 = new Bank("Bankt2", BankType.FOREIGN, validFrom, validTo, "123123120", "321321320");
		bankRepo.save(temp2);
		
		validTo = validTo.minusDays(100);
		validFrom = validFrom.minusDays(80);
		Bank temp3 = new Bank("Bankt3", BankType.FOREIGN, validFrom, validTo, "123123120", "321321320");
		bankRepo.save(temp3);
		
	}
	
	public static boolean overlapingPeriods(FEntity fentity, FEntity other) {
		Date bankFrom = localDateTimeToDate(fentity.getValidFrom());
		if (fentity.getValidTo() == null) {
			if (other.getValidTo() == null)
				return true;
			else {
				Date otherTo = localDateTimeToDate(other.getValidTo());
				if (bankFrom.after(otherTo))
					return false;
				else
					return true;
			}
		} else {
			if (other.getValidTo() == null) {
				Date bankTo = localDateTimeToDate(fentity.getValidFrom());
				Date otherFrom = localDateTimeToDate(other.getValidFrom());
				if (bankTo.before(otherFrom))
					return false;
				else
					return true;
			} else {
				Date bankTo = localDateTimeToDate(fentity.getValidTo());
				Date otherFrom = localDateTimeToDate(other.getValidFrom());
				Date otherTo = localDateTimeToDate(other.getValidTo());
				
				if (bankTo.before(otherFrom))
					return false;
				if (bankFrom.after(otherTo))
					return false;
				return true;
			}
		}
	}
	
	public static Date localDateTimeToDate(LocalDateTime local) {
		Date date = Date.from(local.atZone(ZoneId.systemDefault()).toInstant());		
		return date;
	}
	
	public static LocalDateTime dateToLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.systemDefault());
	}
	
	public static LocalDateTime parseDate(String string) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date;
		try {
			date = LocalDateTime.parse(string, formatter);
		} catch (Exception e) {
			return null;
		}
		return date;
	}
	
	public static boolean checkUpdateFields(FEntity fentity, Response response) {
		boolean test = true;
		if (fentity.getId() <= 0) {
			response.addMessage(new Message("no selection", "Bank not selected.", Severity.ERROR));
			test = false;
		}
		if (fentity.getName() == null || fentity.getName().trim().equals("")) {
			response.addMessage(new Message("missing", "[Name] field is required.", Severity.ERROR));
			test = false;
		}
		if (fentity.getValidFrom() == null) {
			response.addMessage(new Message("missing", "[Valid from] field is required.", Severity.ERROR));
			test = false;
		}
		return test;
	}
	
	public static boolean checkBranchUpdateFields(BankBranch branch, Response response) {
		boolean test = checkUpdateFields(branch, response);
		if (branch.getBank() == null || branch.getBank().getId() <= 0) {
			response.addMessage(new Message("missing", "Bank not selected.", Severity.ERROR));
			test = false;
		}
		return test;
	}
	
	public static boolean changed(FEntity fentity1, FEntity fentity2) {
		if (!fentity1.getName().equals(fentity2.getName()) || !fentity1.getValidFrom().equals(fentity2.getValidFrom()))
			return true;
		return changedValidTo(fentity1, fentity2);
	}
	
	public static boolean changedValidTo(FEntity fentity1, FEntity fentity2) {
		if (fentity1.getValidTo() == null && fentity2.getValidTo() != null)
			return true;
		if (fentity1.getValidTo() != null && fentity2.getValidTo() == null)
			return true;
		if (fentity1.getValidTo() != null && fentity2.getValidTo() != null) {
			if (!fentity1.getValidTo().equals(fentity2.getValidTo()))
				return true;
		}
		return false;
	}
	
	public static boolean branchChanged(BankBranch branch1, BankBranch branch2) {
		if (branch1.getBank().getId() != branch2.getBank().getId())
			return true;
		return changed(branch1, branch2);
	}
	
	public static Gson createGson() {
		return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()).create();
	}

}
