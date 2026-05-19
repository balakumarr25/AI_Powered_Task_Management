import { useEffect, useState } from 'react';
import { generateTaskDetails, fetchAiProviders } from '../api/ai';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'];
const STATUSES   = ['TODO', 'IN_PROGRESS', 'DONE'];

const emptyForm = {
  title:       '',
  description: '',
  priority:    'MEDIUM',
  dueDate:     '',
  status:      'TODO',
};

const PROVIDER_META = {
  huggingface: { label: 'Hugging Face', icon: '🤗', color: 'text-amber-300', border: 'border-amber-500/40' },
};

export default function TaskForm({ initial, onSubmit, onCancel, title = 'New Task' }) {
  const [form,      setForm]      = useState(initial || emptyForm);
  const [errors,    setErrors]    = useState({});
  const [aiLoading, setAiLoading] = useState(false);
  const [aiMessage, setAiMessage] = useState('');
  const [provider,  setProvider]  = useState('huggingface');
  const [providerStatus, setProviderStatus] = useState(null); // null = loading

  // Fetch provider status once on mount
  useEffect(() => {
    fetchAiProviders()
      .then(({ data }) => setProviderStatus(data))
      .catch(() => setProviderStatus({}));
  }, []);

  const validate = () => {
    const next = {};
    if (!form.title.trim())       next.title = 'Title is required';
    if (form.title.length > 200)  next.title = 'Title max 200 characters';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleAiGenerate = async () => {
    if (!form.title.trim()) {
      setErrors({ title: 'Enter a title before using AI' });
      return;
    }
    setAiLoading(true);
    setAiMessage('');
    try {
      const { data } = await generateTaskDetails(form.title.trim(), 'huggingface');
      setForm((prev) => ({
        ...prev,
        description: data.description || prev.description,
        priority:    data.suggestedPriority || prev.priority,
      }));
      setAiMessage(
        data.fallbackUsed
          ? `${data.message} · Effort: ${data.estimatedEffort}`
          : `${data.message} · Effort: ${data.estimatedEffort}`
      );
    } catch {
      setAiMessage('AI request failed. Try again or fill fields manually.');
    } finally {
      setAiLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      title:       form.title.trim(),
      description: form.description || null,
      priority:    form.priority,
      dueDate:     form.dueDate || null,
      status:      form.status,
    });
  };

  const activeProviderMeta = PROVIDER_META['huggingface'];

  return (
    <form onSubmit={handleSubmit} className="glass-card rounded-2xl p-6 sm:p-8 animate-in">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6 pb-4 border-b border-[var(--color-border)]">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500/20 to-cyan-500/20 flex items-center justify-center border border-[var(--color-border)]">
          <svg className="w-5 h-5 text-violet-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        </div>
        <div>
          <h2 className="text-lg font-semibold text-[var(--color-text)]">{title}</h2>
          <p className="text-xs text-[var(--color-text-dim)]">Use AI Fill to auto-generate details</p>
        </div>
      </div>

      {/* AI Provider Badge */}
      <div className="mb-5 p-4 rounded-xl bg-[var(--color-surface-2,#0d1424)] border border-[var(--color-border)] flex items-center gap-3">
        <span className="text-xl">🤗</span>
        <div>
          <p className="text-sm font-medium text-amber-300">Hugging Face AI</p>
          <p className="text-xs text-[var(--color-text-dim)]">
            Powered by Qwen2.5-7B-Instruct · {providerStatus?.huggingface
              ? <span className="text-emerald-400">● Connected</span>
              : <span className="text-red-400/70">● No key</span>}
          </p>
        </div>
      </div>

      <div className="space-y-5">
        {/* Title + AI Fill */}
        <div>
          <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">
            Title <span className="text-cyan-400">*</span>
          </label>
          <div className="flex flex-col sm:flex-row gap-2">
            <input
              name="title"
              value={form.title}
              onChange={handleChange}
              className="input-field flex-1"
              placeholder="Prepare client presentation"
            />
            <button
              type="button"
              onClick={handleAiGenerate}
              disabled={aiLoading}
              className="btn-ai flex items-center justify-center gap-2 min-w-[130px]"
            >
              {aiLoading ? (
                <span className="loading-pulse">Generating…</span>
              ) : (
                <>
                  <span>{activeProviderMeta.icon}</span>
                  <span>AI Fill</span>
                </>
              )}
            </button>
          </div>
          {errors.title && <p className="text-red-400 text-sm mt-1.5">{errors.title}</p>}
          {aiMessage && (
            <p className="text-sm mt-2 px-3 py-2 rounded-lg bg-violet-500/10 border border-violet-500/20 text-violet-200">
              {aiMessage}
              <span className="block text-xs text-violet-300/70 mt-1">
                Prep guide is matched to your task type — try different titles to see different plans.
              </span>
            </p>
          )}
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">
            Description
          </label>
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows={10}
            className="input-field resize-y min-h-[200px]"
            placeholder="AI will generate a detailed brief with Overview, Key Steps, Deliverables, and Notes…"
          />
        </div>

        {/* Priority / Status / Due Date */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">Priority</label>
            <select name="priority" value={form.priority} onChange={handleChange} className="input-field">
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">Status</label>
            <select name="status" value={form.status} onChange={handleChange} className="input-field">
              {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">Due Date</label>
            <input type="date" name="dueDate" value={form.dueDate} onChange={handleChange} className="input-field" />
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-3 justify-end mt-8 pt-6 border-t border-[var(--color-border)]">
        {onCancel && (
          <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
        )}
        <button type="submit" className="btn-primary">Save Task</button>
      </div>
    </form>
  );
}
