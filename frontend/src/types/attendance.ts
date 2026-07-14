export interface AttendanceRecord {
  id: number;
  date: string;
  clockIn: string;
  clockOut: string | null;
  workMinutes: number | null;
  overtimeMinutes: number | null;
}

export interface MonthlyReport {
  employeeId: number;
  employeeName: string;
  yearMonth: string;
  workDays: number;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
}
