import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Bank } from '../bank';
import { BankService } from '../bank.service';
import { IResponse } from '../response';
import { BankForm } from '../bankform';

@Component({
  selector: 'app-addbank',
  templateUrl: './addbank.component.html',
  styleUrls: ['./addbank.component.css']
})
export class AddbankComponent implements OnInit {

  bankTypes = ['LOCAL', 'FOREIGN', 'OTHER'];

  public bankForm = new BankForm();

  //date validation
  public dateError = false;

  public validToError = false;

  public bankTypeError = true;

  public response: IResponse;

  public showMessage = false;
  public messageError = false;

  constructor(private route: ActivatedRoute, private _bankService: BankService) { }

  ngOnInit() {
    this.bankForm.bankType = 'default';
  }

  onSubmit() {
    let obj: any;
    let str = (this.bankForm.validFromDate + '').split("-");
    obj = {
      name: this.bankForm.name,
      bankType: this.bankForm.bankType,
      bankCode: this.bankForm.bankCode,
      swiftCode: this.bankForm.swiftCode,
      validFrom: str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.bankForm.validFromTime
    };

    if ((this.bankForm.validFromTime + '').split(":").length == 2) {
      obj.validFrom += ':00';
    }

    if (this.bankForm.validToDate !== undefined) {
      str = (this.bankForm.validToDate + '').split("-");
      obj.validTo = str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.bankForm.validToTime;

      if ((this.bankForm.validToTime + '').split(":").length == 2) {
        obj.validTo += ':00';
      }

    }
    console.log(JSON.stringify(obj));
    this._bankService.addBank(obj).subscribe((res: IResponse) => {
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
  }

  validateType(value) {
    if (value === 'default') {
      this.bankTypeError = true;
    } else {
      this.bankTypeError = false;
    }
  }

}
