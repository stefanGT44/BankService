import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Bank } from '../bank';
import { BankService } from '../bank.service';
import { IResponse } from '../response';
import { BankForm } from '../bankform';
import { ThrowStmt } from '@angular/compiler';

@Component({
  selector: 'app-updatebank',
  templateUrl: './updatebank.component.html',
  styleUrls: ['./updatebank.component.css']
})
export class UpdatebankComponent implements OnInit {

  constructor(private route: ActivatedRoute, private bankService: BankService) { }

  bankTypes = ['LOCAL', 'FOREIGN', 'OTHER'];

  public selectedBankId;
  public bank: Bank;
  public bankForm: BankForm;

  public dateError = false;
  public validToError = false;

  public response: IResponse;
  
  public showMessage = false;
  public messageError = false;

  public disabledFrom = false;
  public disabledTo = false;

  public changed = false;

  ngOnInit() {
    if (this.route.snapshot.paramMap.has('id')) {
      this.selectedBankId = parseInt(this.route.snapshot.paramMap.get('id'));
      this.bankService.getBankById(this.selectedBankId).subscribe((res: IResponse) => {
        this.bank = JSON.parse(res.data);
        this.bankForm = new BankForm();
        this.bankForm.name = this.bank.name;
        this.bankForm.bankType = this.bank.bankType;
        this.bankForm.bankCode = this.bank.bankCode;
        this.bankForm.swiftCode = this.bank.swiftCode;
        this.bankForm.validFromDate = (this.bank.validFrom + '').split("T")[0];
        this.bankForm.validFromTime = (this.bank.validFrom + '').split("T")[1];

        let today = new Date();

        if (new Date(this.bank.validFrom) < today) {
          this.disabledFrom = true;
        }

        if (this.bank.validTo !== undefined) {
          this.bankForm.validToDate = (this.bank.validTo + '').split("T")[0];
          this.bankForm.validToTime = (this.bank.validTo + '').split("T")[1];

          if (new Date(this.bank.validTo) < today) {
            this.disabledTo = true;
          }

        }
      });
    }
  }

  onSubmit() {
    console.log(this.bankForm);
    let obj: any;
    let str = (this.bankForm.validFromDate + '').split("-");
    obj = {
      id: this.bank.id,
      name: this.bankForm.name,
      validFrom: str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.bankForm.validFromTime
    };

    if ((this.bankForm.validFromTime + '').split(":").length == 2) {
      obj.validFrom += ':00';
    }
    
    if (this.bankForm.validToDate != undefined && this.bankForm.validToDate != "") {
      str = (this.bankForm.validToDate + '').split("-");
      obj.validTo = str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.bankForm.validToTime;

      if ((this.bankForm.validToTime + '').split(":").length == 2) {
        obj.validTo += ':00';
      }

    }
    this.bankService.updateBank(obj).subscribe((res: IResponse) => {
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

  testChanged() {
    this.changed = false;
    
    if (this.bankForm.name != this.bank.name){
      this.changed = true;
      return;
    }

    if (!this.disabledFrom) {

      let split = (this.bank.validFrom + '').split("T");
      if (split[0] != this.bankForm.validFromDate || split[1] != this.bankForm.validFromTime) {
        this.changed = true;
        return;
      }
    }

    if (!this.disabledTo) {

      let split = (this.bank.validTo + '').split("T");
      if (split[0] != this.bankForm.validToDate || split[1] != this.bankForm.validToTime) {

        if (split[0] == undefined && this.bankForm.validToDate == '') {
          return;
        }

        if (split[1] == undefined && this.bankForm.validToTime == '') {
          return;
        }

        if (!this.validToError) {
          this.changed = true;
        }
      }
    }
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
    this.validateValidTo(this.bankForm.validToDate, this.bankForm.validToTime, this.bankForm.validFromDate, this.bankForm.validFromTime);
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
    this.testChanged();
  }

}
