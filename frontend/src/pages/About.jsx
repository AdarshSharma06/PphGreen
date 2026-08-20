import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { aboutApi } from '../api/client';
import { Plus, Pencil, Trash2, Leaf } from 'lucide-react';
import Modal from '../components/ui/Modal';
import EmptyState from '../components/ui/EmptyState';
import './About.css';

export default function About() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => aboutApi.getAll(), []);
  const [showCreate, setShowCreate] = useState(false);
  const [editing, setEditing] = useState(null);

  const aboutEntries = data?.data || [];

  return (
    <div className="about-page">
      <div className="container">
        <div className="page-header flex items-center justify-between">
          <div>
            <h1>About Us</h1>
            <p>Our mission, values, and impact</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={16} /> New Entry
            </button>
          )}
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && aboutEntries.length === 0 && (
          <EmptyState icon={Leaf} title="About page coming soon" description="Content will be added here." />
        )}

        {aboutEntries.map((entry) => (
          <div key={entry.id} className="about-entry animate-fade-in">
            <div className="about-entry-grid">
              <div className="about-entry-content">
                <h2 className="about-entry-title">{entry.title}</h2>
                {entry.description && <p className="about-entry-desc">{entry.description}</p>}
                {entry.ideals && (
                  <div className="about-ideals">
                    <h4>Our Values</h4>
                    {entry.ideals.split('\n').filter(Boolean).map((ideal, i) => (
                      <div key={i} className="about-ideal-item">
                        <div className="about-ideal-dot" />
                        <span>{ideal.trim()}</span>
                      </div>
                    ))}
                  </div>
                )}
                {entry.impactMetrics && (
                  <div className="about-impact">
                    <h4>Our Impact</h4>
                    <p className="about-impact-text">{entry.impactMetrics}</p>
                  </div>
                )}
                {isAdmin && (
                  <div className="about-actions">
                    <button className="btn btn-ghost btn-sm" onClick={() => setEditing(entry)}>
                      <Pencil size={14} /> Edit
                    </button>
                    <button className="btn btn-ghost btn-sm" style={{ color: 'var(--error)' }}
                      onClick={async () => { if (window.confirm('Delete this entry?')) { await aboutApi.delete(entry.id); refetch(); } }}>
                      <Trash2 size={14} /> Delete
                    </button>
                  </div>
                )}
              </div>
              {entry.image && (
                <div className="about-entry-image">
                  <img src={entry.image} alt={entry.title} loading="lazy" />
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {showCreate && <AboutModal onClose={() => setShowCreate(false)} onSaved={() => { setShowCreate(false); refetch(); }} />}
      {editing && <AboutModal entry={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); refetch(); }} />}
    </div>
  );
}

function AboutModal({ entry, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: entry?.title || '',
    description: entry?.description || '',
    ideals: entry?.ideals || '',
    impactMetrics: entry?.impactMetrics || '',
    image: entry?.image || '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (entry) {
        await aboutApi.update(entry.id, form);
      } else {
        await aboutApi.create(form);
      }
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen onClose={onClose} title={entry ? 'Edit About' : 'New About Entry'}
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
          <label className="input-label">Values (one per line)</label>
          <textarea className="input" rows={3} value={form.ideals} onChange={(e) => setForm({ ...form, ideals: e.target.value })} placeholder="Sustainability&#10;Community&#10;Innovation" />
        </div>
        <div className="input-group">
          <label className="input-label">Impact Metrics</label>
          <textarea className="input" rows={3} value={form.impactMetrics} onChange={(e) => setForm({ ...form, impactMetrics: e.target.value })} />
        </div>
        <div className="input-group">
          <label className="input-label">Image URL</label>
          <input className="input" placeholder="https://..." value={form.image} onChange={(e) => setForm({ ...form, image: e.target.value })} />
        </div>
      </form>
    </Modal>
  );
}
