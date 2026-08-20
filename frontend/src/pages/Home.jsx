import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useApi } from '../hooks/useApi';
import { eventApi, galleryApi, programApi, aboutApi } from '../api/client';
import { Calendar, ArrowRight, Sprout, Leaf, TreePine, Users } from 'lucide-react';
import './Home.css';

export default function Home() {
  const { isAuthenticated } = useAuth();
  const { data: upcomingData } = useApi(() => eventApi.getUpcoming(), []);
  const { data: programsData } = useApi(() => programApi.getAll(), []);
  const { data: galleryData } = useApi(() => galleryApi.getAll(), []);
  const { data: aboutData } = useApi(() => aboutApi.getAll(), []);

  const upcoming = (upcomingData?.data || []).slice(0, 3);
  const programs = (programsData?.data || []).slice(0, 3);
  const galleryItems = (galleryData?.data || []).slice(0, 6);
  const about = (aboutData?.data || [])[0];

  return (
    <div className="home">
      <section className="hero-section">
        <div className="container">
          <div className="hero-content animate-fade-in-up">
            <div className="hero-badge">
              <Sprout size={14} />
              <span>PPH Green Community</span>
            </div>
            <h1 className="hero-title">
              Together for a<br />
              <span className="hero-title-accent">greener tomorrow</span>
            </h1>
            <p className="hero-description">
              Join our community of residents committed to sustainable living,
              meaningful connections, and a shared vision for a better neighborhood.
            </p>
            <div className="hero-actions">
              {isAuthenticated ? (
                <>
                  <Link to="/events" className="btn btn-primary btn-lg">
                    View Events
                    <ArrowRight size={18} />
                  </Link>
                  <Link to="/gallery" className="btn btn-secondary btn-lg">
                    Gallery
                  </Link>
                </>
              ) : (
                <>
                  <Link to="/register" className="btn btn-primary btn-lg">
                    Join the Community
                    <ArrowRight size={18} />
                  </Link>
                  <Link to="/about" className="btn btn-secondary btn-lg">
                    Learn More
                  </Link>
                </>
              )}
            </div>
          </div>
          <div className="hero-visual">
            <div className="hero-visual-grid">
              {galleryItems.slice(0, 4).map((item, i) => (
                <div key={item.id} className={`hero-visual-item hero-visual-item-${i + 1}`}>
                  {item.mediaType?.startsWith('image/') ? (
                    <img src={item.fileUrl} alt={item.fileName} loading="lazy" />
                  ) : (
                    <div className="hero-visual-placeholder">
                      <Sprout size={24} />
                    </div>
                  )}
                </div>
              ))}
              {galleryItems.length === 0 && (
                <>
                  <div className="hero-visual-item hero-visual-item-1 hero-visual-placeholder"><Leaf size={32} /></div>
                  <div className="hero-visual-item hero-visual-item-2 hero-visual-placeholder"><TreePine size={32} /></div>
                  <div className="hero-visual-item hero-visual-item-3 hero-visual-placeholder"><Users size={32} /></div>
                  <div className="hero-visual-item hero-visual-item-4 hero-visual-placeholder"><Sprout size={32} /></div>
                </>
              )}
            </div>
          </div>
        </div>
      </section>

      {about && (
        <section className="section intro-section">
          <div className="container">
            <div className="intro-grid">
              <div className="intro-content animate-fade-in">
                <span className="section-label">About Us</span>
                <h2>{about.title}</h2>
                {about.description && <p className="intro-text">{about.description}</p>}
                {about.ideals && (
                  <div className="intro-ideals">
                    {about.ideals.split('\n').filter(Boolean).map((ideal, i) => (
                      <div key={i} className="intro-ideal">
                        <div className="intro-ideal-dot" />
                        <span>{ideal.trim()}</span>
                      </div>
                    ))}
                  </div>
                )}
                <Link to="/about" className="btn btn-ghost" style={{ marginTop: 'var(--space-4)' }}>
                  Read more <ArrowRight size={16} />
                </Link>
              </div>
              {about.image && (
                <div className="intro-image">
                  <img src={about.image} alt={about.title} loading="lazy" />
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {isAuthenticated && upcoming.length > 0 && (
        <section className="section events-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">Happening Soon</span>
                <h2>Upcoming Events</h2>
              </div>
              <Link to="/events" className="btn btn-ghost">
                All events <ArrowRight size={16} />
              </Link>
            </div>
            <div className="events-grid">
              {upcoming.map((event) => (
                <Link to={`/events/${event.id}`} key={event.id} className="event-card card">
                  {event.image && (
                    <div className="event-card-image">
                      <img src={event.image} alt={event.title} loading="lazy" />
                    </div>
                  )}
                  <div className="card-body">
                    <div className="event-card-date">
                      <Calendar size={14} />
                      <span>{formatEventDate(event.date, event.time)}</span>
                    </div>
                    <h3 className="event-card-title">{event.title}</h3>
                    {event.venue && (
                      <p className="event-card-venue">{event.venue}</p>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      {programs.length > 0 && (
        <section className="section programs-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">What We Do</span>
                <h2>Our Programs</h2>
              </div>
              <Link to="/programs" className="btn btn-ghost">
                All programs <ArrowRight size={16} />
              </Link>
            </div>
            <div className="programs-grid">
              {programs.map((program) => (
                <div key={program.id} className="program-card card">
                  {program.image && (
                    <div className="program-card-image">
                      <img src={program.image} alt={program.title} loading="lazy" />
                    </div>
                  )}
                  <div className="card-body">
                    <h3 className="program-card-title">{program.title}</h3>
                    {program.description && (
                      <p className="program-card-desc">{program.description}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {galleryItems.length > 0 && (
        <section className="section gallery-preview-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">Our Community</span>
                <h2>Gallery</h2>
              </div>
              <Link to="/gallery" className="btn btn-ghost">
                View gallery <ArrowRight size={16} />
              </Link>
            </div>
            <div className="gallery-preview-grid">
              {galleryItems.map((item) => (
                <div key={item.id} className="gallery-preview-item">
                  {item.mediaType?.startsWith('image/') ? (
                    <img src={item.fileUrl} alt={item.fileName} loading="lazy" />
                  ) : (
                    <div className="gallery-preview-placeholder">
                      <Sprout size={20} />
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      <section className="section cta-section">
        <div className="container">
          <div className="cta-card">
            <div className="cta-content">
              <h2>Be part of something meaningful</h2>
              <p>
                Join our growing community and help shape a greener, more connected future
                for PPH Green.
              </p>
              <div className="cta-actions">
                {isAuthenticated ? (
                  <Link to="/events" className="btn btn-primary btn-lg">
                    Explore Events <ArrowRight size={18} />
                  </Link>
                ) : (
                  <Link to="/register" className="btn btn-primary btn-lg">
                    Join Now <ArrowRight size={18} />
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

function formatEventDate(date, time) {
  if (!date) return '';
  try {
    const d = new Date(date + 'T00:00:00');
    const formatted = d.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    });
    if (time) {
      const [h, m] = time.split(':');
      const hour = parseInt(h, 10);
      const ampm = hour >= 12 ? 'PM' : 'AM';
      const hour12 = hour % 12 || 12;
      return `${formatted} at ${hour12}:${m} ${ampm}`;
    }
    return formatted;
  } catch {
    return date;
  }
}
