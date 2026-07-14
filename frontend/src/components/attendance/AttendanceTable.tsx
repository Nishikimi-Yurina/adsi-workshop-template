'use client';

import type { AttendanceRecord } from '@/types/attendance';

interface AttendanceTableProps {
  records: AttendanceRecord[];
  yearMonth: string;
  onPrevMonth: () => void;
  onNextMonth: () => void;
}

function formatTime(dateTimeStr: string): string {
  return new Date(dateTimeStr).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
}

function formatMinutes(minutes: number | null): string {
  if (minutes == null) return '--';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${String(m).padStart(2, '0')}`;
}

export function AttendanceTable({ records, yearMonth, onPrevMonth, onNextMonth }: AttendanceTableProps) {
  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <button onClick={onPrevMonth} className="text-sm text-blue-600 hover:underline">&lt; 前月</button>
        <h3 className="font-medium">{yearMonth}</h3>
        <button onClick={onNextMonth} className="text-sm text-blue-600 hover:underline">次月 &gt;</button>
      </div>
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b border-gray-200">
            <th className="py-2 text-left">日付</th>
            <th className="py-2 text-left">出勤</th>
            <th className="py-2 text-left">退勤</th>
            <th className="py-2 text-left">勤務</th>
            <th className="py-2 text-left">残業</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record) => (
            <tr key={record.id} className="border-b border-gray-100">
              <td className="py-2">{record.date}</td>
              <td className="py-2">{formatTime(record.clockIn)}</td>
              <td className="py-2">{record.clockOut ? formatTime(record.clockOut) : '--:--'}</td>
              <td className="py-2">{formatMinutes(record.workMinutes)}</td>
              <td className="py-2">{formatMinutes(record.overtimeMinutes)}</td>
            </tr>
          ))}
          {records.length === 0 && (
            <tr><td colSpan={5} className="py-4 text-center text-gray-400">記録がありません</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
