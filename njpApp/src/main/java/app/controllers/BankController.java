package app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.BNK.Bank;
import app.CORE.Response;
import app.services.BankService;

@CrossOrigin
@RestController
@RequestMapping("/bank")
public class BankController {
	
	@Autowired
	private BankService service;
	
	@PostMapping("/add")
	public Response addNewBank(HttpServletRequest request, @RequestBody Bank bank) {
		return service.add(bank);
	}
	
	@GetMapping("/search")
	public Response searchBanks(HttpServletRequest request) {
		return service.search(request);
	}
	
	@PostMapping("/update")
	public Response updateBank(@RequestBody Bank bank) {
		return service.update(bank);
	}
	
	@PostMapping("/delete")
	public Response deleteBank(@RequestBody Bank bank) {
		return service.delete(bank);
	}
	
	@GetMapping("/getBank")
	public Response getBank(HttpServletRequest request) {
		return service.getBank(request);
	}

}
