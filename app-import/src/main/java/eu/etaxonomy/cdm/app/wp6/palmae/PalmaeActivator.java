/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.io.File;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.app.tcs.TcsSources;
import eu.etaxonomy.cdm.app.wp6.palmae.config.PalmaeProtologueImportConfigurator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.PalmaeImageImport;
import eu.etaxonomy.cdm.io.PalmaeProtologueImport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class PalmaeActivator {
	private static final Logger logger = Logger.getLogger(PalmaeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final String tcsSource = TcsSources.arecaceae_local();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();

	static final UUID featureTreeUuid = UUID.fromString("72ccce05-7cc8-4dab-8e47-bf3f5fd848a0");
		
	static final UUID treeUuid = UUID.fromString("1adb71d4-cce6-45e1-b578-e668778d9ec6");
	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");
	static final String sourceSecId = "palm_pub_ed_999999";
	static final boolean pubishReferencesInBibliography = false;
	
	//should the other imports run as well?
	static final boolean includeTaxonX = true;
	static final boolean includeImages = true;
	static final boolean includeExcelProtologue = true;
	static final boolean includeMediaProtologue = true;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	
	static boolean useTaxonomicTree = true;
	
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;

	
	private boolean doImport(){
		boolean success = true;
		System.out.println("Start import from Tcs("+ tcsSource.toString() + ") ...");
		
		//make BerlinModel Source
		String source = tcsSource;
		ICdmDataSource destination = cdmDestination;
		
		TcsRdfImportConfigurator tcsImportConfigurator = TcsRdfImportConfigurator.NewInstance(source,  destination);
		
		tcsImportConfigurator.setTreeUuid(treeUuid);
		tcsImportConfigurator.setSecUuid(secUuid);
		tcsImportConfigurator.setSourceSecId(sourceSecId);
		
		tcsImportConfigurator.setDoAuthors(doAuthors);
		tcsImportConfigurator.setDoReferences(doReferences);
		tcsImportConfigurator.setDoTaxonNames(doTaxonNames);
		tcsImportConfigurator.setDoRelNames(doRelNames);
		
		tcsImportConfigurator.setDoTaxa(doTaxa);
		tcsImportConfigurator.setDoRelTaxa(doRelTaxa);
		tcsImportConfigurator.setDoFacts(doFacts);
		tcsImportConfigurator.setUseTaxonomicTree(useTaxonomicTree);
		tcsImportConfigurator.setPublishReferences(pubishReferencesInBibliography);
		
		tcsImportConfigurator.setCheck(check);
		tcsImportConfigurator.setDbSchemaValidation(hbm2dll);

		// invoke import
		CdmDefaultImport<TcsRdfImportConfigurator> tcsImport = new CdmDefaultImport<TcsRdfImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		success &= tcsImport.invoke(tcsImportConfigurator);
		
		//make feature tree
		logger.info("Make feature tree");
		CdmApplicationController app = tcsImport.getCdmAppController();
		
		FeatureTree tree = getFeatureTree();
		app.getDescriptionService().saveFeatureTree(tree);
		System.out.println("End import from TCS ("+ source.toString() + ")...");
		
		return success;
	}
	
	
	private FeatureTree getFeatureTree(){
		
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		FeatureNode root = result.getRoot();
		
		FeatureNode newNode;
		newNode = FeatureNode.NewInstance(Feature.INTRODUCTION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.BIOLOGY_ECOLOGY());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.CONSERVATION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.COMMON_NAME());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.ETYMOLOGY());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.USES());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.CULTIVATION());
		root.addChild(newNode);
		// "anatomy", "fossil record" "relationships"
		//for future
		newNode = FeatureNode.NewInstance(Feature.DISCUSSION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.DIAGNOSIS());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.DESCRIPTION());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.MATERIALS_EXAMINED());
		root.addChild(newNode);
		newNode = FeatureNode.NewInstance(Feature.ANATOMY());
		root.addChild(newNode);
		return result;
		

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean success = true;
		
		logger.debug("start");
		
		PalmaeActivator me = new PalmaeActivator();
		me.doImport();
		
		if (includeImages){
			System.out.println("Start importing images ...");
			CdmDefaultImport<IImportConfigurator> imageImporter = new CdmDefaultImport<IImportConfigurator>();
			ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(
					PalmaeImageActivator.sourceFolder, cdmDestination, PalmaeImageImport.class);
			imageConfigurator.setSecUuid(secUuid);
			success &= imageImporter.invoke(imageConfigurator);
			System.out.println("End importing images ...");
		}

		if (includeExcelProtologue){
			System.out.println("Start importing protologues ...");
			ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(
					PalmaeExcelProtologueActivator.sourceFile, cdmDestination, PalmaeProtologueImport.class);
			imageConfigurator.setSecUuid(secUuid);
			
			CdmDefaultImport<IImportConfigurator> imageImporter = new CdmDefaultImport<IImportConfigurator>();
			imageImporter.invoke(imageConfigurator);
			System.out.println("End importing protologues ...");
		}
		if (includeMediaProtologue){
			System.out.println("Start importing protologues from \\\\media...");
			String protologueSource = "\\\\media\\EditWP6\\palmae\\protologe";
			String urlString = "http://wp5.e-taxonomy.eu/media/palmae/protologe/";
			File source = new File (protologueSource);

			PalmaeProtologueImportConfigurator protologConfig = PalmaeProtologueImportConfigurator.NewInstance(protologueSource, cdmDestination, urlString);
			CdmDefaultImport<IImportConfigurator> cdmImport = new CdmDefaultImport<IImportConfigurator>();
			
			//protologConfig.setDoFacts(doDescriptions);
			protologConfig.setCheck(check);
			protologConfig.setDbSchemaValidation(DbSchemaValidation.UPDATE);

			protologConfig.setSource(source);
			success &= cdmImport.invoke(protologConfig);

			System.out.println("End importing protologues ...");
		}
		
		if (includeTaxonX){
			System.out.println("Start importing taxonX ...");
			PalmaeTaxonXImportActivator taxonXimporter = new PalmaeTaxonXImportActivator();
			PalmaeTaxonXImportActivator.cdmDestination = cdmDestination;
			success &= taxonXimporter.runImport();
			System.out.println("End importing taxonX ...");
		}
		
		String strSuccess = "";
		if (success == false){
			strSuccess = "not ";
		}
		System.out.println("Import " + strSuccess + "successful");
		
	}
	
}
