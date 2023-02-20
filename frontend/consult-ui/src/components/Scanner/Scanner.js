import styles from './Scanner.module.css';
import { useEffect, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import useLocalisation from '../../hooks/use-localisation';

const qrcodeRegionId = 'html5qr-code-full-region';

const Scanner = (props) => {
  const [cameras, setCameras] = useState([]);
  const [selectedCamera, setSelectedCamera] = useState(null);
  const [strings, setLanguage] = useLocalisation();
  console.log('Creating');
  useEffect(() => {
    console.log('Effect');
    Html5Qrcode.getCameras()
      .then((devices) => {
        /**
         * devices would be an array of objects of type:
         * { id: "id", label: "label" }
         */
        if (devices && devices.length) {
          setCameras(devices);
          setSelectedCamera(devices[0]);
        }
      })
      .catch((err) => {
        // handle err
      });
  }, []);

  const onCameraSelect = (event) => {
    console.log('Selected ' + cameras.find(c => c.id === event.target.value).id);
  }

  const cameraSelector = (
    <div>
      <label htmlFor="camera">{strings.selectCamera}</label>
      <p>
        <select id="camera" name="camera" onChange={onCameraSelect}>
          {cameras.map((c) => (
            <option key={c.id} value={c.id}>{c.label}</option>
          ))}
        </select>
      </p>
    </div>
  );

  return (
    <div className={styles.scanner}>
      <div className={styles['scanner-wrapper']}>
        <div id={qrcodeRegionId} />
        {cameras.length > 1 && cameraSelector}
      </div>
    </div>
  );
};

export default Scanner;
