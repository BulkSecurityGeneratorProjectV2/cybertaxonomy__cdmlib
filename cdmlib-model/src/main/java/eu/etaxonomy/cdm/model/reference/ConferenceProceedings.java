/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;

import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 */
@Entity
public class ConferenceProceedings extends StrictReferenceBase {
	private static final Logger logger = Logger.getLogger(ConferenceProceedings.class);


	public static ConferenceProceedings NewInstance(){
		ConferenceProceedings result = new ConferenceProceedings();
		return result;
	}
	
	@Override
	public String generateTitle(){
		return "";
	}

}