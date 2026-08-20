import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { eventApi } from '../api/client';
import { Calendar, MapPin, Plus, Clock } from 'lucide-react';
import Modal from '../components/ui/Modal';
import './Events.css';

export default function Events() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => eventApi.getAll(), []);
  const { data: upcomingData } = useApi(() => eventApi.getUpcoming(), []);
  const [showCreate, setShowCreate] = useState(false);

  const events = data?.data || [];
  const upcoming = upcomingData?.data || [];

  return (
    <div className="events-page">
      <div className="container">
        <div className="page-header flex items-center justify-between">
          <div>
            <h1>Events</h1>
            <p>Stay connected with what's happening in our community</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={16} /> New Event
            </button>
          )}
        </div>

        {loading && (
          <div className="loading-container">
            <div className="loading-spinner" />
            <p className="loading-text">Loading events...</p>
          </div>
        )}

        {error && (
          <div className="alert alert-error">{error}</div>
        )}

        {!loading && events.length === 0 && (
          <div className="empty-state">
            <Calendar size={48} strokeWidth={1.5} />
            <h4>No events yet</h4>
            <p>Events will appear here once they are created.</p>
          </div>
        )}

        {upcoming.length > 0 && (
          <section className="events-section-block">
            <h2 className="events-section-title">Upcoming</h2>
            <div className="events-list">
              {upcoming.map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>
          </section>
        )}

        {events.length > 0 && (
          <section className="events-section-block">
            <h2 className="events-section-title">All Events</h2>
            <div className="events-list">
              {events.map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>
          </section>
        )}
      </div>

      {showCreate && (
        <EventCreateModal onClose={() => setShowCreate(false)} onCreated={() => { setShowCreate(false); refetch(); }} />
      )}
    </div>
  );
}

function EventCard({ event }) {
  return (
    <Link to={`/events/${event.id}`} className="event-list-item card">
      {event.image && (
        <div className="event-list-image">
          <img src={event.image} alt={event.title} loading="lazy" />
        </div>
      )}
      <div className="card-body event-list-body">
        <div className="event-list-meta">
          <div className="event-list-date">
            <Calendar size={14} />
            <span>{formatEventDate(event.date)}</span>
          </div>
          {event.time && (
            <div className="event-list-time">
              <Clock size={14} />
              <span>{formatEventTime(event.time)}</span>
            </div>
          )}
        </div>
        <h3 className="event-list-title">{event.title}</h3>
        {event.description && (
          <p className="event-list-desc">{event.description}</p>
        )}
        <div className="event-list-footer">
          {event.venue && (
            <div className="event-list-venue">
              <MapPin size={14} />
              <span>{event.venue}</span>
            </div>
          )}
          {event.createdBy?.name && (
            <span className="event-list-author">by {event.createdBy.name}</span>
          )}
        </div>
      </div>
    </Link>
  );
}

function EventCreateModal({ onClose, onCreated }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    date: '',
    time: '',
    venue: '',
    image: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = {
        title: form.title,
        date: form.date,
      };
      if (form.description) payload.description = form.description;
      if (form.time) payload.time = form.time;
      if (form.venue) payload.venue = form.venue;
      if (form.image) payload.image = form.image;
      await eventApi.create(payload);
      onCreated();
    } catch (err) {
      setError(err.message || 'Failed to create event');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Create Event"
      footer={
        <>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Creating...' : 'Create Event'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error mb-4">{error}</div>}
      <form onSubmit={handleSubmit} className="form-grid">
        <div className="input-group">
          <label className="input-label">Title *</label>
          <input className="input" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div className="input-group">
          <label className="input-label">Description</label>
          <textarea className="input" rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
          <div className="input-group">
            <label className="input-label">Date *</label>
            <input className="input" type="date" required value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
          </div>
          <div className="input-group">
            <label className="input-label">Time</label>
            <input className="input" type="time" value={form.time} onChange={(e) => setForm({ ...form, time: e.target.value })} />
          </div>
        </div>
        <div className="input-group">
          <label className="input-label">Venue</label>
          <input className="input" value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} />
        </div>
        <div className="input-group">
          <label className="input-label">Image URL</label>
          <input className="input" placeholder="https://..." value={form.image} onChange={(e) => setForm({ ...form, image: e.target.value })} />
        </div>
      </form>
    </Modal>
  );
}

function formatEventDate(date) {
  if (!date) return '';
  try {
    const d = new Date(date + 'T00:00:00');
    return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
  } catch { return date; }
}

function formatEventTime(time) {
  if (!time) return '';
  const [h, m] = time.split(':');
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  return `${hour % 12 || 12}:${m} ${ampm}`;
}
