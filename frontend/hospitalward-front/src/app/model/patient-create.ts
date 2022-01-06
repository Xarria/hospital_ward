export interface PatientCreate {
  pesel: string;
  diseases: string[];
  age: string;
  sex: string;
  referralNr: string;
  referralDate: string;
  mainDoctor: string;
  covidStatus: string;
  name: string;
  surname: string;
  phoneNumber: string;
  admissionDate: string;
  urgent: boolean;
}
