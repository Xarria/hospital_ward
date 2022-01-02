import { Component, OnInit, ViewChild } from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {QueueService} from '../../services/queue-service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {PatientGeneral} from '../../model/patient-general';
import {ActivatedRoute} from '@angular/router';
import {MatSort, Sort} from '@angular/material/sort';

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

  constructor(public queueService: QueueService,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private translate: TranslateService) {
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
        this.patientsData = new MatTableDataSource<PatientGeneral>(
          response.body?.patientsConfirmed.concat(response.body?.patientsWaiting));
      }
    );
  }

  refresh(): void {
    this.getQueue();
  }

  openDetails(id: number): void {

  }

  deletePatient(id: number): void {

  }

  confirmPatient(id: number): void {

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
