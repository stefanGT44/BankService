package app.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.BNK.Bank;

@Repository
public interface BankRepo extends JpaRepository<Bank, Long>{
	
	List<Bank> findAllByBankCode(String bankCode);
	
	List<Bank> findAllBySwiftCode(String swiftCode);
	
	List<Bank> findAllBySwiftCodeAndIdNot(String swiftCode, Long id);
	
	Bank findById(long id);
	
	@Query(
			value="SELECT * FROM BANK b WHERE :validOn > b.valid_from AND (b.valid_to IS NULL OR :validOn < b.valid_to)",
			nativeQuery = true)
	List<Bank> searchByValidDate(@Param("validOn") LocalDateTime validFrom);
	
	@Query(
			value="SELECT * FROM BANK b WHERE b.name LIKE :identifier or b.swift_code LIKE :identifier or b.bank_code LIKE :identifier",
			nativeQuery = true)
	List<Bank> searchByIdentifier(@Param("identifier") String identifier);
	
	@Query(
			value="SELECT * FROM BANK b WHERE (b.name LIKE :identifier or b.swift_code LIKE :identifier or b.bank_code LIKE :identifier) and "
					+ "(:validOn > b.valid_from AND (b.valid_to IS NULL OR :validOn < b.valid_to))",
			nativeQuery = true)
	List<Bank> fullSearch(@Param("identifier") String identifier,@Param("validOn") LocalDateTime validFrom);

}
