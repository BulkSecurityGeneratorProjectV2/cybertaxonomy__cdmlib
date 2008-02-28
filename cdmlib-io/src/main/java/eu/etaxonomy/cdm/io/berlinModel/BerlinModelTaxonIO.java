/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;


/**
 * @author a.mueller
 *
 */
public class BerlinModelTaxonIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonIO.class);

	private static int modCount = 30000;

	public static boolean invoke(Source source, CdmApplicationController cdmApp, boolean deleteAll, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap){
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxa ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = deleteAll;

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
				}
				
				if (taxonName == null ){
					//logger.warn("TaxonName belonging to taxon (RIdentifier = " + taxonId + ") could not be found in store. Taxon will not be transported");
					continue;
				}else if (reference == null ){
					//logger.warn("Reference belonging to taxon could not be found in store. Taxon will not be imported");
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
			logger.info("saving taxa ...");
			taxonService.saveTaxonAll(taxonMap.objects());
			
			logger.info("end makeTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	

	public static boolean invokeRelations(Source source, CdmApplicationController cdmApp, boolean deleteAll, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();

		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = deleteAll;

		try {
			//get data from database
			String strQuery = 
					" SELECT RelPTaxon.*, FromTaxon.RIdentifier as taxon1Id, ToTaxon.RIdentifier as taxon2Id " + 
					" FROM PTaxon as FromTaxon INNER JOIN " +
                      	" RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 INNER JOIN " +
                      	" PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("RelPTaxa handled: " + (i-1));}
				
				int relPTaxonId = rs.getInt("RelPTaxonId");
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				int relRefFk = rs.getInt("relRefFk");
				int relQualifierFk = rs.getInt("relQualifierFk");
				
				TaxonBase taxon1 = taxonMap.get(taxon1Id);
				TaxonBase taxon2 = taxonMap.get(taxon2Id);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				if (taxon2 != null && taxon1 != null){
					if (relQualifierFk == IS_INCLUDED_IN){
						((Taxon)taxon2).addTaxonomicChild((Taxon)taxon1, citation, microcitation);
					}else if (relQualifierFk == IS_SYNONYM_OF){
						((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.SYNONYM_OF());
					}else if (relQualifierFk == IS_HOMOTYPIC_SYNONYM_OF){
						((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
					}else if (relQualifierFk == IS_HETEROTYPIC_SYNONYM_OF){
						if (Synonym.class.isAssignableFrom(taxon1.getClass())){
							((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
						}else{
							logger.warn("Taxon (RIdentifier = " + taxon1Id + ") can not be casted to Synonym");
						}
					}else if (relQualifierFk == IS_MISAPPLIED_NAME_OF){
						((Taxon)taxon2).addMisappliedName((Taxon)taxon1, citation, microcitation);
					}else {
						//TODO
						logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented");
					}
					taxonStore.add(taxon2);
					
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					//TODO
					//logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
				}
			}
			logger.info("Taxa to save: " + taxonStore.size());
			//taxonService.saveTaxonAll(taxonStore);
			
			logger.info("end makeRelTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	
}
