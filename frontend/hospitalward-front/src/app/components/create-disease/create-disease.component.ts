import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DiseaseTableService} from '../../services/disease-table-service';
import {MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxChange} from '@angular/material/checkbox';

@Component({
  selector: 'app-create-disease',
  templateUrl: './create-disease.component.html',
  styleUrls: ['./create-disease.component.less']
})
export class CreateDiseaseComponent implements OnInit {

  constructor(private diseaseService: DiseaseTableService,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialogRef: MatDialogRef<CreateDiseaseComponent>) {
  }

  diseaseForm = new FormGroup({
    name: new FormControl('', Validators.required),
    cathererRequired: new FormControl(false, Validators.required),
    surgeryRequired: new FormControl(false, Validators.required)
  });

  isCathererRequired = false;
  isSurgeryRequired = false;

  ngOnInit(): void {
  }

  create(name: string): void {
    this.diseaseService.addDisease(name, this.isCathererRequired, this.isSurgeryRequired).subscribe(
      () => {
        this.close();
        this.snackBar.open('Pomyślnie utworzono chorobę', '', {
          duration: 2500,
          verticalPosition: 'top'
        });
      },
      (error: any) => {
        if (error.status === 409) {
          this.snackBar.open('Choroba o danej nazwie już istnieje', '', {
            duration: 2500,
            verticalPosition: 'top'
          });
        }
        else {
          this.snackBar.open('Wystąpił błąd podczas tworzenia choroby', '', {
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
