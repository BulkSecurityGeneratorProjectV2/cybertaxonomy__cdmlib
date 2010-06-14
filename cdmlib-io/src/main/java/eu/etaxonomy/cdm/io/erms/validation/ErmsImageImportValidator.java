// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms.validation;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.erms.ErmsImportConfigurator;
import eu.etaxonomy.cdm.io.erms.ErmsImportState;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class ErmsImageImportValidator implements IOValidator<ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsImageImportValidator.class);

	public boolean validate(ErmsImportState state){
		boolean result = true;
		ErmsImportConfigurator config = state.getConfig();
		logger.warn("Checking for images not yet fully implemented");

		return result;
	}
	


}
