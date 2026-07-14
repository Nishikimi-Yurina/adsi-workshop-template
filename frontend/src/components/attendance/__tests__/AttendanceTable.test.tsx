import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { AttendanceTable } from '../AttendanceTable';

describe('AttendanceTable', () => {
  const onPrevMonth = vi.fn();
  const onNextMonth = vi.fn();

  it('レコードがない場合「記録がありません」と表示される', () => {
    render(
      <AttendanceTable records={[]} yearMonth="2026-07" onPrevMonth={onPrevMonth} onNextMonth={onNextMonth} />
    );
    expect(screen.getByText('記録がありません')).toBeInTheDocument();
  });

  it('レコードがある場合、テーブルに表示される', () => {
    const records = [{
      id: 1, date: '2026-07-01', clockIn: '2026-07-01T09:00:00',
      clockOut: '2026-07-01T18:00:00', workMinutes: 540, overtimeMinutes: 60,
    }];
    render(
      <AttendanceTable records={records} yearMonth="2026-07" onPrevMonth={onPrevMonth} onNextMonth={onNextMonth} />
    );
    expect(screen.getByText('2026-07-01')).toBeInTheDocument();
    expect(screen.getByText('9:00')).toBeInTheDocument();
  });

  it('前月・次月ボタンがコールバックを呼ぶ', () => {
    render(
      <AttendanceTable records={[]} yearMonth="2026-07" onPrevMonth={onPrevMonth} onNextMonth={onNextMonth} />
    );
    fireEvent.click(screen.getByText('< 前月'));
    fireEvent.click(screen.getByText('次月 >'));
    expect(onPrevMonth).toHaveBeenCalled();
    expect(onNextMonth).toHaveBeenCalled();
  });
});
