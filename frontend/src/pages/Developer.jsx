import { useApi } from '../hooks/useApi';
import { developerApi } from '../api/client';
import { Code } from 'lucide-react';
import EmptyState from '../components/ui/EmptyState';
import './Developer.css';

export default function Developer() {
  const { data, loading, error } = useApi(() => developerApi.getAll(), []);

  const developers = data?.data || [];

  return (
    <div className="developer-page">
      <div className="container">
        <div className="page-header">
          <h1>Developer</h1>
          <p>The team behind the PPH Green platform</p>
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && developers.length === 0 && (
          <EmptyState icon={Code} title="Developer info coming soon" description="Details about the development team will appear here." />
        )}

        {developers.length > 0 && (
          <div className="developers-grid">
            {developers.map((dev) => (
              <div key={dev.id} className="developer-card card">
                <div className="card-body">
                  <div className="developer-avatar">
                    {dev.image ? (
                      <img src={dev.image} alt={dev.name} />
                    ) : (
                      <span>{dev.name[0].toUpperCase()}</span>
                    )}
                  </div>
                  <h3 className="developer-name">{dev.name}</h3>
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
