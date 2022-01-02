export interface PatientGeneral {
  id: number;
  age: string;
  sex: string;
  patientType: string;
  name: string;
  surname: string;
  positionInQueue: number;
  admissionDate: Date;
  status: string;
  urgent: boolean;
  cathererRequired: boolean;
  surgeryRequired: boolean;
}
