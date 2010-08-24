/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.babadshanjan
 * @created 21.08.2010
 * @version 1.0
 */
@Component
public class FaunaEuropaeaAdditionalTaxonDataImport extends FaunaEuropaeaImportBase  {
	
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaAdditionalTaxonDataImport.class);
	private static final String parentPluralString = "TaxonBases";
	private static final String pluralString = "InfraGenericEpithet";
	private static final String acceptedTaxonUUID = "A9C24E42-69F5-4681-9399-041E652CF338"; // any accepted taxon uuid, taken from original fauna europaea database

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state) {
		return ! state.getConfig().isDoTaxa();
	}

	private boolean checkTaxonStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
		boolean result = true;
//		try {
			Source source = fauEuConfig.getSource();
			String sqlStr = "";
			ResultSet rs = source.getResultSet(sqlStr);
			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/** 
	 * Import taxa from FauEU DB
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;
		if(logger.isInfoEnabled()) {
			logger.info("Start making " + pluralString + "...");
		}
		
		success = processAdditionalInfraGenericEpithets(state);
		
		logger.info("End making " + pluralString + "...");
		return success;
	}


	/**
	 * 
	 * @param state
	 * @return
	 */
	private boolean processAdditionalInfraGenericEpithets(FaunaEuropaeaImportState state) {
		boolean success = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		int count = 0;
		int pastCount = 0;
		int limit = 1000;
		TransactionStatus txStatus = null;
		List<TaxonBase> list = null;
		
		txStatus = startTransaction(true);
		TaxonBase singleTaxon = getTaxonService().find(UUID.fromString(acceptedTaxonUUID));
		ReferenceBase<?> sourceRef = singleTaxon.getSec();
		commitTransaction(txStatus);
		
		txStatus = startTransaction(false);
		logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		
		// Optimum: Fetch all synonyms without infraGenericEpithet
		Synonym synonym = Synonym.NewInstance(null, sourceRef);
		while ((list = getTaxonService().list(synonym, null, limit, count, null, null)).size() > 0) {
			
			logger.error("Fetched " + list.size() + " " + parentPluralString + ". Processing...");
			for (TaxonBase taxonBase : list) {
				
				if (!taxonBase.isInstanceOf(Synonym.class)) {
					logger.error("This taxonBase is not of instance Synonym: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache());
				}
				
				TaxonNameBase taxonName = taxonBase.getName();

				if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
					NonViralName targetNonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
					String infraGenericEpithet = targetNonViralName.getInfraGenericEpithet();
					if (infraGenericEpithet == null) {
						String genusOrUninomial = targetNonViralName.getGenusOrUninomial();
						String specificEpithet = targetNonViralName.getSpecificEpithet();
						List<TaxonBase> foundTaxa = getTaxonService().listTaxaByName(Taxon.class, genusOrUninomial, null, specificEpithet, 
								null, Rank.SPECIES(), 10, 1);
						if (foundTaxa.size() == 1) {
							// one matching taxon found
							TaxonBase taxon = foundTaxa.iterator().next();
							if (taxon != null) {
								TaxonNameBase name = taxon.getName();
								if (name != null && name.isInstanceOf(NonViralName.class)) {
									NonViralName nonViralName = CdmBase.deproxy(name, NonViralName.class);
									infraGenericEpithet = nonViralName.getInfraGenericEpithet();
									
									// set infraGenericEpithet
//									targetNonViralName.setInfraGenericEpithet(infraGenericEpithet);
									count++;
								}
							}
						} else if (foundTaxa.size() > 1) {
							logger.error("Multiple taxa match search criteria: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
							for (TaxonBase foundTaxonBase : foundTaxa) {
								logger.error(foundTaxonBase.getUuid() + ", " + foundTaxonBase.getTitleCache());
							}
						} else if (foundTaxa.size() == 0) {
//							logger.error("No matches for search criteria: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						}
					}
					
				}
				
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");
			logger.error("Added " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(false);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		}
		if (list.size() == 0) {
			logger.error("No " + parentPluralString + " left to fetch.");
		}
		// Commit transaction
		commitTransaction(txStatus);
		logger.error("Committed transaction.");
		
		return success;
	}

}
