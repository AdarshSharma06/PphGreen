import { useApi } from '../../hooks/useApi';
import { adminApi } from '../../api/client';
import { useNavigate } from 'react-router-dom';
import { Users, Check, X, ArrowLeft } from 'lucide-react';
import EmptyState from '../../components/ui/EmptyState';
import './Admin.css';

export default function AdminRequests() {
  const { data, loading, refetch } = useApi(() => adminApi.getPending(), []);
  const navigate = useNavigate();

  const requests = data?.data || [];

  const handleApprove = async (id) => {
    try {
      await adminApi.approve(id);
      refetch();
    } catch { /* approval may fail */ }
  };

  const handleReject = async (id) => {
    try {
      await adminApi.reject(id);
      refetch();
    } catch { /* rejection may fail */ }
  };

  return (
    <div className="admin-page">
      <div className="container">
        <button className="btn btn-ghost mb-4" onClick={() => navigate('/admin')}>
          <ArrowLeft size={16} /> Back to Dashboard
        </button>

        <div className="page-header">
          <h1>Admin Requests</h1>
          <p>Review and manage admin access requests</p>
        </div>

        {loading && <div className="loading-container"><div className="loading-spinner" /></div>}

        {!loading && requests.length === 0 && (
          <EmptyState icon={Users} title="No pending requests" description="Admin access requests will appear here." />
        )}

        {requests.length > 0 && (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Joined</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((user) => (
                  <tr key={user.id}>
                    <td className="font-medium">{user.name || '—'}</td>
                    <td>{user.email}</td>
                    <td className="text-muted">{new Date(user.id).toLocaleDateString()}</td>
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-primary btn-sm" onClick={() => handleApprove(user.id)}>
                          <Check size={14} /> Approve
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleReject(user.id)}>
                          <X size={14} /> Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
