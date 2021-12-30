import {Component, Inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {DiseaseDetailsService} from '../../services/disease-details-service';

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
              private dialogRef: MatDialogRef<ModifyDiseaseComponent>) {
    this.name = this.data;

    this.getDisease();
  }

  name = '';

  ngOnInit(): void {
  }

  getDisease(): void {
    this.diseaseService.getDisease(this.name).subscribe(
      (response) => {
        this.diseaseService.readDiseaseAndEtagFromResponse(response);
      }
    );
  }

  modify(): void {
    this.diseaseService.modifyDisease(this.name, this.diseaseService.disease.cathererRequired, this.diseaseService.disease.surgeryRequired)
      .subscribe(
        () => {
          this.close();
          this.snackBar.open('Pomyślnie zmodyfikowano chorobę', '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        },
        (error: any) => {
          if (error.status === 404) {
            this.snackBar.open('Choroba nie istnieje', '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          }
          if (error.status === 400) {
            this.snackBar.open('Choroba została zmieniona w trakcie modyfikacji', '', {
              duration: 2500,
              verticalPosition: 'top'
            });
          } else {
            this.snackBar.open('Wystąpił błąd podczas tworzenia choroby', '', {
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
