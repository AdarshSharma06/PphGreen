import { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Navbar from './Navbar';
import Footer from './Footer';

export default function Shell() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  const isFullScreen = pathname.startsWith('/community') || pathname.startsWith('/messages');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100dvh', height: isFullScreen ? '100dvh' : 'auto' }}>
      <Navbar />
      <main
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          minHeight: 0,
          minWidth: 0,
          height: isFullScreen ? 'calc(100dvh - var(--nav-height))' : 'auto',
          overflow: isFullScreen ? 'hidden' : 'visible',
        }}
      >
        <Outlet />
      </main>
      {!isFullScreen && <Footer />}
    </div>
  );
}
