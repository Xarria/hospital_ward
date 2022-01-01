import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable()
export class IdentityService {

  constructor(private cookieService: CookieService) {
  }

  getLogin(): string {
    return this.cookieService.get('login') as string;
  }

  isTreatmentDirector(): boolean {
    return this.cookieService.get('accessLevel') === 'TREATMENT DIRECTOR';
  }

  isDoctor(): boolean {
    return this.cookieService.get('accessLevel') === 'DOCTOR';
  }

  isTreatmentDirectorOrDoctor(): boolean {
    return this.cookieService.get('accessLevel') === 'TREATMENT DIRECTOR' || this.cookieService.get('accessLevel') === 'DOCTOR';
  }

  isHeadNurse(): boolean {
    return this.cookieService.get('accessLevel') === 'HEAD NURSE';
  }

  isSecretary(): boolean {
    return this.cookieService.get('accessLevel') === 'SECRETARY';
  }

  isGuest(): boolean {
    return this.cookieService.get('accessLevel') === '';
  }

  getAccessLevel(): string {
    return this.cookieService.get('accessLevel');
  }
}
