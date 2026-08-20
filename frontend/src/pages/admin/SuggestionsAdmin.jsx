import { useApi } from '../../hooks/useApi';
import { useNavigate } from 'react-router-dom';
import { suggestionApi } from '../../api/client';
import { ArrowLeft, MessageSquare } from 'lucide-react';
import EmptyState from '../../components/ui/EmptyState';
import './Admin.css';

export default function SuggestionsAdmin() {
  const { data, loading } = useApi(() => suggestionApi.getAll(), []);
  const navigate = useNavigate();
  const suggestions = data?.data || [];

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="page-header"><h1>Suggestions</h1><p>Review community suggestions</p></div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {!loading && suggestions.length === 0 && (
          <EmptyState icon={MessageSquare} title="No suggestions" />
        )}
        {suggestions.length > 0 && (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead><tr><th>Content</th><th>Submitted By</th><th>Date</th></tr></thead>
              <tbody>
                {suggestions.map((s) => (
                  <tr key={s.id}>
                    <td style={{ maxWidth: 400 }}>{s.content}</td>
                    <td className="text-muted">{s.submittedBy?.name || 'Anonymous'}</td>
                    <td className="text-muted">{new Date(s.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
