'use client';

import { useState } from 'react';
import { apiFetch, ApiError } from '@/lib/api-client';

interface AttendanceRecord {
  id: number;
  date: string;
  clockIn: string;
  clockOut: string | null;
  workMinutes: number | null;
  overtimeMinutes: number | null;
}

interface ClockButtonProps {
  todayRecord: AttendanceRecord | null;
  onUpdate: () => void;
}

export function ClockButton({ todayRecord, onUpdate }: ClockButtonProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const hasClockedIn = todayRecord !== null;
  const hasClockedOut = todayRecord?.clockOut !== null;

  const handleClockIn = async () => {
    setLoading(true);
    setError('');
    try {
      await apiFetch<AttendanceRecord>('/api/attendance/clock-in', { method: 'POST' });
      onUpdate();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.status === 409 ? '既に出勤済みです' : '打刻に失敗しました');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClockOut = async () => {
    setLoading(true);
    setError('');
    try {
      await apiFetch<AttendanceRecord>('/api/attendance/clock-out', { method: 'POST' });
      onUpdate();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.status === 409 ? '既に退勤済みです' : '打刻に失敗しました');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-3">
      {error && <div className="text-red-600 text-sm">{error}</div>}
      <div className="flex gap-4">
        <button
          onClick={handleClockIn}
          disabled={loading || hasClockedIn}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          出勤する
        </button>
        <button
          onClick={handleClockOut}
          disabled={loading || !hasClockedIn || hasClockedOut}
          className="px-6 py-3 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          退勤する
        </button>
      </div>
    </div>
  );
}
