import { Component, OnInit } from '@angular/core';
import {DiseaseTableService} from '../../services/disease-table-service';
import {DiseaseGeneral} from '../../model/disease-general';
import {MatTableDataSource} from '@angular/material/table';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CreateDiseaseComponent} from '../create-disease/create-disease.component';
import {ModifyDiseaseComponent} from '../modify-disease/modify-disease.component';

@Component({
  selector: 'app-disease-list',
  templateUrl: './disease-list.component.html',
  styleUrls: ['./disease-list.component.less']
})
export class DiseaseListComponent implements OnInit {

  diseaseData: MatTableDataSource<DiseaseGeneral>;
  displayedColumns: string[] = ['Name', 'Catherer required', 'Surgery required', ' '];
  searchKey = '';

  constructor(private diseaseService: DiseaseTableService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
    this.diseaseData = new MatTableDataSource<DiseaseGeneral>();
  }

  ngOnInit(): void {
    this.getDiseases();
  }

  getDiseases(): void {
    this.diseaseService.getDiseases().subscribe(
      (diseaseGenerals: DiseaseGeneral[]) => {
        this.diseaseData = new MatTableDataSource<DiseaseGeneral>(diseaseGenerals);
      }
    );
  }

  clearSearch(): void {
    this.searchKey = '';
    this.applyFilter();
  }

  applyFilter(): void {
    this.diseaseData.filter = this.searchKey.trim().toLowerCase();
  }

  refresh(): void {
    this.getDiseases();
  }

  deleteDisease(name: string): void {
    this.diseaseService.remove(name).subscribe(
      () => {
        this.getDiseases();
        this.snackBar.open('Pomyślnie usunięto chorobę', '', {
          duration: 2500,
          verticalPosition: 'top'
        });
      },
      (error => {
        if (error.status === 409) {
          this.snackBar.open('Nie można usunąć choroby, ponieważ jest ona przypisana do pacjenta', '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        }
        else {
          this.snackBar.open('Wystąpił błąd podczas usuwania choroby', '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        }
        })
    );
  }

  createDisease(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.width = '40%';
    const dialogRef = this.dialog.open(CreateDiseaseComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(() => {
      this.refresh();
    });
  }

  openDetails(name: string): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = false;
    dialogConfig.width = '50%';
    dialogConfig.data = name;
    const dialogRef = this.dialog.open(ModifyDiseaseComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(() => {
      this.refresh();
    });
  }

}
