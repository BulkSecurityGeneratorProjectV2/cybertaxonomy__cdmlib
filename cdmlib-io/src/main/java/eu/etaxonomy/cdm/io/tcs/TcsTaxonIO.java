/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class TcsTaxonIO  extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonIO.class);

	private static int modCount = 30000;
	private static final String ioNameLocal = "TcsTaxonIO";
	
	public TcsTaxonIO(boolean ignore){
		super(ioNameLocal, ignore);
	}
	
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		String cdmAttrName;
		String value;

		logger.info("start makeTaxa ...");
		
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element root = tcsConfig.getSourceRoot();
		boolean success =true;
		INameService nameService = cdmApp.getNameService();
		
		String prefix;
		Namespace rdfNamespace = root.getNamespace();
		prefix = "tc";
		Namespace taxonConceptNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		Namespace commonNamespace = root.getNamespace(prefix);
		prefix = "tgeo";
		Namespace geoNamespace = root.getNamespace(prefix);

		
		xmlElementName = "TaxonConcept";
		elementNamespace = taxonConceptNamespace;
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		ITaxonService taxonService = cdmApp.getTaxonService();
		
		int i = 0;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			
			//
			String taxonAbout = elTaxonConcept.getAttributeValue("about", rdfNamespace);
			
			//hasName
			xmlElementName = "hasName";
			elementNamespace = taxonConceptNamespace;
			xmlAttributeName = "resource";
			attributeNamespace = rdfNamespace;
			String strNameResource= XmlHelp.getChildAttributeValue(elTaxonConcept, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
			TaxonNameBase taxonNameBase = taxonNameMap.get(strNameResource);
				
			//accordingTo
			xmlElementName = "accordingTo";
			elementNamespace = taxonConceptNamespace;
			String strAccordingTo = elTaxonConcept.getChildTextTrim(xmlElementName, elementNamespace);
			//FIXME
			Generic sec = Generic.NewInstance();
			sec.setTitleCache(strAccordingTo);
			
			//FIXME or synonym
			TaxonBase taxonBase;
			if (hasIsSynonymRelation(elTaxonConcept, rdfNamespace)){
				taxonBase = Synonym.NewInstance(taxonNameBase, sec);
				List<DescriptionElementBase> geo = makeGeo(elTaxonConcept, geoNamespace, rdfNamespace);
				if (geo.size() > 0){
					logger.warn("Synonym (" + taxonAbout + ") has geo description!");
				}
			}else{
				Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
				List<DescriptionElementBase> geoList = makeGeo(elTaxonConcept, geoNamespace, rdfNamespace);
				TaxonDescription description = TaxonDescription.NewInstance(taxon);
				for (DescriptionElementBase geo: geoList){
					description.addElement(geo);
				}
				taxon.addDescription(description);
				taxonBase = taxon;
			}
			
			//primary
//			xmlElementName = "primary";
//			elementNamespace = taxonConceptNamespace;
//			cdmAttrName = "isPrimary";
//			Boolean primary = ImportHelper.addXmlBooleanValue(elTaxonConcept, taxon, xmlElementName, elementNamespace, cdmAttrName);
			
			taxonMap.put(taxonAbout, taxonBase);
			
		}
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info("saving taxa ...");
		taxonService.saveTaxonAll(taxonMap.objects());
		logger.info("end makeTaxa ...");
		return success;
	}
	
	
	private boolean hasIsSynonymRelation(Element taxonConcept, Namespace rdfNamespace){
		boolean result = false;
		if (taxonConcept == null || ! "TaxonConcept".equalsIgnoreCase(taxonConcept.getName()) ){
			return false;
		}
		
		String elName = "relationshipCategory";
		Filter filter = new ElementFilter(elName, taxonConcept.getNamespace());
		Iterator<Element> relationshipCategories = taxonConcept.getDescendants(filter);
		while (relationshipCategories.hasNext()){
			Element relationshipCategory = relationshipCategories.next();
			Attribute resource = relationshipCategory.getAttribute("resource", rdfNamespace);
			String isSynonymFor = "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsSynonymFor";
			if (resource != null && isSynonymFor.equalsIgnoreCase(resource.getValue()) ){
				return true;
			}
		}
		return result;
	}
	
	private List<DescriptionElementBase> makeGeo(Element elConcept, Namespace geoNamespace, Namespace rdfNamespace){
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		String xmlElementName = "code";
		List<Element> elGeos = elConcept.getChildren(xmlElementName, geoNamespace);

		int i = 0;
		//for each geoTag
		for (Element elGeo : elGeos){
			//if ((i++ % modCount) == 0){ logger.info("Geocodes handled: " + (i-1));}
			
			String strGeoRegion = elGeo.getAttributeValue("resource", rdfNamespace);
			strGeoRegion = strGeoRegion.replace("http://rs.tdwg.org/ontology/voc/GeographicRegion#", "");
			NamedArea namedArea = TdwgArea.getAreaByTdwgLabel(strGeoRegion);
			PresenceAbsenceTermBase status = null;
			DescriptionElementBase distribution = Distribution.NewInstance(namedArea, status);
			distribution.setFeature(Feature.DISTRIBUTION());
			//System.out.println(namedArea);
			
			result.add(distribution);
		}
		return result;
	}
	
}
