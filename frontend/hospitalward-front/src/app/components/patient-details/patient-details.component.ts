import {Component, Inject, OnInit} from '@angular/core';
import {PatientService} from '../../services/patient-service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ModifyAdmissionDateComponent} from '../modify-admission-date/modify-admission-date.component';
import {ModifyPatientComponent} from '../modify-patient/modify-patient.component';

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.less']
})
export class PatientDetailsComponent implements OnInit {

  patientId = '';

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public patientService: PatientService,
              private translate: TranslateService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
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
    this.getPatient();
  }

  modify(id: number): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.width = '30%';
    dialogConfig.data = id;
    const dialogRef = this.dialog.open(ModifyPatientComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(() => {
      this.refresh();
    });

  }

  changeAdmissionDate(id: number): void {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.autoFocus = true;
      dialogConfig.width = '20%';
      dialogConfig.data = id;
      const dialogRef = this.dialog.open(ModifyAdmissionDateComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(() => {
        this.refresh();
      });
  }

  changeUrgency(currentUrgency: boolean, id: string): void {
    this.patientService.changeUrgency(id, !currentUrgency)
      .subscribe(
        () => {
          this.snackBar.open(this.translate.instant('snackbar.urgencySuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          this.getPatient();
        },
        (error: any) => {
          if (error.status === 404) {
            this.snackBar.open(this.translate.instant('snackbar.patientNotFound'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
          if (error.status === 400) {
            this.snackBar.open(this.translate.instant('snackbar.modifyAdmissionAlreadyAdmitted'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          } else {
            this.snackBar.open(this.translate.instant('snackbar.defaultError'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
          this.getPatient();
        }
      );
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

  displaySex(sex: string): string {
    if (sex === 'F') {
      return this.translate.instant('patient.female');
    }
    else {
      return this.translate.instant('patient.male');
    }
  }

  displayAge(age: string): string {
    if (age.charAt(age.length - 1) === 'Y') {
      return age.slice(0, -1) + ' ' + this.translate.instant('patient.years');
    }
    else {
      return age.slice(0, -1) + ' ' + this.translate.instant('patient.months');
    }
  }

  displayType(patientType: string): string {
    if (patientType === 'URGENT') {
      return this.translate.instant('patient.patientType.urgent');
    }
    if (patientType === 'GIRL') {
      return this.translate.instant('patient.patientType.girl');
    }
    if (patientType === 'BOY') {
      return this.translate.instant('patient.patientType.boy');
    }
    if (patientType === 'INTENSIVE_SUPERVISION') {
      return this.translate.instant('patient.patientType.intensive');
    }
    else {
      return this.translate.instant('patient.patientType.under6');
    }
  }

  displayCovidStatus(status: string): string {
    if (status === 'VACCINATED') {
      return this.translate.instant('patient.covidStatusList.vaccinated');
    }
    if (status === 'UNVACCINATED') {
      return this.translate.instant('patient.covidStatusList.unvaccinated');
    }
    else {
      return this.translate.instant('patient.covidStatusList.afterIllness');
    }
  }

  displayStatus(status: string): string {
    if (status === 'WAITING') {
      return this.translate.instant('patient.confirmationStatus.waiting');
    }
    if (status === 'CONFIRMED_ONCE') {
      return this.translate.instant('patient.confirmationStatus.confirmedOnce');
    }
    else {
      return this.translate.instant('patient.confirmationStatus.confirmedTwice');
    }
  }

  displayUrgency(urgent: boolean): string {
    if (urgent) {
      return this.translate.instant('patient.yes');
    }
    else {
      return this.translate.instant('patient.no');
    }
  }
}
