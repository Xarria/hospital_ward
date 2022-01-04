import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {DiseaseDetails} from '../model/disease-details';
import {CookieService} from 'ngx-cookie-service';
import {environment} from '../../environments/environment';
import {PatientDetails} from '../model/patient-details';
import {DiseaseGeneral} from '../model/disease-general';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  eTag = '';
  private readonly url: string = '';
  patient: PatientDetails = this.getEmptyPatientDetails();

  constructor(private http: HttpClient,
              private cookieService: CookieService) {
    this.url = environment.appUrl + 'patients';
  }

  private getEmptyPatientDetails(): PatientDetails {
    return {
      version: 0,
      id: 0,
      pesel: '',
      diseases: [],
      age: '',
      sex: '',
      referralNr: '',
      referralDate: new Date(),
      patientType: '',
      mainDoctor: '',
      covidStatus: '',
      name: '',
      surname: '',
      admissionDate: new Date(),
      status: '',
      phoneNumber: '',
      urgent: false,
      createdBy: '',
      creationDate: new Date(),
      modifiedBy: '',
      modificationDate: new Date(),
    };
  }

  getPatient(id: string): Observable<HttpResponse<PatientDetails>> {
    this.patient = this.getEmptyPatientDetails();
    return this.http.get<PatientDetails>(this.url + '/' + id, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'response',
      responseType: 'json'
    });
  }

  readPatientAndEtagFromResponse(response: HttpResponse<PatientDetails>): void {
    this.patient = response.body as PatientDetails;
    this.patient.diseases = response.body?.diseases as DiseaseGeneral[];
    this.eTag = (response.headers.get('ETag') as string).slice(1, -1);
  }

  remove(id: string): Observable<any> {
    return this.http.delete(this.url.concat('/', id), {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  confirm(id: string): Observable<any> {
    return this.http.get(this.url.concat('/confirm/', id), {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  changeUrgency(id: string, newValue: boolean): Observable<any> {
    return this.http.get(this.url + '/urgency/' + id + '/' + String(newValue), {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token'),
        'If-Match': this.eTag
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  changeAdmissionDate(id: string, newDate: string): Observable<any> {
    return this.http.put(this.url + '/date/' + id, {
      newDate
    }, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token'),
        'If-Match': this.eTag,
        'Content-Type': 'text/plain',
      },
      observe: 'body',
      responseType: 'json'
    });
  }
}
