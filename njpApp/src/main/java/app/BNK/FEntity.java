package app.BNK;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonFormat;

@MappedSuperclass
public class FEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	@JsonFormat(pattern="dd-MM-yyyy HH:mm:ss")
	private LocalDateTime validFrom;
	
	@JsonFormat(pattern="dd-MM-yyyy HH:mm:ss")
	private LocalDateTime validTo;
	
	@Column(nullable=false, updatable=false)
	private String swiftCode;
	
	public FEntity() {
	}

	public FEntity(long id, String name, LocalDateTime validFrom, LocalDateTime validTo, String swiftCode) {
		super();
		this.id = id;
		this.name = name;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.swiftCode = swiftCode;
	}
	
	public FEntity(String name, LocalDateTime validFrom, LocalDateTime validTo, String swiftCode) {
		super();
		this.name = name;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.swiftCode = swiftCode;
	}
	
	public FEntity(String name, LocalDateTime validFrom, String swiftCode) {
		super();
		this.name = name;
		this.validFrom = validFrom;
		this.swiftCode = swiftCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDateTime getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDateTime validTo) {
		this.validTo = validTo;
	}

	public String getSwiftCode() {
		return swiftCode;
	}

	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}

	public long getId() {
		return id;
	}
	
}
