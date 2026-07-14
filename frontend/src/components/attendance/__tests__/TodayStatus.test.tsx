import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { TodayStatus } from '../TodayStatus';

describe('TodayStatus', () => {
  it('未出勤の場合「未出勤」と表示される', () => {
    render(<TodayStatus record={null} />);
    expect(screen.getByText('未出勤')).toBeInTheDocument();
  });

  it('出勤済みの場合、出勤時刻が表示される', () => {
    const record = {
      id: 1, date: '2026-07-14', clockIn: '2026-07-14T09:00:00',
      clockOut: null, workMinutes: null, overtimeMinutes: null,
    };
    render(<TodayStatus record={record} />);
    expect(screen.getByText('--:--')).toBeInTheDocument();
  });

  it('退勤済みの場合、勤務時間と残業が表示される', () => {
    const record = {
      id: 1, date: '2026-07-14', clockIn: '2026-07-14T09:00:00',
      clockOut: '2026-07-14T19:00:00', workMinutes: 600, overtimeMinutes: 120,
    };
    render(<TodayStatus record={record} />);
    expect(screen.getByText('10:00')).toBeInTheDocument();
    expect(screen.getByText('2:00')).toBeInTheDocument();
  });
});
