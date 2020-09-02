import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class BankService {

  constructor(private http: HttpClient) { }

  searchBanks(data) {
      if (data) {
        let params1 = new HttpParams();
        for (let [key, value] of Object.entries(data)) {
          params1 = params1.set(key, '' + value);
        }
      return this.http.get('http://localhost:8080/bank/search', {params: params1});
    } else {
      return this.http.get('http://localhost:8080/bank/search');
    }
  }

  invalidateBank(bank){
    return this.http.post('http://localhost:8080/bank/delete', bank);
  }

  getBankById(id) {
    let params = new HttpParams().set('id', id);
    return this.http.get('http://localhost:8080/bank/getBank', {params: params});
  }

  addBank(bank) {
    return this.http.post('http://localhost:8080/bank/add', bank);
  }

  updateBank(bank) {
    return this.http.post('http://localhost:8080/bank/update', bank);
  }

}
