import { Component, OnInit } from '@angular/core';
import {DiseaseTableService} from '../../services/disease-table-service';
import {DiseaseGeneral} from '../../model/disease-general';
import {MatTableDataSource} from '@angular/material/table';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CreateDiseaseComponent} from '../create-disease/create-disease.component';
import {ModifyDiseaseComponent} from '../modify-disease/modify-disease.component';
import {TranslateService} from '@ngx-translate/core';
import {IdentityService} from '../../services/identity-service';

@Component({
  selector: 'app-disease-list',
  templateUrl: './disease-list.component.html',
  styleUrls: ['./disease-list.component.less']
})
export class DiseaseListComponent implements OnInit {

  diseaseData: MatTableDataSource<DiseaseGeneral>;
  displayedColumns: string[] = ['Polish name', 'Latin name', 'Catherer required', 'Surgery required', ' '];
  searchKey = '';

  constructor(private diseaseService: DiseaseTableService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private translate: TranslateService,
              public identityService: IdentityService) {
    this.diseaseData = new MatTableDataSource<DiseaseGeneral>();
  }

  ngOnInit(): void {
    this.getDiseases();
  }

  isPolishLanguage(): boolean {
    return this.translate.currentLang === 'pl';
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
        this.snackBar.open(this.translate.instant('snackbar.deleteDiseaseSuccess'), '', {
          duration: 2500,
          verticalPosition: 'top'
        });
      },
      (error => {
        if (error.status === 409) {
          this.snackBar.open(this.translate.instant('snackbar.deleteDisease409'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
          return;
        }
        if (error.status === 404) {
          this.snackBar.open(this.translate.instant('snackbar.diseaseNotFound'), '', {
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
