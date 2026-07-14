'use client';

import type { AttendanceRecord } from '@/types/attendance';

interface TodayStatusProps {
  record: AttendanceRecord | null;
}

function formatTime(dateTimeStr: string): string {
  return new Date(dateTimeStr).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${String(m).padStart(2, '0')}`;
}

export function TodayStatus({ record }: TodayStatusProps) {
  if (!record) {
    return (
      <div className="bg-white p-4 rounded-lg border border-gray-200">
        <h3 className="text-sm font-medium text-gray-500 mb-2">今日の勤怠</h3>
        <p className="text-gray-400">未出勤</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-4 rounded-lg border border-gray-200">
      <h3 className="text-sm font-medium text-gray-500 mb-2">今日の勤怠</h3>
      <div className="space-y-1 text-sm">
        <div>出勤: <span className="font-medium">{formatTime(record.clockIn)}</span></div>
        <div>退勤: <span className="font-medium">{record.clockOut ? formatTime(record.clockOut) : '--:--'}</span></div>
        {record.workMinutes != null && (
          <>
            <div>勤務時間: <span className="font-medium">{formatMinutes(record.workMinutes)}</span></div>
            {record.overtimeMinutes != null && record.overtimeMinutes > 0 && (
              <div>残業: <span className="font-medium text-orange-600">{formatMinutes(record.overtimeMinutes)}</span></div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
