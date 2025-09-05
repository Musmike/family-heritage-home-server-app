import { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [message, setMessage] = useState<string>('Loading...');

 useEffect(() => {
    const apiUrl = 'http://localhost:8080/api/v1/health';

    axios.get<{ message: string }>(apiUrl)
      .then(response => {
        setMessage(response.data.message);
      })
      .catch(error => {
        console.error('There was an error fetching the data!', error);
        setMessage('Failed to load data from backend.');
      });
  }, []);

  return (
    <>
      <h1>Family Heritage App</h1>
      <div className="card">
        <p>
          Message from server: <strong>{message}</strong>
        </p>
      </div>
    </>
  );
}

export default App;