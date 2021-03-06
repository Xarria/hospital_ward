import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DiseaseTableService} from '../../services/disease-table-service';
import {MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-create-disease',
  templateUrl: './create-disease.component.html',
  styleUrls: ['./create-disease.component.less']
})
export class CreateDiseaseComponent implements OnInit {

  constructor(private diseaseService: DiseaseTableService,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialogRef: MatDialogRef<CreateDiseaseComponent>,
              private translate: TranslateService) {
  }

  diseaseForm = new FormGroup({
    polishName: new FormControl('', Validators.required),
    latinName: new FormControl('', Validators.required),
    cathererRequired: new FormControl(false, Validators.required),
    surgeryRequired: new FormControl(false, Validators.required)
  });

  isCathererRequired = false;
  isSurgeryRequired = false;

  ngOnInit(): void {
  }

  create(polishName: string, latinName: string): void {
    this.diseaseService.addDisease(polishName, latinName, this.isCathererRequired, this.isSurgeryRequired).subscribe(
      () => {
        this.close();
        this.snackBar.open(this.translate.instant('snackbar.createDiseaseSuccess'), '', {
          duration: 2500,
          verticalPosition: 'top'
        });
      },
      (error: any) => {
        if (error.status === 409) {
          this.snackBar.open(this.translate.instant('snackbar.createDisease409'), '', {
            duration: 2500,
            verticalPosition: 'top'
          });
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

  close(): void {
    this.dialogRef.close();
  }

  checkSurgeryCheckbox($event: MatCheckboxChange): void {
    this.isSurgeryRequired = $event.checked;
  }

  checkCathererCheckbox($event: MatCheckboxChange): void {
    this.isCathererRequired = $event.checked;
  }
}
