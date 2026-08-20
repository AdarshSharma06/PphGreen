import { useApi } from '../../hooks/useApi';
import { useNavigate } from 'react-router-dom';
import { aboutApi } from '../../api/client';
import { ArrowLeft, Trash2 } from 'lucide-react';
import './Admin.css';

export default function AboutAdmin() {
  const { data, loading, refetch } = useApi(() => aboutApi.getAll(), []);
  const navigate = useNavigate();
  const entries = data?.data || [];

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this entry?')) return;
    try { await aboutApi.delete(id); refetch(); } catch { /* best-effort */ }
  };

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="page-header"><h1>About</h1><p>Manage organization information</p></div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {entries.length > 0 && (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead><tr><th>Title</th><th>Description</th><th>Actions</th></tr></thead>
              <tbody>
                {entries.map((a) => (
                  <tr key={a.id}>
                    <td className="font-medium">{a.title}</td>
                    <td className="text-muted truncate" style={{ maxWidth: 400 }}>{a.description || '—'}</td>
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-ghost btn-sm" onClick={() => navigate('/about')}>View</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a.id)}><Trash2 size={14} /></button>
                      </div>
                    </td>
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
