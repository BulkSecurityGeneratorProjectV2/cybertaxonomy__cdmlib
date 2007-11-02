/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.reference;


import etaxonomy.cdm.model.agent.Institution;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:24
 */
public class Thesis extends PublicationBase {
	static Logger logger = Logger.getLogger(Thesis.class);

	private Institution school;

	public Institution getSchool(){
		return school;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSchool(Institution newVal){
		school = newVal;
	}

}