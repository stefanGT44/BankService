package app.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.BNK.Bank;
import app.BNK.BankBranch;

@Repository
public interface BankBranchRepo extends JpaRepository<BankBranch, Long>{

	List<BankBranch> findAllBySwiftCode(String swiftCode);
	List<BankBranch> findAllByBranchCode(String branchCode);
	List<BankBranch> findAllBySwiftCodeAndBankNot(String swiftCode, Bank bank);
	List<BankBranch> findAllByBank(Bank bank);
	
	BankBranch findById(long id);
	
	@Query(
			value="SELECT * FROM BANK_BRANCH b WHERE :validOn > b.valid_from AND (b.valid_to IS NULL OR :validOn < b.valid_to)",
			nativeQuery = true)
	List<BankBranch> searchByValidDate(@Param("validOn") LocalDateTime validFrom);
	
	@Query(
			value="SELECT * FROM BANK_BRANCH b WHERE b.name LIKE :identifier or b.swift_code LIKE :identifier or b.branch_code LIKE :identifier",
			nativeQuery = true)
	List<BankBranch> searchByIdentifier(@Param("identifier") String identifier);
	
	@Query(
			value="SELECT * FROM BANK_BRANCH b WHERE (b.name LIKE :identifier or b.swift_code LIKE :identifier or b.branch_code LIKE :identifier) and "
					+ "(:validOn > b.valid_from AND (b.valid_to IS NULL OR :validOn < b.valid_to))",
			nativeQuery = true)
	List<BankBranch> fullSearch(@Param("identifier") String identifier,@Param("validOn") LocalDateTime validFrom);
	
}
