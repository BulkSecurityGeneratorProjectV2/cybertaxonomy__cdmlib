package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	private boolean deleteAll = false;
	
	//BerlinModelDB
	private Source source;
	
	//CdmApplication
	private CdmApplicationController cdmApp;
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 100;

	
	//Hashmaps for Joins
	private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonNameMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonMap = new HashMap<Integer, UUID>();


	/**
	 * Executes the whole 
	 */
	public boolean doImport(Source source, CdmApplicationController cdmApp){
		if (source == null || cdmApp == null){
			throw new NullPointerException("Source and CdmApplicationController must not be null");
		}
		this.source = source;
		this.cdmApp = cdmApp;

		//make and save Authors
		makeAuthors();
		
		//make and save References
		if (! makeReferences()){
			return false;
		}
		
		//make and save Names
		makeTaxonNames();
		
		//make and save Taxa
		makeTaxa();
		
		//make and save Facts
		makeRelTaxa();
		
		//make and save Facts
		makeFacts();
		
		if (false){
			//make and save publications
/*				makePublications(root);
				saveToXml(root.getChild("Publications", nsTcs), outputPath, outputFileName + "Publications", format);
				
				saveToXml(root.getChild("TaxonNames", nsTcs), outputPath, outputFileName + "_TaxonNames", format);
				
				//make and save Concepts
				makeConcepts(root);
				saveToXml(root.getChild("TaxonConcepts", nsTcs), outputPath, outputFileName + "_TaxonConcepts", format);
*/		}
		return true;
	}
	
	
	
	/**
	 * @return
	 */
	private boolean makeAuthors(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeAuthors ...");
		logger.warn("Authors not yet implemented !!");

		IAgentService agentService = cdmApp.getAgentService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
//		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM AuthorTeam " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			
			
			
			
			
			logger.info("end makeAuthors ...");
			return true;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
	}
	
	
	/**
	 * @return
	 */
	private boolean makeReferences(){
		String dbAttrName;
		String cdmAttrName;
		boolean success = true;
		
		logger.info("start makeReferences ...");
		IReferenceService referenceService = cdmApp.getReferenceService();
		boolean delete = deleteAll;

//		if (delete){
//			List<TaxonNameBase> listAllReferences =  referenceService.getAllReferences(0, 1000);
//			while(listAllReferences.size() > 0 ){
//				for (TaxonNameBase name : listAllReferences ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllReferences =  referenceService.getAllReferences(0, 1000);
//			}			
//		}
		try {
			
			
			//get data from database
			String strQuery = 
					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk " +
                    " FROM Reference AS InInReference " +
                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk ";
			
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("References handled: " + (i-1));}
				
				//create TaxonName element
				int refId = rs.getInt("refId");
				int categoryFk = rs.getInt("refCategoryFk");
				int inRefFk = rs.getInt("inRefFk");
				int inRefCategoryFk = rs.getInt("InRefCategoryFk");
				
				StrictReferenceBase ref;
				try {
					logger.debug("RefCategoryFk: " + categoryFk);
					
					if (categoryFk == REF_JOURNAL){
						ref = new Journal();
					}else if(categoryFk == REF_BOOK){
						ref = new Book();
					}else if(categoryFk == REF_ARTICLE){
						ref = new Article();
					}else if(categoryFk == REF_DATABASE){
						ref = new Database();
					}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
						if (inRefCategoryFk == REF_BOOK){
							//TODO
							ref = new BookSection();
						}else{
							logger.warn("Reference type of part-of-reference not recognized");
							ref = new Generic();
						}
					}else if(categoryFk == REF_UNKNOWN){
						ref = new Generic();
					}else{
						ref = new Generic();	
					}
					
					
					dbAttrName = "refCache";
					cdmAttrName = "titleCache";
					//TODO wohin kommt der refCache
					//INomenclaturalReference hat nur getNomenclaturalCitation , müsste es nicht so was wie setAbbrevTitle geben? 
					success &= ImportHelper.addStringValue(rs, ref, dbAttrName, cdmAttrName);
					
					dbAttrName = "nomRefCache";
					cdmAttrName = "titleCache";
					success &= ImportHelper.addStringValue(rs, ref, dbAttrName, cdmAttrName);
					
//					dbAttrName = "BinomHybFlag";
//					cdmAttrName = "isBinomHybrid";
//					ImportHelper.addBooleanValue(rs, ref, dbAttrName, cdmAttrName);
					
					//TODO
					// all attributes
					
					
					UUID refUuid = referenceService.saveReference(ref);
					//Session sess = new Session().
					referenceMap.put(refId, refUuid);
					
				} catch (Exception e) {
					logger.warn("Reference with id " + refId +  " threw Exception and could not be saved");
					e.printStackTrace();
					success = false;
					return success;
				}
				
			}	
				

			logger.info("end makeReferences ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}

	
	
	/**
	 * @return
	 */
	private boolean makeTaxonNames(){
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		boolean delete = deleteAll;
		
		if (delete){
			List<TaxonNameBase> listAllNames =  nameService.getAllNames(0, 1000);
			while(listAllNames.size() > 0 ){
				for (TaxonNameBase name : listAllNames ){
					//FIXME
					//nameService.remove(name);
				}
				listAllNames =  nameService.getAllNames(0, 1000);
			}
		}
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT TOP 102   Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    " Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk"; 
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				
				try {
					logger.info(rankId);
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					//FIXME
					//BotanicalName name = BotanicalName.NewInstance(BerlinModelTransformer.rankId2Rank(rankId));
					BotanicalName botanicalName = new BotanicalName(rank);
					
					if (rankId < 40){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "genusOrUninomial";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					

					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "preliminaryFlag";
					cdmAttrName = "XX" + "protectedTitleCache";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "HybridFormulaFlag";
					cdmAttrName = "isHybridFormula";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "MonomHybFlag";
					cdmAttrName = "isMonomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "BinomHybFlag";
					cdmAttrName = "isBinomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "TrinomHybFlag";
					cdmAttrName = "isTrinomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					//botanicalName.s

//					dbAttrName = "notes";
//					cdmAttrName = "isTrinomHybrid";
//					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					//TODO
					//Created
					//Note
					//makeAuthorTeams
					//CultivarGroupName
					//CultivarName
					//Source_Acc
					//OrthoProjection
					
					//Details
					
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalMicroReference";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);

					//TODO
					//preliminaryFlag
					
					
					UUID nameUuid = nameService.saveTaxonName(botanicalName);
					taxonNameMap.put(nameId, nameUuid);
					
				} catch (UnknownRankException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
					success = false; 
				}
				
			}	
				
//				//id
//				strDbAttr = "NameId";
//					//save id  for later
//				int intID = rs.getInt(strDbAttr);
//				
//				String nameId = rs.getString(strDbAttr);
//				
//				//add TaxonName to nameMap
//				nameMap.put(nameId, elTaxonName);
//				
//				
//				strValue = nameId;
//				strAttrName = "id";
//				parent = elTaxonName;
//				xml.addStringAttribute(strValue, parent, strAttrName, NS_NULL);
//				
//				//Code
//				strAttrName = "nomenclaturalCode";
//				strValue = "Botanical";
//				parent = elTaxonName;
//				xml.addStringAttribute(strValue, parent,  strAttrName, NS_NULL);
//				
//				//Simple
//				strDbAttr = "FullNameCache";
//				strElName = "Simple";
//				parent = elTaxonName;
//				xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//				
//				if (fullVersion){
//					//Rank
//					strDbAttr = "RankAbbrev";
//					strElName = "Rank";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//					
//					//CanonicalName
//					parent = elTaxonName;
//					makeCanonicalName(rs, parent);
//					
//					
//					//CanonicalAuthorship
//					parent = elTaxonName;
//					makeCanonicalAuthorship(rs, parent);
//				
//				}  //fi fullVersion
//				
//				//PublishedIn
//				strDbAttr = "NomRefFk";
//				strAttrName = "ref";
//				strElName = "PublishedIn";
//				parent = elTaxonName;
//				Attribute attrPublRef = xml.addAttributeInElement(rs, strDbAttr, parent, strAttrName, strElName, nsTcs, FACULTATIVE);
//				
//				if (attrPublRef != null){
//					//does Publication exist?
//					String ref = attrPublRef.getValue();
//					if (! publicationMap.containsKey(ref)){
//						logger.error("PublishedIn ref " + ref + " for " + nameId + " does not exist.");
//					}
//				}
//				
//				
//				if (fullVersion){
//					//Year
//					String year = rs.getString("RefYear");
//					if (year == null) {
//						year = rs.getString("HigherRefYear");
//					}
//					strValue = year;
//					strElName = "Year";
//					parent = elTaxonName;
//					xml.addStringElement(strValue, parent, strElName, nsTcs, FACULTATIVE);
//					
//					//MicroReference
//					strDbAttr = "Details";
//					strElName = "MicroReference";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, FACULTATIVE);
//
//				}//fi fullversion
//			}//while
//			
//			//insert related Names (Basionyms, ReplacedSyns, etc.
//			makeSpellingCorrections(nameMap);
//			makeBasionyms(nameMap);
//			makeLaterHomonyms(nameMap);
//			makeReplacedNames(nameMap);
//			
//			//insert Status infos
//			makeNameSpecificData(nameMap);
			//cdmApp.flush();
			logger.info("end makeTaxonNames ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	
	/**
	 * @return
	 */
	private boolean makeTaxa(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxa ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		INameService nameService = cdmApp.getNameService();
		IReferenceService referenceService = cdmApp.getReferenceService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<TaxonBase> listAllTaxa =  taxonService.getAllTaxa(0, 1000);
//			while(listAllTaxa.size() > 0 ){
//				for (TaxonBase taxon : listAllTaxa ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllTaxa =  taxonService.getAllTaxa(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM PTaxon " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int taxonId = rs.getInt("taxonId");
				int statusFk = rs.getInt("statusFk");
				
				int nameFk = rs.getInt("nameFk");
				int refFk = rs.getInt("refFk");
				
				TaxonNameBase taxonName;
				UUID nameUuid = taxonNameMap.get(nameFk);
				if (nameUuid == null){
					taxonName = null;
				}else{
					taxonName  = nameService.getTaxonNameByUuid(nameUuid);
				}
				
				
				ReferenceBase reference;
				UUID refUuid = referenceMap.get(refFk);
				if (refUuid == null){
					reference = null;
				}else{
					reference  = referenceService.getReferenceByUuid(refUuid);
				}
				
				TaxonBase taxonBase;
				Synonym synonym;
				Taxon taxon;
				try {
					logger.info(statusFk);
					if (statusFk == 1){
						taxon = Taxon.NewInstance(taxonName, reference);
						taxonBase = taxon;
					}else if (statusFk == 2){
						synonym = Synonym.NewInstance(taxonName, reference);
						taxonBase = synonym;
					}else{
						synonym = Synonym.NewInstance(taxonName, reference);
						taxonBase = synonym;
					}
					
					dbAttrName = "xxx";
					cdmAttrName = "yyy";
					ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "isDoubtful";
					cdmAttrName = "isDoubtful";
					ImportHelper.addBooleanValue(rs, taxonBase, dbAttrName, cdmAttrName);


					//TODO
					//Created
					//Note
					//ALL
					
					UUID taxonUuid = taxonService.saveTaxon(taxonBase);
					taxonMap.put(taxonId, taxonUuid);
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon with id " + taxonId + ". Taxon could not be saved.");
				}
				
			}	
			
			logger.info("end makeTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	

	/**
	 * @return
	 */
	private boolean makeRelTaxa(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		logger.warn("RelTaxa not yet implemented !!");

		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM RelPTaxon Join Taxon1 Join Taxon2" ;
			ResultSet rs = source.getResultSet(strQuery) ;
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("RelPTaxa handled: " + (i-1));}
				
				
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				int factId = rs.getInt("factId");
				int relTypeFk = rs.getInt("relTypeFk");
				
				TaxonBase taxon1 = getTaxonById(taxon1Id, taxonService);
				TaxonBase taxon2 = getTaxonById(taxon2Id, taxonService);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				
				if (relTypeFk == IS_INCLUDED_IN){
					((Taxon)taxon2).addTaxonomicChild((Taxon)taxon1, citation, microcitation);
				}else if (relTypeFk == IS_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.SYNONYM_OF());
				}else if (relTypeFk == IS_HOMOTYPIC_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
				}else if (relTypeFk == IS_HETEROTYPIC_SYNONYM_OF){
					((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
				}else if (relTypeFk == IS_MISAPPLIED_NAME_OF){
					((Taxon)taxon2).addMisappliedName((Taxon)taxon1, citation, microcitation);
				}else {
					//TODO
					logger.warn("TaxonRelationShipType " + relTypeFk + " not yet implemented");
				}
				
				
			//....
			
			
			
			}
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	private TaxonBase getTaxonById(int id, IService<TaxonBase> service){
		TaxonBase result;
		UUID uuid = taxonMap.get(id);
		if (uuid == null){
			result = null;
		}else{
			result  = ((ITaxonService)service).getTaxonByUuid(uuid); //.getCdmObjectByUuid(uuid);//  taxonService.getTaxonByUuid(taxonUuid);
		}
		return result;
	
	}

	/**
	 * @return
	 */
	private boolean makeFacts(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeFacts ...");
		logger.warn("Facts not yet implemented !!");

		//IAgentService agentService = cdmApp.getAgentService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<Agent> listAllAgents =  agentService.getAllAgents(0, 1000);
//			while(listAllAgents.size() > 0 ){
//				for (Agent name : listAllAgents ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllAgents =  agentService.getAllAgents(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM Facts " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
				
				//create TaxonName element
				int factId = rs.getInt("factId");

			//....
			
			
			
			}
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	

	
	


}
