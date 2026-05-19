import { useState } from 'react';
import { deleteTask, updateTaskStatus, fetchLedger } from '../api/tasks';

const statusBadge = {
  TODO: 'badge-todo',
  IN_PROGRESS: 'badge-progress',
  DONE: 'badge-done',
};

const priorityBorder = {
  LOW: 'border-l-slate-500',
  MEDIUM: 'border-l-amber-400',
  HIGH: 'border-l-red-400',
};

const priorityClass = {
  LOW: 'badge-priority-low',
  MEDIUM: 'badge-priority-medium',
  HIGH: 'badge-priority-high',
};

export default function TaskCard({ task, onEdit, onRefresh }) {
  const [ledger, setLedger] = useState(null);
  const [showLedger, setShowLedger] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleStatusChange = async (status) => {
    await updateTaskStatus(task.id, status);
    onRefresh();
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this task?')) return;
    await deleteTask(task.id);
    onRefresh();
  };

  const loadLedger = async () => {
    if (showLedger) {
      setShowLedger(false);
      return;
    }
    setLoading(true);
    try {
      const { data } = await fetchLedger(task.id);
      setLedger(data);
      setShowLedger(true);
    } catch {
      setLedger([]);
      setShowLedger(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <article
      className={`glass-card glass-card-hover rounded-2xl p-5 flex flex-col gap-4 border-l-4 ${priorityBorder[task.priority]} animate-in`}
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <h3 className="font-semibold text-lg text-[var(--color-text)] leading-snug pr-2">
          {task.title}
        </h3>
        <span className={`badge ${statusBadge[task.status]}`}>
          {task.status.replace('_', ' ')}
        </span>
      </div>

      {task.description && (
        <p className="text-[var(--color-text-muted)] text-sm leading-relaxed whitespace-pre-line">
          {task.description}
        </p>
      )}

      <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm">
        <span className={`font-medium ${priorityClass[task.priority]}`}>
          ● {task.priority}
        </span>
        {task.dueDate && (
          <span className="text-[var(--color-text-dim)] flex items-center gap-1">
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            {task.dueDate}
          </span>
        )}
        <span className="text-[var(--color-text-dim)]">
          {new Date(task.createdAt).toLocaleDateString()}
        </span>
      </div>

      <div className="flex flex-wrap gap-2 mt-auto pt-3 border-t border-[var(--color-border)]">
        <select
          value={task.status}
          onChange={(e) => handleStatusChange(e.target.value)}
          className="input-field !py-1.5 !px-2 text-sm w-auto min-w-[130px]"
        >
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
        <button type="button" onClick={() => onEdit(task)} className="btn-ghost">
          Edit
        </button>
        <button
          type="button"
          onClick={loadLedger}
          className="text-sm px-3 py-1.5 rounded-lg bg-cyan-500/10 text-cyan-300 border border-cyan-500/25 hover:bg-cyan-500/20 transition-colors"
        >
          {loading ? '…' : showLedger ? 'Hide Ledger' : '⛓ Ledger'}
        </button>
        <button type="button" onClick={handleDelete} className="btn-danger ml-auto">
          Delete
        </button>
      </div>

      {showLedger && (
        <div className="rounded-xl bg-[#070b14]/80 border border-[var(--color-border)] p-4 font-mono text-xs overflow-x-auto">
          <p className="text-cyan-400/80 font-sans font-medium text-sm mb-3 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-cyan-400 animate-pulse" />
            Blockchain audit trail
          </p>
          {ledger?.length === 0 && (
            <p className="text-[var(--color-text-dim)]">No ledger entries yet.</p>
          )}
          {ledger?.map((entry) => (
            <div
              key={entry.id}
              className="mb-3 pb-3 border-b border-[var(--color-border)] last:border-0 last:mb-0 last:pb-0"
            >
              <div className="text-violet-300 font-sans font-medium mb-1">
                Block #{entry.blockIndex} · {entry.eventType}
              </div>
              <div className="text-[var(--color-text-dim)] truncate">hash: {entry.payloadHash}</div>
              <div className="text-[var(--color-text-dim)] opacity-70">
                prev: {entry.previousHash.slice(0, 20)}…
              </div>
            </div>
          ))}
        </div>
      )}
    </article>
  );
}
