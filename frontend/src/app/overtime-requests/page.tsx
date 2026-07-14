'use client';

import { useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api-client';
import { Layout } from '@/components/layout/Layout';

interface OvertimeRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  date: string;
  expectedHours: number;
  reason: string;
  status: string;
}

const STATUS_LABELS: Record<string, string> = {
  PENDING: '申請中',
  APPROVED: '承認済',
  REJECTED: '却下',
};

export default function OvertimeRequestsPage() {
  const [requests, setRequests] = useState<OvertimeRequest[]>([]);
  const [date, setDate] = useState('');
  const [expectedHours, setExpectedHours] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  const fetchRequests = async () => {
    const data = await apiFetch<OvertimeRequest[]>('/api/overtime-requests');
    setRequests(data);
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await apiFetch<OvertimeRequest>('/api/overtime-requests', {
        method: 'POST',
        body: JSON.stringify({ date, expectedHours: parseFloat(expectedHours), reason }),
      });
      setDate('');
      setExpectedHours('');
      setReason('');
      fetchRequests();
    } catch {
      setError('申請に失敗しました');
    }
  };

  return (
    <Layout>
      <h1 className="text-xl font-bold mb-4">残業申請</h1>

      <form onSubmit={handleSubmit} className="mb-6 p-4 bg-white border rounded space-y-3">
        <h2 className="font-semibold">新規申請</h2>
        <div className="flex gap-4 items-end flex-wrap">
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
            <label className="block text-sm text-gray-600">予定時間（時間）</label>
            <input
              type="number"
              step="0.5"
              min="0.5"
              value={expectedHours}
              onChange={(e) => setExpectedHours(e.target.value)}
              required
              className="border rounded px-2 py-1 w-24"
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
            <th className="border px-3 py-2 text-left">予定時間</th>
            <th className="border px-3 py-2 text-left">理由</th>
            <th className="border px-3 py-2 text-left">ステータス</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id}>
              <td className="border px-3 py-2">{r.date}</td>
              <td className="border px-3 py-2">{r.expectedHours}h</td>
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
