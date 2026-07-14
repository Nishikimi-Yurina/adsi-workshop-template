'use client';

import { useAuth } from '@/context/AuthContext';

type View = 'dashboard' | 'attendance' | 'reports';

interface NavItem {
  view: View;
  label: string;
  roles: string[];
}

const navItems: NavItem[] = [
  { view: 'dashboard', label: 'ダッシュボード', roles: ['EMPLOYEE', 'ADMIN'] },
  { view: 'attendance', label: '勤怠一覧', roles: ['EMPLOYEE', 'ADMIN'] },
  { view: 'reports', label: '月次レポート', roles: ['EMPLOYEE', 'ADMIN'] },
];

interface SidebarProps {
  onNavigate: (view: View) => void;
  currentView: View;
}

export function Sidebar({ onNavigate, currentView }: SidebarProps) {
  const { user } = useAuth();

  const visibleItems = navItems.filter(
    (item) => user && item.roles.includes(user.role)
  );

  return (
    <aside className="w-56 bg-gray-50 border-r border-gray-200 min-h-screen p-4">
      <nav>
        <ul className="space-y-1">
          {visibleItems.map((item) => (
            <li key={item.view}>
              <button
                onClick={() => onNavigate(item.view)}
                className={`block w-full text-left px-3 py-2 rounded text-sm ${
                  currentView === item.view
                    ? 'bg-blue-100 text-blue-700 font-medium'
                    : 'text-gray-700 hover:bg-gray-200'
                }`}
              >
                {item.label}
              </button>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
