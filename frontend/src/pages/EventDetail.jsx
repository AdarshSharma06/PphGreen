import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { eventApi, commentApi, reactionApi } from '../api/client';
import { Calendar, Clock, MapPin, ArrowLeft, Send, Trash2, Heart, ThumbsUp, Laugh, PartyPopper } from 'lucide-react';
import './EventDetail.css';

const REACTION_TYPES = [
  { type: 'LIKE', icon: ThumbsUp, label: 'Like' },
  { type: 'LOVE', icon: Heart, label: 'Love' },
  { type: 'LAUGH', icon: Laugh, label: 'Laugh' },
  { type: 'CELEBRATE', icon: PartyPopper, label: 'Celebrate' },
];

export default function EventDetail() {
  const { id } = useParams();
  const { user, isAdmin } = useAuth();
  const navigate = useNavigate();
  const { data, loading, error } = useApi(() => eventApi.getById(id), [id]);
  const { data: commentsData, refetch: refetchComments } = useApi(() => commentApi.getByEvent(id), [id]);
  const { data: reactionsData, refetch: refetchReactions } = useApi(() => reactionApi.getByEvent(id), [id]);
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [commentError, setCommentError] = useState('');
  const [submittingReaction, setSubmittingReaction] = useState(false);

  const event = data?.data;
  const comments = commentsData?.data || [];
  const reactions = reactionsData?.data || [];

  const myReactions = reactions.filter((r) => r.user?.id === user?.id);
  const myReactionTypes = new Set(myReactions.map((r) => r.reactionType));
  const reactionCounts = {};
  reactions.forEach((r) => {
    reactionCounts[r.reactionType] = (reactionCounts[r.reactionType] || 0) + 1;
  });

  const handleComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setSubmittingComment(true);
    setCommentError('');
    try {
      await commentApi.create(id, newComment.trim());
      setNewComment('');
      refetchComments();
    } catch (err) {
      setCommentError(err.message || 'Failed to post comment');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Delete this comment?')) return;
    try {
      await commentApi.delete(commentId);
      refetchComments();
    } catch { /* best-effort */ }
  };

  const handleReaction = async (reactionType) => {
    if (submittingReaction) return;
    setSubmittingReaction(true);
    try {
      if (myReactionTypes.has(reactionType)) {
        await reactionApi.remove(id);
      } else {
        await reactionApi.add(id, reactionType);
      }
      refetchReactions();
    } catch { /* reaction conflicts are expected (409) */ } finally {
      setSubmittingReaction(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this event? This cannot be undone.')) return;
    try {
      await eventApi.delete(id);
      navigate('/events');
    } catch { /* delete may fail due to FK constraints */ }
  };

  if (loading) {
    return <div className="loading-container"><div className="loading-spinner" /><p className="loading-text">Loading event...</p></div>;
  }

  if (error) {
    return (
      <div className="container" style={{ padding: 'var(--space-16) var(--space-6)' }}>
        <div className="alert alert-error">{error}</div>
        <Link to="/events" className="btn btn-ghost" style={{ marginTop: 'var(--space-4)' }}>
          <ArrowLeft size={16} /> Back to events
        </Link>
      </div>
    );
  }

  if (!event) return null;

  return (
    <div className="event-detail-page">
      <div className="container">
        <Link to="/events" className="back-link">
          <ArrowLeft size={16} /> Back to events
        </Link>

        <article className="event-detail animate-fade-in">
          {event.image && (
            <div className="event-detail-hero">
              <img src={event.image} alt={event.title} />
            </div>
          )}

          <div className="event-detail-content">
            <div className="event-detail-meta">
              <div className="event-detail-date">
                <Calendar size={16} />
                <span>{formatDate(event.date)}</span>
              </div>
              {event.time && (
                <div className="event-detail-time">
                  <Clock size={16} />
                  <span>{formatTime(event.time)}</span>
                </div>
              )}
              {event.venue && (
                <div className="event-detail-venue">
                  <MapPin size={16} />
                  <span>{event.venue}</span>
                </div>
              )}
            </div>

            <h1 className="event-detail-title">{event.title}</h1>

            {event.createdBy && (
              <p className="event-detail-author">
                Organized by {event.createdBy.name || 'Community'}
              </p>
            )}

            {event.description && (
              <div className="event-detail-description">
                <p>{event.description}</p>
              </div>
            )}

            {isAdmin && (
              <div className="event-detail-admin">
                <button className="btn btn-danger btn-sm" onClick={handleDelete}>Delete Event</button>
              </div>
            )}
          </div>
        </article>

        <section className="event-reactions-section">
          <h3>Reactions</h3>
          <div className="reactions-bar">
            {REACTION_TYPES.map(({ type, icon: Icon, label }) => {
              const active = myReactionTypes.has(type);
              const count = reactionCounts[type] || 0;
              return (
                <button
                  key={type}
                  className={`reaction-btn ${active ? 'active' : ''}`}
                  onClick={() => handleReaction(type)}
                  disabled={submittingReaction}
                  aria-label={`${label} (${count})`}
                  title={label}
                >
                  <Icon size={18} />
                  {count > 0 && <span className="reaction-count">{count}</span>}
                </button>
              );
            })}
          </div>
        </section>

        <section className="event-comments-section">
          <h3>Comments ({comments.length})</h3>

          <form className="comment-form" onSubmit={handleComment}>
            <input
              className="input"
              placeholder="Add a comment..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              maxLength={2000}
            />
            <button type="submit" className="btn btn-primary" disabled={submittingComment || !newComment.trim()}>
              <Send size={16} />
            </button>
          </form>
          {commentError && <div className="alert alert-error mt-2" style={{ fontSize: 'var(--text-sm)' }}>{commentError}</div>}

          <div className="comments-list">
            {comments.length === 0 && (
              <p className="text-sm text-muted" style={{ padding: 'var(--space-6) 0' }}>
                No comments yet. Be the first to share your thoughts.
              </p>
            )}
            {comments.map((comment) => (
              <div key={comment.id} className="comment-item">
                <div className="comment-avatar">
                  {comment.author?.name ? comment.author.name[0].toUpperCase() : '?'}
                </div>
                <div className="comment-body">
                  <div className="comment-header">
                    <span className="comment-author">{comment.author?.name || 'Anonymous'}</span>
                    <span className="comment-time">{formatRelativeTime(comment.createdAt)}</span>
                  </div>
                  <p className="comment-text">{comment.content}</p>
                </div>
                {(user?.id === comment.author?.id || isAdmin) && (
                  <button
                    className="comment-delete"
                    onClick={() => handleDeleteComment(comment.id)}
                    aria-label="Delete comment"
                    title="Delete"
                  >
                    <Trash2 size={14} />
                  </button>
                )}
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}

function formatDate(date) {
  if (!date) return '';
  try {
    return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
      weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
    });
  } catch { return date; }
}

function formatTime(time) {
  if (!time) return '';
  const [h, m] = time.split(':');
  const hour = parseInt(h, 10);
  return `${hour % 12 || 12}:${m} ${hour >= 12 ? 'PM' : 'AM'}`;
}

function formatRelativeTime(isoString) {
  if (!isoString) return '';
  const date = new Date(isoString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}
