import { useEffect, useState } from 'react';
import { checkBackendHealth } from './services/api';

interface HealthResponse {
  application: string;
  status: string;
  message: string;
}

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    checkBackendHealth()
      .then(setHealth)
      .catch(() => {
        setError('Unable to connect to backend');
      });
  }, []);

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-slate-950 text-white">
      <h1 className="text-5xl font-bold text-emerald-400">
        GrowLink AI
      </h1>

      <p className="mt-4 text-slate-300">
        Discover Talent. Connect Knowledge. Grow Together.
      </p>

      <div className="mt-10 rounded-xl border border-slate-700 p-8 text-center">
        <h2 className="text-xl font-semibold">
          Backend Connection
        </h2>

        {health && (
          <div className="mt-4 text-emerald-400">
            <p>Application: {health.application}</p>
            <p>Status: {health.status}</p>
            <p>{health.message}</p>
          </div>
        )}

        {error && (
          <p className="mt-4 text-red-400">{error}</p>
        )}

        {!health && !error && (
          <p className="mt-4 text-yellow-400">
            Connecting to backend...
          </p>
        )}
      </div>
    </main>
  );
}

export default App;
