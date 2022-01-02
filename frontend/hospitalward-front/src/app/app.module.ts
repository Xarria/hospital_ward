import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DiseaseListComponent} from './components/disease-list/disease-list.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatTableModule} from '@angular/material/table';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {CookieService} from 'ngx-cookie-service';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {IdentityService} from './services/identity-service';
import {AuthService} from './services/auth-service';
import { NavigationComponent } from './components/navigation/navigation.component';
import {CdkColumnDef} from '@angular/cdk/table';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatDialogModule} from '@angular/material/dialog';
import { CreateDiseaseComponent } from './components/create-disease/create-disease.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatCheckboxModule} from '@angular/material/checkbox';
import { ModifyDiseaseComponent } from './components/modify-disease/modify-disease.component';
import {MatGridListModule} from '@angular/material/grid-list';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import { QueueListComponent } from './components/queue-list/queue-list.component';
import {MatSelectModule} from '@angular/material/select';
import {CommonModule} from '@angular/common';
import { QueueDetailsComponent } from './components/queue-details/queue-details.component';
import {MatSortModule} from '@angular/material/sort';

export function rootLoaderFactory(http: HttpClient): any {
  return new TranslateHttpLoader(http);
}

// @ts-ignore
@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DiseaseListComponent,
    NavigationComponent,
    CreateDiseaseComponent,
    ModifyDiseaseComponent,
    QueueListComponent,
    QueueDetailsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatTableModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    HttpClientModule,
    MatIconModule,
    FormsModule,
    MatSnackBarModule,
    MatDialogModule,
    MatButtonToggleModule,
    MatCheckboxModule,
    MatGridListModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: rootLoaderFactory,
        deps: [HttpClient]
      }
    }),
    MatSelectModule,
    MatSortModule,
  ],
  providers: [
    CookieService,
    IdentityService,
    AuthService,
    CdkColumnDef
  ],
  bootstrap: [AppComponent],
  entryComponents: [CreateDiseaseComponent, ModifyDiseaseComponent]
})
export class AppModule {
}
