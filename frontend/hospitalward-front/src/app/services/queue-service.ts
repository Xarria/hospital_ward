import {HttpClient, HttpResponse} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';
import {environment} from '../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {Queue} from '../model/queue';
import {DiseaseDetails} from '../model/disease-details';

@Injectable({
  providedIn: 'root'
})
export class QueueService {
  public queues: Queue[];
  public queue: Queue;
  private readonly url: string;

  constructor(private http: HttpClient,
              private cookieService: CookieService) {
    this.url = environment.appUrl + 'queues';
    this.queues = [];
    this.queue = this.getEmptyQueue();
  }

  private getEmptyQueue(): Queue {
    return {
      date: new Date(),
      patientsWaiting: [],
      patientsConfirmed: [],
      locked: false,
    };
  }

  getQueues(): Observable<Queue[]> {
    return this.http.get<Queue[]>(this.url, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'body',
      responseType: 'json'
    });
  }

  getQueue(date: string): Observable<HttpResponse<Queue>> {
    this.queue = this.getEmptyQueue();
    return this.http.get<Queue>(this.url + '/' + date, {
      headers: {
        Authorization: 'Bearer ' + this.cookieService.get('token')
      },
      observe: 'response',
      responseType: 'json'
    });
  }

  readQueue(response: HttpResponse<Queue>): void {
    this.queue = response.body as Queue;
  }
}
