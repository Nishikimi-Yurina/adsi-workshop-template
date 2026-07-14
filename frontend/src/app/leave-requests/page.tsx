'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api-client';
import { Layout } from '@/components/layout/Layout';

interface LeaveRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: string;
  date: string;
  reason: string;
  status: string;
}

const LEAVE_TYPE_LABELS: Record<string, string> = {
  PAID_LEAVE: '有給休暇',
  ABSENCE: '欠勤',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: '申請中',
  APPROVED: '承認済',
  REJECTED: '却下',
};

export default function LeaveRequestsPage() {
  const [requests, setRequests] = useState<LeaveRequest[]>([]);
  const [leaveType, setLeaveType] = useState('PAID_LEAVE');
  const [date, setDate] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  const fetchRequests = async () => {
    const data = await apiFetch<LeaveRequest[]>('/api/leave-requests');
    setRequests(data);
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await apiFetch<LeaveRequest>('/api/leave-requests', {
        method: 'POST',
        body: JSON.stringify({ leaveType, date, reason }),
      });
      setDate('');
      setReason('');
      fetchRequests();
    } catch {
      setError('申請に失敗しました');
    }
  };

  return (
    <Layout>
      <h1 className="text-xl font-bold mb-4">休暇申請</h1>

      <form onSubmit={handleSubmit} className="mb-6 p-4 bg-white border rounded space-y-3">
        <h2 className="font-semibold">新規申請</h2>
        <div className="flex gap-4 items-end flex-wrap">
          <div>
            <label className="block text-sm text-gray-600">種別</label>
            <select
              value={leaveType}
              onChange={(e) => setLeaveType(e.target.value)}
              className="border rounded px-2 py-1"
            >
              <option value="PAID_LEAVE">有給休暇</option>
              <option value="ABSENCE">欠勤</option>
            </select>
          </div>
          <div>
            <label className="block text-sm text-gray-600">日付</label>
            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
              className="border rounded px-2 py-1"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600">理由</label>
            <input
              type="text"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="border rounded px-2 py-1 w-48"
            />
          </div>
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-1 rounded hover:bg-blue-700"
          >
            申請
          </button>
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
      </form>

      <table className="w-full border-collapse border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="border px-3 py-2 text-left">日付</th>
            <th className="border px-3 py-2 text-left">種別</th>
            <th className="border px-3 py-2 text-left">理由</th>
            <th className="border px-3 py-2 text-left">ステータス</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id}>
              <td className="border px-3 py-2">{r.date}</td>
              <td className="border px-3 py-2">{LEAVE_TYPE_LABELS[r.leaveType] ?? r.leaveType}</td>
              <td className="border px-3 py-2">{r.reason}</td>
              <td className="border px-3 py-2">{STATUS_LABELS[r.status] ?? r.status}</td>
            </tr>
          ))}
          {requests.length === 0 && (
            <tr>
              <td colSpan={4} className="border px-3 py-4 text-center text-gray-500">
                申請がありません
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </Layout>
  );
}
