package eu.etaxonomy.cdm.io.tcs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public class TcsTaxonNameRelationsIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonNameRelationsIO.class);

	private static int modCount = 5000;
	
	public TcsTaxonNameRelationsIO(){
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
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element source = tcsConfig.getSourceRoot();
		
		logger.info("start makeNameRelationships ...");
		INameService nameService = cdmApp.getNameService();

//		<tn:hasBasionym rdf:resource="palm_tn_14530"/>
		
		Element root = tcsConfig.getSourceRoot();
		boolean success =true;
		
		Namespace rdfNamespace = tcsConfig.getRdfNamespace();
		Namespace taxonNameNamespace = tcsConfig.getTnNamespace();
		
		List<Element> elTaxonNames = root.getChildren("TaxonName", taxonNameNamespace);
		
		int i = 0;
		int nameRelCount = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNames){
			
			TaxonNameBase fromName = null;
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			//Basionyms
			tcsElementName = "hasBasionym";
			tcsNamespace = taxonNameNamespace;
			List<Element> elBasionymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			
			for (Element elBasionym: elBasionymList){
				nameRelCount++;
				logger.debug("BASIONYM "+  nameRelCount);
				tcsElementName = "resource";
				tcsNamespace = rdfNamespace;
				Attribute attrResource = elBasionym.getAttribute(tcsElementName, tcsNamespace);
				if (attrResource == null){
					logger.warn("Basionym rdf:resource is missing ! Basionym not set!");
					continue;
				}
				String basionymId = attrResource.getValue();
				TaxonNameBase basionym = taxonNameMap.get(basionymId);
				if (basionym == null){
					logger.warn("Basionym name ("+basionymId+") not found in Map! Basionym not set!");
					continue;
				}
				if (fromName == null){
					Attribute about = elTaxonName.getAttribute("about", rdfNamespace);
					if (about != null){
						fromName = taxonNameMap.get(about.getValue() );
					}
					if (fromName == null){
						logger.warn("From name ("+about+") not found in Map! Basionym not set!");
						continue;
					}
				}
				String ruleConcidered = null; //TODO
				fromName.addBasionym(basionym, ruleConcidered);
				nameStore.add(fromName);

			}
		}// end Basionyms
		
		//Other Relations
		//TODO
		
		logger.info(nameRelCount + " nameRelations handled");
		nameService.saveTaxonNameAll(nameStore);
		logger.info("end makeNameRelationships ...");
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoRelNames();
	}

	public boolean invoke(IImportConfigurator config, Map stores) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean invoke(IImportConfigurator config) {
		// TODO Auto-generated method stub
		return false;
	}
}
