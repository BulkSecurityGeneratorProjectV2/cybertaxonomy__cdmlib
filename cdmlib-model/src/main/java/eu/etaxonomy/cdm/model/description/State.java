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
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * This class represents terms describing different states (like "oval" or
 * "triangular") for {@link Feature features} that can be described with
 * categorical values (like for instance shapes).
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:53
 */
@Entity
public class State extends OrderedTermBase<State> {
	static Logger logger = Logger.getLogger(State.class);

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty state.
	 * 
	 * @see #State(String, String, String)
	 */
	public State() {
		super();
	}

	/** 
	 * Class constructor: creates a new state with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new state to be created 
	 * @param	label  		 the string identifying the new state to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new state to be created
	 * @see 				 #State()
	 */
	public State(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	//********* METHODS **************************************/
	/** 
	 * Creates a new empty state.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static State NewInstance(){
		return new State();
	}
	
	/** 
	 * Creates a new state with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new state to be created 
	 * @param	label  		 the string identifying the new state to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new state to be created
	 * @see 				 #NewInstance()
	 */
	public static State NewInstance(String term, String label, String labelAbbrev){
		return new State(term, label, labelAbbrev);
	}
	

}