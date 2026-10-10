import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { aboutApi } from '../api/client';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Trash2, Leaf, Sprout, TreePine, Mountain, Building2, HeartHandshake, Users, Eye, ArrowRight, Recycle } from 'lucide-react';
import Modal from '../components/ui/Modal';
import './About.css';

export default function About() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => aboutApi.getAll(), []);
  const [showCreate, setShowCreate] = useState(false);
  const [editing, setEditing] = useState(null);

  const aboutEntries = data?.data || [];

  return (
    <div className="about-page">
      {/* Hero */}
      <section className="about-hero">
        <div className="container">
          <div className="about-hero-grid">
            <div className="about-hero-content animate-fade-in-up">
              <span className="section-label"><Leaf size={14} /> PPH Green Group</span>
              <h1>Growing a Greener, <span className="text-accent">More Connected</span> Community</h1>
              <p className="about-hero-desc">
                PPH Green Group brings residents together through environmental awareness, shared experiences, and community activities that help us reconnect with nature and one another.
              </p>
              <div className="about-hero-actions">
                <Link to="/programs" className="btn btn-primary">Our Programs <ArrowRight size={16} /></Link>
                <Link to="/events" className="btn btn-secondary">Upcoming Events</Link>
              </div>
            </div>
            <div className="about-hero-visual animate-fade-in">
              <div className="about-hero-image-wrap">
                <img src="/assets/aesthetic/aesthetic.jpg" alt="Green leaves and natural textures representing PPH Green community" loading="eager" />
                <div className="about-hero-badge">
                  <Sprout size={18} />
                  <span>Prestige Primrose Hills<br />Apartment Green Group</span>
                </div>
              </div>
              <div className="about-hero-accent" aria-hidden="true" />
            </div>
          </div>
        </div>
      </section>

      {/* Mission */}
      <section className="section about-mission-section">
        <div className="container">
          <div className="section-header" style={{ textAlign: 'center', flexDirection: 'column' }}>
            <span className="section-label">Our Mission</span>
            <h2>What we stand for</h2>
            <p className="section-desc" style={{ maxWidth: 640, margin: 'var(--space-3) auto 0' }}>
              We focus on practical environmental awareness, sustainability, and neighbourly connections — welcoming every resident to participate.
            </p>
          </div>

          <div className="mission-grid">
            <div className="mission-card card">
              <div className="mission-icon" style={{ background: 'var(--green-50)', color: 'var(--green-600)' }}><Recycle size={22} /></div>
              <h3>Environmental Awareness</h3>
              <p>Encouraging everyday sustainable choices and a deeper respect for our shared environment.</p>
            </div>
            <div className="mission-card card">
              <div className="mission-icon" style={{ background: '#fef3c7', color: '#d97706' }}><Eye size={22} /></div>
              <h3>Appreciate Nature</h3>
              <p>Helping residents notice, enjoy, and care for the greenery around us.</p>
            </div>
            <div className="mission-card card">
              <div className="mission-icon" style={{ background: '#dbeafe', color: '#2563eb' }}><Users size={22} /></div>
              <h3>Stronger Neighbours</h3>
              <p>Building genuine relationships through shared walks, talks, and time together.</p>
            </div>
            <div className="mission-card card">
              <div className="mission-icon" style={{ background: '#fce7f3', color: '#db2777' }}><HeartHandshake size={22} /></div>
              <h3>Welcoming & Accessible</h3>
              <p>Making participation easy, friendly, and open to everyone in the community.</p>
            </div>
          </div>
        </div>
      </section>

      {/* What we do */}
      <section className="section about-activities-section" style={{ background: 'var(--gray-50)', borderTop: '1px solid var(--border-light)', borderBottom: '1px solid var(--border-light)' }}>
        <div className="container">
          <div className="section-header">
            <div>
              <span className="section-label">What we do</span>
              <h2>Community activities</h2>
            </div>
            <p className="text-sm text-muted" style={{ maxWidth: 380 }}>Regular and occasional gatherings — from weekly walks to seasonal green initiatives.</p>
          </div>

          <div className="activities-grid">
            <div className="activity-card card activity-featured">
              <div className="activity-icon"><Sprout size={24} /></div>
              <span className="badge badge-green">Every Sunday</span>
              <h3>Sunday Nature Walks</h3>
              <p>A recurring opportunity to walk together, enjoy greenery, and connect with fellow residents.</p>
              <Link to="/programs" className="btn btn-ghost btn-sm" style={{ marginTop: 'auto', alignSelf: 'flex-start' }}>Learn more <ArrowRight size={14} /></Link>
            </div>

            <div className="activity-card card">
              <div className="activity-icon" style={{ color: 'var(--green-600)', background: 'var(--green-50)' }}><TreePine size={20} /></div>
              <h4>Plantation Drives</h4>
              <p>Occasional activities that encourage residents to participate in planting and caring for greenery.</p>
            </div>

            <div className="activity-card card">
              <div className="activity-icon" style={{ color: '#0ea5e9', background: '#e0f2fe' }}><Mountain size={20} /></div>
              <h4>Nature Excursions</h4>
              <p>Visits to places such as Cubbon Park and Lalbagh to appreciate natural surroundings together.</p>
            </div>

            <div className="activity-card card">
              <div className="activity-icon" style={{ color: '#7c3aed', background: '#ede9fe' }}><Building2 size={20} /></div>
              <h4>Cultural Visits</h4>
              <p>Occasional visits to temples and other destinations, combining shared experiences with stronger relationships.</p>
            </div>

            <div className="activity-card card">
              <div className="activity-icon" style={{ color: '#db2777', background: '#fce7f3' }}><HeartHandshake size={20} /></div>
              <h4>Community Connections</h4>
              <p>Informal opportunities for neighbours to meet, exchange ideas, and participate in community life.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Vision */}
      <section className="section about-vision-section">
        <div className="container">
          <div className="about-vision-card card">
            <div className="about-vision-icon"><Leaf size={28} /></div>
            <h2>Our Vision</h2>
            <p>
              We imagine PPH Green as a place where sustainability is a shared habit and neighbours know each other by name. Through small, regular steps — a Sunday walk, a newly planted sapling, a conversation on the way to Lalbagh — we build a more connected, responsible, and joyful residential community together.
            </p>
            <p className="text-sm text-muted" style={{ marginTop: 'var(--space-4)' }}>
              No single person has to do everything. When many of us do a little, our community becomes greener and warmer for everyone.
            </p>
          </div>
        </div>
      </section>

      {/* Get involved */}
      <section className="section about-cta-section">
        <div className="container">
          <div className="about-cta-card">
            <h2>Get Involved</h2>
            <p>Explore what’s happening, revisit shared memories, and join the next community activity.</p>
            <div className="about-cta-actions">
              <Link to="/events" className="btn btn-primary">View Events <ArrowRight size={16} /></Link>
              <Link to="/gallery" className="btn btn-secondary">Community Gallery</Link>
            </div>
          </div>
        </div>
      </section>

      {/* Admin-managed entries (if any) — kept for scalability, below static content */}
      <section className="container" style={{ paddingBottom: 'var(--space-8)' }}>
        <div className="page-header flex items-center justify-between" style={{ marginTop: 'var(--space-8)' }}>
          <div>
            <h3 style={{ fontSize: 'var(--text-lg)' }}>Admin Content</h3>
            <p className="text-sm text-muted">Additional entries managed by admins</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              <Plus size={16} /> New Entry
            </button>
          )}
        </div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && aboutEntries.length === 0 && (
          <p className="text-sm text-muted" style={{ textAlign: 'center', padding: 'var(--space-6)' }}>
            No admin entries yet — the static content above is always visible.
          </p>
        )}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)', marginTop: 'var(--space-6)' }}>
          {aboutEntries.map((entry) => (
            <div key={entry.id} className="about-entry card animate-fade-in">
              <div className="card-body">
                <h3 className="about-entry-title">{entry.title}</h3>
                {entry.description && <p className="about-entry-desc">{entry.description}</p>}
                {isAdmin && (
                  <div className="about-actions">
                    <button className="btn btn-ghost btn-sm" onClick={() => setEditing(entry)}><Pencil size={14} /> Edit</button>
                    <button className="btn btn-ghost btn-sm" style={{ color: 'var(--error)' }} onClick={async () => { if (window.confirm('Delete this entry?')) { await aboutApi.delete(entry.id); refetch(); } }}><Trash2 size={14} /> Delete</button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </section>

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
    <Modal isOpen onClose={onClose} title={entry ? 'Edit About' : 'New About Entry'} footer={<><button className="btn btn-secondary" onClick={onClose}>Cancel</button><button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>{loading ? 'Saving...' : 'Save'}</button></>}>
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
