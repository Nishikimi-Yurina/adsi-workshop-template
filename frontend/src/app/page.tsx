'use client';

import { useState, useEffect, useCallback } from 'react';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { Layout } from '@/components/layout/Layout';
import { LoginForm } from '@/components/LoginForm';
import { ClockButton } from '@/components/attendance/ClockButton';
import { TodayStatus } from '@/components/attendance/TodayStatus';
import { AttendanceTable } from '@/components/attendance/AttendanceTable';
import { MonthlyReportTable } from '@/components/reports/MonthlyReportTable';
import { apiFetch } from '@/lib/api-client';
import type { AttendanceRecord, MonthlyReport } from '@/types/attendance';

type View = 'dashboard' | 'attendance' | 'reports';

function AppContent() {
  const { user, loading } = useAuth();
  const [view, setView] = useState<View>('dashboard');
  const [todayRecord, setTodayRecord] = useState<AttendanceRecord | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [reports, setReports] = useState<MonthlyReport[]>([]);
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  });

  const fetchTodayRecord = useCallback(async () => {
    try {
      const now = new Date();
      const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
      const all = await apiFetch<AttendanceRecord[]>(`/api/attendance/records?yearMonth=${ym}`);
      const today = new Date().toISOString().split('T')[0];
      setTodayRecord(all.find(r => r.date === today) ?? null);
    } catch {
      setTodayRecord(null);
    }
  }, []);

  const fetchRecords = useCallback(async () => {
    try {
      const data = await apiFetch<AttendanceRecord[]>(`/api/attendance/records?yearMonth=${yearMonth}`);
      setRecords(data);
    } catch {
      setRecords([]);
    }
  }, [yearMonth]);

  const fetchReports = useCallback(async () => {
    try {
      if (user?.role === 'ADMIN') {
        const data = await apiFetch<MonthlyReport[]>(`/api/reports/monthly/all?yearMonth=${yearMonth}`);
        setReports(data);
      } else {
        const data = await apiFetch<MonthlyReport>(`/api/reports/monthly?yearMonth=${yearMonth}`);
        setReports([data]);
      }
    } catch {
      setReports([]);
    }
  }, [yearMonth, user?.role]);

  useEffect(() => {
    if (user) fetchTodayRecord();
  }, [user, fetchTodayRecord]);

  useEffect(() => {
    if (user && view === 'attendance') fetchRecords();
  }, [user, view, fetchRecords]);

  useEffect(() => {
    if (user && view === 'reports') fetchReports();
  }, [user, view, fetchReports]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    );
  }

  if (!user) {
    return <LoginForm />;
  }

  const changeMonth = (delta: number) => {
    const [y, m] = yearMonth.split('-').map(Number);
    const d = new Date(y, m - 1 + delta, 1);
    setYearMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
  };

  return (
    <Layout onNavigate={setView} currentView={view}>
      {view === 'dashboard' && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold">ダッシュボード</h2>
          <ClockButton todayRecord={todayRecord} onUpdate={fetchTodayRecord} />
          <TodayStatus record={todayRecord} />
        </div>
      )}
      {view === 'attendance' && (
        <div className="space-y-4">
          <h2 className="text-xl font-bold">勤怠一覧</h2>
          <AttendanceTable
            records={records}
            yearMonth={yearMonth}
            onPrevMonth={() => changeMonth(-1)}
            onNextMonth={() => changeMonth(1)}
          />
        </div>
      )}
      {view === 'reports' && (
        <div className="space-y-4">
          <h2 className="text-xl font-bold">月次レポート</h2>
          <div className="flex items-center gap-4 mb-4">
            <button onClick={() => changeMonth(-1)} className="text-sm text-blue-600">&lt; 前月</button>
            <span className="font-medium">{yearMonth}</span>
            <button onClick={() => changeMonth(1)} className="text-sm text-blue-600">次月 &gt;</button>
          </div>
          <MonthlyReportTable reports={reports} />
        </div>
      )}
    </Layout>
  );
}

export default function Page() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
