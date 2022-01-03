import {DiseaseGeneral} from './disease-general';

export interface PatientDetails {
  version: number;
  id: number;
  pesel: string;
  diseases: DiseaseGeneral[];
  age: string;
  sex: string;
  referralNr: string;
  referralDate: Date;
  patientType: string;
  mainDoctor: string;
  covidStatus: string;
  name: string;
  surname: string;
  admissionDate: Date;
  status: string;
  phoneNumber: string;
  urgent: boolean;
  createdBy: string;
  creationDate: Date;
  modifiedBy: string;
  modificationDate: Date;
}
