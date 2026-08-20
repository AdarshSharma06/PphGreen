import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { programApi } from '../api/client';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import Modal from '../components/ui/Modal';
import EmptyState from '../components/ui/EmptyState';
import './Programs.css';

export default function Programs() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => programApi.getAll(), []);
  const [showCreate, setShowCreate] = useState(false);
  const [editing, setEditing] = useState(null);

  const programs = data?.data || [];

  return (
    <div className="programs-page">
      <div className="container">
        <div className="page-header flex items-center justify-between">
          <div>
            <h1>Programs</h1>
            <p>Initiatives shaping our community</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={16} /> New Program
            </button>
          )}
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && programs.length === 0 && (
          <EmptyState title="No programs yet" description="Programs will appear here once created." />
        )}

        <div className="programs-list">
          {programs.map((program) => (
            <div key={program.id} className="program-list-item card">
              {program.image && (
                <div className="program-list-image">
                  <img src={program.image} alt={program.title} loading="lazy" />
                </div>
              )}
              <div className="card-body">
                <h3 className="program-list-title">{program.title}</h3>
                {program.description && <p className="program-list-desc">{program.description}</p>}
                {program.createdBy?.name && (
                  <p className="text-xs text-muted mt-4">by {program.createdBy.name}</p>
                )}
                {isAdmin && (
                  <div className="program-list-actions mt-4">
                    <button className="btn btn-ghost btn-sm" onClick={() => setEditing(program)}>
                      <Pencil size={14} /> Edit
                    </button>
                    <button className="btn btn-ghost btn-sm" style={{ color: 'var(--error)' }}
                      onClick={async () => { if (window.confirm('Delete this program?')) { await programApi.delete(program.id); refetch(); } }}>
                      <Trash2 size={14} /> Delete
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {showCreate && <ProgramModal onClose={() => setShowCreate(false)} onSaved={() => { setShowCreate(false); refetch(); }} />}
      {editing && <ProgramModal program={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); refetch(); }} />}
    </div>
  );
}

function ProgramModal({ program, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: program?.title || '',
    description: program?.description || '',
    image: program?.image || '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (program) {
        await programApi.update(program.id, form);
      } else {
        await programApi.create(form);
      }
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen onClose={onClose} title={program ? 'Edit Program' : 'New Program'}
      footer={<><button className="btn btn-secondary" onClick={onClose}>Cancel</button><button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>{loading ? 'Saving...' : 'Save'}</button></>}>
      {error && <div className="alert alert-error mb-4">{error}</div>}
      <form onSubmit={handleSubmit} className="form-grid">
        <div className="input-group">
          <label className="input-label">Title *</label>
          <input className="input" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div className="input-group">
          <label className="input-label">Description</label>
          <textarea className="input" rows={4} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div className="input-group">
          <label className="input-label">Image URL</label>
          <input className="input" placeholder="https://..." value={form.image} onChange={(e) => setForm({ ...form, image: e.target.value })} />
        </div>
      </form>
    </Modal>
  );
}
