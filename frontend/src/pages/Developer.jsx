import { useApi } from '../hooks/useApi';
import { developerApi } from '../api/client';
import { Users, AlertCircle } from 'lucide-react';
import EmptyState from '../components/ui/EmptyState';
import './Developer.css';

export default function Developer() {
  const { data, loading, error } = useApi(() => developerApi.getAll(), []);

  const payload = data?.data;
  const developers = Array.isArray(payload) ? payload : [];
  const requestFailed = Boolean(error) || (data && data.success === false);

  return (
    <div className="developer-page">
      <div className="container">
        <div className="page-header">
          <h1>Developer</h1>
          <p>The people behind the PPH Green platform</p>
        </div>

        {loading && (
          <div className="loading-container">
            <div className="loading-spinner" />
          </div>
        )}

        {!loading && requestFailed && (
          <div className="developer-state" role="alert">
            <span className="developer-state-icon" aria-hidden="true">
              <AlertCircle size={22} />
            </span>
            <h2>Couldn't load developer information</h2>
            <p>{error || 'Something went wrong while contacting the server.'}</p>
            <p className="developer-state-hint">
              Please check your connection and try again later.
            </p>
          </div>
        )}

        {!loading && !requestFailed && developers.length === 0 && (
          <EmptyState
            icon={Users}
            title="No developers listed yet"
            description="Profiles will appear here once added."
          />
        )}

        {!loading && !requestFailed && developers.length === 1 && (
          <DeveloperProfile developer={developers[0]} featured />
        )}

        {!loading && !requestFailed && developers.length > 1 && (
          <>
            <DeveloperProfile developer={developers[0]} featured />
            <div className="developers-grid">
              {developers.slice(1).map((dev) => (
                <DeveloperProfile key={dev.id} developer={dev} />
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function DeveloperProfile({ developer, featured = false }) {
  if (!developer) return null;

  const name = developer.name || 'Unnamed member';
  const initials = name.trim().charAt(0).toUpperCase();

  return (
    <article className={`developer-profile card${featured ? ' developer-profile-featured' : ''}`}>
      <figure className="developer-portrait">
        {developer.image ? (
          <img src={developer.image} alt={`Portrait of ${name}`} loading="lazy" />
        ) : (
          <div className="developer-portrait-monogram" aria-hidden="true">
            {initials}
          </div>
        )}
      </figure>
      <div className="developer-details">
        {developer.role && <p className="developer-role">{developer.role}</p>}
        <h2 className="developer-name">{name}</h2>
        {developer.bio && <p className="developer-bio">{developer.bio}</p>}
      </div>
    </article>
  );
}
