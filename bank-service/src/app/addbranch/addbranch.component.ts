import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Bank } from '../bank';
import { BankService } from '../bank.service';
import { IResponse } from '../response';
import { BranchForm } from '../branchform';
import { BranchService } from '../branch.service';

@Component({
  selector: 'app-addbranch',
  templateUrl: './addbranch.component.html',
  styleUrls: ['./addbranch.component.css']
})
export class AddbranchComponent implements OnInit {

  constructor(private _route1: ActivatedRoute, private _bankService: BankService, private _branchService: BranchService) { }

  public selectedBank;
  public banks: Bank[];

  public branchForm = new BranchForm();

  public bankSelectError = true;

  public dateError = false;

  public validToError = false;

  public response: IResponse;

  public showMessage = false;
  public messageError = false;

  ngOnInit() {
    if (this._route1.snapshot.paramMap.has('id')) {
      this.selectedBank = parseInt(this._route1.snapshot.paramMap.get('id'));
      this.branchForm.bank = this.selectedBank;
    } else {
      this.branchForm.bank = "default";
    }
    this._bankService.searchBanks(undefined).subscribe((res: IResponse) => {
      this.banks = JSON.parse(res.data);
      this.banks.forEach(function(bank){
        bank.validFrom = new Date(bank.validFrom);
        if (bank.validTo !== undefined) {
          bank.validTo = new Date(bank.validTo);
        }
      });
    });
  }

  onSubmit() {
    let obj: any;
    let str = (this.branchForm.validFromDate + '').split("-");
    
    obj = {
      name: this.branchForm.name,
      bank: {
        id: this.branchForm.bank
      },
      branchCode: this.branchForm.branchCode,
      swiftCode: this.branchForm.swiftCode,
      validFrom: str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.branchForm.validFromTime
    };

    if ((this.branchForm.validFromTime + '').split(":").length == 2) {
      obj.validFrom += ':00';
    }

    if (this.branchForm.validToDate !== undefined) {
      str = (this.branchForm.validToDate + '').split("-");
      obj.validTo = str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.branchForm.validToTime;

      if ((this.branchForm.validToTime + '').split(":").length == 2) {
        obj.validTo += ':00';
      }

    }

    console.log(JSON.stringify(obj));

    this._branchService.addBranch(obj).subscribe((res: IResponse) => {
      this.response = res;
      this.showMessage = true;
      this.messageError = false;
      for (let i = 0; i < res.messages.length; i++) {
        if (res.messages[i].severity === 'ERROR') {
          this.messageError = true;
          break;
        }
      }
    });
  }

  validateValidFrom(value) {
    let selected = new Date(value);
    let today = new Date();
    today.setHours(0);
    today.setMinutes(0);
    today.setSeconds(0);
    if (selected < today) {
      this.dateError = true;
    } else {
      this.dateError = false;
    }
    this.validateValidTo(this.branchForm.validToDate, this.branchForm.validToTime, this.branchForm.validFromDate, this.branchForm.validFromTime);
  }

  validateValidTo(validToDate, validToTime, validFromDate, validFromTime) {
    if (validToDate !== undefined && validToTime !== undefined && validFromDate !== undefined && validFromTime !== undefined) {
      let to = new Date(validToDate);
      let str = validToTime.split(":", 3);
      to.setHours(str[0]);
      to.setMinutes(str[1]);
      to.setSeconds(str[2]);

      console.log(to);

      let from = new Date(validFromDate);
      let str2 = validFromTime.split(":", 3);
      from.setHours(str2[0]);
      from.setMinutes(str2[1]);
      from.setSeconds(str2[2]);
      console.log(from);

      if (to < from) {
        console.log('Valid to ne sme biti pre validFrom');
        this.validToError = true;
        return;
      }
    }
    this.validToError = false;
  }

  validateBankSelect(value) {
    if (value === 'default') {
      this.bankSelectError = true;
    } else {
      this.bankSelectError = false;
    }
  }

}
