/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * {type is "host" or "hybrid_parent"}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@Entity
public class IndividualsAssociation extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(IndividualsAssociation.class);
	private MultilanguageSet description;
	private SpecimenOrObservationBase associatedSpecimenOrObservation;


	/**
	 * Factory method
	 * @return
	 */
	public static IndividualsAssociation NewInstance(){
		return new IndividualsAssociation();
	}
	
	/**
	 * Constructor
	 */
	protected IndividualsAssociation(){
		super(null);
	}
	

	@ManyToOne
	public SpecimenOrObservationBase getAssociatedSpecimenOrObservation() {
		return associatedSpecimenOrObservation;
	}
	public void setAssociatedSpecimenOrObservation(
			SpecimenOrObservationBase associatedSpecimenOrObservation) {
		this.associatedSpecimenOrObservation = associatedSpecimenOrObservation;
	}

	
	public MultilanguageSet getDescription(){
		return this.description;
	}
	private void setDescription(MultilanguageSet description){
		this.description = description;
	}
	public void addDescription(LanguageString description){
		this.description.add(description);
	}
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	public void removeDescription(Language language){
		this.description.remove(language);
	}

}