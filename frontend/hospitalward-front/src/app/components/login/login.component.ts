import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth-service';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService,
              private router: Router,
              private snackBar: MatSnackBar,
              private translate: TranslateService) {
  }

  loginForm = new FormGroup({
    login: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  });

  ngOnInit(): void {
  }

  login(login: string, password: string): void {
    this.authService.auth(login, password).subscribe(
      (response: string) => {
        this.authService.setSession(response);
        this.goToQueues();
      },
      () => this.displayError()
    );
  }

  goToQueues(): void {
    this.router.navigate(['/queues']);
  }

  displayError(): void {
    this.snackBar.open(this.translate.instant('snackbar.loginInvalid'), '', {
      duration: 2000,
      verticalPosition: 'top'
    });
  }
}
