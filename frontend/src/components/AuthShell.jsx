import { Link } from 'react-router-dom';

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="app-bg min-h-screen flex items-center justify-center px-4 py-10 relative">
      <div className="absolute top-8 left-1/2 -translate-x-1/2 flex items-center gap-2 z-10">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-cyan-400 to-violet-500 flex items-center justify-center shadow-lg shadow-cyan-500/20">
          <svg className="w-6 h-6 text-[#070b14]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
          </svg>
        </div>
        <span className="text-xl font-bold logo-text">TaskPortal AI</span>
      </div>

      <div className="w-full max-w-md glass-card rounded-2xl p-8 sm:p-10 animate-in relative z-10 mt-16">
        <h1 className="text-2xl font-bold text-[var(--color-text)] mb-1">{title}</h1>
        <p className="text-[var(--color-text-muted)] text-sm mb-8">{subtitle}</p>
        {children}
        {footer && (
          <p className="text-[var(--color-text-dim)] text-sm mt-6 text-center">{footer}</p>
        )}
      </div>

      <div className="absolute bottom-6 text-center text-xs text-[var(--color-text-dim)] w-full">
        AI-powered tasks · Blockchain audit trail
      </div>
    </div>
  );
}

export function AuthLink({ to, children }) {
  return (
    <Link to={to} className="text-cyan-400 hover:text-cyan-300 font-medium transition-colors">
      {children}
    </Link>
  );
}
