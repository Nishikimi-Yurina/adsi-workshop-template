import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MonthlyReportTable } from '../MonthlyReportTable';

describe('MonthlyReportTable', () => {
  it('データがない場合「データがありません」と表示される', () => {
    render(<MonthlyReportTable reports={[]} />);
    expect(screen.getByText('データがありません')).toBeInTheDocument();
  });

  it('レポートが表示される', () => {
    const reports = [{
      employeeId: 1, employeeName: '山田太郎', yearMonth: '2026-07',
      workDays: 20, totalWorkMinutes: 9600, totalOvertimeMinutes: 400,
    }];
    render(<MonthlyReportTable reports={reports} />);
    expect(screen.getByText('山田太郎')).toBeInTheDocument();
    expect(screen.getByText('20日')).toBeInTheDocument();
    expect(screen.getByText('160:00')).toBeInTheDocument();
    expect(screen.getByText('6:40')).toBeInTheDocument();
  });
});
