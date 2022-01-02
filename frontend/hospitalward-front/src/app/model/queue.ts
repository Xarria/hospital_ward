import {PatientGeneral} from './patient-general';

export interface Queue {
  date: Date;
  patientsWaiting: PatientGeneral[];
  patientsConfirmed: PatientGeneral[];
  locked: boolean;
}
