/* eslint-disable */
import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { messageApi, communityApi } from '../api/client';
import { Search, ArrowLeft, MessageCircle } from 'lucide-react';
import './Messages.css';

function useDebounced(value, delay = 300) {
  const [d, setD] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setD(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return d;
}

export default function Messages() {
  const { user } = useAuth();
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounced(search, 300);
  const [showNew, setShowNew] = useState(false);
  const isFetchingRef = useRef(false);

  const fetchConversations = useCallback(async (isPoll = false) => {
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
    if (!isPoll) setLoading(true);
    try {
      const res = await messageApi.getConversations();
      setConversations(res.data || []);
      setError('');
    } catch (err) {
      if (!isPoll) setError(err.message || 'Failed to load conversations');
      console.warn('Failed to refresh conversations', err);
    } finally {
      if (!isPoll) setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    fetchConversations(false);
  }, [fetchConversations]);

  useEffect(() => {
    const id = setInterval(() => fetchConversations(true), 5000);
    return () => clearInterval(id);
  }, [fetchConversations]);

  const handleNewConversation = useCallback((conv) => {
    setShowNew(false);
    fetchConversations(false);
    setSelectedId(conv.id);
  }, [fetchConversations]);

  const handleRead = useCallback(() => {
    fetchConversations(true);
  }, [fetchConversations]);

  const filtered = conversations.filter((c) => {
    if (!debouncedSearch) return true;
    return c.otherParticipant.name?.toLowerCase().includes(debouncedSearch.toLowerCase());
  });

  const selected = conversations.find((c) => c.id === selectedId) || null;

  return (
    <div className="messages-fullscreen">
      <div className="messages-workspace">
        <div className={`conversations-sidebar ${selectedId ? 'hidden-mobile' : ''}`}>
          <div className="conversations-header">
            <div className="search-box">
              <Search size={16} className="search-icon" />
              <input className="input search-input" placeholder="Search conversations..." value={search} onChange={(e) => setSearch(e.target.value)} />
            </div>
            <button className="btn btn-primary btn-sm" onClick={() => setShowNew(true)}>New Message</button>
          </div>
          {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
          {error && <div className="alert alert-error text-sm">{error}</div>}
          <div className="conversation-list">
            {filtered.length === 0 && !loading && <div className="empty-state" style={{ padding: 'var(--space-8)' }}><MessageCircle size={32} /><p className="text-sm text-muted">No conversations yet. Start a new one from the Community member directory.</p></div>}
            {filtered.map((conv) => (
              <button key={conv.id} className={`conversation-item ${selectedId === conv.id ? 'active' : ''} ${conv.unreadCount > 0 ? 'unread' : ''}`} onClick={() => setSelectedId(conv.id)}>
                <div className="avatar">
                  {conv.otherParticipant.profilePicture ? <img src={conv.otherParticipant.profilePicture} alt="" /> : <span>{(conv.otherParticipant.name || '?')[0].toUpperCase()}</span>}
                </div>
                <div className="conversation-info">
                  <div className="conversation-name">{conv.otherParticipant.name}</div>
                  <div className="conversation-preview truncate">{conv.lastMessagePreview || 'No messages yet'}</div>
                </div>
                <div className="conversation-meta">
                  <span className="text-xs text-muted">{conv.lastMessageAt ? new Date(conv.lastMessageAt).toLocaleDateString() : ''}</span>
                  {conv.unreadCount > 0 && <span className="unread-badge">{conv.unreadCount}</span>}
                </div>
              </button>
            ))}
          </div>
        </div>
        <div className={`conversation-panel ${!selectedId ? 'hidden-mobile' : ''}`}>
          {selected ? (
            <ConversationView conversation={selected} onBack={() => setSelectedId(null)} onRead={handleRead} currentUserId={user?.id} />
          ) : (
            <div className="empty-conversation">
              <MessageCircle size={48} strokeWidth={1.2} />
              <h3>Select a conversation</h3>
              <p className="text-sm text-muted">Choose a conversation from the list or start a new one.</p>
            </div>
          )}
        </div>
      </div>
      {showNew ? <NewConversationModal onClose={() => setShowNew(false)} onCreated={handleNewConversation} /> : null}
    </div>
  );
}

function ConversationView({ conversation, onBack, onRead, currentUserId }) {
  const [messages, setMessages] = useState([]);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [text, setText] = useState('');
  const [fetchError, setFetchError] = useState('');
  const [sendError, setSendError] = useState('');
  const listRef = useRef(null);
  const [loadingMore, setLoadingMore] = useState(false);

  const fetchMessages = useCallback(async (cur, isInitial = false) => {
    try {
      if (isInitial) setFetchError('');
      const res = await messageApi.getMessages(conversation.id, cur, 30);
      const data = res.data || [];
      if (isInitial) {
        setMessages(data);
        setHasMore(data.length === 30);
        if (data.length) setCursor(data[0].id);
      } else {
        if (data.length === 0) { setHasMore(false); return; }
        const container = listRef.current;
        const prevHeight = container ? container.scrollHeight : 0;
        setMessages((prev) => {
          const merged = [...data, ...prev];
          const map = new Map();
          merged.forEach((m) => map.set(m.id, m));
          return Array.from(map.values()).sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
        });
        setCursor(data[0].id);
        setHasMore(data.length === 30);
        requestAnimationFrame(() => {
          if (container) container.scrollTop = container.scrollHeight - prevHeight;
        });
      }
    } catch (err) {
      setFetchError(err.message || 'Failed to load messages');
    }
  }, [conversation.id]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setFetchError('');
    setMessages([]);
    setCursor(null);
    setHasMore(true);
    fetchMessages(null, true).finally(() => { if (!cancelled) setLoading(false); });
    messageApi.markRead(conversation.id).catch(() => {}).then(() => { if (!cancelled) onRead(); });
    return () => { cancelled = true; };
  }, [conversation.id, fetchMessages, onRead]);

  const isPollingRef = useRef(false);
  useEffect(() => {
    const id = setInterval(async () => {
      if (isPollingRef.current) return;
      isPollingRef.current = true;
      try {
        const res = await messageApi.getMessages(conversation.id, null, 30);
        const data = res.data || [];
        setMessages((prev) => {
          const existingIds = new Set(prev.map((m) => m.id));
          const newOnes = data.filter((m) => !existingIds.has(m.id));
          if (newOnes.length === 0) return prev;
          const merged = [...prev, ...newOnes].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
          const container = listRef.current;
          const atBottom = container && container.scrollTop + container.clientHeight >= container.scrollHeight - 50;
          setTimeout(() => {
            if (container && atBottom) container.scrollTop = container.scrollHeight;
          }, 50);
          return merged;
        });
        messageApi.markRead(conversation.id).catch(() => {});
      } catch {
        // polling failure is silent
      } finally {
        isPollingRef.current = false;
      }
    }, 4000);
    return () => clearInterval(id);
  }, [conversation.id]);

  useEffect(() => {
    if (listRef.current && messages.length && !loading) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages.length, loading]);

  const handleScroll = () => {
    const el = listRef.current;
    if (!el) return;
    if (el.scrollTop < 50 && hasMore && !loadingMore) {
      setLoadingMore(true);
      fetchMessages(cursor, false).finally(() => setLoadingMore(false));
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!text.trim() || sending) return;
    if (text.length > 2000) { setSendError('Message too long'); return; }
    setSending(true);
    setSendError('');
    const content = text.trim();
    setText('');
    try {
      const res = await messageApi.sendMessage(conversation.id, content);
      setMessages((prev) => [...prev, res.data]);
      setTimeout(() => { if (listRef.current) listRef.current.scrollTop = listRef.current.scrollHeight; }, 50);
    } catch (err) {
      setSendError(err.message || 'Failed to send');
      setText(content);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="conversation-view">
      <div className="conversation-header">
        <button className="btn btn-ghost btn-sm hidden-desktop" onClick={onBack}><ArrowLeft size={16} /> Back</button>
        <div className="avatar small">
          {conversation.otherParticipant.profilePicture ? <img src={conversation.otherParticipant.profilePicture} alt="" /> : <span>{(conversation.otherParticipant.name || '?')[0]}</span>}
        </div>
        <span className="font-semibold text-sm">{conversation.otherParticipant.name}</span>
      </div>
      <div className="messages-list" ref={listRef} onScroll={handleScroll}>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {fetchError && (
          <div className="alert alert-error text-sm" style={{ margin: 'var(--space-2)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>{fetchError}</span>
            <button className="btn btn-ghost btn-sm" onClick={() => fetchMessages(null, true)}>Retry</button>
          </div>
        )}
        {!loading && !fetchError && messages.length === 0 && (
          <div className="text-sm text-muted" style={{ textAlign: 'center', padding: 'var(--space-4)' }}>No messages yet. Say hello!</div>
        )}
        {loadingMore && <div className="text-xs text-muted" style={{ textAlign: 'center' }}>Loading older messages...</div>}
        {messages.map((m) => {
          const isOwn = m.senderId === currentUserId;
          return (
            <div key={m.id} className={`message-bubble ${isOwn ? 'own' : 'other'}`}>
              <div className="message-content">{m.content}</div>
              <div className="message-time">{m.createdAt ? new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''} {m.readAt ? '· Read' : ''}</div>
            </div>
          );
        })}
      </div>
      {sendError && <div className="alert alert-error text-sm" style={{ margin: 'var(--space-2)' }}>{sendError}</div>}
      <form className="composer-bar" onSubmit={handleSend}>
        <input className="input" placeholder="Type a message..." value={text} onChange={(e) => setText(e.target.value)} maxLength={2000} />
        <button className="btn btn-primary btn-sm" disabled={sending || !text.trim()}>{sending ? 'Sending...' : 'Send'}</button>
      </form>
    </div>
  );
}

function NewConversationModal({ onClose, onCreated }) {
  const [q, setQ] = useState('');
  const debounced = useDebounced(q, 300);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!debounced.trim()) { setResults([]); return; }
    setLoading(true);
    communityApi.searchMembers(debounced, null, 10).then((res) => setResults(res.data || [])).catch(() => {}).finally(() => setLoading(false));
  }, [debounced]);

  const handleSelect = async (member) => {
    try {
      const res = await messageApi.createConversation(member.id);
      onCreated(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 400 }}>
        <div className="modal-header"><h3>New Conversation</h3><button className="btn-icon" onClick={onClose}>×</button></div>
        <div className="modal-body">
          <input className="input" placeholder="Search members..." value={q} onChange={(e) => setQ(e.target.value)} autoFocus />
          {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
          <div className="member-list" style={{ marginTop: 'var(--space-3)', maxHeight: 300, overflowY: 'auto' }}>
            {results.map((m) => (
              <button key={m.id} className="member-row" onClick={() => handleSelect(m)} style={{ width: '100%', border: 'none', background: 'none', cursor: 'pointer' }}>
                <div className="avatar small">{m.profilePicture ? <img src={m.profilePicture} alt="" /> : <span>{(m.name || '?')[0]}</span>}</div>
                <div className="member-info" style={{ textAlign: 'left' }}>
                  <div className="font-semibold text-sm">{m.name}</div>
                  <div className="text-xs text-muted">{[m.tower, m.apartmentNumber].filter(Boolean).join(' - ')}</div>
                </div>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
