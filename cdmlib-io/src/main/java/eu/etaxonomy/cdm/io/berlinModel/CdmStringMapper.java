/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import eu.etaxonomy.cdm.io.common.CdmIoMapperBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CdmStringMapper extends CdmIoMapperBase {

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmStringMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}
	
	public Class getTypeClass(){
		return String.class;
	}

}
