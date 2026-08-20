import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { HelmetProvider, Helmet } from 'react-helmet-async';
import Shell from './components/layout/Shell';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Events from './pages/Events';
import EventDetail from './pages/EventDetail';
import Gallery from './pages/Gallery';
import Programs from './pages/Programs';
import About from './pages/About';
import Developer from './pages/Developer';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import Suggestion from './pages/Suggestion';
import NotFound from './pages/NotFound';
import AdminDashboard from './pages/admin/Dashboard';
import EventsAdmin from './pages/admin/EventsAdmin';
import GalleryAdmin from './pages/admin/GalleryAdmin';
import ProgramsAdmin from './pages/admin/ProgramsAdmin';
import AboutAdmin from './pages/admin/AboutAdmin';
import SuggestionsAdmin from './pages/admin/SuggestionsAdmin';
import AdminRequests from './pages/admin/AdminRequests';

export default function App() {
  return (
    <HelmetProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<><Helmet><title>Sign In - PPH Green</title></Helmet><Login /></>} />
            <Route path="/register" element={<><Helmet><title>Join - PPH Green</title></Helmet><Register /></>} />

            <Route path="/" element={<Shell />}>
              <Route index element={<><Helmet><title>PPH Green - Community Portal</title></Helmet><Home /></>} />

              <Route path="events" element={<ProtectedRoute><Helmet><title>Events - PPH Green</title></Helmet><Events /></ProtectedRoute>} />
              <Route path="events/:id" element={<ProtectedRoute><Helmet><title>Event - PPH Green</title></Helmet><EventDetail /></ProtectedRoute>} />

              <Route path="gallery" element={<><Helmet><title>Gallery - PPH Green</title></Helmet><Gallery /></>} />
              <Route path="programs" element={<><Helmet><title>Programs - PPH Green</title></Helmet><Programs /></>} />
              <Route path="about" element={<><Helmet><title>About Us - PPH Green</title></Helmet><About /></>} />
              <Route path="developer" element={<><Helmet><title>Developer - PPH Green</title></Helmet><Developer /></>} />

              <Route path="profile" element={<ProtectedRoute><Helmet><title>Profile - PPH Green</title></Helmet><Profile /></ProtectedRoute>} />
              <Route path="notifications" element={<ProtectedRoute><Helmet><title>Notifications - PPH Green</title></Helmet><Notifications /></ProtectedRoute>} />
              <Route path="suggestion" element={<ProtectedRoute><Helmet><title>Suggestion - PPH Green</title></Helmet><Suggestion /></ProtectedRoute>} />

              <Route path="admin" element={<ProtectedRoute adminOnly><Helmet><title>Admin - PPH Green</title></Helmet><AdminDashboard /></ProtectedRoute>} />
              <Route path="admin/events" element={<ProtectedRoute adminOnly><Helmet><title>Manage Events - PPH Green</title></Helmet><EventsAdmin /></ProtectedRoute>} />
              <Route path="admin/gallery" element={<ProtectedRoute adminOnly><Helmet><title>Manage Gallery - PPH Green</title></Helmet><GalleryAdmin /></ProtectedRoute>} />
              <Route path="admin/programs" element={<ProtectedRoute adminOnly><Helmet><title>Manage Programs - PPH Green</title></Helmet><ProgramsAdmin /></ProtectedRoute>} />
              <Route path="admin/about" element={<ProtectedRoute adminOnly><Helmet><title>Manage About - PPH Green</title></Helmet><AboutAdmin /></ProtectedRoute>} />
              <Route path="admin/suggestions" element={<ProtectedRoute adminOnly><Helmet><title>Suggestions - PPH Green</title></Helmet><SuggestionsAdmin /></ProtectedRoute>} />
              <Route path="admin/approvals" element={<ProtectedRoute adminOnly><Helmet><title>Admin Requests - PPH Green</title></Helmet><AdminRequests /></ProtectedRoute>} />

              <Route path="*" element={<><Helmet><title>Not Found - PPH Green</title></Helmet><NotFound /></>} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </HelmetProvider>
  );
}
