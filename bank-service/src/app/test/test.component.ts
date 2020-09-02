import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { BankService } from '../bank.service';
import { Observable } from 'rxjs';
import { IResponse } from '../response';
import { Bank } from '../bank';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {

  public user = "Stefke";
  public siteUrl = window.location.href;
  public myId = "testId";
  public isDisabled = false;

  public successClass = "text-success";
  public hasError = false;

  public isSpecial = true;
  public messageClasses = {
    "text-success": !this.hasError,
    "text-danger": this.hasError,
    "text-special": this.isSpecial
  }

  public highlightColor = "orange";

  public titleStyles = {
    color: "blue",
    fontStyle: "italic"
  }

  public greeting = "";

  public displayName = false;

  public colors = ["red", "blue", "green", "yellow"];

  @Input('parentData') public name;
  @Output() public childEvent = new EventEmitter();

  public response: IResponse;
  public banks: Bank[] = [];

  constructor(private _bankService: BankService) { }

  ngOnInit() {
    //this._bankService.searchBanks()
    //.subscribe(data => this.response = data);
    //console.log();
    this._bankService.searchBanks(undefined).subscribe((res : IResponse) => {
        this.response = res;
        this.banks = JSON.parse(this.response.data);
        this.banks.forEach(function(bank){
          bank.validFrom = new Date(bank.validFrom);
          bank.validTo = new Date(bank.validTo);
        });
        console.log(this.banks);
        console.log(this.banks[0].validFrom);
        console.log(new Date());
    });

  }

  fireEvent(){
    this.childEvent.emit('Hey buraz');
  }

  logMessage(value){
    console.log(value);
  }

  onClick(event){
    console.log(event);
    this.greeting = event.type;
  }

  greetUser(){
    return "Hello " + this.user;
  }

}
