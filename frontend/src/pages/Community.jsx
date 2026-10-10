/* eslint-disable */
import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { communityApi } from '../api/client';
import { Image as ImageIcon, MessageCircle, Search, X, Users, Sprout } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { messageApi } from '../api/client';
import Modal from '../components/ui/Modal';
import './Community.css';

function useDebounced(value, delay = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return debounced;
}

export default function Community() {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const feedRef = useRef(null);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const [showComposer, setShowComposer] = useState(false);
  const [mobileMembersOpen, setMobileMembersOpen] = useState(false);
  const [selectedPost, setSelectedPost] = useState(null);

  const fetchPosts = useCallback(async (cur, isInitial = false) => {
    try {
      const res = await communityApi.getPosts(cur, 10);
      const data = res.data || [];
      if (isInitial) {
        setPosts(data);
        setHasMore(data.length === 10);
        if (data.length) setCursor(data[0].id);
        else setHasMore(false);
      } else {
        if (data.length === 0) {
          setHasMore(false);
          return;
        }
        const container = feedRef.current;
        const prevHeight = container ? container.scrollHeight : 0;
        const prevTop = container ? container.scrollTop : 0;
        setPosts((prev) => {
          const merged = [...data, ...prev];
          const map = new Map();
          merged.forEach((p) => map.set(p.id, p));
          const deduped = Array.from(map.values()).sort((a, b) => {
            if (a.createdAt !== b.createdAt) return new Date(a.createdAt) - new Date(b.createdAt);
            return a.id - b.id;
          });
          return deduped;
        });
        setCursor(data[0].id);
        setHasMore(data.length === 10);
        requestAnimationFrame(() => {
          if (container) {
            const newHeight = container.scrollHeight;
            container.scrollTop = prevTop + (newHeight - prevHeight);
          }
        });
      }
    } catch (err) {
      setError(err.message || 'Failed to load posts');
    }
  }, []);

  useEffect(() => {
    setLoading(true);
    fetchPosts(null, true).finally(() => setLoading(false));
  }, [fetchPosts]);

  useEffect(() => {
    if (posts.length && feedRef.current && isAtBottom) {
      feedRef.current.scrollTop = feedRef.current.scrollHeight;
    }
  }, [posts.length]);

  const handleScroll = () => {
    const el = feedRef.current;
    if (!el) return;
    const atBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 50;
    setIsAtBottom(atBottom);
    if (el.scrollTop < 100 && hasMore && !loadingMore) {
      setLoadingMore(true);
      fetchPosts(cursor, false).finally(() => setLoadingMore(false));
    }
  };

  const handleNewPost = (newPost) => {
    setPosts((prev) => [...prev, newPost]);
    setTimeout(() => {
      if (feedRef.current) feedRef.current.scrollTop = feedRef.current.scrollHeight;
    }, 100);
  };

  const handleCommentAdded = (postId) => {
    setPosts((prev) => prev.map((p) => p.id === postId ? { ...p, commentCount: (p.commentCount || 0) + 1 } : p));
  };

  const startConversation = async (memberId) => {
    try {
      await messageApi.createConversation(memberId);
      navigate('/messages');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="community-fullscreen">
      <div className="community-workspace">
        <div className="community-feed-column">
          <div className="community-feed" ref={feedRef} onScroll={handleScroll}>
            {loading && <div className="loading-container"><div className="loading-spinner" /><p className="loading-text">Loading posts...</p></div>}
            {error && <div className="alert alert-error">{error} <button className="btn btn-ghost btn-sm" onClick={() => fetchPosts(null, true)}>Retry</button></div>}

            {loadingMore && <div className="text-sm text-muted" style={{ textAlign: 'center', padding: 'var(--space-2)' }}>Loading older posts...</div>}
            {!hasMore && posts.length > 0 && <div className="text-xs text-muted" style={{ textAlign: 'center', padding: 'var(--space-2)' }}>Beginning of community history</div>}

            {!loading && posts.length === 0 && !error && (
              <div className="empty-state">
                <Sprout size={48} strokeWidth={1.5} />
                <h4>No posts yet</h4>
                <p>Be the first to share an update with the community.</p>
              </div>
            )}

            <div className="posts-list">
              {posts.map((post) => (
                <PostCard key={post.id} post={post} onOpenComments={setSelectedPost} />
              ))}
            </div>

            {!isAtBottom && (
              <button className="btn btn-secondary btn-sm" style={{ position: 'sticky', bottom: 10, margin: 'var(--space-4) auto', display: 'block' }} onClick={() => { if (feedRef.current) feedRef.current.scrollTop = feedRef.current.scrollHeight; setIsAtBottom(true); }}>
                New posts below ↓
              </button>
            )}
          </div>

          {isAuthenticated && (
            <button className="fab" onClick={() => setShowComposer(true)} aria-label="Create post">
              <X size={24} style={{ transform: 'rotate(45deg)' }} />
              <span style={{ position: 'absolute', width: 1, height: 1, overflow: 'hidden', clip: 'rect(0,0,0,0)' }}>Create post</span>
              <span aria-hidden="true" style={{ fontSize: 28, lineHeight: 1, marginTop: -2 }}>+</span>
            </button>
          )}

          <button className="btn btn-ghost members-toggle-mobile" onClick={() => setMobileMembersOpen(!mobileMembersOpen)}>
            <Users size={16} /> Members
          </button>
        </div>

        <div className={`community-sidebar ${mobileMembersOpen ? 'open' : ''}`}>
          <MemberDirectory onMessage={startConversation} currentUserId={user?.id} onClose={() => setMobileMembersOpen(false)} />
        </div>
      </div>

      {showComposer && (
        <CreatePostModal onClose={() => setShowComposer(false)} onCreated={(p) => { setShowComposer(false); handleNewPost(p); }} />
      )}
      {selectedPost && (
        <PostCommentsDialog post={selectedPost} onClose={() => setSelectedPost(null)} onCommentAdded={() => handleCommentAdded(selectedPost.id)} />
      )}
    </div>
  );
}

function CreatePostModal({ onClose, onCreated }) {
  const [content, setContent] = useState('');
  const [files, setFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleFiles = (e) => {
    const list = Array.from(e.target.files || []);
    if (list.length + files.length > 6) {
      setError('Maximum 6 images per post');
      return;
    }
    const newFiles = [...files, ...list].slice(0, 6);
    setFiles(newFiles);
    const newPreviews = newFiles.map((f) => URL.createObjectURL(f));
    previews.forEach((url) => URL.revokeObjectURL(url));
    setPreviews(newPreviews);
    setError('');
  };

  const removeAt = (idx) => {
    const nf = files.filter((_, i) => i !== idx);
    const np = previews.filter((_, i) => i !== idx);
    URL.revokeObjectURL(previews[idx]);
    setFiles(nf);
    setPreviews(np);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) { setError('Post cannot be empty'); return; }
    setSubmitting(true);
    setError('');
    try {
      const res = await communityApi.createPost(content.trim(), files);
      onCreated(res.data);
      previews.forEach((url) => URL.revokeObjectURL(url));
    } catch (err) {
      setError(err.message || 'Failed to create post');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen onClose={onClose} title="Create post">
      <form onSubmit={handleSubmit}>
        <textarea className="input" placeholder="Share an update, ask a question, or celebrate a moment..." value={content} onChange={(e) => setContent(e.target.value)} rows={4} maxLength={5000} style={{ resize: 'vertical' }} autoFocus />
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 'var(--space-2)' }}>
          <label className="btn btn-ghost btn-sm" style={{ cursor: 'pointer' }}>
            <ImageIcon size={16} /> Add images ({files.length}/6)
            <input type="file" accept="image/*" multiple onChange={handleFiles} style={{ display: 'none' }} />
          </label>
          <span className="text-xs text-muted">{content.length}/5000</span>
        </div>
        {previews.length > 0 && (
          <div className="composer-previews">
            {previews.map((url, i) => (
              <div key={i} className="preview-item">
                <img src={url} alt="preview" />
                <button type="button" className="preview-remove" onClick={() => removeAt(i)}><X size={12} /></button>
              </div>
            ))}
          </div>
        )}
        {error && <div className="alert alert-error" style={{ marginTop: 'var(--space-2)', fontSize: 'var(--text-sm)' }}>{error}</div>}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-2)', marginTop: 'var(--space-4)' }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={submitting || !content.trim()}>
            {submitting ? 'Posting...' : 'Post'}
          </button>
        </div>
      </form>
    </Modal>
  );
}

function PostCard({ post, onOpenComments }) {
  const author = post.author;
  const [imgFailed, setImgFailed] = useState({});
  const handleImgError = (id) => setImgFailed((prev) => ({ ...prev, [id]: true }));

  return (
    <div className="post-card card">
      <div className="card-body">
        <div className="post-header">
          <div className="avatar">
            {author.profilePicture ? <img src={author.profilePicture} alt="" onError={(e) => { e.currentTarget.style.display = 'none'; e.currentTarget.nextSibling.style.display = 'flex'; }} /> : null}
            <span style={{ display: author.profilePicture ? 'none' : 'flex' }}>{(author.name || '?')[0].toUpperCase()}</span>
            {author.profilePicture && <span style={{ display: 'none' }}>{(author.name || '?')[0].toUpperCase()}</span>}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="font-semibold text-sm truncate">{author.name || 'Resident'}</div>
            <div className="text-xs text-muted">{new Date(post.createdAt).toLocaleString()}</div>
          </div>
        </div>

        <div className="post-content">{post.content}</div>

        {post.images && post.images.length > 0 && (
          <div className={`post-images grid-${Math.min(post.images.length, 4)}`}>
            {post.images.map((img) => (
              <a key={img.id} href={img.fileUrl} target="_blank" rel="noopener noreferrer" className="post-image-item">
                {imgFailed[img.id] ? (
                  <div className="post-image-fallback"><ImageIcon size={24} /></div>
                ) : (
                  <img src={img.fileUrl} alt="" loading="lazy" onError={() => handleImgError(img.id)} />
                )}
              </a>
            ))}
          </div>
        )}

        <div className="post-footer">
          <button className="btn btn-ghost btn-sm" onClick={() => onOpenComments(post)}>
            <MessageCircle size={16} /> {post.commentCount} Comments
          </button>
        </div>
      </div>
    </div>
  );
}

function PostCommentsDialog({ post, onClose, onCommentAdded }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [text, setText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { isAuthenticated, user } = useAuth();
  const listRef = useRef(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    setComments([]);
    communityApi.getComments(post.id)
      .then((res) => { if (!cancelled) setComments(res.data || []); })
      .catch((err) => { if (!cancelled) setError(err.message || 'Failed to load comments'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [post.id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!text.trim() || submitting) return;
    setSubmitting(true);
    setError('');
    try {
      const res = await communityApi.addComment(post.id, text.trim());
      setComments((prev) => [...prev, res.data]);
      setText('');
      onCommentAdded();
      setTimeout(() => { if (listRef.current) listRef.current.scrollTop = listRef.current.scrollHeight; }, 50);
    } catch (err) {
      setError(err.message || 'Failed to post comment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true" aria-label={`Comments for ${post.author.name}`}>
      <div className="modal modal-comments" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3 style={{ margin: 0 }}>{post.author.name || 'Resident'}</h3>
            <p className="text-xs text-muted" style={{ marginTop: 4, whiteSpace: 'pre-wrap', maxWidth: 400 }}>{post.content.slice(0, 120)}{post.content.length > 120 ? '…' : ''}</p>
          </div>
          <button className="btn-icon" onClick={onClose} aria-label="Close"><X size={18} /></button>
        </div>
        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', minHeight: 0, padding: 0 }}>
          <div ref={listRef} style={{ flex: 1, overflowY: 'auto', padding: 'var(--space-4)', minHeight: 200 }}>
            {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
            {error && <div className="alert alert-error text-sm" style={{ marginBottom: 'var(--space-2)' }}>{error} <button className="btn btn-ghost btn-sm" onClick={() => window.location.reload()}>Retry</button></div>}
            {!loading && comments.length === 0 && !error && <div className="empty-state" style={{ padding: 'var(--space-6)' }}><MessageCircle size={32} /><p className="text-sm text-muted">No comments yet. Be the first to reply.</p></div>}
            {comments.map((c) => (
              <div key={c.id} className="comment-item" style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
                <div className="avatar small" style={{ flexShrink: 0 }}>
                  {c.author.profilePicture ? <img src={c.author.profilePicture} alt="" onError={(e) => { e.currentTarget.style.display = 'none'; }} /> : <span>{(c.author.name || '?')[0].toUpperCase()}</span>}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <span className="font-semibold text-sm">{c.author.name || 'Resident'}</span>
                    <span className="text-xs text-muted">{new Date(c.createdAt).toLocaleString()}</span>
                  </div>
                  <div className="text-sm" style={{ marginTop: 2, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{c.content}</div>
                </div>
              </div>
            ))}
          </div>
          <form onSubmit={handleSubmit} className="comment-composer">
            {isAuthenticated ? (
              <>
                <input className="input" placeholder="Write a comment..." value={text} onChange={(e) => setText(e.target.value)} maxLength={2000} disabled={submitting} />
                <button className="btn btn-primary" type="submit" disabled={submitting || !text.trim()}>{submitting ? 'Posting...' : 'Reply'}</button>
              </>
            ) : (
              <p className="text-sm text-muted" style={{ width: '100%', textAlign: 'center' }}>Please log in to comment</p>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}

function MemberDirectory({ onMessage, currentUserId, onClose }) {
  const [q, setQ] = useState('');
  const debounced = useDebounced(q, 400);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [failedImgs, setFailedImgs] = useState({});

  useEffect(() => {
    setLoading(true);
    setError('');
    communityApi.searchMembers(debounced, null, 20)
      .then((res) => setMembers(res.data || []))
      .catch((err) => setError(err.message || 'Failed to load members'))
      .finally(() => setLoading(false));
  }, [debounced]);

  const handleImgError = (id) => setFailedImgs((prev) => ({ ...prev, [id]: true }));

  return (
    <div className="member-directory">
      <div className="member-directory-header">
        <h3 className="flex items-center gap-2"><Users size={18} /> Members</h3>
        {onClose && <button className="btn btn-ghost btn-sm hidden-desktop" onClick={onClose}><X size={16} /></button>}
      </div>
      <div className="member-search">
        <Search size={16} className="member-search-icon" />
        <input className="input member-search-input" placeholder="Search by name or flat..." value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      <div className="member-list">
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {error && <div className="alert alert-error text-sm">{error}</div>}
        {!loading && members.length === 0 && !error && <div className="empty-state"><p className="text-sm text-muted">No members found</p></div>}

        {members.filter((m) => m.id !== currentUserId).map((m) => (
          <div key={m.id} className="member-row">
            <div className="avatar">
              {!failedImgs[m.id] && m.profilePicture ? (
                <img src={m.profilePicture} alt="" onError={() => handleImgError(m.id)} />
              ) : (
                <span>{(m.name || '?')[0].toUpperCase()}</span>
              )}
            </div>
            <div className="member-info">
              <div className="font-semibold text-sm truncate">{m.name || 'Unnamed'}</div>
              <div className="text-xs text-muted truncate">{[m.tower, m.apartmentNumber].filter(Boolean).join(' - ') || 'Resident'}</div>
            </div>
            <button className="btn-icon" onClick={() => onMessage(m.id)} aria-label={`Message ${m.name}`} title="Message">
              <MessageCircle size={16} />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
