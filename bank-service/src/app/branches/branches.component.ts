import { Component, OnInit } from '@angular/core';
import { IResponse } from '../response';
import { Branch } from '../branch';
import { BranchService } from '../branch.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-branches',
  templateUrl: './branches.component.html',
  styleUrls: ['./branches.component.css']
})
export class BranchesComponent implements OnInit {

  public response: IResponse;
  public branches: Branch[] = [];

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

  constructor(private _branchService: BranchService, private router: Router) { }

  ngOnInit() {
    this.refreshBranches();
    this.deleted = false;
  }

  search() {
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
    this._branchService.searchBranches(obj).subscribe((res: IResponse) => {
      this.response = res;
      this.branches = JSON.parse(this.response.data);
      this.branches.forEach(function(branch){
        branch.validFrom = new Date(branch.validFrom);
        if (branch.validTo !== undefined) {
          branch.validTo = new Date(branch.validTo);
        }
      });
    });
  }

  goToAddBranch(id) {
    this.router.navigate(['updatebranch', id]);
  }

  invalidate(branch) {
    let id = branch.id;
    let obj = {id: id};
    this._branchService.invalidateBranch(obj).subscribe((res: IResponse) => {
      this.deletedMsg = res;
      this.error = false;
      this.deleted = true;

      for (let i = 0; i < res.messages.length; i++) {
        if (res.messages[i].severity === 'ERROR') {
          this.error = true;
        }
      }
      console.log(res.messages);
      this.refreshBranches();
    });
    this.disabledTest(branch);
  }

  refreshBranches() {
    this._branchService.searchBranches(undefined).subscribe((res: IResponse) => {
      this.response = res;
      this.branches = JSON.parse(res.data);
      this.branches.forEach(function(branch){
        branch.validFrom = new Date(branch.validFrom);
        if (branch.validTo !== undefined) {
          branch.validTo = new Date(branch.validTo);
        }
      });
    });
  }


  disabledTest(branch){
    if (branch.validTo == undefined) {
      return false;
    } else {
      let now = new Date();
      if (branch.validTo < now) {
        return true;
      }

      let fromPlusOne = new Date(branch.validFrom.getTime() + 1000);
      let to = new Date(branch.validTo.getTime() + 0);

      if (fromPlusOne.toISOString() == to.toISOString()) {
        return true;
      }

      return false;
    }
  }

}
