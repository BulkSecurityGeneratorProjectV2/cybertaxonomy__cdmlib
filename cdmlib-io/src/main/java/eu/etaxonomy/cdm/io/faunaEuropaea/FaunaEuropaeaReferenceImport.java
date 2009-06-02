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

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImportConfigurator;
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
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
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
public class FaunaEuropaeaReferenceImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaReferenceImport.class);

	private int modCount = 10000;
	/* Max number of references to be saved with one service call */
	private int limit = 20000; // TODO: Make configurable
	
	public FaunaEuropaeaReferenceImport() {
	}
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkReferenceStatus(fauEuConfig);
		
		return result;
	}
	
	private boolean checkReferenceStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
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
			Map<String, MapWrapper<? extends CdmBase>> stores) {				
		
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		
//		MapWrapper<ReferenceBase> refStore= new MapWrapper<ReferenceBase>(null);
//		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);
		
		//make not needed maps empty
//		MapWrapper<TeamOrPersonBase<?>> authorStore = (MapWrapper<TeamOrPersonBase<?>>)stores.get(ICdmIO.TEAM_STORE);
//		authorMap.makeEmpty();

		
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		Source source = fauEuConfig.getSource();
		
		String namespace = "Reference";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making References..."); }
		
		try {
			String strQuery = 
				" SELECT Reference.*, TaxRefs.* " + 
                " FROM Reference INNER JOIN TaxRefs ON Reference.ref_id = TaxRefs.trf_ref_id " +
                " WHERE (1=1)";
            					
//			String strQuery = 
//				" SELECT Reference.*, TaxRefs.*, author.aut_id " + 
//                " FROM Reference INNER JOIN TaxRefs ON Reference.ref_id = TaxRefs.trf_ref_id " +
//                " INNER JOIN author ON Reference.ref_author = author.aut_name" +
//                " WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()) {
				
				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("References handled: " + (i-1)); 
					}
				}
				
				int refId = rs.getInt("ref_id");
				//String author = rs.getString("ref_author");
				String year = rs.getString("ref_year");
				String title = rs.getString("ref_title");
				String ref_author = rs.getString("ref_author");
				String refSource = rs.getString("ref_source");
//				int authorId = rs.getInt("aut_id");
				
				StrictReferenceBase<?> reference = null;
				
				try {
					reference = Generic.NewInstance();
					reference.setTitleCache(title);
					reference.setDatePublished(ImportHelper.getDatePublished(year));
					
					// FIXME: author.aut_name and Reference.ref_author don't match
//					if (authorStore != null) {
//						TeamOrPersonBase<?> author = authorStore.get(authorId);
//						if (author != null) {
//							reference.setAuthorTeam(author);
//						}
//					}
										
					ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), refId, namespace);
					
					if (!refStore.containsId(refId)) {
						if (reference == null) {
							logger.warn("Reference is null");
						}
						refStore.put(refId, reference);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored reference (" + refId + ") " + ref_author); 
						}
					} else {
						logger.warn("Not imported reference with duplicated ref_id (" + refId + 
								") " + ref_author);
					}
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating reference with id " + refId + 
					". Reference could not be saved.");
				}
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving references ..."); }
			
			// save references
			getReferenceService().saveReferenceAll(refStore.objects());
			
			if(logger.isInfoEnabled()) { logger.info("End making references ..."); }
			
			return true;
			
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return (config.getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
