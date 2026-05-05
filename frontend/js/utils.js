/* ============================================================
   utils.js — Shared UI utilities
   ============================================================ */

// ── Toast Notifications ───────────────────────────────────
const Toast = {
  show(message, type = 'info', duration = 3500) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => { toast.remove(); }, duration);
  },
  success: (msg) => Toast.show(msg, 'success'),
  error:   (msg) => Toast.show(msg, 'error'),
  info:    (msg) => Toast.show(msg, 'info'),
};

// ── Modal helpers ─────────────────────────────────────────
const Modal = {
  open(id)  { document.getElementById(id)?.classList.add('open'); },
  close(id) { document.getElementById(id)?.classList.remove('open'); },
  closeAll() {
    document.querySelectorAll('.modal-overlay.open')
      .forEach(m => m.classList.remove('open'));
  },
};

// Close any modal when clicking the overlay background
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) Modal.closeAll();
});

// ── Confirm Dialog (popup) ────────────────────────────────
function confirmAction(message, onConfirm) {
  document.getElementById('confirmMessage').textContent = message;
  Modal.open('confirmModal');
  const btn = document.getElementById('confirmOkBtn');
  const newBtn = btn.cloneNode(true); // remove old listeners
  btn.parentNode.replaceChild(newBtn, btn);
  newBtn.addEventListener('click', () => {
    Modal.close('confirmModal');
    onConfirm();
  });
}

// ── Form Validation ───────────────────────────────────────
function validateForm(formId, rules) {
  let valid = true;
  // Clear previous errors
  document.querySelectorAll(`#${formId} .form-error`).forEach(el => el.textContent = '');

  Object.entries(rules).forEach(([fieldId, checks]) => {
    const el    = document.getElementById(fieldId);
    const errEl = document.getElementById(fieldId + 'Error');
    if (!el) return;
    const val   = el.value.trim();

    for (const check of checks) {
      let err = null;
      if (check === 'required'   && !val)           err = 'This field is required';
      if (check === 'email'      && val && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) err = 'Invalid email format';
      if (check === 'minlen6'    && val && val.length < 6) err = 'Must be at least 6 characters';
      if (check === 'phone10'    && val && !/^\d{10}$/.test(val)) err = 'Phone must be 10 digits';
      if (typeof check === 'function') err = check(val, formId);

      if (err) {
        if (errEl) errEl.textContent = err;
        el.style.borderColor = 'var(--danger)';
        valid = false;
        break;
      } else {
        el.style.borderColor = '';
      }
    }
  });
  return valid;
}

// ── Date formatter ────────────────────────────────────────
function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', {
    year: 'numeric', month: 'short', day: '2-digit',
  });
}

// ── Debounce ──────────────────────────────────────────────
function debounce(fn, delay = 300) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), delay); };
}

// ── Loading state on button ───────────────────────────────
function setLoading(btn, isLoading, label = 'Save') {
  btn.disabled = isLoading;
  btn.textContent = isLoading ? 'Please wait...' : label;
}

// ── Render avatar initials ────────────────────────────────
function getInitials(name) {
  return (name || 'U').split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
}
