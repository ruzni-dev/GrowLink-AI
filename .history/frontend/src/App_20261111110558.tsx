import { useState } from 'react';
import { Home } from './pages/Home';

function App() {
  const [notice, setNotice] = useState('');

  const notify = (message: string) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 3200);
  };

  return (
    <div className="app-shell">
      <Home onNotify={notify} />
      {notice && <div className="toast" role="status">{notice}</div>}
    </div>
  );
}

export default App;
