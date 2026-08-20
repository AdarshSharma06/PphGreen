import { useState } from 'react';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import { galleryApi } from '../api/client';
import { Upload, X, Play, Image as ImageIcon } from 'lucide-react';
import Modal from '../components/ui/Modal';
import EmptyState from '../components/ui/EmptyState';
import './Gallery.css';

export default function Gallery() {
  const { isAdmin } = useAuth();
  const { data, loading, error, refetch } = useApi(() => galleryApi.getAll(), []);
  const [lightbox, setLightbox] = useState(null);
  const [showUpload, setShowUpload] = useState(false);

  const items = data?.data || [];

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
              <Upload size={16} /> Upload
            </button>
          )}
        </div>

        {loading && (
          <div className="loading-container">
            <div className="loading-spinner" />
            <p className="loading-text">Loading gallery...</p>
          </div>
        )}

        {error && <div className="alert alert-error">{error}</div>}

        {!loading && items.length === 0 && (
          <EmptyState
            icon={ImageIcon}
            title="Gallery is empty"
            description="Photos and videos from the community will appear here."
          />
        )}

        {items.length > 0 && (
          <div className="gallery-masonry">
            {items.map((item, index) => (
              <button
                key={item.id}
                className={`gallery-item gallery-item-${(index % 5) + 1}`}
                onClick={() => setLightbox(item)}
                aria-label={`View ${item.fileName}`}
              >
                {item.mediaType?.startsWith('video/') ? (
                  <div className="gallery-item-video">
                    <video src={item.fileUrl} muted playsInline preload="metadata" />
                    <div className="gallery-item-play">
                      <Play size={24} fill="white" />
                    </div>
                  </div>
                ) : (
                  <img src={item.fileUrl} alt={item.fileName} loading="lazy" />
                )}
              </button>
            ))}
          </div>
        )}
      </div>

      {lightbox && (
        <div className="lightbox-overlay" onClick={() => setLightbox(null)}>
          <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
            <button className="lightbox-close" onClick={() => setLightbox(null)} aria-label="Close">
              <X size={24} />
            </button>
            {lightbox.mediaType?.startsWith('video/') ? (
              <video src={lightbox.fileUrl} controls autoPlay className="lightbox-media" />
            ) : (
              <img src={lightbox.fileUrl} alt={lightbox.fileName} className="lightbox-media" />
            )}
            <div className="lightbox-info">
              <span>{lightbox.fileName}</span>
              {lightbox.uploadedBy?.name && (
                <span className="text-muted"> by {lightbox.uploadedBy.name}</span>
              )}
            </div>
          </div>
        </div>
      )}

      {showUpload && (
        <GalleryUploadModal onClose={() => setShowUpload(false)} onUploaded={() => { setShowUpload(false); refetch(); }} />
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
    if (selected.type.startsWith('image/') || selected.type.startsWith('video/')) {
      setPreview(URL.createObjectURL(selected));
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    if (file.size > 50 * 1024 * 1024) {
      setError('File too large. Maximum size is 50MB.');
      return;
    }
    setUploading(true);
    setError('');
    try {
      await galleryApi.upload(file);
      onUploaded();
    } catch (err) {
      setError(err.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Upload to Gallery"
      footer={
        <>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleUpload} disabled={!file || uploading}>
            {uploading ? 'Uploading...' : 'Upload'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error mb-4">{error}</div>}
      <div className="upload-area">
        {preview ? (
          <div className="upload-preview">
            {file?.type?.startsWith('video/') ? (
              <video src={preview} className="upload-preview-media" />
            ) : (
              <img src={preview} alt="Preview" className="upload-preview-media" />
            )}
            <button className="upload-preview-remove" onClick={() => { setFile(null); setPreview(null); }}>
              <X size={16} /> Remove
            </button>
          </div>
        ) : (
          <label className="upload-dropzone">
            <Upload size={32} strokeWidth={1.5} />
            <p className="font-medium text-sm" style={{ marginTop: 'var(--space-3)' }}>Click to select a file</p>
            <p className="text-xs text-muted" style={{ marginTop: 'var(--space-1)' }}>
              Images and videos up to 50MB
            </p>
            <input
              type="file"
              accept="image/*,video/*"
              onChange={handleFileChange}
              style={{ display: 'none' }}
            />
          </label>
        )}
      </div>
    </Modal>
  );
}
