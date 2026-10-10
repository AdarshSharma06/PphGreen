import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { galleryApi } from '../api/client';
import { Upload, X, Play, Image as ImageIcon, Calendar, Images, Heart } from 'lucide-react';
import Modal from '../components/ui/Modal';
import EmptyState from '../components/ui/EmptyState';
import './Gallery.css';

function formatDate(date) {
  if (!date) return '';
  try { return new Date(date + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }); } catch { return date; }
}

export default function Gallery() {
  const { isAdmin } = useAuth();
  const { data: albumsData, loading: albumsLoading, error: albumsError, refetch: refetchAlbums } = useApi(() => galleryApi.getAlbums(), []);
  const { data: standaloneData, loading: standaloneLoading, error: standaloneError, refetch: refetchStandalone } = useApi(() => galleryApi.getStandalone(), []);
  const [lightbox, setLightbox] = useState(null);
  const [showUpload, setShowUpload] = useState(false);
  const [mediaErrors, setMediaErrors] = useState({});

  const albums = albumsData?.data || [];
  const standalone = standaloneData?.data || [];
  const loading = albumsLoading || standaloneLoading;
  const error = albumsError || standaloneError;

  const handleMediaError = (id) => setMediaErrors((p) => ({ ...p, [id]: true }));
  const refetchAll = () => { refetchAlbums(); refetchStandalone(); };

  return (
    <div className="gallery-page">
      <div className="container">
        <div className="page-header flex items-center justify-between">
          <div>
            <h1>Gallery</h1>
            <p>Moments from our community</p>
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setShowUpload(true)}>
              <Upload size={16} /> Add Media
            </button>
          )}
        </div>

        {loading && (
          <div className="loading-container">
            <div className="loading-spinner" />
            <p className="loading-text">Loading gallery...</p>
          </div>
        )}

        {error && (
          <div className="alert alert-error" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
            <span>{error}</span>
            <button className="btn btn-secondary btn-sm" onClick={refetchAll}>Retry</button>
          </div>
        )}

        {!loading && !error && (
          <>
            {/* Event Albums */}
            <section className="gallery-albums-section">
              <div className="flex items-center justify-between" style={{ marginBottom: 'var(--space-6)' }}>
                <h2 style={{ fontSize: 'var(--text-xl)' }}>Event Albums</h2>
                <span className="text-sm text-muted">{albums.length} events</span>
              </div>

              {albums.length === 0 ? (
                <EmptyState icon={ImageIcon} title="No events yet" description="Event albums will appear here once events are created." />
              ) : (
                <div className="album-grid">
                  {albums.map((album) => (
                    <Link key={album.eventId} to={`/gallery/event/${album.eventId}`} className="album-card card">
                      <div className="album-thumb">
                        {album.thumbnailUrl ? (
                          <img src={album.thumbnailUrl} alt={album.title} loading="lazy" onError={(e) => { e.currentTarget.style.display = 'none'; e.currentTarget.nextSibling.style.display = 'flex'; }} />
                        ) : null}
                        <div className="album-thumb-placeholder" style={{ display: album.thumbnailUrl ? 'none' : 'flex' }}>
                          <Images size={32} strokeWidth={1.5} />
                          <span>0 photos</span>
                        </div>
                        <div className="album-count-badge">
                          <Images size={12} /> {album.totalMediaCount}
                          {album.totalLikes > 0 && <><Heart size={12} fill="currentColor" /> {album.totalLikes}</>}
                        </div>
                      </div>
                      <div className="card-body album-body">
                        <h3 className="album-title">{album.title}</h3>
                        <div className="album-meta">
                          <span className="flex items-center gap-1 text-xs text-muted"><Calendar size={12} />{formatDate(album.date)}</span>
                          <span className="text-xs text-muted">{album.photoCount} photos{album.videoCount ? ` · ${album.videoCount} videos` : ''}</span>
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </section>

            {/* Community Gallery - standalone */}
            <section className="gallery-standalone-section" style={{ marginTop: 'var(--space-12)' }}>
              <div className="flex items-center justify-between" style={{ marginBottom: 'var(--space-6)' }}>
                <h2 style={{ fontSize: 'var(--text-xl)' }}>Community Gallery</h2>
                <span className="text-sm text-muted">{standalone.length} items</span>
              </div>
              {standalone.length === 0 ? (
                <EmptyState icon={ImageIcon} title="Community gallery is empty" description="Photos not linked to events will appear here." />
              ) : (
                <div className="gallery-masonry">
                  {standalone.map((item, index) => (
                    <button key={item.id} className={`gallery-item gallery-item-${(index % 5) + 1}`} onClick={() => setLightbox(item)} aria-label={`View ${item.fileName}`}>
                      {mediaErrors[item.id] ? (
                        <div className="gallery-item-fallback" style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 'var(--space-2)', color: 'var(--text-muted)', background: 'var(--green-50)', padding: 'var(--space-4)' }}>
                          <ImageIcon size={28} strokeWidth={1.5} />
                          <span style={{ fontSize: 'var(--text-xs)', textAlign: 'center' }}>Unable to load</span>
                        </div>
                      ) : item.mediaType?.startsWith('video/') ? (
                        <div className="gallery-item-video">
                          <video src={item.fileUrl} muted playsInline preload="metadata" onError={() => handleMediaError(item.id)} />
                          <div className="gallery-item-play"><Play size={24} fill="white" /></div>
                        </div>
                      ) : (
                        <img src={item.fileUrl} alt={item.fileName} loading="lazy" onError={() => handleMediaError(item.id)} />
                      )}
                    </button>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </div>

      {lightbox && (
        <div className="lightbox-overlay" onClick={() => setLightbox(null)}>
          <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
            <button className="lightbox-close" onClick={() => setLightbox(null)} aria-label="Close"><X size={24} /></button>
            {lightbox.mediaType?.startsWith('video/') ? (
              <video src={lightbox.fileUrl} controls autoPlay className="lightbox-media" />
            ) : (
              <img src={lightbox.fileUrl} alt={lightbox.fileName} className="lightbox-media" />
            )}
            <div className="lightbox-info"><span>{lightbox.fileName}</span>{lightbox.uploadedBy?.name && <span className="text-muted"> by {lightbox.uploadedBy.name}</span>}</div>
          </div>
        </div>
      )}

      {showUpload && (
        <GalleryUploadModal onClose={() => setShowUpload(false)} onUploaded={() => { setShowUpload(false); refetchAll(); }} />
      )}
    </div>
  );
}

function GalleryUploadModal({ onClose, onUploaded }) {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (!selected) return;
    setFile(selected);
    setError('');
    if (preview) URL.revokeObjectURL(preview);
    if (selected.type.startsWith('image/') || selected.type.startsWith('video/')) {
      setPreview(URL.createObjectURL(selected));
    } else {
      setPreview(null);
    }
  };
  const handleRemove = () => {
    if (preview) URL.revokeObjectURL(preview);
    setFile(null); setPreview(null); setError('');
  };
  const handleUpload = async () => {
    if (!file) return;
    if (file.size > 50 * 1024 * 1024) { setError('File too large. Maximum size is 50MB.'); return; }
    setUploading(true); setError('');
    try {
      await galleryApi.upload(file, null);
      if (preview) URL.revokeObjectURL(preview);
      onUploaded();
    } catch (err) { setError(err.message || 'Upload failed'); } finally { setUploading(false); }
  };
  return (
    <Modal isOpen={true} onClose={onClose} title="Add Media"
      footer={<><button className="btn btn-secondary" onClick={onClose} disabled={uploading}>Cancel</button><button className="btn btn-primary" onClick={handleUpload} disabled={!file || uploading}>{uploading ? 'Uploading...' : 'Upload'}</button></>}>
      {error && <div className="alert alert-error mb-4">{error}</div>}
      <div className="upload-area">
        {file ? (
          <div className="upload-preview">
            {preview ? (
              file?.type?.startsWith('video/') ? <video src={preview} className="upload-preview-media" /> : <img src={preview} alt="Preview" className="upload-preview-media" />
            ) : (
              <div style={{ padding: 'var(--space-6)', textAlign: 'center', color: 'var(--text-muted)' }}>
                <ImageIcon size={32} strokeWidth={1.5} style={{ margin: '0 auto' }} />
                <p className="text-sm" style={{ marginTop: 'var(--space-2)' }}>{file.name}</p>
              </div>
            )}
            <div style={{ padding: 'var(--space-2) var(--space-3)', fontSize: 'var(--text-xs)', color: 'var(--text-muted)', borderTop: '1px solid var(--border-light)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span className="truncate" style={{ maxWidth: '70%' }}>{file.name} — {(file.size / 1024 / 1024).toFixed(2)} MB</span>
              <span>{file.type}</span>
            </div>
            <button className="upload-preview-remove" onClick={handleRemove}><X size={16} /> Remove</button>
          </div>
        ) : (
          <label className="upload-dropzone">
            <Upload size={32} strokeWidth={1.5} />
            <p className="font-medium text-sm" style={{ marginTop: 'var(--space-3)' }}>Click to select a file</p>
            <p className="text-xs text-muted" style={{ marginTop: 'var(--space-1)' }}>Images and videos up to 50MB</p>
            <input type="file" accept="image/*,video/*" onChange={handleFileChange} style={{ display: 'none' }} />
          </label>
        )}
      </div>
    </Modal>
  );
}
