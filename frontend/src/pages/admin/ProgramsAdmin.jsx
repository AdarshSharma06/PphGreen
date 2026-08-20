import { useApi } from '../../hooks/useApi';
import { useNavigate } from 'react-router-dom';
import { programApi } from '../../api/client';
import { ArrowLeft, Trash2 } from 'lucide-react';
import './Admin.css';

export default function ProgramsAdmin() {
  const { data, loading, refetch } = useApi(() => programApi.getAll(), []);
  const navigate = useNavigate();
  const programs = data?.data || [];

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this program?')) return;
    try { await programApi.delete(id); refetch(); } catch { /* best-effort */ }
  };

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="page-header"><h1>Programs</h1><p>Manage community programs</p></div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {programs.length > 0 && (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead><tr><th>Title</th><th>Description</th><th>Created By</th><th>Actions</th></tr></thead>
              <tbody>
                {programs.map((p) => (
                  <tr key={p.id}>
                    <td className="font-medium">{p.title}</td>
                    <td className="text-muted truncate" style={{ maxWidth: 300 }}>{p.description || '—'}</td>
                    <td className="text-muted">{p.createdBy?.name || '—'}</td>
                    <td><button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.id)}><Trash2 size={14} /></button></td>
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
