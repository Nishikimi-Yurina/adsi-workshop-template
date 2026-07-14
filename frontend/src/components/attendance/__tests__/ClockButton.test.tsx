import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ClockButton } from '../ClockButton';

vi.mock('@/lib/api-client', () => ({
  apiFetch: vi.fn(),
  ApiError: class ApiError extends Error {
    constructor(public readonly status: number, public readonly body: unknown) {
      super(`API Error: ${status}`);
    }
  },
}));

import { apiFetch } from '@/lib/api-client';
const mockApiFetch = vi.mocked(apiFetch);

describe('ClockButton', () => {
  const onUpdate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('未出勤の場合、出勤ボタンが活性・退勤ボタンが非活性', () => {
    render(<ClockButton todayRecord={null} onUpdate={onUpdate} />);

    const clockInBtn = screen.getByRole('button', { name: '出勤する' });
    const clockOutBtn = screen.getByRole('button', { name: '退勤する' });

    expect(clockInBtn).not.toBeDisabled();
    expect(clockOutBtn).toBeDisabled();
  });

  it('出勤済み・未退勤の場合、出勤ボタンが非活性・退勤ボタンが活性', () => {
    const record = {
      id: 1, date: '2026-07-14', clockIn: '2026-07-14T09:00:00',
      clockOut: null, workMinutes: null, overtimeMinutes: null,
    };
    render(<ClockButton todayRecord={record} onUpdate={onUpdate} />);

    expect(screen.getByRole('button', { name: '出勤する' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤する' })).not.toBeDisabled();
  });

  it('退勤済みの場合、両方非活性', () => {
    const record = {
      id: 1, date: '2026-07-14', clockIn: '2026-07-14T09:00:00',
      clockOut: '2026-07-14T18:00:00', workMinutes: 540, overtimeMinutes: 60,
    };
    render(<ClockButton todayRecord={record} onUpdate={onUpdate} />);

    expect(screen.getByRole('button', { name: '出勤する' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤する' })).toBeDisabled();
  });

  it('出勤ボタンを押すとAPIが呼ばれ、onUpdateが実行される', async () => {
    mockApiFetch.mockResolvedValueOnce({});
    render(<ClockButton todayRecord={null} onUpdate={onUpdate} />);

    fireEvent.click(screen.getByRole('button', { name: '出勤する' }));

    await waitFor(() => {
      expect(mockApiFetch).toHaveBeenCalledWith('/api/attendance/clock-in', { method: 'POST' });
      expect(onUpdate).toHaveBeenCalled();
    });
  });

  it('API失敗時にエラーメッセージが表示される', async () => {
    const { ApiError } = await import('@/lib/api-client');
    mockApiFetch.mockRejectedValueOnce(new ApiError(409, {}));
    render(<ClockButton todayRecord={null} onUpdate={onUpdate} />);

    fireEvent.click(screen.getByRole('button', { name: '出勤する' }));

    await waitFor(() => {
      expect(screen.getByText('既に出勤済みです')).toBeInTheDocument();
    });
  });
});
