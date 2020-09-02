package app.BNK;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Bank extends FEntity{
	
	@Column(nullable=false, updatable=false)
	private BankType bankType;
	
	@Column(nullable=false, updatable=false)
	private String bankCode;

	public Bank() {
	}

	public Bank(long id, String name, BankType bankType, LocalDateTime validFrom, LocalDateTime validTo,
			String bankCode, String swiftCode) {
		super(id, name, validFrom, validTo, swiftCode);
		this.bankType = bankType;
		this.bankCode = bankCode;
	}
	
	public Bank(String name, BankType bankType, LocalDateTime validFrom, LocalDateTime validTo,
			String bankCode, String swiftCode) {
		super(name, validFrom, validTo, swiftCode);
		this.bankType = bankType;
		this.bankCode = bankCode;
	}
	
	public Bank(String name, BankType bankType, LocalDateTime validFrom,
			String bankCode, String swiftCode) {
		super(name, validFrom, swiftCode);
		this.bankType = bankType;
		this.bankCode = bankCode;
	}

	public BankType getBankType() {
		return bankType;
	}

	public void setBankType(BankType bankType) {
		this.bankType = bankType;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	
}
