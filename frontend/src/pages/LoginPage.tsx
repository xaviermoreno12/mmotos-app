import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginApi } from '../api/auth';
import { useAuth } from '../hooks/useAuth';
import { Spinner } from '../components/ui/Spinner';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPwd, setShowPwd] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const resp = await loginApi({ username, password });
      login(resp.token, resp.usuarioId, resp.username, resp.rol);
      navigate('/pos', { replace: true });
    } catch {
      setError('Credenciales incorrectas.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-surface flex items-center justify-center">
      <div className="w-full max-w-sm">
        <div className="text-center mb-10">
          <p className="text-primary font-black text-2xl tracking-widest uppercase leading-tight">
            M MOTOS<br />CORE
          </p>
          <p className="text-on-surface-variant text-xs mt-2 tracking-wider uppercase">
            Sistema de Gestión
          </p>
        </div>

        <form onSubmit={handleSubmit} className="card space-y-4">
          <div>
            <label className="kpi-label block mb-1.5">Usuario</label>
            <input
              className="input"
              type="text"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Ingrese su usuario"
              required
            />
          </div>
          <div>
            <label className="kpi-label block mb-1.5">Contraseña</label>
            <div className="relative">
              <input
                className="input pr-10"
                type={showPwd ? 'text' : 'password'}
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-on-surface"
                onClick={() => setShowPwd(v => !v)}
                tabIndex={-1}
              >
                <span className="material-symbols-outlined text-[18px]">{showPwd ? 'visibility_off' : 'visibility'}</span>
              </button>
            </div>
          </div>

          {error && (
            <p className="text-error text-xs py-2 px-3 bg-error-container/20 rounded border border-error-container/40">
              {error}
            </p>
          )}

          <button type="submit" className="btn-primary w-full flex items-center justify-center gap-2 py-2.5" disabled={loading}>
            {loading ? <Spinner size="sm" /> : null}
            INGRESAR
          </button>
        </form>
      </div>
    </div>
  );
}
