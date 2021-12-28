import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from './components/login/login.component';
import {DiseaseListComponent} from './components/disease-list/disease-list.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'diseases', component: DiseaseListComponent}
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
