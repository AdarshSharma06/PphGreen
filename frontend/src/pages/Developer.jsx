import { useApi } from '../hooks/useApi';
import { developerApi } from '../api/client';
import { Code, GraduationCap, Layers, MapPin, ExternalLink } from 'lucide-react';
import EmptyState from '../components/ui/EmptyState';
import './Developer.css';

const PORTRAIT_FALLBACK = '/assets/developer/developer_1.jpg';

const PROFILE_FACTS = [
  { icon: GraduationCap, label: 'Education', value: 'B.Tech CSE (Core), 3rd Year — VIT Chennai' },
  { icon: Layers, label: 'Built with', value: 'Spring Boot & React' },
  { icon: MapPin, label: 'Home', value: 'Apartment 2138, PPH Green' },
];

export default function Developer() {
  const { data, loading, error } = useApi(() => developerApi.getAll(), []);

  const developers = data?.data || [];
  const [lead, ...others] = developers;

  return (
    <div className="developer-page">
      <div className="container">
        <div className="page-header">
          <h1>Developer</h1>
          <p>The person behind the PPH Green platform</p>
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && developers.length === 0 && (
          <EmptyState icon={Code} title="Developer info coming soon" description="Details about the development team will appear here." />
        )}

        {lead && (
          <article className="developer-profile card animate-fade-in">
            <figure className="developer-portrait">
              <img
                src={lead.image || PORTRAIT_FALLBACK}
                alt={`Portrait of ${lead.name}`}
              />
            </figure>
            <div className="developer-details">
              <p className="developer-role">{lead.role}</p>
              <h2 className="developer-name">{lead.name}</h2>
              {lead.bio && <p className="developer-bio">{lead.bio}</p>}

              <dl className="developer-facts">
                {PROFILE_FACTS.map(({ icon: Icon, label, value }) => (
                  <div key={label} className="developer-fact">
                    <span className="developer-fact-icon" aria-hidden="true">
                      <Icon size={16} />
                    </span>
                    <div>
                      <dt>{label}</dt>
                      <dd>{value}</dd>
                    </div>
                  </div>
                ))}
                <div className="developer-fact">
                  <span className="developer-fact-icon" aria-hidden="true">
                    <ExternalLink size={16} />
                  </span>
                  <div>
                    <dt>GitHub</dt>
                    <dd>
                      <a
                        href="https://github.com/AdarshSharma06"
                        target="_blank"
                        rel="noreferrer"
                      >
                        github.com/AdarshSharma06
                      </a>
                    </dd>
                  </div>
                </div>
              </dl>

              {lead.createdBy?.name && (
                <p className="text-xs text-muted">Added by {lead.createdBy.name}</p>
              )}
            </div>
          </article>
        )}

        {others.length > 0 && (
          <div className="developers-grid">
            {others.map((dev) => (
              <div key={dev.id} className="developer-card card">
                <div className="card-body">
                  <div className="developer-avatar">
                    {dev.image ? (
                      <img src={dev.image} alt={`Portrait of ${dev.name}`} />
                    ) : (
                      <span aria-hidden="true">{dev.name[0].toUpperCase()}</span>
                    )}
                  </div>
                  <h3 className="developer-mini-name">{dev.name}</h3>
                  <p className="developer-role">{dev.role}</p>
                  {dev.bio && <p className="developer-bio">{dev.bio}</p>}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
