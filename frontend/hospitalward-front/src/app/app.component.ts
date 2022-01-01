import { Component } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {CookieService} from 'ngx-cookie-service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  title = 'hospitalward';
  constructor(public translateService: TranslateService,
              private router: Router,
              private cookieService: CookieService) {
    const expirationTime = cookieService.get('expirationTime');
    if (expirationTime != null && expirationTime !== '' && expirationTime < (Date.now().valueOf() / 1000).toString()) {
      this.cookieService.delete('token');
      this.cookieService.delete('login');
      this.cookieService.delete('accessLevel');
      this.cookieService.delete('expirationTime');
      this.cookieService.delete('JSESSIONID');
      this.cookieService.deleteAll();
    }
    translateService.addLangs(['en', 'pl']);
    translateService.setDefaultLang('en');
    const browserLang = translateService.getBrowserLang();
    if (browserLang === 'pl' || browserLang === 'en') {
      translateService.use(browserLang);
    }
    else {
      translateService.use('en');
    }
  }
}
