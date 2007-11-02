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
 * @created 02-Nov-2007 18:14:54
 */
public class InProceedings extends SectionBase {
	static Logger logger = Logger.getLogger(InProceedings.class);

	private Proceedings inProceedings;

	public Proceedings getInProceedings(){
		return inProceedings;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInProceedings(Proceedings newVal){
		inProceedings = newVal;
	}

}