import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { suggestionApi } from '../api/client';
import { Send, MessageSquare } from 'lucide-react';
import EmptyState from '../components/ui/EmptyState';
import './Suggestion.css';

export default function Suggestion() {
  const { isAdmin } = useAuth();
  const { data } = useApi(() => isAdmin ? suggestionApi.getAll() : Promise.resolve({ data: [] }), [isAdmin]);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const suggestions = data?.data || [];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      await suggestionApi.create(content.trim());
      setContent('');
      setSuccess(true);
      setTimeout(() => setSuccess(false), 4000);
    } catch (err) {
      setError(err.message || 'Failed to submit suggestion');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="suggestion-page">
      <div className="container">
        <div className="page-header">
          <h1>Suggestion Box</h1>
          <p>Share your ideas for improving our community</p>
        </div>

        <div className="suggestion-form-card card" style={{ maxWidth: 640 }}>
          <div className="card-body">
            {success && <div className="alert alert-success mb-4">Thank you! Your suggestion has been submitted.</div>}
            {error && <div className="alert alert-error mb-4">{error}</div>}

            <form onSubmit={handleSubmit} className="suggestion-form">
              <textarea
                className="input"
                placeholder="Share your suggestion, feedback, or idea..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={5}
                maxLength={10000}
              />
              <div className="flex items-center justify-between">
                <span className="text-xs text-muted">{content.length}/10000</span>
                <button type="submit" className="btn btn-primary" disabled={submitting || !content.trim()}>
                  <Send size={16} /> {submitting ? 'Submitting...' : 'Submit'}
                </button>
              </div>
            </form>
          </div>
        </div>

        {isAdmin && (
          <div style={{ marginTop: 'var(--space-10)' }}>
            <h2 style={{ fontSize: 'var(--text-xl)', marginBottom: 'var(--space-4)' }}>All Suggestions</h2>
            {suggestions.length === 0 && (
              <EmptyState icon={MessageSquare} title="No suggestions yet" />
            )}
            <div className="suggestions-list">
              {suggestions.map((s) => (
                <div key={s.id} className="suggestion-item card">
                  <div className="card-body">
                    <p className="suggestion-content">{s.content}</p>
                    <div className="suggestion-meta">
                      <span className="text-xs text-muted">
                        {s.submittedBy?.name || 'Anonymous'} &middot; {new Date(s.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
