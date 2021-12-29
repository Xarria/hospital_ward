import { Component, OnInit } from '@angular/core';
import {IdentityService} from '../../services/identity-service';
import {AuthService} from '../../services/auth-service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.less']
})
export class NavigationComponent implements OnInit {

  constructor(public identityService: IdentityService,
              private authService: AuthService) { }

  ngOnInit(): void {
  }

  signOut(): void {
    this.authService.signOut();
  }
}
