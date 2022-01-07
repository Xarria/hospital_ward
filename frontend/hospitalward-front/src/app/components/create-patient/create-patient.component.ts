import {Component, OnInit} from '@angular/core';
import {DiseaseGeneral} from '../../model/disease-general';
import {PatientService} from '../../services/patient-service';
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DiseaseTableService} from '../../services/disease-table-service';
import {DateAdapter} from '@angular/material/core';
import {QueueService} from '../../services/queue-service';
import {Router} from '@angular/router';
import {Doctor} from '../../model/doctor';
import {FormBuilder, FormControl, Validators} from '@angular/forms';

@Component({
  selector: 'app-create-patient',
  templateUrl: './create-patient.component.html',
  styleUrls: ['./create-patient.component.less']
})
export class CreatePatientComponent implements OnInit {

  diseasesArray: DiseaseGeneral[] = [];
  ageUnit = '';
  age = '';
  minDate = new Date();
  fullDatesArray: Array<number>[] = [];
  fullDates: number[] = [];
  pickedDate: Date | null = null;
  pickedAdmissionDate: Date | null = null;
  pickedDiseases: DiseaseGeneral[] = [];
  doctorsArray: Doctor[] = [];

  patientForm = this.formBuilder.group({
    pesel: new FormControl('', [Validators.required, Validators.pattern('[0-9]{11}')]),
    diseases: new FormControl('', Validators.required),
    age: new FormControl('', [Validators.required, Validators.pattern('[0-9]*')]),
    ageUnit: new FormControl('', [Validators.required, Validators.pattern('[M,Y]')]),
    sex: new FormControl('', [Validators.required, Validators.pattern('[F,M]')]),
    referralNr: new FormControl(''),
    referralDate: new FormControl(''),
    mainDoctor: new FormControl('', Validators.required),
    covidStatus: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    phoneNumber: new FormControl('', [Validators.required, Validators.pattern('[0-9]*')]),
    admissionDate: new FormControl('', Validators.required),
    urgent: new FormControl(false)
  });

  myFilter = (d: Date | null): boolean => {
    const day = (d || new Date()).getDay();
    return day !== 0 && day !== 6 && day !== 5 && !this.fullDates.includes(d?.getTime() as number);
  }

  constructor(public patientService: PatientService,
              private translate: TranslateService,
              private snackBar: MatSnackBar,
              private diseaseService: DiseaseTableService,
              private dateAdapter: DateAdapter<Date>,
              private queueService: QueueService,
              private router: Router,
              private formBuilder: FormBuilder) {
    this.getDiseases();
    this.getDoctors();
    this.minDate.setDate(this.minDate.getDate() + 14);
    this.getFullAdmissionDates();
    this.dateAdapter.setLocale(translate.currentLang);
  }

  getFullAdmissionDates(): void {
    this.queueService.getFullAdmissionDates().subscribe(
      (response) => {
        this.fullDatesArray = response.body as Array<number>[];
        this.fullDates = this.fullDatesArray.map(d => new Date(d[0], d[1] - 1, d[2])).map(d => d.getTime());
      }
    );
  }

  ngOnInit(): void {
  }

  getDateString(pickedDate: Date | null): string {
    let date: string;
    const month = pickedDate?.getMonth() as number + 1;
    if (month as number >= 10) {
      date = pickedDate?.getDate()
        + '-' + month + '-' + pickedDate?.getFullYear();
    } else {
      date = pickedDate?.getDate()
        + '-0' + month + '-' + pickedDate?.getFullYear();
    }
    if ((pickedDate?.getDate() as number) < 10) {
      date = '0' + date;
    }
    return date;
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

  setPatientAge(): void {
    this.patientService.patientCreate.age = this.age + this.ageUnit;
  }

  submit(): void {
    this.setPatientAge();
    this.patientService.patientCreate.diseases = this.pickedDiseases.map(d => d.latinName);
    this.patientService.patientCreate.admissionDate = this.getDateString(this.pickedAdmissionDate);
    if (this.pickedDate != null) {
      this.patientService.patientCreate.referralDate = this.getDateString(this.pickedDate);
    }
    if (this.patientService.patientCreate.urgent) {
      this.createPatientUrgent();
    } else {
      this.createPatient();
    }
  }

  getDoctors(): void {
    this.patientService.getDoctors().subscribe(
      (doctors: Doctor[]) => {
        this.doctorsArray = doctors;
      }
    );
  }

  createPatient(): void {
    this.patientService.createPatient(this.patientService.patientCreate)
      .subscribe(
        () => {
          this.snackBar.open(this.translate.instant('snackbar.patientCreateSuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          this.router.navigate(['/queues']);
          this.patientService.patientCreate = this.patientService.getEmptyPatientCreate();
        },
        (error: any) => {
          if (error.error.message === 'error.referral_number_or_date_required') {
            this.snackBar.open(this.translate.instant('snackbar.patientCreateReferralError'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
            return;
          }
          if (error.error.message === 'error.error.disease_not_found') {
            this.snackBar.open(this.translate.instant('snackbar.diseaseNotFound'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
            return;
          }
          if (error.error.message === 'error.queue_for_date_is_locked_or_full') {
            this.snackBar.open(this.translate.instant('snackbar.patientCreateQueueLocked'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
            return;
          } else {
            this.snackBar.open(this.translate.instant('snackbar.defaultError'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
        }
      );
  }

  createPatientUrgent(): void {
    this.patientService.createPatientUrgent(this.patientService.patientCreate)
      .subscribe(
        () => {
          this.snackBar.open(this.translate.instant('snackbar.urgencySuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          this.router.navigate(['/queues']);
          this.patientService.patientCreate = this.patientService.getEmptyPatientCreate();
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
