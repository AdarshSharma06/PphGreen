import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="loading-container" style={{ minHeight: '60vh' }}>
      <h1 style={{ fontSize: 'var(--text-5xl)', color: 'var(--text-muted)' }}>404</h1>
      <p style={{ color: 'var(--text-secondary)', marginTop: 'var(--space-2)' }}>Page not found</p>
      <Link to="/" className="btn btn-primary" style={{ marginTop: 'var(--space-6)' }}>
        <Home size={16} /> Go home
      </Link>
    </div>
  );
}
