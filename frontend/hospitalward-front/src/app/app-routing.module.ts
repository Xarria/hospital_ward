import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from './components/login/login.component';
import {DiseaseListComponent} from './components/disease-list/disease-list.component';
import {QueueListComponent} from './components/queue-list/queue-list.component';
import {QueueDetailsComponent} from './components/queue-details/queue-details.component';
import {CreatePatientComponent} from './components/create-patient/create-patient.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'diseases', component: DiseaseListComponent},
  { path: 'queues', component: QueueListComponent},
  { path: 'queues/:date', component: QueueDetailsComponent},
  {path: 'patient', component: CreatePatientComponent}
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
