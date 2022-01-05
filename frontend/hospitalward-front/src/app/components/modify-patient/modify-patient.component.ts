import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {PatientService} from '../../services/patient-service';
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DiseaseGeneral} from '../../model/disease-general';
import {DiseaseTableService} from '../../services/disease-table-service';

@Component({
  selector: 'app-modify-patient',
  templateUrl: './modify-patient.component.html',
  styleUrls: ['./modify-patient.component.less']
})
export class ModifyPatientComponent implements OnInit {

  patientId = '';
  diseasesArray: DiseaseGeneral[] = [];
  ageUnit = '';
  age = '';

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public patientService: PatientService,
              private translate: TranslateService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private diseaseService: DiseaseTableService,
              private dialogRef: MatDialogRef<ModifyPatientComponent>) {
    this.patientId = this.data;
    this.getPatient();
    this.getDiseases();
  }

  ngOnInit(): void {
  }

  compareDiseases(d1: DiseaseGeneral, d2: DiseaseGeneral): boolean {
    return d1 && d2 ? d1.latinName === d2.latinName : d1 === d2;
  }

  getPatient(): void {
    this.patientService.getPatient(this.patientId).subscribe(
      (response) => {
        this.patientService.readPatientAndEtagFromResponse(response);
      }
    );
  }

  getDiseases(): void {
    this.diseaseService.getDiseases().subscribe(
      (diseaseGenerals: DiseaseGeneral[]) => {
        this.diseasesArray = diseaseGenerals;
      }
    );
  }

  isPolishLanguage(): boolean {
    return this.translate.currentLang === 'pl';
  }

  modify(): void {
    if (this.age === '') {
      this.age = this.patientService.patient.age.slice(0, -1);
    }
    if (this.ageUnit === '') {
      this.ageUnit = this.patientService.patient.age.charAt(this.patientService.patient.age.length - 1);
    }
    this.patientService.modifyPatient(this.patientService.patient, this.age + this.ageUnit)
      .subscribe(
        () => {
          this.snackBar.open(this.translate.instant('snackbar.urgencySuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          this.dialogRef.close();
        },
        (error: any) => {
          if (error.status === 404) {
            this.snackBar.open(this.translate.instant('snackbar.patient404'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
          if (error.status === 400) {
            this.snackBar.open(this.translate.instant('snackbar.urgency400'), '', {
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

  displayAge(age: string): string {
    if (age.charAt(age.length - 1) === 'Y') {
      return age.slice(0, -1);
    } else {
      return age.slice(0, -1);
    }
  }

  displayAgeUnit(age: string): string {
    return age.charAt(age.length - 1);
  }

}
