'use client';

import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';

const navItems = [
  { href: '/', label: 'ダッシュボード', roles: ['EMPLOYEE', 'ADMIN'] },
  { href: '/attendance', label: '勤怠一覧', roles: ['EMPLOYEE', 'ADMIN'] },
  { href: '/leave-requests', label: '休暇申請', roles: ['EMPLOYEE', 'ADMIN'] },
  { href: '/overtime-requests', label: '残業申請', roles: ['EMPLOYEE', 'ADMIN'] },
  { href: '/approvals', label: '承認管理', roles: ['ADMIN'] },
  { href: '/reports/monthly', label: '月次レポート', roles: ['EMPLOYEE', 'ADMIN'] },
];

export function Sidebar() {
  const { user } = useAuth();

  const visibleItems = navItems.filter(
    (item) => user && item.roles.includes(user.role)
  );

  return (
    <aside className="w-56 bg-gray-50 border-r border-gray-200 min-h-screen p-4">
      <nav>
        <ul className="space-y-1">
          {visibleItems.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className="block px-3 py-2 rounded text-sm text-gray-700 hover:bg-gray-200"
              >
                {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
