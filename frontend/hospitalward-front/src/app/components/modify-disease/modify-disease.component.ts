import {Component, Inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DiseaseDetailsService} from '../../services/disease-details-service';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-modify-disease',
  templateUrl: './modify-disease.component.html',
  styleUrls: ['./modify-disease.component.less']
})
export class ModifyDiseaseComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public diseaseService: DiseaseDetailsService,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialogRef: MatDialogRef<ModifyDiseaseComponent>,
              private translate: TranslateService) {
    this.latinName = this.data;

    this.getDisease();
  }

  latinName = '';

  ngOnInit(): void {
  }

  getDisease(): void {
    this.diseaseService.getDisease(this.latinName).subscribe(
      (response) => {
        this.diseaseService.readDiseaseAndEtagFromResponse(response);
      }
    );
  }

  isPolishLanguage(): boolean {
    return this.translate.currentLang === 'pl';
  }

  modify(): void {
    this.diseaseService.modifyDisease(this.latinName, this.diseaseService.disease.cathererRequired,
      this.diseaseService.disease.surgeryRequired)
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
          this.getDisease();
        }
      );
  }

  close(): void {
    this.dialogRef.close();
  }

}
