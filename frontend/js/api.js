/* ============================================================
   api.js — Centralized API service layer
   All backend calls go through this module.
   ============================================================ */

const API_BASE = 'http://localhost:8080/api';

// ── Token helpers ─────────────────────────────────────────
const Auth = {
  getToken:    ()    => localStorage.getItem('jala_token'),
  setToken:    (t)   => localStorage.setItem('jala_token', t),
  getUser:     ()    => JSON.parse(localStorage.getItem('jala_user') || 'null'),
  setUser:     (u)   => localStorage.setItem('jala_user', JSON.stringify(u)),
  clear:       ()    => { localStorage.removeItem('jala_token'); localStorage.removeItem('jala_user'); },
  isLoggedIn:  ()    => !!localStorage.getItem('jala_token'),
  isAdmin:     ()    => Auth.getUser()?.role === 'ADMIN',
};

// ── Base fetch wrapper ────────────────────────────────────
async function apiFetch(path, options = {}) {
  const token = Auth.getToken();
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  // Handle 401 - redirect to login
  if (res.status === 401) {
    Auth.clear();
    window.location.reload();
    return;
  }

  const data = await res.json();
  if (!data.success) throw new Error(data.message || 'API error');
  return data;
}

// Multipart form (file upload) — no Content-Type header
async function apiUpload(path, formData) {
  const token = Auth.getToken();
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData,
  });
  const data = await res.json();
  if (!data.success) throw new Error(data.message || 'Upload error');
  return data;
}

// ── Auth API ──────────────────────────────────────────────
const AuthAPI = {
  login: (email, password) =>
    apiFetch('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
};

// ── Employee API ──────────────────────────────────────────
const EmployeeAPI = {
  getById: (id) =>
    apiFetch(`/employees/${id}`),

  create: (data) =>
    apiFetch('/employees', { method: 'POST', body: JSON.stringify(data) }),

  update: (id, data) =>
    apiFetch(`/employees/${id}`, { method: 'PUT', body: JSON.stringify(data) }),

  delete: (id) =>
    apiFetch(`/employees/${id}`, { method: 'DELETE' }),

  search: ({ keyword = '', status = '', role = '', page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = {}) => {
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (status)  params.set('status', status);
    if (role)    params.set('role', role);
    params.set('page', page);
    params.set('size', size);
    params.set('sortBy', sortBy);
    params.set('sortDir', sortDir);
    return apiFetch(`/employees/search?${params}`);
  },

  autocomplete: (query) =>
    apiFetch(`/employees/autocomplete?query=${encodeURIComponent(query)}`),

  dashboard: () =>
    apiFetch('/employees/dashboard'),

  uploadImage: (id, file) => {
    const fd = new FormData();
    fd.append('file', file);
    return apiUpload(`/employees/${id}/upload-image`, fd);
  },
};

// ── Settings API ──────────────────────────────────────────
const SettingsAPI = {
  getProfile: () =>
    apiFetch('/settings/profile'),

  updateProfile: (data) =>
    apiFetch('/settings/profile', { method: 'PUT', body: JSON.stringify(data) }),

  changePassword: (data) =>
    apiFetch('/settings/change-password', { method: 'PUT', body: JSON.stringify(data) }),
};
