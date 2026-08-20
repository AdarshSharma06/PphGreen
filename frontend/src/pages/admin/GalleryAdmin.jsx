import { useApi } from '../../hooks/useApi';
import { useNavigate } from 'react-router-dom';
import { galleryApi } from '../../api/client';
import { ArrowLeft } from 'lucide-react';
import './Admin.css';

export default function GalleryAdmin() {
  const { data, loading } = useApi(() => galleryApi.getAll(), []);
  const navigate = useNavigate();
  const items = data?.data || [];

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
        <div className="page-header"><h1>Gallery</h1><p>Manage community media</p></div>
        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}
        {!loading && items.length === 0 && <p className="text-muted">No gallery items. Upload via the <button className="btn btn-ghost btn-sm" onClick={() => navigate('/gallery')}>Gallery page</button>.</p>}
        <div className="admin-gallery-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 'var(--space-4)' }}>
          {items.map((item) => (
            <div key={item.id} style={{ borderRadius: 'var(--radius-md)', overflow: 'hidden', background: 'var(--green-50)' }}>
              {item.mediaType?.startsWith('image/') ? (
                <img src={item.fileUrl} alt={item.fileName} style={{ width: '100%', height: 160, objectFit: 'cover', display: 'block' }} loading="lazy" />
              ) : (
                <div style={{ height: 160, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--green-300)' }}>Video</div>
              )}
              <div style={{ padding: 'var(--space-3)', fontSize: 'var(--text-xs)' }}>
                <div className="truncate">{item.fileName}</div>
                <div className="text-muted">{item.uploadedBy?.name || '—'}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
