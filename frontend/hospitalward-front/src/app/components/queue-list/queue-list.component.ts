import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {QueueService} from '../../services/queue-service';
import {Queue} from '../../model/queue';
import {MatSelectChange} from '@angular/material/select';
import {Router} from '@angular/router';
import {MatSort} from '@angular/material/sort';

@Component({
  selector: 'app-queue-list',
  templateUrl: './queue-list.component.html',
  styleUrls: ['./queue-list.component.less']
})
export class QueueListComponent implements OnInit {

  @ViewChild(MatSort) sort: MatSort | undefined;

  queueData: MatTableDataSource<Queue>;
  displayedColumns: string[] = ['Date', 'Patients waiting', 'Patients confirmed', 'Locked', ' '];
  searchKey = '';
  selectedMonth = '';

  constructor(private queueService: QueueService,
              private router: Router) {
    this.queueData = new MatTableDataSource<Queue>();
  }

  ngOnInit(): void {
    this.getQueues();
  }

  getQueues(): void {
    this.queueService.getQueues().subscribe(
      (queues: Queue[]) => {
        this.queueData = new MatTableDataSource<Queue>(queues);
      }
    );
  }

  clearSearch(): void {
    this.searchKey = '';
    this.applyFilter();
  }

  applyFilter(): void {
    this.queueData.filter = this.searchKey.trim().toLowerCase();
  }

  refresh(): void {
    this.getQueues();
  }

  openDetails(date: string): void {
    this.router.navigate(['/queues/' + date]);
  }


  changeFilterMonth($event: MatSelectChange): void {
    this.queueData.filter = $event.value.toLowerCase() + '-';
  }

  clearFilter($event: any): void {
    this.selectedMonth = '';
    $event.stopPropagation();
    this.queueData.filter = '';
  }
}
