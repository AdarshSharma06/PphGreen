import { useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { galleryApi, eventApi } from '../api/client';
import { ArrowLeft, Upload, Image as ImageIcon, Heart, MessageCircle, Star, Trash2, Send, X } from 'lucide-react';
import HighlightsCarousel from '../components/gallery/HighlightsCarousel';
import Modal from '../components/ui/Modal';
import EmptyState from '../components/ui/EmptyState';

export default function GalleryAlbum() {
  const { eventId } = useParams();
  const { isAdmin } = useAuth();
  const { data: eventData, loading: eventLoading, error: eventError } = useApi(() => eventApi.getById(eventId), [eventId]);
  const { data: mediaData, loading: mediaLoading, error: mediaError, refetch: refetchMedia } = useApi(() => galleryApi.getByEvent(eventId), [eventId]);
  const { data: highlightsData, loading: highlightsLoading, refetch: refetchHighlights } = useApi(() => galleryApi.getHighlights(eventId), [eventId]);
  const [showUpload, setShowUpload] = useState(false);
  const [lightbox, setLightbox] = useState(null);

  const event = eventData?.data;
  const media = mediaData?.data || [];
  const highlights = highlightsData?.data || [];

  const refetchAll = useCallback(() => { refetchMedia(); refetchHighlights(); }, [refetchMedia, refetchHighlights]);

  if (eventLoading) return <div className="loading-container"><div className="loading-spinner" /><p className="loading-text">Loading album...</p></div>;
  if (eventError) return <div className="container" style={{ padding: 'var(--space-8)' }}><div className="alert alert-error">{eventError}</div><Link to="/gallery" className="btn btn-ghost" style={{ marginTop: 'var(--space-4)' }}><ArrowLeft size={16} /> Back to Gallery</Link></div>;
  if (!event) return <div className="container" style={{ padding: 'var(--space-8)' }}><EmptyState icon={ImageIcon} title="Event not found" description="This album is unavailable." action={<Link to="/gallery" className="btn btn-primary">Back to Gallery</Link>} /></div>;

  return (
    <div className="gallery-album-page">
      <div className="container">
        <Link to="/gallery" className="back-link"><ArrowLeft size={16} /> Back to Gallery</Link>

        <div className="page-header" style={{ marginTop: 'var(--space-4)' }}>
          <div>
            <h1>{event.title}</h1>
            <p className="text-sm text-muted">{event.date} {event.venue ? `· ${event.venue}` : ''} · {media.length} media</p>
            {event.description && <p style={{ marginTop: 'var(--space-2)' }}>{event.description}</p>}
          </div>
          {isAdmin && <button className="btn btn-primary" onClick={() => setShowUpload(true)}><Upload size={16} /> Add Photos</button>}
        </div>

        <section className="highlights-section" style={{ marginTop: 'var(--space-8)' }}>
          <h2 style={{ fontSize: 'var(--text-lg)', marginBottom: 'var(--space-4)' }}>Highlights</h2>
          {highlightsLoading ? <div className="loading-container"><div className="loading-spinner" /></div> : <HighlightsCarousel items={highlights} />}
        </section>

        <section className="all-photos-section" style={{ marginTop: 'var(--space-10)' }}>
          <div className="flex items-center justify-between" style={{ marginBottom: 'var(--space-4)' }}>
            <h2 style={{ fontSize: 'var(--text-lg)' }}>All Photos ({media.length})</h2>
            <span className="text-xs text-muted">Sorted by likes</span>
          </div>

          {mediaLoading && <div className="loading-container"><div className="loading-spinner" /></div>}
          {mediaError && <div className="alert alert-error">{mediaError} <button className="btn btn-secondary btn-sm" onClick={refetchMedia}>Retry</button></div>}
          {!mediaLoading && media.length === 0 && <EmptyState icon={ImageIcon} title="No photos yet" description="Admin can upload photos for this event." />}
          <div className="album-media-grid">
            {media.map((item) => (
              <MediaCard key={item.id} item={item} isAdmin={isAdmin} onToggleHighlight={refetchAll} onLikeToggle={refetchAll} onOpen={setLightbox} />
            ))}
          </div>
        </section>
      </div>

      {showUpload && <AlbumUploadModal eventId={eventId} onClose={() => setShowUpload(false)} onUploaded={() => { setShowUpload(false); refetchAll(); }} />}

      {lightbox && (
        <div className="lightbox-overlay" onClick={() => setLightbox(null)}>
          <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
            <button className="lightbox-close" onClick={() => setLightbox(null)}><X size={24} /></button>
            {lightbox.mediaType?.startsWith('video/') ? <video src={lightbox.fileUrl} controls autoPlay className="lightbox-media" /> : <img src={lightbox.fileUrl} alt={lightbox.fileName} className="lightbox-media" />}
            <div className="lightbox-info"><span>{lightbox.fileName}</span> <span className="text-muted">· {lightbox.likeCount} likes</span></div>
            <PhotoComments galleryId={lightbox.id} />
          </div>
        </div>
      )}
    </div>
  );
}

function MediaCard({ item, isAdmin, onToggleHighlight, onLikeToggle, onOpen }) {
  const [likeLoading, setLikeLoading] = useState(false);
  const [liked, setLiked] = useState(item.likedByCurrentUser);
  const [count, setCount] = useState(item.likeCount);
  const [highlightLoading, setHighlightLoading] = useState(false);

  const handleLike = async (e) => {
    e.stopPropagation();
    if (likeLoading) return;
    setLikeLoading(true);
    const prevLiked = liked;
    const prevCount = count;
    // optimistic
    setLiked(!prevLiked);
    setCount(prevLiked ? prevCount - 1 : prevCount + 1);
    try {
      const res = prevLiked ? await galleryApi.unlike(item.id) : await galleryApi.like(item.id);
      const newCount = res.data ?? (!prevLiked ? prevCount + 1 : prevCount - 1);
      setCount(newCount);
      setLiked(!prevLiked);
      onLikeToggle();
    } catch (_e) {
      void _e;
      setLiked(prevLiked);
      setCount(prevCount);
    } finally { setLikeLoading(false); }
  };

  const handleHighlight = async (e) => {
    e.stopPropagation();
    setHighlightLoading(true);
    try {
      await galleryApi.toggleHighlight(item.id, !item.highlight);
      onToggleHighlight();
    } catch (_e) { void _e; } finally { setHighlightLoading(false); }
  };

  return (
    <div className="media-card card">
      <button className="media-thumb" onClick={() => onOpen(item)} aria-label={`View ${item.fileName}`}>
        {item.mediaType?.startsWith('video/') ? (
          <video src={item.fileUrl} muted preload="metadata" />
        ) : (
          <img src={item.fileUrl} alt={item.fileName} loading="lazy" />
        )}
        {item.highlight && <span className="highlight-badge"><Star size={12} fill="currentColor" /> Highlight</span>}
      </button>
      <div className="media-actions">
        <button className={`btn btn-ghost btn-sm ${liked ? 'text-primary' : ''}`} onClick={handleLike} disabled={likeLoading} aria-label="Like">
          <Heart size={16} fill={liked ? 'currentColor' : 'none'} /> {count}
        </button>
        <button className="btn btn-ghost btn-sm" onClick={() => onOpen(item)}><MessageCircle size={16} /> Comment</button>
        {isAdmin && (
          <button className="btn btn-ghost btn-sm" onClick={handleHighlight} disabled={highlightLoading} title="Toggle highlight">
            <Star size={16} fill={item.highlight ? 'currentColor' : 'none'} />
          </button>
        )}
      </div>
      <PhotoCommentsInline galleryId={item.id} />
    </div>
  );
}

function PhotoCommentsInline({ galleryId }) {
  const { data, refetch } = useApi(() => galleryApi.getComments(galleryId), [galleryId]);
  const { isAuthenticated } = useAuth();
  const [text, setText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const comments = data?.data || [];
  const preview = comments.slice(0, 2);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!text.trim() || submitting) return;
    setSubmitting(true);
    try {
      await galleryApi.addComment(galleryId, text.trim());
      setText('');
      refetch();
    } catch (_e) { void _e; } finally { setSubmitting(false); }
  };

  return (
    <div className="photo-comments">
      {preview.map((c) => (
        <div key={c.id} className="comment-row">
          <span className="comment-author-sm">{c.author?.name}</span>
          <span className="comment-text-sm">{c.content}</span>
        </div>
      ))}
      {comments.length > 2 && <span className="text-xs text-muted">+{comments.length - 2} more</span>}
      {isAuthenticated && (
        <form className="comment-inline-form" onSubmit={handleSubmit}>
          <input className="input input-sm" placeholder="Add a comment..." value={text} onChange={(e) => setText(e.target.value)} maxLength={2000} />
          <button type="submit" className="btn btn-ghost btn-sm" disabled={submitting || !text.trim()}><Send size={14} /></button>
        </form>
      )}
    </div>
  );
}

function PhotoComments({ galleryId }) {
  const { data, refetch } = useApi(() => galleryApi.getComments(galleryId), [galleryId]);
  const { isAuthenticated, user, isAdmin } = useAuth();
  const [text, setText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const comments = data?.data || [];
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!text.trim() || submitting) return;
    setSubmitting(true);
    try { await galleryApi.addComment(galleryId, text.trim()); setText(''); refetch(); } catch (_e) { void _e; } finally { setSubmitting(false); }
  };
  const handleDelete = async (id) => {
    if (!window.confirm('Delete comment?')) return;
    try { await galleryApi.deleteComment(id); refetch(); } catch (_e) { void _e; }
  };
  return (
    <div className="photo-comments-full" style={{ width: '100%', maxHeight: 200, overflowY: 'auto', marginTop: 'var(--space-4)', background: 'white', borderRadius: 'var(--radius)', padding: 'var(--space-3)' }}>
      {comments.map((c) => (
        <div key={c.id} className="comment-item" style={{ display: 'flex', gap: 'var(--space-2)', padding: 'var(--space-2) 0', borderBottom: '1px solid var(--border-light)' }}>
          <span className="font-medium text-sm">{c.author?.name}</span>
          <span className="text-sm flex-1">{c.content}</span>
          <span className="text-xs text-muted">{new Date(c.createdAt).toLocaleDateString()}</span>
          {(user?.id === c.author?.id || isAdmin) && <button onClick={() => handleDelete(c.id)}><Trash2 size={12} /></button>}
        </div>
      ))}
      {isAuthenticated ? (
        <form onSubmit={handleSubmit} className="flex gap-2" style={{ marginTop: 'var(--space-3)' }}>
          <input className="input" placeholder="Add a comment..." value={text} onChange={(e) => setText(e.target.value)} />
          <button className="btn btn-primary" disabled={submitting}><Send size={16} /></button>
        </form>
      ) : <p className="text-xs text-muted">Login to comment</p>}
    </div>
  );
}

function AlbumUploadModal({ eventId, onClose, onUploaded }) {
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const handleChange = (e) => {
    const list = Array.from(e.target.files || []);
    setFiles(list);
    setError('');
  };
  const handleUpload = async () => {
    if (!files.length) return;
    const oversized = files.find(f => f.size > 50 * 1024 * 1024);
    if (oversized) { setError('One file exceeds 50MB'); return; }
    setUploading(true); setError('');
    try {
      for (const f of files) {
        await galleryApi.upload(f, eventId);
      }
      onUploaded();
    } catch (err) { setError(err.message || 'Upload failed'); } finally { setUploading(false); }
  };
  return (
    <Modal isOpen={true} onClose={onClose} title="Add Photos to Event"
      footer={<><button className="btn btn-secondary" onClick={onClose} disabled={uploading}>Cancel</button><button className="btn btn-primary" onClick={handleUpload} disabled={!files.length || uploading}>{uploading ? 'Uploading...' : `Upload ${files.length || ''}`}</button></>}>
      {error && <div className="alert alert-error mb-4">{error}</div>}
      <label className="upload-dropzone" style={{ border: '2px dashed var(--border)', borderRadius: 'var(--radius)', padding: 'var(--space-8)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Upload size={32} />
        <p>Select photos/videos for this event</p>
        <input type="file" multiple accept="image/*,video/*" onChange={handleChange} style={{ display: 'none' }} />
        {files.length > 0 && <p className="text-sm text-muted" style={{ marginTop: 'var(--space-2)' }}>{files.length} files selected: {files.map(f=>f.name).join(', ').slice(0,60)}</p>}
      </label>
    </Modal>
  );
}
