package app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.BNK.BankBranch;
import app.CORE.Response;
import app.services.BankBranchService;

@CrossOrigin
@RestController
@RequestMapping("/bank_branch")
public class BankBranchController {

	@Autowired
	private BankBranchService service;

	@PostMapping("/add")
	public Response addnewBranch(@RequestBody BankBranch branch) {
		return service.add(branch);
	}
	
	@GetMapping("/search")
	public Response search(HttpServletRequest request) {
		return service.search(request);
	}

	@PostMapping("/update")
	public Response update(@RequestBody BankBranch branch) {
		return service.update(branch);
	}
	
	@PostMapping("/delete")
	public Response delete(@RequestBody BankBranch branch) {
		return service.delete(branch);
	}
	
	@GetMapping("/getBranch")
	public Response getBranch(HttpServletRequest request) {
		return this.service.getBranch(request);
	}
	
}
