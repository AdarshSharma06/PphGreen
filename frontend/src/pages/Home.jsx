import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useApi } from '../hooks/useApi';
import { eventApi, galleryApi, programApi, aboutApi } from '../api/client';
import { Calendar, ArrowRight, Sprout } from 'lucide-react';
import './Home.css';

const HERO_IMAGE = '/assets/apartments/Apartment_1.jpg';
const COMMUNITY_IMAGE = '/assets/apartments/Apartment_2.jpg';
const INTRO_IMAGE_FALLBACK = '/assets/aesthetic/aesthetic.jpg';
const PROGRAM_IMAGE_FALLBACKS = ['/assets/programs/program_1.jpg', '/assets/programs/program_2.jpg'];

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

  const [featuredProgram, ...supportingPrograms] = programs;
  const heroSnapshot = galleryItems.find((item) => item.mediaType?.startsWith('image/'));

  return (
    <div className="home">
      <section className="hero-section">
        <div className="container">
          <div className="hero-content animate-fade-in-up">
            <div className="hero-badge">
              <img src="/assets/branding/pph_green_logo.jpeg" alt="" className="hero-badge-logo" />
              <span>PPH Green Residential Community</span>
            </div>
            <h1 className="hero-title">
              Together for a<br />
              <span className="hero-title-accent">greener tomorrow</span>
            </h1>
            <p className="hero-description">
              We are the residents of PPH Green — neighbours turning our apartments into a
              greener, friendlier place to call home through shared programs, events,
              and everyday sustainable living.
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
          <div className="hero-visual animate-fade-in">
            <figure className="hero-photo">
              <img
                src={HERO_IMAGE}
                alt="Residential apartment buildings surrounded by greenery at PPH Green"
                loading="eager"
                fetchpriority="high"
              />
              <figcaption className="hero-photo-chip">PPH Green Residences</figcaption>
            </figure>
            {heroSnapshot && (
              <figure className="hero-snapshot">
                <img
                  src={heroSnapshot.fileUrl}
                  alt={`Community moment at PPH Green — ${heroSnapshot.fileName}`}
                  loading="lazy"
                />
                <figcaption>From our residents</figcaption>
              </figure>
            )}
            <div className="hero-accent" aria-hidden="true" />
          </div>
        </div>
      </section>

      {about && (
        <section className="section intro-section">
          <div className="container">
            <div className="intro-grid">
              <div className="intro-content animate-fade-in">
                <span className="section-label">Who we are</span>
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
              <div className="intro-image">
                <img
                  src={about.image || INTRO_IMAGE_FALLBACK}
                  alt={about.image ? about.title : 'A quiet green corner of the PPH Green neighbourhood'}
                  loading="lazy"
                />
              </div>
            </div>
          </div>
        </section>
      )}

      {featuredProgram && (
        <section className="section programs-story-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">What we do</span>
                <h2>Programs that bring our community together</h2>
              </div>
              <Link to="/programs" className="btn btn-ghost">
                All programs <ArrowRight size={16} />
              </Link>
            </div>
            <div className="programs-feature card">
              <div className="programs-feature-image">
                <img
                  src={featuredProgram.image || PROGRAM_IMAGE_FALLBACKS[0]}
                  alt={
                    featuredProgram.image
                      ? featuredProgram.title
                      : `Residents taking part in ${featuredProgram.title}`
                  }
                  loading="lazy"
                />
              </div>
              <div className="programs-feature-body">
                <span className="programs-feature-tag">Featured program</span>
                <h3>{featuredProgram.title}</h3>
                {featuredProgram.description && (
                  <p className="programs-feature-desc">{featuredProgram.description}</p>
                )}
                {featuredProgram.createdBy?.name && (
                  <p className="text-xs text-muted">Started by {featuredProgram.createdBy.name}</p>
                )}
              </div>
            </div>
            {supportingPrograms.length > 0 && (
              <div className="programs-support-list">
                {supportingPrograms.map((program, i) => (
                  <div key={program.id} className="programs-support-item card">
                    <div className="programs-support-thumb">
                      <img
                        src={program.image || PROGRAM_IMAGE_FALLBACKS[i % PROGRAM_IMAGE_FALLBACKS.length]}
                        alt={
                          program.image
                            ? program.title
                            : `Residents taking part in ${program.title}`
                        }
                        loading="lazy"
                      />
                    </div>
                    <div className="programs-support-body">
                      <h4>{program.title}</h4>
                      {program.description && (
                        <p className="programs-support-desc">{program.description}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      <section className="community-band">
        <div className="container">
          <div className="community-band-grid">
            <figure className="community-band-image">
              <img
                src={COMMUNITY_IMAGE}
                alt="Homes of the PPH Green community in the evening light"
                loading="lazy"
              />
            </figure>
            <div className="community-band-panel">
              <span className="section-label section-label-light">Life at PPH Green</span>
              <h2>More than apartments — a neighbourhood</h2>
              <p>
                Shared gardens, weekend clean-ups, festival evenings in the courtyard.
                PPH Green is a place where neighbours become friends and small everyday
                choices add up to a greener way of living together.
              </p>
              <Link to="/programs" className="btn community-band-btn">
                See how we live <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {isAuthenticated && upcoming.length > 0 && (
        <section className="section events-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">Happening soon</span>
                <h2>Upcoming events</h2>
              </div>
              <Link to="/events" className="btn btn-ghost">
                All events <ArrowRight size={16} />
              </Link>
            </div>
            <div className="events-grid">
              {upcoming.map((event) => (
                <Link to={`/events/${event.id}`} key={event.id} className="event-card card">
                  {event.image ? (
                    <div className="event-card-image">
                      <img src={event.image} alt="" loading="lazy" />
                    </div>
                  ) : (
                    <div className="event-card-banner">
                      <Sprout size={22} aria-hidden="true" />
                      <span>{event.title}</span>
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

      {galleryItems.length > 0 && (
        <section className="section gallery-preview-section">
          <div className="container">
            <div className="section-header flex items-center justify-between">
              <div>
                <span className="section-label">Community moments</span>
                <h2>Moments from our neighbourhood</h2>
              </div>
              <Link to="/gallery" className="btn btn-ghost">
                View gallery <ArrowRight size={16} />
              </Link>
            </div>
            <div className="gallery-preview-grid">
              {galleryItems.map((item) => (
                <div key={item.id} className="gallery-preview-item">
                  {item.mediaType?.startsWith('image/') ? (
                    <img src={item.fileUrl} alt={`Shared by residents — ${item.fileName}`} loading="lazy" />
                  ) : (
                    <div className="gallery-preview-placeholder">
                      <Sprout size={20} aria-hidden="true" />
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
                Join your neighbours and help shape a greener, more connected future
                for PPH Green — right where you live.
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
