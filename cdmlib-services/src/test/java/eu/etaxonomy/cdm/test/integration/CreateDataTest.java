
package eu.etaxonomy.cdm.test.integration;


import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class CreateDataTest {
	private static Logger logger = Logger.getLogger(CreateDataTest.class);

	private static boolean isCreated;
	private CdmApplicationController app;
	private static final String genusUuid = "c399e245-3def-427d-8502-afa0ae87e875";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.info("setUpBeforeClass");
		isCreated = false;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		if (isCreated == false){
			 dbSchemaValidation = DbSchemaValidation.CREATE;
		}
		ICdmDataSource dataSource = paddie();
		app  = CdmApplicationController.NewInstance(dataSource, dbSchemaValidation);
	}

	@After
	public void tearDown() throws Exception {
		isCreated = true;
		app.close();
	}
	
	//just temporarly
	public static ICdmDataSource cdm_test(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	//just temporarly
	public static ICdmDataSource paddie(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
		String cdmServer = "PADDIE";
		String cdmDB = "edit_test"; 
		String cdmUserName = "andreas";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static ICdmDataSource makeDestination(String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		try {
			if (pwd == null){
				pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(cdmUserName) + ": ");
			}
			//TODO not MySQL
			//ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
			ICdmDataSource destination = CdmDataSource.NewSqlServer2005Instance(cdmServer, cdmDB, cdmUserName, pwd);
			return destination;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
/* ********************* TESTS *********************************/
	
	@Test
	public void testCreateTaxon(){
		//Taxon with childs, basionym, childrens synonyms, child misapplied Name
		Taxon genusTaxon = eu.etaxonomy.cdm.datagenerator.TaxonGenerator.getTestTaxon();
		genusTaxon.setUuid(UUID.fromString(genusUuid));
		app.getTaxonService().saveTaxon(genusTaxon);
	}
	
	@Test
	public void testLoadTaxon(){
		//Taxon with childs, basionym, childrens synonyms, child misapplied Name
		
		//taxon
		Taxon genusTaxon = (Taxon)app.getTaxonService().getTaxonByUuid(UUID.fromString(genusUuid));
		assertNotNull(genusTaxon);
		//name
		BotanicalName genusName = (BotanicalName)genusTaxon.getName();
		assertNotNull(genusName);
		
		//taxonBases of Name
		Set<TaxonBase> taxonBases = genusName.getTaxonBases();
		logger.warn(taxonBases.size());
		Set<Taxon> children = genusTaxon.getTaxonomicChildren();
		for (Taxon child : children){
			child.getSynonyms();
			child.getMisappliedNames();
			child.getHomotypicGroup();
			child.getHomotypicSynonyms();
		}
		
		Set<TaxonDescription> descriptions = genusTaxon.getDescriptions();
		assertEquals(2, descriptions.size());
		TaxonDescription description = descriptions.iterator().next();
		
		
		Set<DescriptionElementBase> descriptionElements = description.getElements();
		
		Language language = Language.DEFAULT(); 
		for (DescriptionElementBase descriptionElement : descriptionElements){
			if (descriptionElement instanceof TextData){
				TextData textData = (TextData)descriptionElement;
				textData.getText(language);
			}else if(descriptionElement instanceof CommonTaxonName){
				CommonTaxonName commonTaxonName = (CommonTaxonName)descriptionElement;
				commonTaxonName.getName();
				commonTaxonName.getLanguage();
			}else{
				fail();
			}
		}
		
	}
	
	
	@Test
	public void testSave(){
		logger.warn("testSave");
		ITaxonService taxonService = app.getTaxonService();
		Taxon genusTaxon = (Taxon)taxonService.getTaxonByUuid(UUID.fromString(genusUuid));
		BotanicalName genusName = (BotanicalName)genusTaxon.getName();
		genusName.setGenusOrUninomial("newGenusUninomial");
		genusName.setUpdated(Calendar.getInstance());
		BotanicalName newName = BotanicalName.NewInstance(Rank.SPECIES());
		Taxon newTaxon = Taxon.NewInstance(newName, genusTaxon.getSec());
		genusTaxon.addTaxonomicChild(newTaxon, null, "5677");
		UUID uuid = taxonService.saveTaxon(newTaxon);
		assertNotNull(uuid);
	}

	
}
