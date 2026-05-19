import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-bg min-h-screen text-[var(--color-text)] relative">
      <header className="sticky top-0 z-20 border-b border-[var(--color-border)] bg-[rgba(7,11,20,0.85)] backdrop-blur-xl">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-cyan-400 to-violet-500 flex items-center justify-center shadow-md shadow-cyan-500/20 group-hover:shadow-cyan-500/30 transition-shadow">
              <svg className="w-5 h-5 text-[#070b14]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <span className="text-lg font-bold logo-text">TaskPortal AI</span>
          </Link>

          {user && (
            <div className="flex items-center gap-3">
              <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-[var(--color-surface)] border border-[var(--color-border)]">
                <span className="w-7 h-7 rounded-full bg-gradient-to-br from-cyan-500/30 to-violet-500/30 flex items-center justify-center text-xs font-semibold text-cyan-300">
                  {user.fullName?.charAt(0)?.toUpperCase() || 'U'}
                </span>
                <span className="text-sm text-[var(--color-text-muted)]">
                  {user.fullName}
                </span>
              </div>
              <button type="button" onClick={handleLogout} className="btn-ghost">
                Logout
              </button>
            </div>
          )}
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 relative z-10">{children}</main>
    </div>
  );
}
