/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

public interface IIO<T extends IImportConfigurator> {

	public boolean check(T config);
	
	public boolean invoke(T config, CdmApplicationController app, MapWrapper<? extends CdmBase>[] storeArray);
	

	
}