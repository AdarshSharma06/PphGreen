import { Link } from 'react-router-dom';
import { Sprout } from 'lucide-react';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <Link to="/" className="footer-logo">
              <Sprout size={20} strokeWidth={2.5} color="var(--green-600)" />
              <span>PPH Green</span>
            </Link>
            <p className="footer-tagline">
              Building a sustainable, connected community for greener living.
            </p>
          </div>

          <div className="footer-col">
            <h4 className="footer-col-title">Community</h4>
            <Link to="/events" className="footer-link">Events</Link>
            <Link to="/gallery" className="footer-link">Gallery</Link>
            <Link to="/programs" className="footer-link">Programs</Link>
          </div>

          <div className="footer-col">
            <h4 className="footer-col-title">Organization</h4>
            <Link to="/about" className="footer-link">About Us</Link>
            <Link to="/developer" className="footer-link">Developer</Link>
          </div>

          <div className="footer-col">
            <h4 className="footer-col-title">Account</h4>
            <Link to="/login" className="footer-link">Sign In</Link>
            <Link to="/register" className="footer-link">Join Community</Link>
          </div>
        </div>

        <div className="footer-bottom">
          <p className="footer-copyright">
            &copy; {new Date().getFullYear()} PPH Green. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}
