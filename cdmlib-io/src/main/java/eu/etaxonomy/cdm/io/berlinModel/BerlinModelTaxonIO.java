/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelTaxonIO  extends BerlinModelIOBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonIO.class);

	private static int modCount = 30000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<ReferenceBase> nomRefMap){
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxa ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();

		try {
			//get data from database
			String strQuery = 
					" SELECT * " + 
					" FROM PTaxon " +
					" WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("PTaxa handled: " + (i-1));}
				
				//create TaxonName element
				int taxonId = rs.getInt("RIdentifier");
				int statusFk = rs.getInt("statusFk");
				
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				String doubtful = rs.getString("DoubtfulFlag");
				
				TaxonNameBase taxonName = null;
				if (taxonNameMap != null){
					taxonName  = taxonNameMap.get(nameFk);
				}
								
				ReferenceBase reference = null;
				if (referenceMap != null){
					reference = referenceMap.get(refFk);
					if (reference == null){
						reference = nomRefMap.get(refFk);
					}
				}
				
				if (taxonName == null ){
					logger.warn("TaxonName belonging to taxon (RIdentifier = " + taxonId + ") could not be found in store. Taxon will not be transported");
					continue;
				}else if (reference == null ){
					logger.warn("Reference belonging to taxon could not be found in store. Taxon will not be imported");
					continue;
				}else{
					TaxonBase taxonBase;
					Synonym synonym;
					Taxon taxon;
					try {
						logger.debug(statusFk);
						if (statusFk == T_STATUS_ACCEPTED){
							taxon = Taxon.NewInstance(taxonName, reference);
							taxonBase = taxon;
						}else if (statusFk == T_STATUS_SYNONYM){
							synonym = Synonym.NewInstance(taxonName, reference);
							taxonBase = synonym;
						}else{
							logger.warn("TaxonStatus " + statusFk + " not yet implemented. Taxon (RIdentifier = " + taxonId + ") left out.");
							continue;
						}
						
						//TODO
//						dbAttrName = "Detail";
//						cdmAttrName = "Micro";
//						ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
						
						if (doubtful.equals("a")){
							taxonBase.setDoubtful(false);
						}else if(doubtful.equals("d")){
							taxonBase.setDoubtful(true);
						}else if(doubtful.equals("i")){
							//TODO
							logger.warn("Doubtful = i (inactivated) not yet implemented. Doubtful set to false");
						}
						
						//nameId
						ImportHelper.setOriginalSource(taxonBase, bmiConfig.getSourceReference(), taxonId);

						
						//TODO
						//
						//Created
						//Note
						//ALL
						
						taxonMap.put(taxonId, taxonBase);
					} catch (Exception e) {
						logger.warn("An exception occurred when creating taxon with id " + taxonId + ". Taxon could not be saved.");
					}
				}
			}
			//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
			logger.info("saving taxa ...");
			taxonService.saveTaxonAll(taxonMap.objects());
			
			logger.info("end makeTaxa ...");
			
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
}
