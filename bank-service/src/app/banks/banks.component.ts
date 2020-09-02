import { Component, OnInit } from '@angular/core';
import { BankService } from '../bank.service';
import { Bank } from '../bank';
import { IResponse } from '../response';
import { Router } from '@angular/router';


@Component({
  selector: 'app-banks',
  templateUrl: './banks.component.html',
  styleUrls: ['./banks.component.css']
})
export class BanksComponent implements OnInit {

  public response: IResponse;
  public banks: Bank[] = [];

  //input polja za pretragu
  public identifier;
  public date;
  public time;

  //poruka o rezultatima pretrage
  public searched = false;
  public keyword;
  public valid_on;

  //tumacenje poruke sa servera
  public error = false;
  public deleted = false;
  public deletedMsg: IResponse;

  constructor(private _bankService: BankService, private router: Router) {}

  ngOnInit() {
    this.refreshBanks();
    this.deleted = false;
  }

  search() {
    //da bi nestala poruka ako je brisano nesto pre ovoga
    this.deleted = false;

    let obj:any = {};
    if (this.identifier) {
      obj.identifier = this.identifier;
      this.keyword = this.identifier;
    } else {
      this.keyword = '';
    }
    if (this.date && this.time) {
      obj.valid_on = this.date + ' ' + this.time;
      this.valid_on = obj.valid_on;
    } else {
      this.valid_on = '';
    }
    if (Object.keys(obj).length !== 0) {
      this.searched = true;
    } else {
      this.searched = false;
    }
    this._bankService.searchBanks(obj).subscribe((res: IResponse) => {
      this.response = res;
      this.banks = JSON.parse(this.response.data);
      this.banks.forEach(function(bank){
        bank.validFrom = new Date(bank.validFrom);
        if (bank.validTo !== undefined) {
          bank.validTo = new Date(bank.validTo);
        }
      });
    });
  }

  goToAddBranch(id){
    this.router.navigate(['/addbranch', id]);
  }

  updateBank(id){
    this.router.navigate(['/updatebank', id]);
  }

  invalidate(bank) {
    let id = bank.id;
    let obj = {id: id};
    this._bankService.invalidateBank(obj).subscribe((res: IResponse) => {
      this.deletedMsg = res;
      this.error = false;
      this.deleted = true;

      for (let i = 0; i < res.messages.length; i++) {
        if (res.messages[i].severity === 'ERROR') {
          this.error = true;
        }
      }
      console.log(res.messages);
      this.refreshBanks();
    });
    this.disabledTest(bank);
  }

  refreshBanks() {
    this._bankService.searchBanks(undefined).subscribe((res: IResponse) => {
      this.response = res;
      this.banks = JSON.parse(this.response.data);
      this.banks.forEach(function(bank){
        bank.validFrom = new Date(bank.validFrom);
        if (bank.validTo !== undefined) {
          bank.validTo = new Date(bank.validTo);
        }
      });
  });
  }

  disabledTest(bank){
    if (bank.validTo == undefined) {
      return false;
    } else {
      let now = new Date();
      if (bank.validTo < now) {
        return true;
      }

      let fromPlusOne = new Date(bank.validFrom.getTime() + 1000);
      let to = new Date(bank.validTo.getTime() + 0);

      if (fromPlusOne.toISOString() == to.toISOString()) {
        return true;
      }

      return false;
    }
  }

}
