import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { TestComponent } from './test/test.component';
import { BankService } from './bank.service';
import { HttpClientModule } from '@angular/common/http';
import { NavComponent } from './nav/nav.component';
import { BanksComponent } from './banks/banks.component';
import { AddbankComponent } from './addbank/addbank.component';
import { BranchesComponent } from './branches/branches.component';
import { AddbranchComponent } from './addbranch/addbranch.component';
import { RouterModule } from '@angular/router';
import { UpdatebankComponent } from './updatebank/updatebank.component';
import { UpdatebranchComponent } from './updatebranch/updatebranch.component';

@NgModule({
  declarations: [
    AppComponent,
    TestComponent,
    NavComponent,
    BanksComponent,
    AddbankComponent,
    BranchesComponent,
    AddbranchComponent,
    UpdatebankComponent,
    UpdatebranchComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    RouterModule
  ],
  providers: [BankService],
  bootstrap: [AppComponent]
})
export class AppModule { }
