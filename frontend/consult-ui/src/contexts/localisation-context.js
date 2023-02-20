import React from 'react';
import LocalizedStrings from 'react-localization';
import { LocalisationData } from '../assets/LocalisationData';
const strings = new LocalizedStrings(LocalisationData);

const LocalisationContext = React.createContext(new LocalizedStrings(LocalisationData));

export default LocalisationContext;