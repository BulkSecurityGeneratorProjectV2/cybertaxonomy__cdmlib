/* just for testing */

package eu.etaxonomy.cdm.test.function;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestDatabase {
	private static final Logger logger = Logger.getLogger(TestDatabase.class);
	
	private void test(){
		System.out.println("Start TestDatabase");
		//testNewDatabaseConnection();
		//testFacts();
//		testNewDatasourceClass();
		testHybridRelationships();
	//	testPaddie();
		System.out.println("\nEnd TestDatabase");
	}
	
	public void testNewDatabaseConnection(){
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(DbSchemaValidation.CREATE);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	public void testNewDatasourceClass(){
		try {
//			String server = "192.168.2.10";
//			String database = "cdm_test_andreasM";
//			String username = "edit";
//			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;

//			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
			ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance("CDM", "sa", "");
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = BotanicalName.NewInstance(genus);
			botanicalName.setGenusOrUninomial("GenusName");
		
			Journal journal = Journal.NewInstance();
			journal.setTitleCache("Afro+Doc");
			
			//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
	//		botanicalName.getTitleCache();
			
			Taxon taxon1 = Taxon.NewInstance(botanicalName,journal);
			appCtr.getTaxonService().saveTaxon(taxon1);
			BotanicalName homotypName = BotanicalName.NewInstance(Rank.SUBGENUS(), botanicalName.getHomotypicalGroup());
			homotypName.setGenusOrUninomial("Subgenus");
			homotypName.setInfraGenericEpithet("homotyp");
			
			//Synonym synonym = Synonym.NewInstance(homotypName, journal);
			
			System.out.println("Taxa of " + botanicalName + ": " + botanicalName.getTaxonBases());
			System.out.println("Synonyms of " + homotypName + ": " + homotypName.getSynonyms());
			
			HomotypicalGroup homotypicalGroup = taxon1.getHomotypicGroup();
			System.out.println("HomotypicNames of " + botanicalName + ":" + homotypicalGroup.getTypifiedNames());
			System.out.println("HomotypicSynonymsByGroup of " + taxon1 + ":" + taxon1.getHomotypicSynonymsByHomotypicGroup());
			System.out.println("HomotypicSynonymsBySynonymy of " + taxon1 + ":" + taxon1.getHomotypicSynonymsByHomotypicRelationship());
			
//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);
			
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	
	public void testFacts(){
		try {
			String server = "192.168.2.10";
			String database = "cdm_test_andreasM";
			String username = "edit";
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = BotanicalName.NewInstance(genus);
			botanicalName.setGenusOrUninomial("GenusName");
		
			Journal journal = Journal.NewInstance();
			journal.setTitleCache("Afro+Doc");
			
			Taxon taxon = Taxon.NewInstance(botanicalName,journal);
			appCtr.getTaxonService().saveTaxon(taxon);
			
			TaxonDescription taxonDescription = TaxonDescription.NewInstance();
			taxon.addDescription(taxonDescription);
			
			//textData
			TextData textData = TextData.NewInstance();
			textData.putText("XXX", Language.DEFAULT());
			taxonDescription.addElement(textData);
			
			//commonNames
			String commonNameString;
			if (taxon.getName() != null){
				commonNameString = "Common " + taxon.getName().getTitleCache(); 
			}else{
				commonNameString = "Common (null)";
			}
			CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, Language.DEFAULT());
			taxonDescription.addElement(commonName);
			
			//save
			appCtr.getTaxonService().saveTaxon(taxon);

			
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	
	public void testHybridRelationships(){
		try {
//			String database = "cdm";
//			String username = "sa";

			String server = "192.168.2.10";
			String database = "cdm_test_andreasM";
			String username = "edit";
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
			Rank genus = Rank.GENUS();
			BotanicalName parentName = BotanicalName.NewInstance(genus);
			parentName.setGenusOrUninomial("parent");
		
			BotanicalName childName = BotanicalName.NewInstance(genus);
			childName.setGenusOrUninomial("child");
			parentName.addHybridChild(childName, HybridRelationshipType.FIRST_PARENT(), null);
			
			//save
			appCtr.getNameService().saveTaxonName(parentName);

			
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	public void testPaddie(){
		

		try {
			String server = "PADDIE";
			String database = "edit_test";
			String username = "andreas";
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation validation = DbSchemaValidation.VALIDATE;
			ICdmDataSource datasource = CdmDataSource.NewSqlServer2005Instance(server, database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, validation);
			
			Rank genus = Rank.GENUS();
			BotanicalName botanicalName = BotanicalName.NewInstance(genus);
			botanicalName.setGenusOrUninomial("GenusName");
		
			Journal journal = Journal.NewInstance();
			journal.setTitle("JournalTitel");
			
			//			Taxon taxon = Taxon.NewInstance(botanicalName, journal);
//			Taxon taxon2 = Taxon.NewInstance(botanicalName2, null);
	//		botanicalName.getTitleCache();
			Rank.SPECIES();
			appCtr.getNameService().saveTaxonName(botanicalName);

//			appCtr.getTaxonService().saveTaxon(taxon2);
//			appCtr.getTaxonService().saveTaxon(taxon);
			
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	

	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestDatabase sc = new TestDatabase();
    	sc.test();
	}

}
