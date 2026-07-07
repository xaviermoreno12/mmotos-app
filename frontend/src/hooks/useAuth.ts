import { useCallback } from 'react';

const TOKEN_KEY    = 'mmotos_token';
const USER_ID_KEY  = 'mmotos_user_id';
const USERNAME_KEY = 'mmotos_username';
const ROL_KEY      = 'mmotos_rol';

export function useAuth() {
  const token = localStorage.getItem(TOKEN_KEY);
  const isAuthenticated = !!token;
  const usuarioId = localStorage.getItem(USER_ID_KEY) ?? undefined;
  const username  = localStorage.getItem(USERNAME_KEY) ?? undefined;

  const login = useCallback((token: string, usuarioId?: string, username?: string, rol?: string) => {
    localStorage.setItem(TOKEN_KEY, token);
    if (usuarioId) localStorage.setItem(USER_ID_KEY, usuarioId);
    if (username)  localStorage.setItem(USERNAME_KEY, username);
    if (rol)       localStorage.setItem(ROL_KEY, rol.toUpperCase());
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(ROL_KEY);
    window.location.href = '/login';
  }, []);

  return { isAuthenticated, login, logout, token, usuarioId, username };
}
