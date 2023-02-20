import { useReducer, useContext, useState } from 'react';
import LocalisationContext from '../contexts/localisation-context';

const useLocalisation = () => {
  const strings = useContext(LocalisationContext);
  const forceUpdate = useReducer((x) => x + 1, 0)[1];
  return [
    strings,
    (lang) => {
      console.log('changed from ' + strings.getLanguage() + ' to ' + lang);
      if (strings.getLanguage() !== lang) {
        strings.setLanguage(lang);
        forceUpdate();
      }
    },
  ];
};

export default useLocalisation;
