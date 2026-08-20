import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { notificationApi } from '../api/client';
import { Bell, Check } from 'lucide-react';
import EmptyState from '../components/ui/EmptyState';
import './Notifications.css';

const TYPE_LABELS = {
  EVENT: { label: 'Event', color: 'badge-green' },
  SYSTEM: { label: 'System', color: 'badge-gray' },
  ADMIN: { label: 'Admin', color: 'badge-yellow' },
  GITHUB_SUPPORT: { label: 'Support', color: 'badge-blue' },
};

export default function Notifications() {
  const { data, loading, refetch } = useApi(() => notificationApi.getAll(), []);
  const [marking, setMarking] = useState(null);

  const notifications = data?.data || [];

  const handleMarkRead = async (id) => {
    setMarking(id);
    try {
      await notificationApi.markRead(id);
      refetch();
    } catch { /* mark-read is best-effort */ } finally {
      setMarking(null);
    }
  };

  return (
    <div className="notifications-page">
      <div className="container">
        <div className="page-header">
          <h1>Notifications</h1>
          <p>Stay updated with your community</p>
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}

        {!loading && notifications.length === 0 && (
          <EmptyState icon={Bell} title="No notifications" description="You're all caught up." />
        )}

        {notifications.length > 0 && (
          <div className="notifications-list">
            {notifications.map((n) => {
              const typeInfo = TYPE_LABELS[n.type] || TYPE_LABELS.SYSTEM;
              return (
                <div key={n.id} className={`notification-item ${!n.read ? 'unread' : ''}`}>
                  <div className="notification-content">
                    <div className="notification-top">
                      <span className={`badge ${typeInfo.color}`}>{typeInfo.label}</span>
                      <span className="notification-time">{formatTime(n.createdAt)}</span>
                    </div>
                    <h4 className="notification-title">{n.title}</h4>
                    <p className="notification-message">{n.message}</p>
                  </div>
                  {!n.read && (
                    <button
                      className="btn btn-ghost btn-sm"
                      onClick={() => handleMarkRead(n.id)}
                      disabled={marking === n.id}
                      title="Mark as read"
                    >
                      <Check size={14} />
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}
