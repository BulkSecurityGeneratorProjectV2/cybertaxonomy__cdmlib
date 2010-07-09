package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

public class DefaultCategoricalDescriptionBuilder extends AbstractCategoricalDescriptionBuilder{
	
	protected TextData doBuild(List<StateData> states){
		TextData textData = TextData.NewInstance();// TextData that will contain the description and the language corresponding
		Language language = Language.DEFAULT();
		
		StringBuilder CategoricalDescription = new StringBuilder();

		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			Set<Modifier> modifiers = stateData.getModifiers(); // the states and their according modifiers are simply written one after the other
			for (Iterator<Modifier> mod = modifiers.iterator() ; mod.hasNext() ;){
				Modifier modifier = mod.next();
				CategoricalDescription.append(" " + modifier.getPreferredRepresentation(language).getLabel());
			}
			CategoricalDescription.append(" " + s.getPreferredRepresentation(language).getLabel());
		}
		textData.putText(CategoricalDescription.toString(), language);
		
		return textData;
	}
}
