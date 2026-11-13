// Shared types for the StudentHub frontend.
// These mirror the backend Student entity (Student.java).

export type Student = {
  id: number;
  name: string;
  email: string;
  department: string;
  yearOfStudy: number;
  phoneNumber: string;
};

// What we send to the backend on POST / PUT.
// There is no "id": the backend generates it.
export type StudentInput = {
  name: string;
  email: string;
  department: string;
  yearOfStudy: number;
  phoneNumber: string;
};
