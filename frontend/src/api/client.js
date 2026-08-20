const API_URL = import.meta.env.VITE_API_URL;

class ApiClient {
  constructor() {
    this.baseUrl = API_URL;
  }

  getToken() {
    return localStorage.getItem('token');
  }

  async request(endpoint, options = {}) {
    const { method = 'GET', body, headers = {}, raw = false } = options;
    const token = this.getToken();

    const config = {
      method,
      headers: {
        ...headers,
      },
    };

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    if (body instanceof FormData) {
      delete config.headers['Content-Type'];
      config.body = body;
    } else if (body) {
      config.headers['Content-Type'] = 'application/json';
      config.body = JSON.stringify(body);
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, config);

    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.dispatchEvent(new Event('auth:unauthorized'));
      throw new ApiError('Unauthorized', 401);
    }

    if (response.status === 403) {
      throw new ApiError('Access denied', 403);
    }

    if (raw) {
      if (!response.ok) {
        throw new ApiError('Request failed', response.status);
      }
      return response.text();
    }

    const data = await response.json();

    if (!response.ok) {
      const message = data.message || 'An error occurred';
      const errors = data.data && typeof data.data === 'object' ? data.data : null;
      throw new ApiError(message, response.status, errors);
    }

    return data;
  }

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  post(endpoint, body) {
    return this.request(endpoint, { method: 'POST', body });
  }

  put(endpoint, body) {
    return this.request(endpoint, { method: 'PUT', body });
  }

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }

  upload(endpoint, file) {
    const formData = new FormData();
    formData.append('file', file);
    return this.request(endpoint, { method: 'POST', body: formData });
  }
}

export class ApiError extends Error {
  constructor(message, status, errors) {
    super(message);
    this.status = status;
    this.errors = errors;
  }
}

export const api = new ApiClient();

export const authApi = {
  login: (email, password) =>
    api.post('/api/auth/login', { email, password }),

  register: (email, password, adminRequest = false) =>
    api.post('/api/auth/register', { email, password, adminRequest }),
};

export const userApi = {
  getMe: () => api.get('/api/users/me'),
  getAll: () => api.get('/api/users'),
  getById: (id) => api.get(`/api/users/${id}`),
  updateMe: (data) => api.put('/api/users/me', data),
};

export const eventApi = {
  getAll: () => api.get('/api/events'),
  getById: (id) => api.get(`/api/events/${id}`),
  getUpcoming: () => api.get('/api/events/upcoming'),
  create: (data) => api.post('/api/events', data),
  update: (id, data) => api.put(`/api/events/${id}`, data),
  delete: (id) => api.delete(`/api/events/${id}`),
};

export const aboutApi = {
  getAll: () => api.get('/api/about'),
  getById: (id) => api.get(`/api/about/${id}`),
  create: (data) => api.post('/api/about', data),
  update: (id, data) => api.put(`/api/about/${id}`, data),
  delete: (id) => api.delete(`/api/about/${id}`),
};

export const programApi = {
  getAll: () => api.get('/api/programs'),
  getById: (id) => api.get(`/api/programs/${id}`),
  create: (data) => api.post('/api/programs', data),
  update: (id, data) => api.put(`/api/programs/${id}`, data),
  delete: (id) => api.delete(`/api/programs/${id}`),
};

export const galleryApi = {
  getAll: () => api.get('/api/gallery'),
  upload: (file) => api.upload('/api/gallery', file),
};

export const developerApi = {
  getAll: () => api.get('/api/developer'),
};

export const commentApi = {
  getByEvent: (eventId) => api.get(`/api/events/${eventId}/comments`),
  create: (eventId, content) => api.post(`/api/events/${eventId}/comments`, { content }),
  delete: (id) => api.delete(`/api/comments/${id}`),
};

export const reactionApi = {
  getByEvent: (eventId) => api.get(`/api/events/${eventId}/reactions`),
  add: (eventId, reactionType) =>
    api.post(`/api/events/${eventId}/reactions`, { reactionType }),
  remove: (eventId) => api.delete(`/api/events/${eventId}/reactions`),
};

export const suggestionApi = {
  getAll: () => api.get('/api/suggestions'),
  create: (content) => api.post('/api/suggestions', { content }),
  update: (id, content) => api.put(`/api/suggestions/${id}`, { content }),
};

export const notificationApi = {
  getAll: () => api.get('/api/notifications'),
  markRead: (id) => api.put(`/api/notifications/${id}/read`),
};

export const adminApi = {
  getPending: () => api.get('/api/admin/pending'),
  approve: (id, note) => api.post(`/api/admin/approve/${id}`, note ? { note } : undefined),
  reject: (id, note) => api.post(`/api/admin/reject/${id}`, note ? { note } : undefined),
};
