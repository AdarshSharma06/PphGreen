import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { programApi } from '../api/client';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Trash2, Calendar, MapPin, Leaf, Sprout, Footprints, Users, Clock, ArrowRight, TreePine, Mountain, Building2, HeartHandshake } from 'lucide-react';
import Modal from '../components/ui/Modal';
import './Programs.css';

const FEATURED_PROGRAM = {
  id: 'sunday-nature-walks',
  title: 'Sunday Nature Walks',
  category: 'Nature & Wellness',
  frequency: 'Every Sunday',
  description: 'Join fellow residents for a refreshing Sunday morning walk, enjoy greenery around our community, reconnect with nature, and build meaningful connections with neighbours.',
  activities: ['Walking together', 'Enjoying green spaces', 'Observing nature', 'Friendly conversations', 'Community bonding'],
  location: 'To be announced for each walk',
  schedule: 'Every Sunday',
  status: 'Recurring program',
  image: '/assets/programs/program_1.jpg',
};

const FUTURE_PROGRAM_TEMPLATES = [
  { title: 'Plantation Drives', category: 'Green Action', desc: 'Hands-on tree planting and caring for our shared green spaces.', icon: TreePine },
  { title: 'Nature Park Visits', category: 'Exploration', desc: 'Group visits to Cubbon Park, Lalbagh and other green oases.', icon: Mountain },
  { title: 'Temple Visits', category: 'Culture & Community', desc: 'Peaceful visits that blend cultural heritage with togetherness.', icon: Building2 },
  { title: 'Community Gatherings', category: 'Togetherness', desc: 'Festive evenings and neighbourhood meetups in the courtyard.', icon: HeartHandshake },
];

export default function Programs() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => programApi.getAll(), []);
  const [showCreate, setShowCreate] = useState(false);
  const [editing, setEditing] = useState(null);

  const apiPrograms = data?.data || [];
  // Filter out duplicate Sunday Nature Walks if admin already created it via API
  const filteredApiPrograms = apiPrograms.filter((p) => p.title?.toLowerCase() !== FEATURED_PROGRAM.title.toLowerCase());
  const hasApiPrograms = filteredApiPrograms.length > 0;

  return (
    <div className="programs-page">
      <div className="container">
        {/* Intro */}
        <div className="programs-hero animate-fade-in-up">
          <div className="programs-hero-content">
            <span className="section-label"><Sprout size={14} /> Community Programs</span>
            <h1>Our Community Programs</h1>
            <p>Bringing residents together through nature, wellness, and meaningful community activities — right where we live.</p>
          </div>
          <div className="programs-hero-visual" aria-hidden="true">
            <div className="programs-hero-blob" />
            <Leaf size={80} strokeWidth={1.2} className="programs-hero-icon" />
          </div>
        </div>

        <div className="page-header flex items-center justify-between" style={{ marginTop: 'var(--space-8)' }}>
          <div>
            <h2 style={{ fontSize: 'var(--text-xl)' }}>Recurring Programs</h2>
            <p className="text-sm text-muted">Ongoing initiatives for a greener, friendlier neighbourhood</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={16} /> New Program
            </button>
          )}
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}

        {/* Featured Sunday Nature Walks */}
        <section className="featured-program-section animate-fade-in">
          <div className="featured-program-card card">
            <div className="featured-program-image">
              <img
                src={FEATURED_PROGRAM.image}
                alt="Residents enjoying a Sunday nature walk among green trees"
                loading="eager"
                onError={(e) => { e.currentTarget.src = '/assets/aesthetic/aesthetic.jpg'; }}
              />
              <div className="featured-badge">
                <Calendar size={12} /> {FEATURED_PROGRAM.frequency}
              </div>
            </div>
            <div className="featured-program-body">
              <div className="featured-program-meta">
                <span className="badge badge-green">{FEATURED_PROGRAM.category}</span>
                <span className="featured-recurring"><Clock size={12} /> Recurring</span>
              </div>
              <h3>{FEATURED_PROGRAM.title}</h3>
              <p className="featured-program-desc">{FEATURED_PROGRAM.description}</p>

              <div className="featured-program-details">
                <div className="featured-detail">
                  <Footprints size={14} className="featured-detail-icon" />
                  <div>
                    <span className="featured-detail-label">Activities</span>
                    <span className="featured-detail-text">{FEATURED_PROGRAM.activities.join(' · ')}</span>
                  </div>
                </div>
                <div className="featured-detail">
                  <MapPin size={14} className="featured-detail-icon" />
                  <div>
                    <span className="featured-detail-label">Location</span>
                    <span className="featured-detail-text">{FEATURED_PROGRAM.location}</span>
                  </div>
                </div>
                <div className="featured-detail">
                  <Calendar size={14} className="featured-detail-icon" />
                  <div>
                    <span className="featured-detail-label">Schedule</span>
                    <span className="featured-detail-text">{FEATURED_PROGRAM.schedule}</span>
                  </div>
                </div>
              </div>

              <div className="featured-program-footer">
                <span className="text-xs text-muted flex items-center gap-1"><Users size={12} /> Open to all residents</span>
                <Link to="/events" className="btn btn-primary btn-sm">
                  View Events <ArrowRight size={14} />
                </Link>
              </div>
            </div>
          </div>
        </section>

        {/* Scalable grid for API programs */}
        <section className="programs-grid-section">
          <div className="section-header flex items-center justify-between">
            <h2>All Programs</h2>
            <span className="text-sm text-muted">{hasApiPrograms ? `${filteredApiPrograms.length + 1} programs` : '1 featured program'}</span>
          </div>

          <div className="programs-grid">
            {/* Show API programs first */}
            {filteredApiPrograms.map((program) => (
              <div key={program.id} className="program-card card">
                {program.image ? (
                  <div className="program-card-image">
                    <img src={program.image} alt={program.title} loading="lazy" onError={(e) => e.currentTarget.style.display = 'none'} />
                  </div>
                ) : (
                  <div className="program-card-image program-card-image-fallback">
                    <Sprout size={32} strokeWidth={1.2} />
                  </div>
                )}
                <div className="card-body program-card-body">
                  <h3 className="program-card-title">{program.title}</h3>
                  {program.description && <p className="program-card-desc">{program.description}</p>}
                  {program.createdBy?.name && <p className="text-xs text-muted" style={{ marginTop: 'var(--space-3)' }}>by {program.createdBy.name}</p>}
                  {isAdmin && (
                    <div className="program-card-actions">
                      <button className="btn btn-ghost btn-sm" onClick={() => setEditing(program)}>
                        <Pencil size={14} /> Edit
                      </button>
                      <button
                        className="btn btn-ghost btn-sm"
                        style={{ color: 'var(--error)' }}
                        onClick={async () => {
                          if (window.confirm('Delete this program?')) {
                            await programApi.delete(program.id);
                            refetch();
                          }
                        }}
                      >
                        <Trash2 size={14} /> Delete
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}

            {/* Show future templates as subtle placeholders when API has few items */}
            {!hasApiPrograms && FUTURE_PROGRAM_TEMPLATES.slice(0, 2).map((tpl) => (
              <div key={tpl.title} className="program-card card program-card-placeholder">
                <div className="program-card-image program-card-image-fallback">
                  <tpl.icon size={32} strokeWidth={1.4} />
                </div>
                <div className="card-body program-card-body">
                  <span className="badge badge-green" style={{ fontSize: '10px', marginBottom: 'var(--space-2)' }}>{tpl.category}</span>
                  <h3 className="program-card-title">{tpl.title}</h3>
                  <p className="program-card-desc">{tpl.desc}</p>
                  <span className="text-xs text-muted" style={{ marginTop: 'var(--space-3)', display: 'block' }}>Coming soon</span>
                </div>
              </div>
            ))}
          </div>

          {!hasApiPrograms && (
            <p className="text-sm text-muted" style={{ textAlign: 'center', marginTop: 'var(--space-8)' }}>
              More programs like plantation drives and nature excursions will appear here as our community grows.
            </p>
          )}
        </section>

        <section className="programs-cta card" style={{ marginTop: 'var(--space-12)', background: 'var(--green-50)', border: '1px solid var(--green-100)' }}>
          <div className="card-body" style={{ textAlign: 'center', padding: 'var(--space-10)' }}>
            <h3>Want to join a program?</h3>
            <p className="text-sm text-muted" style={{ maxWidth: 520, margin: 'var(--space-3) auto 0' }}>
              Explore upcoming events and community moments, or suggest a new activity for our neighbourhood.
            </p>
            <div className="flex items-center justify-center gap-3" style={{ marginTop: 'var(--space-6)' }}>
              <Link to="/events" className="btn btn-primary">
                View Events <ArrowRight size={16} />
              </Link>
              <Link to="/gallery" className="btn btn-secondary">
                Gallery
              </Link>
            </div>
          </div>
        </section>
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
    <Modal isOpen onClose={onClose} title={program ? 'Edit Program' : 'New Program'} footer={<><button className="btn btn-secondary" onClick={onClose}>Cancel</button><button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>{loading ? 'Saving...' : 'Save'}</button></>}>
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
