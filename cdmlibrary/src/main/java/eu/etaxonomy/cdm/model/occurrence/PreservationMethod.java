/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.occurrence;


import etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:09
 */
public class PreservationMethod extends DefinedTermBase {
	static Logger logger = Logger.getLogger(PreservationMethod.class);

	@Description("")
	private static final int initializationClassUri = http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm;

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInitializationClassUri(newVal){
		initializationClassUri = newVal;
	}

}