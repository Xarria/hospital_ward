import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth-service';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService,
              private router: Router,
              private snackBar: MatSnackBar) {
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
        this.goToCalendar();
      },
      () => this.displayError()
    );
  }

  goToCalendar(): void {
    this.router.navigate(['/diseases']);
  }

  displayError(): void {
    this.snackBar.open('Niepoprawne dane logowania', 'OK', {
      duration: 2000,
      verticalPosition: 'top'
    });
  }
}
