import {Observable} from 'rxjs';
import {DiseaseGeneral} from '../model/disease-general';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Injectable, OnDestroy} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';

@Injectable({
  providedIn: 'root'
})
export class DiseaseTableService implements OnDestroy {

  public diseases: DiseaseGeneral[];
  private readonly url: string;

  constructor(private http: HttpClient,
              private cookieService: CookieService) {
    this.url = environment.appUrl + 'diseases';
    this.diseases = [];
  }

  getDiseases(): Observable<DiseaseGeneral[]> {
    return this.http.get<DiseaseGeneral[]>(this.url, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  remove(diseaseName: string): Observable<any> {
    return this.http.delete(this.url.concat('/', diseaseName), {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  ngOnDestroy(): void {
    this.diseases = [];
  }

  addDisease(name: string, catherer: boolean, surgery: boolean): Observable<any> {
    return this.http.post(this.url, {
      name: name,
      cathererRequired: catherer,
      surgeryRequired: surgery
    }, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  modifyDisease(name: string, catherer: boolean, surgery: boolean): Observable<any> {
    return this.http.post(this.url, {
      name: name,
      cathererRequired: catherer,
      surgeryRequired: surgery
    }, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }
}
