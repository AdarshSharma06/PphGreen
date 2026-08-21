import { useState, useRef, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import useClickOutside from '../../hooks/useClickOutside';
import { notificationApi } from '../../api/client';
import { Menu, X, Bell, ChevronDown, LogOut, User, Shield, Sprout } from 'lucide-react';
import './Navbar.css';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const profileRef = useRef(null);
  const notifRef = useRef(null);

  useClickOutside(profileRef, () => setProfileOpen(false));
  useClickOutside(notifRef, () => setNotifOpen(false));

  useEffect(() => {
    if (!isAuthenticated) return;
    notificationApi.getAll()
      .then((res) => {
        setNotifications(res.data || []);
        setUnreadCount((res.data || []).filter((n) => !n.read).length);
      })
      .catch(() => {});
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    setProfileOpen(false);
    navigate('/');
  };

  const markAsRead = async (id) => {
    try {
      await notificationApi.markRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch { /* notification mark-read is best-effort */ }
  };

  return (
    <nav className="navbar" role="navigation" aria-label="Main navigation">
      <div className="navbar-inner container">
        <Link to="/" className="navbar-brand">
          <img
            src="/assets/branding/pph_green_logo.jpeg"
            alt="PPH Green logo"
            className="navbar-brand-logo"
          />
          <span className="navbar-brand-text">PPH Green</span>
        </Link>

        <div className={`navbar-links ${mobileOpen ? 'open' : ''}`}>
          <NavLink to="/gallery" className="navbar-link" onClick={() => setMobileOpen(false)}>
            Gallery
          </NavLink>
          <NavLink to="/programs" className="navbar-link" onClick={() => setMobileOpen(false)}>
            Programs
          </NavLink>
          <NavLink to="/about" className="navbar-link" onClick={() => setMobileOpen(false)}>
            About
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/events" className="navbar-link" onClick={() => setMobileOpen(false)}>
              Events
            </NavLink>
          )}
          <NavLink to="/developer" className="navbar-link" onClick={() => setMobileOpen(false)}>
            Developer
          </NavLink>
        </div>

        <div className="navbar-actions">
          {isAuthenticated ? (
            <>
              <div className="dropdown" ref={notifRef}>
                <button
                  className="btn-icon navbar-icon-btn"
                  onClick={() => { setNotifOpen(!notifOpen); setProfileOpen(false); }}
                  aria-label={`Notifications${unreadCount > 0 ? ` (${unreadCount} unread)` : ''}`}
                >
                  <Bell size={20} />
                  {unreadCount > 0 && (
                    <span className="notif-badge">{unreadCount > 9 ? '9+' : unreadCount}</span>
                  )}
                </button>
                {notifOpen && (
                  <div className="dropdown-menu notif-dropdown animate-slide-down">
                    <div className="notif-dropdown-header">
                      <span className="font-semibold">Notifications</span>
                    </div>
                    {notifications.length === 0 ? (
                      <div className="notif-empty">No notifications yet</div>
                    ) : (
                      <div className="notif-list">
                        {notifications.slice(0, 8).map((n) => (
                          <button
                            key={n.id}
                            className={`notif-item ${!n.read ? 'unread' : ''}`}
                            onClick={() => { if (!n.read) markAsRead(n.id); }}
                          >
                            <div className="notif-item-title">{n.title}</div>
                            <div className="notif-item-message">{n.message}</div>
                            <div className="notif-item-time">
                              {new Date(n.createdAt).toLocaleDateString()}
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                    {notifications.length > 0 && (
                      <Link
                        to="/notifications"
                        className="notif-dropdown-footer"
                        onClick={() => setNotifOpen(false)}
                      >
                        View all notifications
                      </Link>
                    )}
                  </div>
                )}
              </div>

              <div className="dropdown" ref={profileRef}>
                <button
                  className="navbar-profile-btn"
                  onClick={() => { setProfileOpen(!profileOpen); setNotifOpen(false); }}
                  aria-label="User menu"
                >
                  <div className="navbar-avatar">
                    {user?.profilePicture ? (
                      <img src={user.profilePicture} alt="" />
                    ) : (
                      <span>{(user?.name || user?.email || '?')[0].toUpperCase()}</span>
                    )}
                  </div>
                  <ChevronDown size={14} className="navbar-chevron" />
                </button>
                {profileOpen && (
                  <div className="dropdown-menu animate-slide-down" style={{ minWidth: 220 }}>
                    <div style={{ padding: '10px 12px', borderBottom: '1px solid var(--border-light)' }}>
                      <div className="font-semibold text-sm" style={{ color: 'var(--text-primary)' }}>
                        {user?.name || 'User'}
                      </div>
                      <div className="text-xs text-muted" style={{ marginTop: 2 }}>
                        {user?.email}
                      </div>
                    </div>
                    <Link
                      to="/profile"
                      className="dropdown-item"
                      onClick={() => setProfileOpen(false)}
                    >
                      <User size={16} /> Profile
                    </Link>
                    <Link
                      to="/suggestion"
                      className="dropdown-item"
                      onClick={() => setProfileOpen(false)}
                    >
                      <Sprout size={16} /> Suggestion
                    </Link>
                    {isAdmin && (
                      <Link
                        to="/admin"
                        className="dropdown-item"
                        onClick={() => setProfileOpen(false)}
                      >
                        <Shield size={16} /> Admin
                      </Link>
                    )}
                    <div className="dropdown-divider" />
                    <button className="dropdown-item" onClick={handleLogout}>
                      <LogOut size={16} /> Sign out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="navbar-auth">
              <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Join</Link>
            </div>
          )}

          <button
            className="navbar-mobile-toggle"
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Toggle menu"
            aria-expanded={mobileOpen}
          >
            {mobileOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="navbar-mobile animate-slide-down">
          <NavLink to="/gallery" className="navbar-mobile-link" onClick={() => setMobileOpen(false)}>
            Gallery
          </NavLink>
          <NavLink to="/programs" className="navbar-mobile-link" onClick={() => setMobileOpen(false)}>
            Programs
          </NavLink>
          <NavLink to="/about" className="navbar-mobile-link" onClick={() => setMobileOpen(false)}>
            About
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/events" className="navbar-mobile-link" onClick={() => setMobileOpen(false)}>
              Events
            </NavLink>
          )}
          <NavLink to="/developer" className="navbar-mobile-link" onClick={() => setMobileOpen(false)}>
            Developer
          </NavLink>
          {!isAuthenticated && (
            <div className="navbar-mobile-auth">
              <Link to="/login" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setMobileOpen(false)}>
                Sign in
              </Link>
              <Link to="/register" className="btn btn-primary" style={{ flex: 1 }} onClick={() => setMobileOpen(false)}>
                Join
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
}
