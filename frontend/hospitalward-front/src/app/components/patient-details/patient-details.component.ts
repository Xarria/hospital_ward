import {Component, Inject, OnInit} from '@angular/core';
import {PatientService} from '../../services/patient-service';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.less']
})
export class PatientDetailsComponent implements OnInit {

  patientId = '';

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public patientService: PatientService,
              private translate: TranslateService) {
    this.patientId = this.data;
    this.getPatient();
  }

  ngOnInit(): void {
  }

  getPatient(): void {
    this.patientService.getPatient(this.patientId).subscribe(
      (response) => {
        this.patientService.readPatientAndEtagFromResponse(response);
      }
    );
  }

  isPolishLanguage(): boolean {
    return this.translate.currentLang === 'pl';
  }

  refresh(): void {

  }

  modify(): void {

  }

  changeAdmissionDate(): void {
  }

  changeUrgency(): void {
  }

  getDiseaseList(): string {
    if (this.isPolishLanguage()) {
      return this.getDiseaseListPolish();
    }
    else {
      return this.getDiseaseListLatin();
    }
  }

  getDiseaseListLatin(): string {
    const diseaseGenerals = this.patientService.patient.diseases;
    let diseases = '';
    diseases = diseaseGenerals.map(disease => disease.latinName).join(', ');
    return diseases;
  }

  getDiseaseListPolish(): string {
    const diseaseGenerals = this.patientService.patient.diseases;
    let diseases = '';
    diseases = diseaseGenerals.map(disease => disease.polishName).join(', ');
    return diseases;
  }
}
