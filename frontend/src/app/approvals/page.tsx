'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api-client';
import { Layout } from '@/components/layout/Layout';

interface LeaveRequest {
  id: number;
  employeeName: string;
  leaveType: string;
  date: string;
  reason: string;
  status: string;
}

interface OvertimeRequest {
  id: number;
  employeeName: string;
  date: string;
  expectedHours: number;
  reason: string;
  status: string;
}

type MainTab = 'pending' | 'processed';
type SubTab = 'leave' | 'overtime';

const LEAVE_TYPE_LABELS: Record<string, string> = {
  PAID_LEAVE: '有給休暇',
  ABSENCE: '欠勤',
};

const STATUS_LABELS: Record<string, string> = {
  APPROVED: '承認済',
  REJECTED: '却下',
};

const STATUS_COLORS: Record<string, string> = {
  APPROVED: 'text-green-700 bg-green-100',
  REJECTED: 'text-red-700 bg-red-100',
};

export default function ApprovalsPage() {
  const [mainTab, setMainTab] = useState<MainTab>('pending');
  const [subTab, setSubTab] = useState<SubTab>('leave');
  const [pendingLeave, setPendingLeave] = useState<LeaveRequest[]>([]);
  const [pendingOvertime, setPendingOvertime] = useState<OvertimeRequest[]>([]);
  const [processedLeave, setProcessedLeave] = useState<LeaveRequest[]>([]);
  const [processedOvertime, setProcessedOvertime] = useState<OvertimeRequest[]>([]);

  const fetchPending = async () => {
    const [leave, overtime] = await Promise.all([
      apiFetch<LeaveRequest[]>('/api/leave-requests?status=PENDING'),
      apiFetch<OvertimeRequest[]>('/api/overtime-requests?status=PENDING'),
    ]);
    setPendingLeave(leave);
    setPendingOvertime(overtime);
  };

  const fetchProcessed = async () => {
    const [approvedLeave, rejectedLeave, approvedOvertime, rejectedOvertime] = await Promise.all([
      apiFetch<LeaveRequest[]>('/api/leave-requests?status=APPROVED'),
      apiFetch<LeaveRequest[]>('/api/leave-requests?status=REJECTED'),
      apiFetch<OvertimeRequest[]>('/api/overtime-requests?status=APPROVED'),
      apiFetch<OvertimeRequest[]>('/api/overtime-requests?status=REJECTED'),
    ]);
    setProcessedLeave([...approvedLeave, ...rejectedLeave]);
    setProcessedOvertime([...approvedOvertime, ...rejectedOvertime]);
  };

  useEffect(() => {
    fetchPending();
    fetchProcessed();
  }, []);

  const handleLeaveAction = async (id: number, action: 'approve' | 'reject') => {
    await apiFetch(`/api/leave-requests/${id}/${action}`, { method: 'PUT' });
    fetchPending();
    fetchProcessed();
  };

  const handleOvertimeAction = async (id: number, action: 'approve' | 'reject') => {
    await apiFetch(`/api/overtime-requests/${id}/${action}`, { method: 'PUT' });
    fetchPending();
    fetchProcessed();
  };

  const pendingCount = pendingLeave.length + pendingOvertime.length;
  const processedCount = processedLeave.length + processedOvertime.length;

  return (
    <Layout>
      <h1 className="text-xl font-bold mb-4">承認管理</h1>

      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setMainTab('pending')}
          className={`px-4 py-2 rounded text-sm font-medium ${
            mainTab === 'pending' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'
          }`}
        >
          未処理 ({pendingCount})
        </button>
        <button
          onClick={() => setMainTab('processed')}
          className={`px-4 py-2 rounded text-sm font-medium ${
            mainTab === 'processed' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'
          }`}
        >
          処理済み ({processedCount})
        </button>
      </div>

      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setSubTab('leave')}
          className={`px-3 py-1 rounded text-xs ${
            subTab === 'leave' ? 'bg-gray-700 text-white' : 'bg-gray-100 text-gray-600'
          }`}
        >
          休暇申請
        </button>
        <button
          onClick={() => setSubTab('overtime')}
          className={`px-3 py-1 rounded text-xs ${
            subTab === 'overtime' ? 'bg-gray-700 text-white' : 'bg-gray-100 text-gray-600'
          }`}
        >
          残業申請
        </button>
      </div>

      {mainTab === 'pending' && subTab === 'leave' && (
        <PendingLeaveTable requests={pendingLeave} onAction={handleLeaveAction} />
      )}
      {mainTab === 'pending' && subTab === 'overtime' && (
        <PendingOvertimeTable requests={pendingOvertime} onAction={handleOvertimeAction} />
      )}
      {mainTab === 'processed' && subTab === 'leave' && (
        <ProcessedLeaveTable requests={processedLeave} />
      )}
      {mainTab === 'processed' && subTab === 'overtime' && (
        <ProcessedOvertimeTable requests={processedOvertime} />
      )}
    </Layout>
  );
}

function PendingLeaveTable({ requests, onAction }: {
  requests: LeaveRequest[];
  onAction: (id: number, action: 'approve' | 'reject') => void;
}) {
  return (
    <table className="w-full border-collapse border text-sm">
      <thead>
        <tr className="bg-gray-100">
          <th className="border px-3 py-2 text-left">申請者</th>
          <th className="border px-3 py-2 text-left">種別</th>
          <th className="border px-3 py-2 text-left">日付</th>
          <th className="border px-3 py-2 text-left">理由</th>
          <th className="border px-3 py-2 text-left">操作</th>
        </tr>
      </thead>
      <tbody>
        {requests.map((r) => (
          <tr key={r.id}>
            <td className="border px-3 py-2">{r.employeeName}</td>
            <td className="border px-3 py-2">{LEAVE_TYPE_LABELS[r.leaveType] ?? r.leaveType}</td>
            <td className="border px-3 py-2">{r.date}</td>
            <td className="border px-3 py-2">{r.reason}</td>
            <td className="border px-3 py-2 space-x-2">
              <button
                onClick={() => onAction(r.id, 'approve')}
                className="bg-green-600 text-white px-2 py-1 rounded text-xs hover:bg-green-700"
              >
                承認
              </button>
              <button
                onClick={() => onAction(r.id, 'reject')}
                className="bg-red-600 text-white px-2 py-1 rounded text-xs hover:bg-red-700"
              >
                却下
              </button>
            </td>
          </tr>
        ))}
        {requests.length === 0 && (
          <tr>
            <td colSpan={5} className="border px-3 py-4 text-center text-gray-500">
              承認待ちの休暇申請はありません
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}

function PendingOvertimeTable({ requests, onAction }: {
  requests: OvertimeRequest[];
  onAction: (id: number, action: 'approve' | 'reject') => void;
}) {
  return (
    <table className="w-full border-collapse border text-sm">
      <thead>
        <tr className="bg-gray-100">
          <th className="border px-3 py-2 text-left">申請者</th>
          <th className="border px-3 py-2 text-left">日付</th>
          <th className="border px-3 py-2 text-left">予定時間</th>
          <th className="border px-3 py-2 text-left">理由</th>
          <th className="border px-3 py-2 text-left">操作</th>
        </tr>
      </thead>
      <tbody>
        {requests.map((r) => (
          <tr key={r.id}>
            <td className="border px-3 py-2">{r.employeeName}</td>
            <td className="border px-3 py-2">{r.date}</td>
            <td className="border px-3 py-2">{r.expectedHours}h</td>
            <td className="border px-3 py-2">{r.reason}</td>
            <td className="border px-3 py-2 space-x-2">
              <button
                onClick={() => onAction(r.id, 'approve')}
                className="bg-green-600 text-white px-2 py-1 rounded text-xs hover:bg-green-700"
              >
                承認
              </button>
              <button
                onClick={() => onAction(r.id, 'reject')}
                className="bg-red-600 text-white px-2 py-1 rounded text-xs hover:bg-red-700"
              >
                却下
              </button>
            </td>
          </tr>
        ))}
        {requests.length === 0 && (
          <tr>
            <td colSpan={5} className="border px-3 py-4 text-center text-gray-500">
              承認待ちの残業申請はありません
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}

function ProcessedLeaveTable({ requests }: { requests: LeaveRequest[] }) {
  return (
    <table className="w-full border-collapse border text-sm">
      <thead>
        <tr className="bg-gray-100">
          <th className="border px-3 py-2 text-left">申請者</th>
          <th className="border px-3 py-2 text-left">種別</th>
          <th className="border px-3 py-2 text-left">日付</th>
          <th className="border px-3 py-2 text-left">理由</th>
          <th className="border px-3 py-2 text-left">ステータス</th>
        </tr>
      </thead>
      <tbody>
        {requests.map((r) => (
          <tr key={r.id}>
            <td className="border px-3 py-2">{r.employeeName}</td>
            <td className="border px-3 py-2">{LEAVE_TYPE_LABELS[r.leaveType] ?? r.leaveType}</td>
            <td className="border px-3 py-2">{r.date}</td>
            <td className="border px-3 py-2">{r.reason}</td>
            <td className="border px-3 py-2">
              <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLORS[r.status] ?? ''}`}>
                {STATUS_LABELS[r.status] ?? r.status}
              </span>
            </td>
          </tr>
        ))}
        {requests.length === 0 && (
          <tr>
            <td colSpan={5} className="border px-3 py-4 text-center text-gray-500">
              処理済みの休暇申請はありません
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}

function ProcessedOvertimeTable({ requests }: { requests: OvertimeRequest[] }) {
  return (
    <table className="w-full border-collapse border text-sm">
      <thead>
        <tr className="bg-gray-100">
          <th className="border px-3 py-2 text-left">申請者</th>
          <th className="border px-3 py-2 text-left">日付</th>
          <th className="border px-3 py-2 text-left">予定時間</th>
          <th className="border px-3 py-2 text-left">理由</th>
          <th className="border px-3 py-2 text-left">ステータス</th>
        </tr>
      </thead>
      <tbody>
        {requests.map((r) => (
          <tr key={r.id}>
            <td className="border px-3 py-2">{r.employeeName}</td>
            <td className="border px-3 py-2">{r.date}</td>
            <td className="border px-3 py-2">{r.expectedHours}h</td>
            <td className="border px-3 py-2">{r.reason}</td>
            <td className="border px-3 py-2">
              <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLORS[r.status] ?? ''}`}>
                {STATUS_LABELS[r.status] ?? r.status}
              </span>
            </td>
          </tr>
        ))}
        {requests.length === 0 && (
          <tr>
            <td colSpan={5} className="border px-3 py-4 text-center text-gray-500">
              処理済みの残業申請はありません
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}
