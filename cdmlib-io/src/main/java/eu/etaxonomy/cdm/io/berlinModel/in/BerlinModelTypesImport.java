/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTypesImport extends BerlinModelImportBase /*implements IIO<BerlinModelImportConfigurator>*/ {
	private static final Logger logger = Logger.getLogger(BerlinModelTypesImport.class);

	private static int modCount = 10000;
	
	public BerlinModelTypesImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		boolean result = true;
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		Map<Integer, Specimen> typeMap = new HashMap<Integer, Specimen>();
		
		logger.info("start makeTypes ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT TypeDesignation.*, TypeStatus.Status " + 
					" FROM TypeDesignation LEFT OUTER JOIN " +
                      	" TypeStatus ON TypeDesignation.TypeStatusFk = TypeStatus.TypeStatusId " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Types handled: " + (i-1));}
				
				int typeDesignationId = rs.getInt("typeDesignationId");
				int nameId = rs.getInt("nameFk");
				int typeStatusFk = rs.getInt("typeStatusFk");
				int refFk = rs.getInt("refFk");
				String refDetail = rs.getString("refDetail");
				String status = rs.getString("Status");
				String typePhrase = rs.getString("typePhrase");
				
				//TODO 
				boolean isNotDesignated = false;
				
				
				//TODO
				//TypeCache leer
				//RejectedFlag false
				//PublishFlag xxx
				
				
				TaxonNameBase<?,?> taxonNameBase = taxonNameMap.get(nameId);
				
				if (taxonNameBase != null){
					try{
						SpecimenTypeDesignationStatus typeDesignationStatus = BerlinModelTransformer.typeStatusId2TypeStatus(typeStatusFk);
						ReferenceBase citation = referenceMap.get(refFk);
						
						Specimen specimen = Specimen.NewInstance();
						specimen.setTitleCache(typePhrase);
						boolean addToAllNames = true;
						String originalNameString = null;
						taxonNameBase.addSpecimenTypeDesignation(specimen, typeDesignationStatus, citation, refDetail, originalNameString, isNotDesignated, addToAllNames);
												
						typeMap.put(typeDesignationId, specimen);
						taxonNameStore.add(taxonNameBase);
						
						//TODO
						//Update, Created, Notes, origId
						//doIdCreatedUpdatedNotes(bmiConfig, media, rs, nameFactId);

					}catch (UnknownCdmTypeException e) {
						logger.warn("TypeStatus '" + status + "' not yet implemented");
						result = false;
					}
				}else{
					//TODO
					logger.warn("TaxonName for TypeDesignation " + typeDesignationId + " does not exist in store");
					result = false;
				}
				//put
			}
			
			result &= makeFigures(typeMap, source);
			
			
			logger.info("Names to save: " + taxonNameStore.size());
			getNameService().saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeTypes ..." + getSuccessString(result));
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private static boolean makeFigures(Map<Integer, Specimen> typeMap, Source source){
		boolean success = true;
		try {
			//get data from database
			String strQuery = 
					" SELECT * " +
					" FROM TypeFigure " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("TypesFigures handled: " + (i-1));}
				
				Integer typeFigureId = rs.getInt("typeFigureId");
				Integer typeDesignationFk = rs.getInt("typeDesignationFk");
				Integer collectionFk = rs.getInt("collectionFk");
				String filename = rs.getString("filename");
				String figurePhrase = rs.getString("figurePhrase");
				
				String mimeType = null; //"image/jpg";
				String suffix = null; //"jpg";
				Media media = ImageFile.NewMediaInstance(null, null, filename, mimeType, suffix, null, null, null);
				if (figurePhrase != null) {
					media.addAnnotation(Annotation.NewDefaultLanguageInstance(figurePhrase));
				}
				Specimen typeSpecimen = typeMap.get(typeDesignationFk);
				if (typeSpecimen != null) {
					typeSpecimen.addMedia(media);
				}
				
				//mimeType + suffix
				//TODO
				//RefFk
				//RefDetail
				//VerifiedBy
				//VerifiedWhen
				//PrefFigureFlag
				//PublishedFlag
				//etc.
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
			
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTypes();
	}

}
