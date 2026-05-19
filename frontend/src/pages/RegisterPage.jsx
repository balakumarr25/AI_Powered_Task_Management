import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { register } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import AuthShell, { AuthLink } from '../components/AuthShell';

const FIELDS = [
  { key: 'fullName', label: 'Full Name', type: 'text', placeholder: 'Jane Doe' },
  { key: 'email', label: 'Email', type: 'email', placeholder: 'you@example.com' },
  { key: 'password', label: 'Password', type: 'password', placeholder: 'Min. 6 characters' },
  { key: 'confirmPassword', label: 'Confirm Password', type: 'password', placeholder: 'Repeat password' },
];

export default function RegisterPage() {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.fullName.trim()) return setError('Full name is required');
    if (!form.email.trim()) return setError('Email is required');
    if (form.password.length < 6) return setError('Password must be at least 6 characters');
    if (form.password !== form.confirmPassword) return setError('Passwords do not match');

    setLoading(true);
    try {
      const { data } = await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      loginUser(data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Create your account"
      subtitle="Start organizing tasks with AI and secure ledger history"
      footer={
        <>
          Already have an account? <AuthLink to="/login">Sign in</AuthLink>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {FIELDS.map(({ key, label, type, placeholder }) => (
          <div key={key}>
            <label className="block text-sm font-medium text-[var(--color-text-muted)] mb-1.5">
              {label}
            </label>
            <input
              type={type}
              value={form[key]}
              onChange={(e) => setForm({ ...form, [key]: e.target.value })}
              className="input-field"
              placeholder={placeholder}
            />
          </div>
        ))}
        {error && (
          <div className="rounded-lg bg-red-500/10 border border-red-500/25 px-3 py-2 text-red-300 text-sm">
            {error}
          </div>
        )}
        <button type="submit" disabled={loading} className="btn-primary w-full py-3 mt-2">
          {loading ? 'Creating account…' : 'Create Account'}
        </button>
      </form>
    </AuthShell>
  );
}
