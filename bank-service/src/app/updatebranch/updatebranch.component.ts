import { Component, OnInit } from '@angular/core';
import { Branch } from '../branch';
import { BranchForm } from '../branchform';
import { ActivatedRoute } from '@angular/router';
import { BranchService } from '../branch.service';
import { IResponse } from '../response';
import { Bank } from '../bank';
import { BankService } from '../bank.service';

@Component({
  selector: 'app-updatebranch',
  templateUrl: './updatebranch.component.html',
  styleUrls: ['./updatebranch.component.css']
})
export class UpdatebranchComponent implements OnInit {

  public selectedBranchId;
  public branch: Branch;
  public branchForm: BranchForm;

  public banks: Bank[];

  public dateError = false;

  public validToError = false;

  public response: IResponse;

  public showMessage = false;
  public messageError = false;

  public disabledFrom = false;
  public disabledTo = false;

  public changed = false;

  constructor(private route: ActivatedRoute, private branchService: BranchService, private _bankService: BankService) { }

  ngOnInit() {
    if (this.route.snapshot.paramMap.has('id')) {
      this.selectedBranchId = parseInt(this.route.snapshot.paramMap.get('id'));
      this.branchService.getBranchById(this.selectedBranchId).subscribe((res: IResponse) => {
        this.branch = JSON.parse(res.data);
        this.branchForm = new BranchForm();
        this.branchForm.name = this.branch.name;
        this.branchForm.bank = this.branch.bank.id;
        this.branchForm.branchCode = this.branch.branchCode;
        this.branchForm.swiftCode = this.branch.swiftCode;
        this.branchForm.validFromDate = (this.branch.validFrom + '').split("T")[0];
        this.branchForm.validFromTime = (this.branch.validFrom + '').split("T")[1];

        let today = new Date();

        if (new Date(this.branch.validFrom) < today) {
          this.disabledFrom = true;
        }

        if (this.branch.validTo !== undefined) {
          this.branchForm.validToDate = (this.branch.validTo + '').split("T")[0];
          this.branchForm.validToTime = (this.branch.validTo + '').split("T")[1];

          if (new Date(this.branch.validTo) < today) {
            this.disabledTo = true;
          }

        }
      });

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
  }

  onSubmit() {
    console.log(this.branchForm);
    let obj: any;
    let str = (this.branchForm.validFromDate + '').split("-");
    obj = {
      id: this.branch.id,
      name: this.branchForm.name,
      bank: {
        id: this.branchForm.bank
      },
      validFrom: str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.branchForm.validFromTime
    };

    if ((this.branchForm.validFromTime + '').split(":").length == 2) {
      obj.validFrom += ':00';
    }
    
    if (this.branchForm.validToDate != undefined && this.branchForm.validToDate != "") {
      str = (this.branchForm.validToDate + '').split("-");
      obj.validTo = str[2] + '-' + str[1] + '-' + str[0] + ' ' + this.branchForm.validToTime;

      if ((this.branchForm.validToTime + '').split(":").length == 2) {
        obj.validTo += ':00';
      }

    }
    this.branchService.updateBranch(obj).subscribe((res: IResponse) => {
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
    
    if (this.branchForm.name != this.branch.name) {
      this.changed = true;
      return;
    }

    if (this.branchForm.bank != this.branch.bank.id) {
      this.changed = true;
      return;
    }

    if (!this.disabledFrom) {

      let split = (this.branch.validFrom + '').split("T");
      if (split[0] != this.branchForm.validFromDate || split[1] != this.branchForm.validFromTime) {
        this.changed = true;
        return;
      }
    }

    if (!this.disabledTo) {

      let split = (this.branch.validTo + '').split("T");
      if (split[0] != this.branchForm.validToDate || split[1] != this.branchForm.validToTime) {

        if (split[0] == undefined && this.branchForm.validToDate == '') {
          return;
        }

        if (split[1] == undefined && this.branchForm.validToTime == '') {
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
    this.testChanged();
  }

}
