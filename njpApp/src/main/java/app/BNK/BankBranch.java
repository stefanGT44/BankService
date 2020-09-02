package app.BNK;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class BankBranch extends FEntity{
	
	@ManyToOne
	private Bank bank;
	
	@Column(nullable=false, updatable=false)
	private String branchCode;

	public BankBranch() {
	}
	
	public BankBranch(long id, Bank bank, String name, String branchCode, String swiftCode, LocalDateTime validFrom,
			LocalDateTime validTo) {
		super(id, name, validFrom, validTo, swiftCode);
		this.bank = bank;
		this.branchCode = branchCode;
	}
	
	public BankBranch(Bank bank, String name, String branchCode, String swiftCode, LocalDateTime validFrom,
			LocalDateTime validTo) {
		super(name, validFrom, validTo, swiftCode);
		this.bank = bank;
		this.branchCode = branchCode;
	}
	
	public BankBranch(Bank bank, String name, String branchCode, String swiftCode, LocalDateTime validFrom) {
		super(name, validFrom, swiftCode);
		this.bank = bank;
		this.branchCode = branchCode;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}
	
}
