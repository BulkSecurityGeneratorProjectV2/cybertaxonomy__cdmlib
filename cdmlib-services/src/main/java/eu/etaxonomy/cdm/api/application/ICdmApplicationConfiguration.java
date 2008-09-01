/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import org.springframework.transaction.PlatformTransactionManager;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
public interface ICdmApplicationConfiguration {

	/**
	 * @return
	 */
	public INameService getNameService();

	/**
	 * @return
	 */
	public ITaxonService getTaxonService();

	/**
	 * @return
	 */
	public IReferenceService getReferenceService();
	
	/**
	 * @return
	 */
	public IAgentService getAgentService();
	
	/**
	 * @return
	 */
	public IDescriptionService getDescriptionService();
	
	/**
	 * @return
	 */
	public IOccurrenceService getOccurrenceService();
	
	/**
	 * @return
	 */
	public IDatabaseService getDatabaseService();
	
	/**
	 * @return
	 */
	public ITermService getTermService();

	
	/**
	 * @return
	 */
	public ICommonService getCommonService();
	
	/**
	 * @return
	 */
	public PlatformTransactionManager getTransactionManager();

}
