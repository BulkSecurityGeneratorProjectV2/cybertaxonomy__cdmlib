/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:25
 */
@Entity
public class GenBankAccession extends VersionableEntity {
	static Logger logger = Logger.getLogger(GenBankAccession.class);
	private String accessionNumber;
	private String uri;
	public String getAccessionNumber(){
		return this.accessionNumber;
	}

	/**
	 * 
	 * @param accessionNumber    accessionNumber
	 */
	public void setAccessionNumber(String accessionNumber){
		this.accessionNumber = accessionNumber;
	}

	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

}