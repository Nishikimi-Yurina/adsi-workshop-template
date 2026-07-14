'use client';

import type { MonthlyReport } from '@/types/attendance';

interface MonthlyReportTableProps {
  reports: MonthlyReport[];
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${String(m).padStart(2, '0')}`;
}

export function MonthlyReportTable({ reports }: MonthlyReportTableProps) {
  return (
    <table className="w-full text-sm border-collapse">
      <thead>
        <tr className="border-b border-gray-200">
          <th className="py-2 text-left">社員名</th>
          <th className="py-2 text-left">勤務日数</th>
          <th className="py-2 text-left">総勤務時間</th>
          <th className="py-2 text-left">残業時間</th>
        </tr>
      </thead>
      <tbody>
        {reports.map((report) => (
          <tr key={report.employeeId} className="border-b border-gray-100">
            <td className="py-2">{report.employeeName}</td>
            <td className="py-2">{report.workDays}日</td>
            <td className="py-2">{formatMinutes(report.totalWorkMinutes)}</td>
            <td className="py-2">{formatMinutes(report.totalOvertimeMinutes)}</td>
          </tr>
        ))}
        {reports.length === 0 && (
          <tr><td colSpan={4} className="py-4 text-center text-gray-400">データがありません</td></tr>
        )}
      </tbody>
    </table>
  );
}
