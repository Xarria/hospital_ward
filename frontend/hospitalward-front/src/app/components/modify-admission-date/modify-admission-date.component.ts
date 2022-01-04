import {Component, Inject, OnInit} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS} from '@angular/material/core';
import {APP_DATE_FORMATS, AppDateAdapter} from '../../adapters/app-date-adapter';
import {QueueService} from '../../services/queue-service';
import {TranslateService} from '@ngx-translate/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PatientService} from '../../services/patient-service';

@Component({
  selector: 'app-modify-admission-date',
  templateUrl: './modify-admission-date.component.html',
  styleUrls: ['./modify-admission-date.component.less'],
  providers: [
    {provide: DateAdapter, useClass: AppDateAdapter},
    {provide: MAT_DATE_FORMATS, useValue: APP_DATE_FORMATS}
  ]
})
export class ModifyAdmissionDateComponent implements OnInit {

  minDate = new Date();
  fullDatesArray: Array<number>[] = [];
  fullDates: number[] = [];
  pickedDate: Date | null = null;

  myFilter = (d: Date | null): boolean => {
    const day = (d || new Date()).getDay();
    return day !== 0 && day !== 6 && day !== 5 && !this.fullDates.includes(d?.getTime() as number);
  }

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private queueService: QueueService,
              private dateAdapter: DateAdapter<Date>,
              private translate: TranslateService,
              private snackBar: MatSnackBar,
              private patientService: PatientService) {
    this.minDate.setDate(this.minDate.getDate() + 14);
    this.getFullAdmissionDates();
    this.dateAdapter.setLocale(translate.currentLang);
  }

  ngOnInit(): void {
  }

  getFullAdmissionDates(): void {
    this.queueService.getFullAdmissionDates().subscribe(
      (response) => {
        this.fullDatesArray = response.body as Array<number>[];
        this.fullDates = this.fullDatesArray.map(d => new Date(d[0], d[1] - 1, d[2])).map(d => d.getTime());
      }
    );
  }

  changeAdmissionDate(): void {
    this.pickedDate?.setMonth(this.pickedDate?.getMonth() + 1);
    let date: string;
    if ( this.pickedDate?.getMonth() as number >= 10) {
      date = this.pickedDate?.getDate()
        + '-' + this.pickedDate?.getMonth() + '-' + this.pickedDate?.getFullYear();
    }
    else {
      date = this.pickedDate?.getDate()
        + '-0' + this.pickedDate?.getMonth() + '-' + this.pickedDate?.getFullYear();
    }
    console.log(date);
    this.patientService.changeAdmissionDate(this.data, date)
      .subscribe(
        () => {
          this.close();
          this.snackBar.open(this.translate.instant('snackbar.modifyDiseaseSuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        },
        (error: any) => {
          if (error.status === 404) {
            this.snackBar.open(this.translate.instant('snackbar.modifyDisease404'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
          if (error.status === 400) {
            this.snackBar.open(this.translate.instant('snackbar.modifyDisease400'), '', {
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
    this.pickedDate = new Date();
  }

  private close(): void {

  }
}
