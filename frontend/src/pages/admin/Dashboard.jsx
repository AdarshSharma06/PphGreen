import { Link } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { adminApi, eventApi, programApi, galleryApi, suggestionApi } from '../../api/client';
import { Calendar, Image, BookOpen, MessageSquare, Users, Shield } from 'lucide-react';
import './Admin.css';

export default function AdminDashboard() {
  const { data: pendingData } = useApi(() => adminApi.getPending(), []);
  const { data: eventsData } = useApi(() => eventApi.getAll(), []);
  const { data: programsData } = useApi(() => programApi.getAll(), []);
  const { data: galleryData } = useApi(() => galleryApi.getAll(), []);
  const { data: suggestionsData } = useApi(() => suggestionApi.getAll(), []);

  const stats = [
    { label: 'Pending Requests', value: (pendingData?.data || []).length, icon: Users, color: 'var(--warning)', to: '/admin/approvals' },
    { label: 'Events', value: (eventsData?.data || []).length, icon: Calendar, color: 'var(--green-600)', to: '/admin/events' },
    { label: 'Programs', value: (programsData?.data || []).length, icon: BookOpen, color: 'var(--info)', to: '/admin/programs' },
    { label: 'Gallery Items', value: (galleryData?.data || []).length, icon: Image, color: '#8b5cf6', to: '/admin/gallery' },
    { label: 'Suggestions', value: (suggestionsData?.data || []).length, icon: MessageSquare, color: '#ec4899', to: '/admin/suggestions' },
  ];

  return (
    <div className="admin-page">
      <div className="container">
        <div className="page-header">
          <div className="flex items-center gap-3">
            <Shield size={24} color="var(--green-600)" />
            <div>
              <h1>Admin Dashboard</h1>
              <p>Manage your community platform</p>
            </div>
          </div>
        </div>

        <div className="admin-stats-grid">
          {stats.map((stat) => (
            <Link key={stat.label} to={stat.to} className="admin-stat-card card">
              <div className="card-body">
                <div className="admin-stat-icon" style={{ color: stat.color }}>
                  <stat.icon size={20} />
                </div>
                <div className="admin-stat-value">{stat.value}</div>
                <div className="admin-stat-label">{stat.label}</div>
              </div>
            </Link>
          ))}
        </div>

        <div className="admin-nav-grid">
          <Link to="/admin/events" className="admin-nav-card card">
            <div className="card-body">
              <Calendar size={24} color="var(--green-600)" />
              <h3>Manage Events</h3>
              <p className="text-sm text-muted">Create, edit, and delete events</p>
            </div>
          </Link>
          <Link to="/admin/gallery" className="admin-nav-card card">
            <div className="card-body">
              <Image size={24} color="#8b5cf6" />
              <h3>Manage Gallery</h3>
              <p className="text-sm text-muted">Upload and manage media</p>
            </div>
          </Link>
          <Link to="/admin/programs" className="admin-nav-card card">
            <div className="card-body">
              <BookOpen size={24} color="var(--info)" />
              <h3>Manage Programs</h3>
              <p className="text-sm text-muted">Create and edit programs</p>
            </div>
          </Link>
          <Link to="/admin/about" className="admin-nav-card card">
            <div className="card-body">
              <Shield size={24} color="var(--green-600)" />
              <h3>Manage About</h3>
              <p className="text-sm text-muted">Update organization info</p>
            </div>
          </Link>
          <Link to="/admin/suggestions" className="admin-nav-card card">
            <div className="card-body">
              <MessageSquare size={24} color="#ec4899" />
              <h3>Suggestions</h3>
              <p className="text-sm text-muted">Review community suggestions</p>
            </div>
          </Link>
          <Link to="/admin/approvals" className="admin-nav-card card">
            <div className="card-body">
              <Users size={24} color="var(--warning)" />
              <h3>Admin Requests</h3>
              <p className="text-sm text-muted">Approve or reject admin requests</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}
