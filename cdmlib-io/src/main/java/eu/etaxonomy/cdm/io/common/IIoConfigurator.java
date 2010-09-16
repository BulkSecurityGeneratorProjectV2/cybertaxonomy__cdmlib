/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;


/**
 * @author a.babadshanjan
 * @created 13.11.2008
 */
public interface IIoConfigurator {

	/**
	 * A String representation of the used source may it be a source to be imported (e.g. "BerlinModel Cichorieae Database")
	 * or a source to be exported (e.g. "CDM Cichorieae Database")
	 * @return String representing the source for the io
	 */
	public String getSourceNameString();
	

	/**
	 * A String representation of the destination may it be an import destination and therefore a CDM (e.g. CDM Cichorieae Database)
	 * or an export destination (e.g. CDM XML)
	 * @return
	 */
	public String getDestinationNameString();
	
	
	/**
	 * Returns the CdmApplicationController
	 * @return
	 */
	public CdmApplicationController getCdmAppController();

	
	/**
	 * Sets the CdmApplicationController
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmAppController(CdmApplicationController cdmApp);

	/**
	 * Get the way how the CDM schema is validated
	 * @see eu.etaxonomy.cdm.database.DbSchemaValidation
	 * @return
	 */
	public DbSchemaValidation getDbSchemaValidation();

	/**
	 * Get the way how the CDM schema is validated
	 * For exports values that delete the source (CREATE, CREATE_DROP) are not allowed and may throw an 
	 * Exception in the further run
	 * @see eu.etaxonomy.cdm.database.DbSchemaValidation
	 * @param dbSchemaValidation
	 */
	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation);

	/**
	 * 
	 * @param monitor
	 */
	public void setProgressMonitor(IProgressMonitor monitor);
	
	/**
	 * 
	 * @return
	 */
	public IProgressMonitor getProgressMonitor();

}
