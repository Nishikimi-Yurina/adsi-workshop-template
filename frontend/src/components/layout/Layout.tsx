'use client';

import { AuthProvider } from '@/context/AuthContext';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

type View = 'dashboard' | 'attendance' | 'reports';

interface LayoutProps {
  children: React.ReactNode;
  onNavigate: (view: View) => void;
  currentView: View;
}

export function Layout({ children, onNavigate, currentView }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <div className="flex flex-1">
        <Sidebar onNavigate={onNavigate} currentView={currentView} />
        <main className="flex-1 p-6 bg-gray-100">{children}</main>
      </div>
    </div>
  );
}
