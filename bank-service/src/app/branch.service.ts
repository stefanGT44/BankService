import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class BranchService {

  constructor(private http: HttpClient) { }

  searchBranches(data) {
    if (data) {
      let params1 = new HttpParams();
      for (let [key, value] of Object.entries(data)) {
        params1 = params1.set(key, '' + value);
      }
      return this.http.get('http://localhost:8080/bank_branch/search', {params: params1});
    } else {
      return this.http.get('http://localhost:8080/bank_branch/search');
    }
  }

  invalidateBranch(branch){
    return this.http.post('http://localhost:8080/bank_branch/delete', branch);
  }

  addBranch(branch) {
    return this.http.post('http://localhost:8080/bank_branch/add', branch);
  }

  getBranchById(id) {
    let params = new HttpParams().set('id', id);
    return this.http.get('http://localhost:8080/bank_branch/getBranch', {params: params});
  }

  updateBranch(branch) {
    return this.http.post('http://localhost:8080/bank_branch/update', branch);
  }

}
