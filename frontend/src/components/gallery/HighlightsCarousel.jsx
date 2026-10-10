import { useEffect, useRef, useState, useCallback } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function HighlightsCarousel({ items, onSelect }) {
  const [index, setIndex] = useState(0);
  const [failed, setFailed] = useState({});
  const containerRef = useRef(null);
  const intervalRef = useRef(null);
  const pausedRef = useRef(false);

  const prefersReduced = typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const count = items.length;
  const handleMediaError = (id) => setFailed((prev) => ({ ...prev, [id]: true }));

  const next = useCallback(() => {
    setIndex((i) => (i + 1) % count);
  }, [count]);

  const prev = useCallback(() => {
    setIndex((i) => (i - 1 + count) % count);
  }, [count]);

  // Auto scroll
  useEffect(() => {
    if (prefersReduced || count <= 1) return;
    const start = () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      intervalRef.current = setInterval(() => {
        if (!pausedRef.current) next();
      }, 3500);
    };
    start();
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [next, count, prefersReduced]);

  // Pause on hover/focus
  const pause = () => { pausedRef.current = true; };
  const resume = () => { pausedRef.current = false; };

  // Reset index if count changes
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (index >= count) setIndex(0);
  }, [count, index]);

  // Touch swipe
  const touchStartX = useRef(0);
  const onTouchStart = (e) => { touchStartX.current = e.touches[0].clientX; pause(); };
  const onTouchEnd = (e) => {
    const dx = e.changedTouches[0].clientX - touchStartX.current;
    if (Math.abs(dx) > 40) {
      if (dx < 0) next(); else prev();
    }
    resume();
  };

  if (count === 0) {
    return <div className="carousel-empty">No highlights yet. Admin can mark photos as highlights.</div>;
  }
  if (count === 1) {
    const item = items[0];
    const isFailed = failed[item.id];
    return (
      <div className="carousel-single">
        {isFailed ? (
          <div className="carousel-fallback">
            <span>Image unavailable</span>
          </div>
        ) : item.mediaType?.startsWith('video/') ? (
          <video src={item.fileUrl} controls className="carousel-media" onError={() => handleMediaError(item.id)} />
        ) : (
          <img src={item.fileUrl} alt={item.fileName} className="carousel-media" loading="eager" onError={() => handleMediaError(item.id)} />
        )}
      </div>
    );
  }

  return (
    <div
      className="highlights-carousel"
      ref={containerRef}
      onMouseEnter={pause}
      onMouseLeave={resume}
      onFocus={pause}
      onBlur={resume}
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
      tabIndex={0}
      aria-roledescription="carousel"
      aria-label="Highlights"
    >
      <div className="carousel-track" style={{ transform: `translateX(-${index * 100}%)` }}>
        {items.map((item) => (
          <button
            key={item.id}
            className="carousel-slide"
            onClick={() => onSelect && onSelect(item)}
            aria-label={`View ${item.fileName}`}
            style={{ cursor: onSelect ? 'pointer' : 'default', border: 'none', padding: 0 }}
          >
            {failed[item.id] ? (
              <div className="carousel-fallback">
                <span>Image unavailable</span>
                <span className="text-xs">{item.fileName}</span>
              </div>
            ) : item.mediaType?.startsWith('video/') ? (
              <video
                src={item.fileUrl}
                muted
                playsInline
                controls={false}
                className="carousel-media"
                preload="metadata"
                onError={() => handleMediaError(item.id)}
                onClick={(e) => { e.stopPropagation(); onSelect && onSelect(item); }}
              />
            ) : (
              <img src={item.fileUrl} alt={item.fileName} className="carousel-media" loading="lazy" onError={() => handleMediaError(item.id)} />
            )}
            <div className="carousel-caption">{item.fileName}</div>
          </button>
        ))}
      </div>

      <button className="carousel-btn prev" onClick={prev} aria-label="Previous highlight"><ChevronLeft size={20} /></button>
      <button className="carousel-btn next" onClick={next} aria-label="Next highlight"><ChevronRight size={20} /></button>

      <div className="carousel-dots" role="tablist">
        {items.map((_, i) => (
          <button key={i} className={`carousel-dot ${i === index ? 'active' : ''}`} onClick={() => setIndex(i)} aria-label={`Go to slide ${i + 1}`} />
        ))}
      </div>
    </div>
  );
}
