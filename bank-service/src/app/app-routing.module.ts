import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BanksComponent } from './banks/banks.component';
import { AddbankComponent } from './addbank/addbank.component';
import { BranchesComponent } from './branches/branches.component';
import { AddbranchComponent } from './addbranch/addbranch.component';
import { UpdatebankComponent } from './updatebank/updatebank.component';
import { UpdatebranchComponent } from './updatebranch/updatebranch.component';

const routes: Routes = [
  { path: '', component: BanksComponent},
  { path: 'addbank', component: AddbankComponent},
  { path: 'updatebank/:id', component: UpdatebankComponent},
  { path: 'branches', component: BranchesComponent},
  { path: 'addbranch', component: AddbranchComponent},
  { path: 'addbranch/:id', component: AddbranchComponent},
  { path: 'updatebranch/:id', component: UpdatebranchComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
