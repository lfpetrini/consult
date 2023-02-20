import { useState } from 'react';
import Scanner from '../Scanner/Scanner';

import styles from './App.module.css';
import useLocalisation from '../../hooks/use-localisation';

function App() {
  const [strings, setLanguage] = useLocalisation();
  const [manualInput, setManualInput] = useState(false);
  return (
    <div className={styles.app}>
      <header className={styles.appheader}>
        <h1>{strings.title}</h1>
      </header>
      {manualInput ? (
        <div>
          <input type="text" placeholder={strings.enterCode}></input>
        </div>
      ) : (
        <Scanner onRead={(data, error) => console.log(data)} />
      )}
      <button type="button" onClick={() => setManualInput((prev) => !prev)}>
        {manualInput ? strings.orScan : strings.orEnterManually}
      </button>
    </div>
  );
}

export default App;
