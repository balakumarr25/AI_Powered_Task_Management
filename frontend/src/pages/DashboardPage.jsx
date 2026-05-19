import { useCallback, useEffect, useMemo, useState } from 'react';
import Layout from '../components/Layout';
import TaskCard from '../components/TaskCard';
import TaskForm from '../components/TaskForm';
import { createTask, fetchTasks, updateTask } from '../api/tasks';

const STAT_CARDS = [
  { key: 'total', label: 'Total Tasks', statKey: 'total', class: 'stat-total', color: 'text-cyan-300' },
  { key: 'todo', label: 'To Do', statKey: 'todo', class: 'stat-todo', color: 'text-slate-300' },
  { key: 'progress', label: 'In Progress', statKey: 'inProgress', class: 'stat-progress', color: 'text-amber-300' },
  { key: 'done', label: 'Completed', statKey: 'done', class: 'stat-done', color: 'text-emerald-300' },
];

export default function DashboardPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  const loadTasks = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await fetchTasks();
      setTasks(data);
    } catch {
      setError('Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const stats = useMemo(
    () => ({
      total: tasks.length,
      todo: tasks.filter((t) => t.status === 'TODO').length,
      inProgress: tasks.filter((t) => t.status === 'IN_PROGRESS').length,
      done: tasks.filter((t) => t.status === 'DONE').length,
    }),
    [tasks]
  );

  const filtered = useMemo(() => {
    return tasks.filter((t) => {
      const matchFilter = filter === 'ALL' || t.status === filter;
      const matchSearch =
        !search ||
        t.title.toLowerCase().includes(search.toLowerCase()) ||
        (t.description || '').toLowerCase().includes(search.toLowerCase());
      return matchFilter && matchSearch;
    });
  }, [tasks, filter, search]);

  const handleCreate = async (payload) => {
    await createTask(payload);
    setShowForm(false);
    loadTasks();
  };

  const handleUpdate = async (payload) => {
    await updateTask(editing.id, payload);
    setEditing(null);
    loadTasks();
  };

  const handleEdit = (task) => {
    setEditing({
      id: task.id,
      initial: {
        title: task.title,
        description: task.description || '',
        priority: task.priority,
        dueDate: task.dueDate || '',
        status: task.status,
      },
    });
    setShowForm(false);
  };

  return (
    <Layout>
      <header className="mb-10 animate-in">
        <p className="text-cyan-400/80 text-sm font-medium uppercase tracking-widest mb-2">
          Dashboard
        </p>
        <h1 className="text-3xl sm:text-4xl font-bold text-[var(--color-text)] mb-2">
          My Tasks
        </h1>
        <p className="text-[var(--color-text-muted)] max-w-xl">
          Create, track, and complete work with AI-assisted descriptions and an immutable ledger for every change.
        </p>
      </header>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {STAT_CARDS.map(({ key, label, statKey, class: cls, color }) => (
          <div key={key} className={`stat-card ${cls} animate-in`}>
            <p className={`text-3xl font-bold ${color}`}>{stats[statKey]}</p>
            <p className="text-xs text-[var(--color-text-dim)] mt-1 uppercase tracking-wide">{label}</p>
          </div>
        ))}
      </div>

      <div className="glass-card rounded-2xl p-4 mb-8 flex flex-col sm:flex-row gap-3 animate-in">
        <div className="relative flex-1">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-dim)]"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="search"
            placeholder="Search tasks…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="input-field !pl-10"
          />
        </div>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="input-field sm:w-44"
        >
          <option value="ALL">All statuses</option>
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
        <button
          type="button"
          onClick={() => {
            setEditing(null);
            setShowForm((v) => !v);
          }}
          className="btn-primary whitespace-nowrap flex items-center justify-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          {showForm ? 'Close' : 'New Task'}
        </button>
      </div>

      {showForm && !editing && (
        <div className="mb-8">
          <TaskForm title="Create New Task" onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </div>
      )}

      {editing && (
        <div className="mb-8">
          <TaskForm
            title="Edit Task"
            initial={editing.initial}
            onSubmit={handleUpdate}
            onCancel={() => setEditing(null)}
          />
        </div>
      )}

      {loading && (
        <div className="text-center py-16">
          <div className="inline-block w-8 h-8 border-2 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin mb-3" />
          <p className="text-[var(--color-text-muted)] loading-pulse">Loading your tasks…</p>
        </div>
      )}

      {error && (
        <div className="rounded-xl bg-red-500/10 border border-red-500/25 px-4 py-3 text-red-300 mb-6">
          {error}
        </div>
      )}

      {!loading && filtered.length === 0 && (
        <div className="glass-card rounded-2xl py-16 px-6 text-center animate-in">
          <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-cyan-500/20 to-violet-500/20 flex items-center justify-center">
            <svg className="w-8 h-8 text-cyan-400/60" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <p className="text-[var(--color-text-muted)] mb-4">No tasks found. Start by creating your first one.</p>
          <button type="button" onClick={() => setShowForm(true)} className="btn-primary">
            + Create Task
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {filtered.map((task, i) => (
          <div key={task.id} style={{ animationDelay: `${i * 0.05}s` }} className="animate-in">
            <TaskCard task={task} onEdit={handleEdit} onRefresh={loadTasks} />
          </div>
        ))}
      </div>
    </Layout>
  );
}
