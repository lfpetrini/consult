import LocalisationContext from '../../contexts/localisation-context'
import LocalizedStrings from 'react-localization';
import { LocalisationData } from '../../assets/LocalisationData';

const LocalisationWrapper = (props) => {
    return <LocalisationContext.Provider value={new LocalizedStrings(LocalisationData)}>
        {props.children}
    </LocalisationContext.Provider>
}

export default LocalisationWrapper;