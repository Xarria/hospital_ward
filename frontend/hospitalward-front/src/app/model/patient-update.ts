
export interface PatientUpdate {
  version: number;
  id: number;
  pesel: string;
  diseases: string[];
  age: string;
  sex: string
  mainDoctor: string;
  covidStatus: string;
  name: string;
  surname: string;
  phoneNumber: string;
}
