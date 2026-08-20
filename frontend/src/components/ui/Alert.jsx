import { AlertCircle, CheckCircle, Info } from 'lucide-react';

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  info: Info,
};

export default function Alert({ type = 'info', children }) {
  const Icon = icons[type];
  return (
    <div className={`alert alert-${type}`} role="alert">
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--space-2)' }}>
        <Icon size={16} style={{ marginTop: 2, flexShrink: 0 }} />
        <span>{children}</span>
      </div>
    </div>
  );
}
