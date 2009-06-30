/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;

/**
 * This is an exporter that invokes the application aware defaultExport when invoked itself
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public class CdmDefaultIOBase<T extends IIoConfigurator>  {
	private static final Logger logger = Logger.getLogger(CdmDefaultIOBase.class);

	protected CdmApplicationController cdmApp = null;


	/**
	 * Creates a new {@link CdmApplicationController} if it does not exist yet or if createNew is <ocde>true</code>
	 * @param config
	 * @param destination
	 * @param omitTermLoading
	 * @param createNew
	 * @return
	 */
	protected boolean startApplicationController(IIoConfigurator config, ICdmDataSource cdmSource, boolean omitTermLoading, boolean createNew){
		if (config.getCdmAppController() != null){
			this.cdmApp = config.getCdmAppController(); 
		}
		DbSchemaValidation schemaValidation = config.getDbSchemaValidation();
		if ( this instanceof CdmDefaultExport){
			if (schemaValidation.equals(DbSchemaValidation.CREATE)|| schemaValidation.equals(DbSchemaValidation.CREATE_DROP)  ){
				throw new IllegalArgumentException("The export may not run with DbSchemaValidation.CREATE or DbSchemaValidation.CREATE_DROP as this value deletes the source database");
			}
		}
		try {
			if ( createNew == true || cdmApp == null){
				cdmApp = CdmApplicationController.NewInstance(cdmSource, schemaValidation, omitTermLoading);
				if (cdmApp != null){
					return true;
				}else{
					return false;
				}
			}
			return true;
		} catch (DataSourceNotFoundException  e) {
			logger.error("could not connect to source CDM database");
			return false;
		}catch (TermNotFoundException e) {
			logger.error("could not find needed term in destination datasource");
			return false;
		}
	}
	
	
	/**
	 * Returns the {@link CdmApplicationController}. This is null if invoke() has not been called yet and if the controller
	 * has not been set manually by setCdmApp() yet. 
	 * @return the cdmApp
	 */
	public CdmApplicationController getCdmAppController() {
		return this.cdmApp;
	}


	/**
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmAppController(CdmApplicationController cdmApp) {
		this.cdmApp = cdmApp;
	}

	
	

}
