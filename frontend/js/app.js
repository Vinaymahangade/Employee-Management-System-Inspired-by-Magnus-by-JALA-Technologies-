/* ============================================================
   app.js — Main application logic
   Handles: routing, sidebar, dashboard, employee CRUD,
            search/filter/pagination, autocomplete, settings
   ============================================================ */

// ── State ─────────────────────────────────────────────────
const State = {
  currentPage: 1,
  pageSize: 10,
  searchKeyword: '',
  filterStatus: '',
  filterRole: '',
  sortBy: 'createdAt',
  sortDir: 'desc',
  editingId: null,
};

// ── Init ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn()) {
    showLoginPage();
  } else {
    initApp();
  }
});

function showLoginPage() {
  document.getElementById('login-page').classList.remove('hidden');
  document.getElementById('app').classList.add('hidden');
}

function initApp() {
  document.getElementById('login-page').classList.add('hidden');
  document.getElementById('app').classList.remove('hidden');

  const user = Auth.getUser();
  document.getElementById('headerUserName').textContent = user?.name || 'User';
  document.getElementById('headerAvatar').textContent = getInitials(user?.name);

  // Hide admin-only nav items for non-admins
  if (!Auth.isAdmin()) {
    document.querySelectorAll('.admin-only').forEach(el => el.classList.add('hidden'));
  }

  initSidebar();
  initTabs();
  initAccordion();
  initSlider();
  initImageUpload();
  loadDashboard();
  navigateTo('dashboard');
}

// ── Login ─────────────────────────────────────────────────
document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const email    = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  const btn      = document.getElementById('loginBtn');
  const errEl    = document.getElementById('loginError');

  errEl.textContent = '';
  setLoading(btn, true, 'Login');

  try {
    const res = await AuthAPI.login(email, password);
    Auth.setToken(res.data.token);
    Auth.setUser(res.data.employee);
    initApp();
  } catch (err) {
    errEl.textContent = err.message;
  } finally {
    setLoading(btn, false, 'Login');
  }
});

// ── Logout ────────────────────────────────────────────────
document.getElementById('logoutBtn').addEventListener('click', () => {
  confirmAction('Are you sure you want to logout?', () => {
    Auth.clear();
    showLoginPage();
    Toast.info('Logged out successfully');
  });
});

// ── Sidebar toggle ────────────────────────────────────────
function initSidebar() {
  document.getElementById('toggleSidebar').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('collapsed');
  });

  // Nested menu expand/collapse
  document.querySelectorAll('.nav-link[data-submenu]').forEach(link => {
    link.addEventListener('click', () => {
      const targetId = link.dataset.submenu;
      const submenu  = document.getElementById(targetId);
      const isOpen   = !submenu.classList.contains('hidden');
      submenu.classList.toggle('hidden', isOpen);
      link.classList.toggle('open', !isOpen);
    });
  });
}

// ── Page Routing ──────────────────────────────────────────
function navigateTo(pageName) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

  const page   = document.getElementById('page-' + pageName);
  const navBtn = document.querySelector(`.nav-link[data-page="${pageName}"]`);
  if (page)   page.classList.add('active');
  if (navBtn) navBtn.classList.add('active');

  document.getElementById('headerTitle').textContent =
    pageName.charAt(0).toUpperCase() + pageName.slice(1).replace(/-/g, ' ');

  // Lazy-load page data
  if (pageName === 'dashboard')  loadDashboard();
  if (pageName === 'employees')  loadEmployees();
  if (pageName === 'settings')   loadSettings();
}

document.querySelectorAll('.nav-link[data-page]').forEach(link => {
  link.addEventListener('click', () => navigateTo(link.dataset.page));
});

// ── Dashboard ─────────────────────────────────────────────
async function loadDashboard() {
  try {
    const res = await EmployeeAPI.dashboard();
    const d   = res.data;
    document.getElementById('statTotal').textContent    = d.totalEmployees;
    document.getElementById('statActive').textContent   = d.activeEmployees;
    document.getElementById('statInactive').textContent = d.inactiveEmployees;
    document.getElementById('statAdmins').textContent   = d.adminCount;

    // Department breakdown
    const deptDiv = document.getElementById('deptBreakdown');
    deptDiv.innerHTML = Object.entries(d.employeesByDepartment || {})
      .map(([dept, cnt]) => `
        <div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid var(--border)">
          <span>${dept || 'Unknown'}</span><strong>${cnt}</strong>
        </div>`).join('') || '<p class="text-muted">No data</p>';
  } catch (err) {
    Toast.error('Failed to load dashboard: ' + err.message);
  }
}

// ── Employee List + Search + Pagination ───────────────────
async function loadEmployees() {
  const tbody = document.getElementById('employeeTableBody');
  tbody.innerHTML = '<tr><td colspan="8" class="text-center">Loading...</td></tr>';
  try {
    const res = await EmployeeAPI.search({
      keyword: State.searchKeyword,
      status:  State.filterStatus,
      role:    State.filterRole,
      page:    State.currentPage - 1,
      size:    State.pageSize,
      sortBy:  State.sortBy,
      sortDir: State.sortDir,
    });
    const { content, totalElements, totalPages, number } = res.data;
    renderEmployeeTable(content);
    renderPagination(totalElements, totalPages, number + 1);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center" style="color:var(--danger)">${err.message}</td></tr>`;
  }
}

function renderEmployeeTable(employees) {
  const tbody   = document.getElementById('employeeTableBody');
  const isAdmin = Auth.isAdmin();
  if (!employees.length) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No employees found</td></tr>';
    return;
  }
  tbody.innerHTML = employees.map(emp => `
    <tr>
      <td>${emp.id}</td>
      <td>
        <div style="display:flex;align-items:center;gap:8px">
          ${emp.profileImage
            ? `<img src="http://localhost:8080${emp.profileImage}" style="width:30px;height:30px;border-radius:50%;object-fit:cover">`
            : `<div class="avatar" style="width:30px;height:30px;background:var(--primary);border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;font-size:11px;font-weight:700">${getInitials(emp.name)}</div>`}
          <span>${emp.name}</span>
        </div>
      </td>
      <td>${emp.email}</td>
      <td>${emp.department || '—'}</td>
      <td><span class="badge badge-${emp.role.toLowerCase()}">${emp.role}</span></td>
      <td><span class="badge badge-${emp.status.toLowerCase()}">${emp.status}</span></td>
      <td>${formatDate(emp.createdAt)}</td>
      <td>
        <div class="flex gap-2">
          <button class="btn btn-sm btn-secondary" data-tooltip="View" onclick="viewEmployee(${emp.id})">👁</button>
          ${isAdmin ? `
          <button class="btn btn-sm btn-primary" data-tooltip="Edit" onclick="openEditModal(${emp.id})">✏️</button>
          <button class="btn btn-sm btn-danger"  data-tooltip="Delete" onclick="deleteEmployee(${emp.id}, '${emp.name}')">🗑</button>` : ''}
        </div>
      </td>
    </tr>`).join('');
}

function renderPagination(total, totalPages, current) {
  const container = document.getElementById('pagination');
  const infoEl    = document.getElementById('pageInfo');
  infoEl.textContent = `Showing ${Math.min((current-1)*State.pageSize+1, total)}–${Math.min(current*State.pageSize, total)} of ${total}`;

  let html = `<button class="page-btn" ${current===1?'disabled':''} onclick="goToPage(${current-1})">‹ Prev</button>`;
  for (let i = 1; i <= Math.min(totalPages, 7); i++) {
    html += `<button class="page-btn ${i===current?'active':''}" onclick="goToPage(${i})">${i}</button>`;
  }
  if (totalPages > 7) html += `<span class="page-info">...</span>`;
  html += `<button class="page-btn" ${current===totalPages?'disabled':''} onclick="goToPage(${current+1})">Next ›</button>`;
  container.innerHTML = html;
}

function goToPage(page) {
  State.currentPage = page;
  loadEmployees();
}

// ── Search & Filter event bindings ────────────────────────
document.getElementById('searchInput').addEventListener('input', debounce((e) => {
  State.searchKeyword = e.target.value;
  State.currentPage = 1;
  loadEmployees();
}, 400));

document.getElementById('filterStatus').addEventListener('change', (e) => {
  State.filterStatus = e.target.value;
  State.currentPage = 1;
  loadEmployees();
});

document.getElementById('filterRole').addEventListener('change', (e) => {
  State.filterRole = e.target.value;
  State.currentPage = 1;
  loadEmployees();
});

document.getElementById('sortBySelect').addEventListener('change', (e) => {
  State.sortBy = e.target.value;
  loadEmployees();
});

document.getElementById('sortDirBtn').addEventListener('click', () => {
  State.sortDir = State.sortDir === 'desc' ? 'asc' : 'desc';
  document.getElementById('sortDirBtn').textContent = State.sortDir === 'desc' ? '↓ Desc' : '↑ Asc';
  loadEmployees();
});

document.getElementById('resetFiltersBtn').addEventListener('click', () => {
  document.getElementById('searchInput').value   = '';
  document.getElementById('filterStatus').value  = '';
  document.getElementById('filterRole').value    = '';
  State.searchKeyword = '';
  State.filterStatus  = '';
  State.filterRole    = '';
  State.currentPage   = 1;
  loadEmployees();
});

// ── View Employee ─────────────────────────────────────────
async function viewEmployee(id) {
  try {
    const res = await EmployeeAPI.getById(id);
    const emp = res.data;
    document.getElementById('viewModalContent').innerHTML = `
      <div style="text-align:center;margin-bottom:16px">
        ${emp.profileImage
          ? `<img src="http://localhost:8080${emp.profileImage}" style="width:80px;height:80px;border-radius:50%;object-fit:cover;border:3px solid var(--border)">`
          : `<div style="width:80px;height:80px;border-radius:50%;background:var(--primary);display:flex;align-items:center;justify-content:center;color:#fff;font-size:28px;font-weight:700;margin:0 auto">${getInitials(emp.name)}</div>`}
        <h3 style="margin-top:12px">${emp.name}</h3>
        <p class="text-muted">${emp.designation || ''}</p>
      </div>
      <table style="width:100%;border-collapse:collapse">
        ${[
          ['ID',          emp.id],
          ['Email',       emp.email],
          ['Phone',       emp.phone || '—'],
          ['Department',  emp.department || '—'],
          ['Role',        `<span class="badge badge-${emp.role.toLowerCase()}">${emp.role}</span>`],
          ['Status',      `<span class="badge badge-${emp.status.toLowerCase()}">${emp.status}</span>`],
          ['Created At',  formatDate(emp.createdAt)],
          ['Updated At',  formatDate(emp.updatedAt)],
        ].map(([k,v]) => `
          <tr>
            <td style="padding:8px 0;font-weight:600;color:var(--text-muted);width:120px">${k}</td>
            <td style="padding:8px 0">${v}</td>
          </tr>`).join('')}
      </table>`;
    Modal.open('viewModal');
  } catch (err) {
    Toast.error(err.message);
  }
}

// ── Create Employee ───────────────────────────────────────
document.getElementById('addEmployeeBtn').addEventListener('click', () => {
  State.editingId = null;
  document.getElementById('empModalTitle').textContent = 'Add Employee';
  document.getElementById('empForm').reset();
  document.getElementById('empPasswordGroup').classList.remove('hidden');
  Modal.open('empModal');
});

// ── Edit Employee ─────────────────────────────────────────
async function openEditModal(id) {
  try {
    const res = await EmployeeAPI.getById(id);
    const emp = res.data;
    State.editingId = id;
    document.getElementById('empModalTitle').textContent = 'Edit Employee';
    document.getElementById('empPasswordGroup').classList.add('hidden');

    document.getElementById('empName').value        = emp.name;
    document.getElementById('empEmail').value       = emp.email;
    document.getElementById('empRole').value        = emp.role;
    document.getElementById('empStatus').value      = emp.status;
    document.getElementById('empPhone').value       = emp.phone || '';
    document.getElementById('empDept').value        = emp.department || '';
    document.getElementById('empDesig').value       = emp.designation || '';
    Modal.open('empModal');
  } catch (err) {
    Toast.error(err.message);
  }
}

// ── Save Employee (Create / Update) ──────────────────────
document.getElementById('empForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const rules = {
    empName:  ['required'],
    empEmail: ['required', 'email'],
    empRole:  ['required'],
    empPhone: ['phone10'],
  };
  if (!State.editingId) {
    rules.empPassword = ['required', 'minlen6'];
  }
  if (!validateForm('empForm', rules)) return;

  const btn  = document.getElementById('saveEmpBtn');
  const data = {
    name:        document.getElementById('empName').value.trim(),
    email:       document.getElementById('empEmail').value.trim(),
    role:        document.getElementById('empRole').value,
    status:      document.getElementById('empStatus').value,
    phone:       document.getElementById('empPhone').value.trim(),
    department:  document.getElementById('empDept').value.trim(),
    designation: document.getElementById('empDesig').value.trim(),
  };
  if (!State.editingId) {
    data.password = document.getElementById('empPassword').value;
  }

  setLoading(btn, true, 'Save');
  try {
    if (State.editingId) {
      await EmployeeAPI.update(State.editingId, data);
      Toast.success('Employee updated successfully');
    } else {
      await EmployeeAPI.create(data);
      Toast.success('Employee created successfully');
    }
    Modal.close('empModal');
    loadEmployees();
    loadDashboard();
  } catch (err) {
    Toast.error(err.message);
  } finally {
    setLoading(btn, false, 'Save');
  }
});

// ── Delete Employee ───────────────────────────────────────
function deleteEmployee(id, name) {
  confirmAction(`Delete employee "${name}"? This will deactivate their account.`, async () => {
    try {
      await EmployeeAPI.delete(id);
      Toast.success('Employee deactivated successfully');
      loadEmployees();
      loadDashboard();
    } catch (err) {
      Toast.error(err.message);
    }
  });
}

// ── Autocomplete ──────────────────────────────────────────
const acInput = document.getElementById('autocompleteInput');
const acList  = document.getElementById('autocompleteList');

acInput.addEventListener('input', debounce(async (e) => {
  const q = e.target.value.trim();
  if (q.length < 2) { acList.innerHTML = ''; return; }
  try {
    const res = await EmployeeAPI.autocomplete(q);
    acList.innerHTML = res.data.length
      ? res.data.map(emp => `
          <div class="autocomplete-item" onclick="selectAcItem('${emp.name}', '${emp.email}')">
            <span class="ac-name">${emp.name}</span>
            <span class="ac-email">${emp.email} · ${emp.department || ''}</span>
          </div>`).join('')
      : '<div class="autocomplete-item text-muted">No results</div>';
  } catch { acList.innerHTML = ''; }
}, 300));

function selectAcItem(name, email) {
  acInput.value = name;
  acList.innerHTML = '';
  document.getElementById('acResult').textContent = `Selected: ${name} (${email})`;
}

document.addEventListener('click', (e) => {
  if (!e.target.closest('.autocomplete-wrapper')) acList.innerHTML = '';
});

// ── Tabs ──────────────────────────────────────────────────
function initTabs() {
  document.querySelectorAll('.tab-list').forEach(tabList => {
    tabList.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const target = btn.dataset.tab;
        const parent = btn.closest('.tab-container');
        parent.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        parent.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        parent.querySelector(`.tab-panel[data-tab="${target}"]`).classList.add('active');
      });
    });
  });
}

// ── Accordion ─────────────────────────────────────────────
function initAccordion() {
  document.querySelectorAll('.accordion-header').forEach(header => {
    header.addEventListener('click', () => {
      const body     = header.nextElementSibling;
      const isOpen   = body.classList.contains('open');
      // Close all others in same accordion
      header.closest('.accordion')?.querySelectorAll('.accordion-body.open')
        .forEach(b => b.classList.remove('open'));
      if (!isOpen) body.classList.add('open');
    });
  });
}

// ── Slider ────────────────────────────────────────────────
function initSlider() {
  document.querySelectorAll('input[type="range"]').forEach(slider => {
    const displayId = slider.dataset.display;
    if (displayId) {
      const display = document.getElementById(displayId);
      const update  = () => { if (display) display.textContent = slider.value; };
      slider.addEventListener('input', update);
      update();
    }
  });
}

// ── Image Upload (profile from employee list) ─────────────
function initImageUpload() {
  const zone  = document.getElementById('uploadZone');
  const input = document.getElementById('fileInput');
  if (!zone) return;

  zone.addEventListener('click', () => input.click());
  zone.addEventListener('dragover', (e) => { e.preventDefault(); zone.style.borderColor = 'var(--primary)'; });
  zone.addEventListener('dragleave', () => { zone.style.borderColor = ''; });
  zone.addEventListener('drop', (e) => {
    e.preventDefault();
    zone.style.borderColor = '';
    handleImageFile(e.dataTransfer.files[0]);
  });

  input.addEventListener('change', (e) => {
    if (e.target.files[0]) handleImageFile(e.target.files[0]);
  });
}

function handleImageFile(file) {
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (e) => {
    document.getElementById('uploadPreviewImg').src = e.target.result;
    document.getElementById('uploadPreview').classList.remove('hidden');
    document.getElementById('uploadFileName').textContent = file.name;
  };
  reader.readAsDataURL(file);

  // Store for upload
  window._pendingUploadFile = file;
}

document.getElementById('uploadImageBtn')?.addEventListener('click', async () => {
  const empId = document.getElementById('uploadEmpId').value.trim();
  if (!empId) { Toast.error('Enter employee ID'); return; }
  if (!window._pendingUploadFile) { Toast.error('Select an image first'); return; }

  const btn = document.getElementById('uploadImageBtn');
  setLoading(btn, true, 'Upload');
  try {
    await EmployeeAPI.uploadImage(empId, window._pendingUploadFile);
    Toast.success('Image uploaded successfully!');
    window._pendingUploadFile = null;
  } catch (err) {
    Toast.error(err.message);
  } finally {
    setLoading(btn, false, 'Upload');
  }
});

// ── Settings ──────────────────────────────────────────────
async function loadSettings() {
  const user = Auth.getUser();
  if (!user) return;
  document.getElementById('settingName').value  = user.name  || '';
  document.getElementById('settingEmail').value = user.email || '';
  document.getElementById('settingPhone').value = user.phone || '';
  document.getElementById('settingDept').value  = user.department  || '';
  document.getElementById('settingDesig').value = user.designation || '';
}

document.getElementById('profileForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn  = document.getElementById('saveProfileBtn');
  const data = {
    name:        document.getElementById('settingName').value.trim(),
    phone:       document.getElementById('settingPhone').value.trim(),
    department:  document.getElementById('settingDept').value.trim(),
    designation: document.getElementById('settingDesig').value.trim(),
  };
  setLoading(btn, true, 'Save Profile');
  try {
    const res = await SettingsAPI.updateProfile(data);
    Auth.setUser({ ...Auth.getUser(), ...res.data });
    document.getElementById('headerUserName').textContent = res.data.name;
    document.getElementById('headerAvatar').textContent   = getInitials(res.data.name);
    Toast.success('Profile updated successfully');
  } catch (err) {
    Toast.error(err.message);
  } finally {
    setLoading(btn, false, 'Save Profile');
  }
});

document.getElementById('passwordForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const newPwd = document.getElementById('newPassword').value;
  const cfmPwd = document.getElementById('confirmPassword').value;
  if (newPwd !== cfmPwd) { Toast.error('Passwords do not match'); return; }

  const btn  = document.getElementById('savePasswordBtn');
  const data = {
    currentPassword: document.getElementById('currentPassword').value,
    newPassword:     newPwd,
    confirmPassword: cfmPwd,
  };
  setLoading(btn, true, 'Change Password');
  try {
    await SettingsAPI.changePassword(data);
    Toast.success('Password changed successfully');
    document.getElementById('passwordForm').reset();
  } catch (err) {
    Toast.error(err.message);
  } finally {
    setLoading(btn, false, 'Change Password');
  }
});
