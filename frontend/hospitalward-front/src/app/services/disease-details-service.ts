import {Observable} from 'rxjs';
import {Injectable, OnDestroy} from '@angular/core';
import {DiseaseDetails} from '../model/disease-details';
import {environment} from '../../environments/environment';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';

@Injectable({
  providedIn: 'root'
})
export class DiseaseDetailsService implements OnDestroy {

  eTag = '';
  private readonly url: string = '';
  disease: DiseaseDetails = this.getEmptyDiseaseDetails();

  constructor(private http: HttpClient,
              private cookieService: CookieService) {
    this.url = environment.appUrl + 'diseases';
  }

  private getEmptyDiseaseDetails(): DiseaseDetails {
    return {
      version: 0,
      polishName: '',
      latinName: '',
      cathererRequired: false,
      surgeryRequired: false,
      modificationDate: new Date(),
      modifiedBy: '',
      creationDate: new Date(),
      createdBy: ''
    };
  }

  modifyDisease(name: string, catherer: boolean, surgery: boolean): Observable<any> {
    return this.http.put(this.url + '/' + name, {
      version: this.disease.version,
      latinName: this.disease.latinName,
      cathererRequired: catherer,
      surgeryRequired: surgery
    }, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token'),
        'If-Match': this.eTag
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  ngOnDestroy(): void {
    this.disease = this.getEmptyDiseaseDetails();
  }

  getDisease(name: string): Observable<HttpResponse<DiseaseDetails>> {
    this.disease = this.getEmptyDiseaseDetails();
    return this.http.get<DiseaseDetails>(this.url + '/' + name, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'response',
      responseType: 'json'
    });
  }

  readDiseaseAndEtagFromResponse(response: HttpResponse<DiseaseDetails>): void {
    this.disease = response.body as DiseaseDetails;
    this.eTag = (response.headers.get('ETag') as string).slice(1, -1);
  }

}

