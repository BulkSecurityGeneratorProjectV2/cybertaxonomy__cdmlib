/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.molecular;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:50
 */
public class GenBankAccession extends VersionableEntity {
	static Logger logger = Logger.getLogger(GenBankAccession.class);

	@Description("")
	private String accessionNumber;
	@Description("")
	private String uri;

	public String getAccessionNumber(){
		return accessionNumber;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAccessionNumber(String newVal){
		accessionNumber = newVal;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUri(String newVal){
		uri = newVal;
	}

}