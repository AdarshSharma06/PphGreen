import { useApi } from '../../hooks/useApi';
import { useNavigate } from 'react-router-dom';
import { eventApi } from '../../api/client';
import { Trash2, ArrowLeft, Plus } from 'lucide-react';
import './Admin.css';

export default function EventsAdmin() {
  const { data, loading, refetch } = useApi(() => eventApi.getAll(), []);
  const navigate = useNavigate();
  const events = data?.data || [];

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this event?')) return;
    try { await eventApi.delete(id); refetch(); } catch { /* best-effort */ }
  };

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="page-header flex items-center justify-between">
          <div><h1>Events</h1><p>Manage community events</p></div>
          <button className="btn btn-primary" onClick={() => navigate('/events')}><Plus size={16} /> Create Event</button>
        </div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {events.length > 0 && (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead><tr><th>Title</th><th>Date</th><th>Venue</th><th>Created By</th><th>Actions</th></tr></thead>
              <tbody>
                {events.map((e) => (
                  <tr key={e.id}>
                    <td className="font-medium">{e.title}</td>
                    <td>{e.date}</td>
                    <td className="text-muted">{e.venue || '—'}</td>
                    <td className="text-muted">{e.createdBy?.name || '—'}</td>
                    <td><button className="btn btn-danger btn-sm" onClick={() => handleDelete(e.id)}><Trash2 size={14} /></button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
