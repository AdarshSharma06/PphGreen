import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/client';
import { Save } from 'lucide-react';
import './Profile.css';

export default function Profile() {
  const { user, refreshUser } = useAuth();
  const [form, setForm] = useState({});
  const [touched, setTouched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const getFormValue = (field) => {
    if (touched && form[field] !== undefined) return form[field];
    return user?.[field] || '';
  };

  const handleChange = (field, value) => {
    setTouched(true);
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);
    try {
      const payload = {};
      const name = getFormValue('name');
      const tower = getFormValue('tower');
      const apartmentNumber = getFormValue('apartmentNumber');
      const profilePicture = getFormValue('profilePicture');
      if (name) payload.name = name;
      if (tower) payload.tower = tower;
      if (apartmentNumber) payload.apartmentNumber = apartmentNumber;
      if (profilePicture) payload.profilePicture = profilePicture;
      await userApi.updateMe(payload);
      await refreshUser();
      setTouched(false);
      setForm({});
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="profile-page">
      <div className="container">
        <div className="page-header">
          <h1>Profile</h1>
          <p>Manage your account information</p>
        </div>

        <div className="profile-card card">
          <div className="card-body">
            <div className="profile-header">
              <div className="profile-avatar-large">
                {getFormValue('profilePicture') ? (
                  <img src={getFormValue('profilePicture')} alt="" />
                ) : (
                  <span>{(user?.name || user?.email || '?')[0].toUpperCase()}</span>
                )}
              </div>
              <div>
                <h3>{user?.name || 'Your Name'}</h3>
                <p className="text-sm text-muted">{user?.email}</p>
                <span className="badge badge-green" style={{ marginTop: 'var(--space-2)' }}>
                  {user?.role || 'MEMBER'}
                </span>
              </div>
            </div>

            <div className="divider" />

            {success && <div className="alert alert-success">Profile updated successfully.</div>}
            {error && <div className="alert alert-error">{error}</div>}

            <form onSubmit={handleSubmit} className="profile-form">
              <div className="input-group">
                <label className="input-label" htmlFor="name">Name</label>
                <input id="name" className="input" value={getFormValue('name')} onChange={(e) => handleChange('name', e.target.value)} placeholder="Your name" />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
                <div className="input-group">
                  <label className="input-label" htmlFor="tower">Tower</label>
                  <input id="tower" className="input" value={getFormValue('tower')} onChange={(e) => handleChange('tower', e.target.value)} placeholder="e.g. Tower A" />
                </div>
                <div className="input-group">
                  <label className="input-label" htmlFor="apartment">Apartment</label>
                  <input id="apartment" className="input" value={getFormValue('apartmentNumber')} onChange={(e) => handleChange('apartmentNumber', e.target.value)} placeholder="e.g. 301" />
                </div>
              </div>

              <div className="input-group">
                <label className="input-label" htmlFor="picture">Profile Picture URL</label>
                <input id="picture" className="input" value={getFormValue('profilePicture')} onChange={(e) => handleChange('profilePicture', e.target.value)} placeholder="https://..." />
              </div>

              <button type="submit" className="btn btn-primary" disabled={loading}>
                <Save size={16} /> {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
