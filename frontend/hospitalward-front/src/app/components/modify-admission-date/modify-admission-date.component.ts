import {Component, Inject, OnInit} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS} from '@angular/material/core';
import {APP_DATE_FORMATS, AppDateAdapter} from '../../adapters/app-date-adapter';
import {QueueService} from '../../services/queue-service';
import {TranslateService} from '@ngx-translate/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
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
    if (this.patientService.patient.urgent) {
      return day !== 0 && day !== 6 && day !== 5;
    }
    return day !== 0 && day !== 6 && day !== 5 && !this.fullDates.includes(d?.getTime() as number);
  }

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private queueService: QueueService,
              private dateAdapter: DateAdapter<Date>,
              private translate: TranslateService,
              private snackBar: MatSnackBar,
              private patientService: PatientService,
              private dialogRef: MatDialogRef<ModifyAdmissionDateComponent>) {
    this.minDate.setDate(this.minDate.getDate() + 14);
    this.getFullAdmissionDates();
    this.dateAdapter.setLocale('pl');
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

  changeAdmissionDate(): void {
    this.patientService.changeAdmissionDate(this.data, this.getDateString(this.pickedDate))
      .subscribe(
        () => {
          this.close();
          this.snackBar.open(this.translate.instant('snackbar.modifyAdmissionSuccess'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        },
        (error: any) => {
          if (error.error.message === 'error.patient_already_admitted') {
            this.snackBar.open(this.translate.instant('snackbar.modifyAdmissionAlreadyAdmitted'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
            return;
          }
          if (error.error.message === 'error.patient_not_found') {
            this.snackBar.open(this.translate.instant('snackbar.patientNotFound'), '', {
              duration: 2500,
              verticalPosition: 'top'
            });
            return;
          }
          if (error.error.message === 'error.queue_for_date_is_locked_or_full') {
            this.snackBar.open(this.translate.instant('snackbar.modifyAdmissionQueueLocked'), '', {
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
    this.pickedDate = new Date();
  }

  private close(): void {
    this.dialogRef.close();
  }
}
