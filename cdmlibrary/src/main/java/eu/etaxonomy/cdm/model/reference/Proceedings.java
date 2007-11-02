/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:10
 */
public class Proceedings extends PrintedUnitBase {
	static Logger logger = Logger.getLogger(Proceedings.class);

	//The conference sponsor
	@Description("The conference sponsor")
	private String organization;

	public String getOrganization(){
		return organization;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOrganization(String newVal){
		organization = newVal;
	}

}