import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import jwtDecode from 'jwt-decode';
import { CookieService } from 'ngx-cookie-service';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient,
              private cookieService: CookieService,
              private router: Router) {
    // this.url = environment.appUrl + '/auth';
  }

  auth(login: string, password: string): any {
    // return this.http.post(this.url, {
    //   login,
    //   password
    // }, { observe: 'body', responseType: 'text' });
  }

  public setSession(token: string): void {
    // this.cookieService.set('token', token);
    // this.decodeTokenInfo(token);
  }

  decodeTokenInfo(token: string): void {
    // const tokenInfo: any = jwtDecode(token);
    // this.cookieService.set('login', tokenInfo.sub);
    // this.cookieService.set('accessLevel', tokenInfo.auth);
    // this.cookieService.set('timezone', tokenInfo.zoneinfo);
    // this.cookieService.set('expirationTime', tokenInfo.exp);
  }

  signOut(): void {
    // this.router.navigate(['/']);
    // this.cookieService.delete('token');
    // this.cookieService.delete('login');
    // this.cookieService.delete('currentAccessLevel');
    // this.cookieService.delete('accessLevel');
    // this.cookieService.delete('expirationTime');
    // this.cookieService.delete('timezone');
  }

  public refreshToken(): void {
    // this.http.get(environment.appUrl + '/auth', {
    //   headers: {
    //     Authorization: 'Bearer ' + localStorage.getItem('token')
    //   }, observe: 'body', responseType: 'text'
    // }).subscribe(
    //   (response: string) => {
    //     this.setSession(response);
    //   }
    // );
  }
}
