package eu.etaxonomy.cdm.io.tcsxml;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public class TcsXmlTaxonNameRelationsIO extends TcsXmlIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonNameRelationsIO.class);

	private static int modCount = 5000;
	
	public TcsXmlTaxonNameRelationsIO(){
		super();
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		
		logger.info("start make taxon name relations ...");
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		INameService nameService = config.getCdmAppController().getNameService();

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element elDataSet = super. getDataSetElement(tcsConfig);
		Namespace tcsNamespace = tcsConfig.getTcsXmlNamespace();
		
		DoubleResult<Element, Boolean> doubleResult;
		childName = "TaxonNames";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "TaxonName";
		List<Element> elTaxonNameList = elTaxonNames.getChildren(tcsElementName, tcsNamespace);

//		Element source = tcsConfig.getSourceRoot();
		
		int i = 0;
		int nameRelCount = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNameList){
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}

			//Basionyms
			tcsElementName = "Basionym";
			List<Element> elBasionymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			
			for (Element elBasionym: elBasionymList){
				nameRelCount++;
				logger.debug("BASIONYM "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.BASIONYM();
				boolean inverse = false;
				
				String id = elTaxonName.getAttributeValue("id");
//				TaxonNameBase<?,?> fromName = taxonNameMap.get(id);
				
				makeNomenclaturalNoteType(tcsConfig, elBasionym, relType, taxonNameMap, nameStore, id, inverse);
			}// end Basionyms
			
			//SpellingCorrections
			tcsElementName = "SpellingCorrectionOf";
			List<Element> elSpellingCorrectionList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			
			for (Element elSpellingCorrection: elSpellingCorrectionList){
				nameRelCount++;
				logger.debug("SpellingCorrectionOf "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.ORTHOGRAPHIC_VARIANT();
				boolean inverse = true;
				
				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(tcsConfig, elSpellingCorrection, relType, taxonNameMap, nameStore, id, inverse);
			}// end SpellingCorrections
			
			//LaterHomonymOf
			tcsElementName = "LaterHomonymOf";
			List<Element> elLaterHomonymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elLaterHomonym: elLaterHomonymList){
				nameRelCount++;
				logger.debug("LaterHomonymOf "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.LATER_HOMONYM();
				boolean inverse = false;
				
				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(tcsConfig, elLaterHomonym, relType, taxonNameMap, nameStore, id, inverse);
			}// end LaterHomonymOf
			
			//ReplacementNameFor
			tcsElementName = "ReplacementNameFor";
			List<Element> elReplacementNameForList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elReplacementNameFor: elReplacementNameForList){
				nameRelCount++;
				logger.debug("LaterHomonymOf "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.REPLACED_SYNONYM();
				boolean inverse = false;
				
				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(tcsConfig, elReplacementNameFor, relType, taxonNameMap, nameStore, id, inverse);
			}// end ReplacementNameFor
			
			//ConservedAgainst
			tcsElementName = "ConservedAgainst";
			List<Element> elConservedAgainstList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elConservedAgainst: elConservedAgainstList){
				nameRelCount++;
				logger.debug("ConservedAgainst "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.CONSERVED_AGAINST();
				boolean inverse = false;
				
				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(tcsConfig, elConservedAgainst, relType, taxonNameMap, nameStore, id, inverse);
			}// end ConservedAgainst

			
			
			//Sanctioned
			tcsElementName = "Sanctioned";
			List<Element> elSanctionedList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elSanctioned: elSanctionedList){
				
				//nameRelCount++;
				//TODO sanctioned
				logger.warn("Sanctioned not yet implemented " );
				
				///NameRelationshipType relType = NameRelationshipType.XXX
				//boolean inverse = false;
				//
				//String id = elTaxonName.getAttributeValue("id");
				//makeNomenclaturalNoteType(tcsConfig, elSanctioned, relType, taxonNameMap, nameStore, id, inverse);
			}// end Sanctioned
			
			//PublicationStatus
			tcsElementName = "PublicationStatus";
			List<Element> elPublicationStatusList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elPublicationStatus: elPublicationStatusList){
				
				//nameRelCount++;
				//TODO PublicationStatus
				logger.warn("PublicationStatus not yet implemented " );
				
				///NameRelationshipType relType = NameRelationshipType.XXX
				//boolean inverse = false;
				//
				//String id = elTaxonName.getAttributeValue("id");
				//makeNomenclaturalNoteType(tcsConfig, elPublicationStatus, relType, taxonNameMap, nameStore, id, inverse);
			}// end PublicationStatus
			
			//BasedOn
			tcsElementName = "BasedOn";
			List<Element> elBasedOnList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elBasedOn: elBasedOnList){
				
				//nameRelCount++;
				//TODO BasedOn
				logger.warn("BasedOn not yet implemented " );
				
				///NameRelationshipType relType = NameRelationshipType.XXX
				//boolean inverse = false;
				//
				//String id = elTaxonName.getAttributeValue("id");
				//makeNomenclaturalNoteType(tcsConfig, elBasedOn, relType, taxonNameMap, nameStore, id, inverse);
			}// end BasedOn
			
			
			
			
		}	
		//Other Relations
		//TODO
		
		logger.info(nameRelCount + " nameRelations handled");
		nameService.saveTaxonNameAll(nameStore);
		logger.info("end make taxon name relationships ...");
		return success.getValue();
	}
	
	private  boolean makeNomenclaturalNoteType(TcsXmlImportConfigurator tcsConfig, Element elRelation, NameRelationshipType relType, MapWrapper<TaxonNameBase<?,?>> taxonNameMap, Set<TaxonNameBase> nameStore, String id, boolean inverse){
		if (elRelation == null){
			return false;
		}
		Namespace ns = elRelation.getNamespace();
		
		String ruleConsidered = elRelation.getChildText("RuleConsidered", ns);
		String note = elRelation.getChildText("Note", ns);
		String microReference = elRelation.getChildText("MicroReference", ns);
		Element elRelatedName = elRelation.getChild("RelatedName", ns);
		//TODO relType
		String relatedNameId = elRelatedName.getAttributeValue("ref");
		
		TaxonNameBase<?,?> fromName = taxonNameMap.get(id);
		TaxonNameBase<?,?> toName = taxonNameMap.get(relatedNameId);
		if (fromName == null){
			logger.warn("fromName (" + id + ") not found in Map! Relationship not set!");
			return false;
		}
		if (toName == null){
			logger.warn("toName (" + id + ") not found in Map! Relationship not set!");
			return false;
		}

		
		//TODO note, microreference
		if (inverse == false){
			toName.addRelationshipToName(fromName, relType, ruleConsidered);
		}else{
			fromName.addRelationshipToName(toName, relType, ruleConsidered);
		}
		nameStore.add(fromName);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoRelNames();
	}

}
