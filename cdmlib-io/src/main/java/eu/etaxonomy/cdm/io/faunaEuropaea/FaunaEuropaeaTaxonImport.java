/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaTaxonImport extends FaunaEuropaeaImportBase  {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTaxonImport.class);

	private int modCount = 10000;
	/* Max number of taxa to be saved with one service call */
	private int limit = 20000; // TODO: Make configurable
	
	public FaunaEuropaeaTaxonImport(){
	}
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	private boolean checkTaxonStatus(FaunaEuropaeaImportConfigurator fauEuConfig){
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){				
		
		boolean success = true;
		//make not needed maps empty
		MapWrapper<TeamOrPersonBase<?>> authorMap = (MapWrapper<TeamOrPersonBase<?>>)stores.get(ICdmIO.TEAM_STORE);
		authorMap.makeEmpty();

		
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<TaxonNameBase<?,?>> taxonNameStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		Source source = fauEuConfig.getSource();
		
		String namespace = "Taxon";
		
		if(logger.isInfoEnabled()) { logger.info("Start making Taxa ..."); }
		
		try {
			String strQuery = 
				" SELECT Taxon.*, rank.* " + 
                " FROM dbo.Taxon INNER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
                " WHERE (1=1)";
            		
//			" SELECT Taxon.*, dbo.rank.rnk_id " + 
//            " FROM dbo.Taxon INNER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
//            " WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("References handled: " + (i-1)); 
					}
				}
				
				int taxonId = rs.getInt("TAX_ID");
				int parentId = rs.getInt("TAX_TAX_IDPARENT");
				String taxonName = rs.getString("TAX_NAME");
				int statusFk = rs.getInt("TAX_VALID");
				int rankId = rs.getInt("rnk_id");
				Rank rank = null;
				
				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					success = false; 
				}
				
				ReferenceBase<?> reference = null;
//				if (referenceMap != null){
//					//int refFk = rs.getInt("PTRefFk");
//					if ()
//					reference = referenceMap.get(refFk);
//					if (reference == null){
//						reference = nomRefMap.get(refFk);
//					}
//				}
				
				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
				zooName.setNameCache(taxonName);
				zooName.setTitleCache(taxonName); // FIXME: Add the author
				zooName.setFullTitleCache(taxonName); // FIXME: Add author, reference, NC status
				
				taxonNameStore.put(taxonId, zooName);
				
				TaxonBase<?> taxonBase;
				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
				
				Synonym synonym;
				Taxon taxon;
				try {
					logger.debug(statusFk);
					if (statusFk == T_STATUS_ACCEPTED) {
						taxon = Taxon.NewInstance(zooName, reference);
						taxonBase = taxon;
					} else if (statusFk == T_STATUS_NOT_ACCEPTED) {
						synonym = Synonym.NewInstance(zooName, reference);
						taxonBase = synonym;
					} else {
						logger.warn("Taxon status " + statusFk + " not yet implemented. Taxon (" + taxonId + ") ignored.");
						continue;
					}
					
					taxonBase.setTitleCache(taxonName);
					
					//nameId
					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, namespace);
					
					taxonStore.put(taxonId, taxonBase);
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon with id " + taxonId + 
							". Taxon could not be saved.");
				}
			}
			//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
			
			if(logger.isInfoEnabled()) { logger.info("Saving taxa ..."); }
			
			int nbrOfTaxa = taxonStore.size();
			int n = nbrOfTaxa / limit;
			
			for (int id : taxonStore.keySet())
			{
//				FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
//				
//				if (fauEuTaxon.getRankId() == FaunaEuropaeaTransformer.R_SPECIES) {
//
//					// Concat parent's taxon name
//					FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(fauEuTaxon.getParentId());
//					StringBuilder name = new StringBuilder(parent.getScientificName());
//					name.append(" ");
//					name.append(fauEuTaxon.getScientificName());
//					fauEuTaxon.setScientificName(name.toString());
//				}
			}
				
			// save taxa in chunks
			for (int j = 1; j <= n; j++)
			{

				if ((j++ % modCount) == 0 && j!= 1 ) { 

					logger.info("Taxa handled: " + (j-1));
				}

				getTaxonService().saveTaxonAll(taxonStore.objects());

			}
			
			logger.info("end making taxa ...");
			
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config) {
		return !config.isDoTaxa();
	}

}
