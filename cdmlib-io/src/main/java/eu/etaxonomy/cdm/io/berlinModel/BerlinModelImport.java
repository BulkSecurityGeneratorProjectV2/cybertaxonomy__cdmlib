package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@Service
public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	
	//Hashmaps for Joins
	//OLD: private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private MapWrapper<Team> authorStore= new MapWrapper<Team>(null);
	private MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
	private MapWrapper<TaxonNameBase> taxonNameStore = new MapWrapper<TaxonNameBase>(null);
	private MapWrapper<TaxonBase> taxonStore = new MapWrapper<TaxonBase>(null);


	/**
	 * Executes the whole 
	 */
	public boolean doImport(BerlinModelImportConfigurator bmiConfig){
		CdmApplicationController cdmApp;
		if (bmiConfig == null){
			logger.warn("BerlinModelImportConfiguration is null");
			return false;
		}else if (! bmiConfig.isValid()){
			logger.warn("BerlinModelImportConfiguration is not valid");
			return false;
		}
		try {
			cdmApp = CdmApplicationController.NewInstance(bmiConfig.getDestination(), bmiConfig.getDbSchemaValidation());
		} catch (DataSourceNotFoundException e) {
			logger.warn("could not connect to destination database");
			return false;
		}
		Source source = bmiConfig.getSource();
		ReferenceBase sourceReference = bmiConfig.getSourceReference();
		System.out.println("Start import from BerlinModel ("+ bmiConfig.getSource().getDatabase() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		

		//Authors
		if (bmiConfig.isDoAuthors()){
			if (! BerlinModelAuthorIO.invoke(bmiConfig, cdmApp, authorStore)){
				logger.warn("No Authors imported");
				return false;
			}
		}else{
			authorStore = null;
		}
		
		//References
		if (bmiConfig.isDoReferences()){
			if (! BerlinModelReferenceIO.invoke(bmiConfig, cdmApp, referenceStore, authorStore)){
				return false;
			}
		}else{
			logger.warn("No References imported");
			referenceStore = null;
		}
		
		//TaxonNames
		if (bmiConfig.isDoTaxonNames()){
			if (! BerlinModelTaxonNameIO.invoke(bmiConfig, cdmApp, taxonNameStore, referenceStore, authorStore)){
				//return false;
			}
		}else{
			logger.warn("No TaxonNames imported");
			taxonNameStore = null;
		}

		
		//make and save RelNames
		if(bmiConfig.isDoRelNames()){
			if (! BerlinModelTaxonNameIO.invokeRelations(bmiConfig, cdmApp, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}

		//TODO NomStatus
		//TODO Types
		
		//make and save Taxa
		if(bmiConfig.isDoTaxa()){
			if (! BerlinModelTaxonIO.invoke(bmiConfig, cdmApp, taxonStore, taxonNameStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Taxa imported");
			taxonNameStore = null;
		}
		
		//make and save RelPTaxa
		if(bmiConfig.isDoRelTaxa()){
			if (! BerlinModelTaxonIO.invokeRelations(bmiConfig, cdmApp, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No RelPTaxa imported");
		}
		
		//make and save Facts
		if(bmiConfig.isDoFacts()){
			if (! BerlinModelFactsIO.invoke(bmiConfig, cdmApp, taxonStore, referenceStore)){
				return false;
			}
		}else{
			logger.warn("No Facts imported");
		}
		
		//return
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

}
