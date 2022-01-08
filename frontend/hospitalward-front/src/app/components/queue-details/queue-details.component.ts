import { Component, OnInit, ViewChild } from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {QueueService} from '../../services/queue-service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {PatientGeneral} from '../../model/patient-general';
import {ActivatedRoute} from '@angular/router';
import {MatSort, Sort} from '@angular/material/sort';
import {PatientService} from '../../services/patient-service';
import {PatientDetailsComponent} from '../patient-details/patient-details.component';
import {IdentityService} from '../../services/identity-service';

@Component({
  selector: 'app-queue-detials',
  templateUrl: './queue-details.component.html',
  styleUrls: ['./queue-details.component.less']
})
export class QueueDetailsComponent implements OnInit {

  @ViewChild(MatSort) sort: MatSort = new MatSort();

  patientsData: MatTableDataSource<PatientGeneral>;
  displayedColumns: string[] = ['Position', 'Type', 'Full name', 'Age', 'Sex', 'Admission date', 'Status', 'Urgent', 'Catherer or surgery', ' '];
  date = '';
  patientsList: PatientGeneral[] = [];

  constructor(public queueService: QueueService,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private translate: TranslateService,
              private patientService: PatientService,
              public identityService: IdentityService) {
    this.patientsData = new MatTableDataSource<PatientGeneral>();
    this.date = this.route.snapshot.paramMap.get('date') as string;
  }

  ngOnInit(): void {
    this.getQueue();
    this.patientsData.sort = this.sort;
    const sortState: Sort = {active: 'Position', direction: 'asc'};
    this.sort.active = sortState.active;
    this.sort.direction = sortState.direction;
    this.sort.sortChange.emit(sortState);
  }

  getQueue(): void {
    this.queueService.getQueue(this.date).subscribe(
      (response) => {
        this.queueService.readQueue(response);
        this.patientsList = this.queueService.queue.patientsConfirmed.concat(this.queueService.queue.patientsWaiting);
        this.patientsList.sort((a, b) => (a.positionInQueue > b.positionInQueue) ? 1 :
          ((b.positionInQueue > a.positionInQueue) ? -1 : 0));
        this.patientsData = new MatTableDataSource(this.patientsList);
      }
    );
  }

  refresh(): void {
    this.getQueue();
  }

  openDetails(id: number): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.width = '70%';
    dialogConfig.data = id;
    const dialogRef = this.dialog.open(PatientDetailsComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(() => {
      this.refresh();
    });
  }

  confirmPatient(id: string): void {
    this.patientService.confirm(id).subscribe(
      () => {
        this.snackBar.open(this.translate.instant('snackbar.confirmPatientSuccess'), '', {
          duration: 2500,
          verticalPosition: 'top'
        });
        this.getQueue();
      },
      (error: any) => {
        if (error.status === 404) {
          this.snackBar.open(this.translate.instant('snackbar.patientNotFound'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        if (error.status === 409) {
          this.snackBar.open(this.translate.instant('snackbar.confirmPatient409'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        if (error.error.message === 'error.queue_locked') {
          this.snackBar.open(this.translate.instant('snackbar.queueLocked'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        else {
          this.snackBar.open(this.translate.instant('snackbar.defaultError'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        }
      }
    );
  }

  deletePatient(id: string): void {
    this.patientService.remove(id).subscribe(
      () => {
        this.snackBar.open(this.translate.instant('snackbar.removePatientSuccess'), '', {
          duration: 2500,
          verticalPosition: 'top'
        });
        this.getQueue();
      },
      (error: any) => {
        if (error.status === 409) {
          this.snackBar.open(this.translate.instant('snackbar.confirmPatient409'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        if (error.status === 404) {
          this.snackBar.open(this.translate.instant('snackbar.patientNotFound'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        else {
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
      return age.slice(0, -1) + ' ' + this.translate.instant('patient.years');
    }
    else {
      return age.slice(0, -1) + ' ' + this.translate.instant('patient.months');
    }
  }
}
