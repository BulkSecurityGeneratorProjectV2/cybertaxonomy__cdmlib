/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.config.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.init.TermNotFoundException;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);


	private void testNewConfigControler(){
		List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;

		String server;
		String database;
		String username;

		System.out.println(lsDataSources);
		ICdmDataSource dataSource;

		dataSource = lsDataSources.get(1);
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;

//		String server = "localhost";
//		database = "cdm36";
////		database = "cdm_production_edaphobase";
//		String username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		String server = "160.45.63.171";
//		String database = "cdm_production_xxx";
//		String username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));


//		String server = "test.e-taxonomy.eu";
////		String database = "cdm_test";
//		String database = "cdm_edit_flora_malesiana";
//		String username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		String server = "localhost";
//		String database = "testCDM";
//		String username = "postgres";
//		dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.PostgreSQL, server, database, DatabaseTypeEnum.PostgreSQL.getDefaultPort(), username, AccountStore.readOrStorePassword(server, database, username, null));


		//SQLServer
		server = "BGBM-PESISQL";
		database = "cdm36";
		int port = 1433;
		username = "cdmupdater";
		dataSource = CdmDataSource.NewSqlServer2012Instance(server, database, port, username, AccountStore.readOrStorePassword(server, database, username, null));
//
//		//H2
//        String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2";
////		String path = "C:\\Users\\pesiimport\\.cdmLibrary\\writableResources\\h2\\LocalH2";
////      String path = "C:\\Users\\a.mueller\\eclipse\\svn\\cdmlib-trunk\\cdmlib-remote-webapp\\src\\test\\resources\\h2";
//		username = "sa";
//    	dataSource = CdmDataSource.NewH2EmbeddedInstance("cdm", username, "", path);

//    	dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa");


//       server = "160.45.63.201";
//       database = "cdm_integration_cichorieae";
//       username = "edit";
//       dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));


// 		try {
//			CdmUpdater updater = new CdmUpdater();
//			if (schema == DbSchemaValidation.VALIDATE){
//				updater.updateToCurrentVersion(dataSource, DefaultProgressMonitor.NewInstance());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource, schema);
//		Classification classification = appCtr.getClassificationService().list(null, null, null, null, null).get(0);
//		logger.warn(classification.getMicroReference());
//        logger.warn(classification.getName());

//		TransactionStatus tx = appCtr.startTransaction();

//		testGroupedTaxa(appCtr);


//		int n = appCtr.getAgentService().count(null);
//		logger.warn("End adding " + n + " persons");

//		appCtr.getCommonService().createFullSampleData();

//		ValidationManager valMan = (ValidationManager)appCtr.getBean("validationManager");
//		valMan.registerValidationListeners();

//		State state = State.NewInstance();
//		Taxon taxon = Taxon.NewInstance(null, null);
//		TaxonDescription desc = TaxonDescription.NewInstance(taxon);
////		CategoricalData catData = CategoricalData.NewInstance(state, Feature.HABITAT());
//		QuantitativeData quantData = QuantitativeData.NewInstance(Feature.ANATOMY());
//		StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.AVERAGE(), 2);
//		quantData.addStatisticalValue(statisticalValue);
//		desc.addElement(quantData);

//		appCtr.getTermService().saveOrUpdate(state);
//
//		appCtr.getTaxonService().save(taxon);

		//		insertSomeData(appCtr);
//		deleteHighLevelNode(appCtr);   //->problem with Duplicate Key in Classification_TaxonNode
		appCtr.close();
		System.exit(0);
	}


    /**
     * @param appCtr
     */
    private void testGroupedTaxa(CdmApplicationController appCtr) {
        UUID classificationUuid = UUID.fromString("91231ebf-1c7a-47b9-a56c-b45b33137244");
		UUID taxonUuid1 = UUID.fromString("3bae1c86-1235-4e2e-be63-c7f8c4410527");
		UUID taxonUuid2 = UUID.fromString("235d3872-defe-4b92-bf2f-75a7c91510de");
		List<UUID> taxonUuids = Arrays.asList(new UUID[]{taxonUuid1, taxonUuid2});
		Rank maxRank = DefinedTermBase.getTermByClassAndUUID(Rank.class, UUID.fromString("af5f2481-3192-403f-ae65-7c957a0f02b6"));
		Rank minRank = DefinedTermBase.getTermByClassAndUUID(Rank.class, UUID.fromString("78786e16-2a70-48af-a608-494023b91904"));
        List<GroupedTaxonDTO> groupedTaxa = appCtr.getClassificationService().groupTaxaByHigherTaxon(taxonUuids, classificationUuid, minRank, maxRank);
        System.out.println(groupedTaxa);
    }


    private void addPerson(CdmApplicationController appCtr) {
        TransactionStatus tx = appCtr.startTransaction();
		appCtr.getAgentService().save(Person.NewInstance());
		appCtr.commitTransaction(tx);
    }


	private void deleteHighLevelNode(CdmApplicationController appCtr) {
		TransactionStatus tx = appCtr.startTransaction();
		ITaxonNodeService service = appCtr.getTaxonNodeService();
		TaxonNode node = service.find(60554);
//		service.delete(node);
		ITaxonService taxonService = appCtr.getTaxonService();
		Taxon taxon = node.getTaxon();
		//try {
			taxonService.deleteTaxon(taxon.getUuid(), new TaxonDeletionConfigurator(), node.getClassification().getUuid());

		/*} catch (DataChangeNoRollbackException e) {
			e.printStackTrace();
		}*/
		try {
			appCtr.commitTransaction(tx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TaxonNode node2 = service.find(60554);


	}

//	private void insertSomeData(CdmApplicationController appCtr) {
//		Classification cl = Classification.NewInstance("myClass");
//		TaxonNode node1 = cl.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(null), null), null, null);
//		appCtr.getClassificationService().save(cl);
//
//		Taxon t2 = Taxon.NewInstance(null, null);
//		t2.setTitleCache("Taxon2", true);
//		TaxonNode node2 = node1.addChildTaxon(t2, null, null);
//
//		Taxon t3 = Taxon.NewInstance(null, null);
//		t3.setTitleCache("Taxon3", true);
//		TaxonNode node3 = node1.addChildTaxon(t3, 0, null, null);
//
//		appCtr.getTaxonNodeService().saveOrUpdate(node1);
//
//		cl.addChildNode(node3, 0, null, null);
//		appCtr.getTaxonNodeService().saveOrUpdate(node3);
//		appCtr.getClassificationService().saveOrUpdate(cl);
//
//		TermTree<Feature> ft1 = TermTree.NewInstance();
//		FeatureNode fn1 = TermNode.NewInstance((Feature)null);
//		ft1.getRoot().addChild(fn1);
//		appCtr.getFeatureNodeService().save(fn1);
//
//		TermNode fn2 = TermNode.NewInstance((Feature)null);
//		fn1.addChild(fn2);
//
//		TermNode fn3 = TermNode.NewInstance((Feature)null);
//		fn1.addChild(fn2, 0);
//
//		appCtr.getFeatureNodeService().saveOrUpdate(fn1);
//
//		ft1.getRoot().addChild(fn3, 0);
//		appCtr.getFeatureNodeService().saveOrUpdate(fn3);
//		appCtr.getFeatureTreeService().saveOrUpdate(ft1);
//	}

	private void testDatabaseChange() throws DataSourceNotFoundException{
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance();

//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		String server = "192.168.2.10";
//		String database = "cdm_test_andreas";
//		String user = "edit";
//		String pwd = "wp5";
//
		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "";

		ICdmDataSource dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.SqlServer2005, "LAPTOPHP", "cdmTest", DatabaseTypeEnum.SqlServer2005.getDefaultPort(), "edit", "");

		appCtr.getDatabaseService().saveDataSource("testSqlServer", dataSource);
		try {
			appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
		} catch (TermNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		appCtr.close();
	}

	private void testSqlServer2005(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.SqlServer2005;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "";

		ICdmDataSource dataSource = CdmDataSource.NewInstance(databaseTypeEnum, server, database, databaseTypeEnum.getDefaultPort(), username, password);

		CdmPersistentDataSource ds = CdmPersistentDataSource.save("testSqlServer", dataSource);

		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
		Person agent = Person.NewInstance();
		appCtr.getAgentService().save(agent);
		TaxonName tn = TaxonNameFactory.NewBotanicalInstance(null);
		appCtr.getNameService().save(tn);
		appCtr.close();

	}


	private void testLocalHsql(){
		CdmApplicationController appCtr = null;
		try {
			CdmPersistentDataSource ds = CdmPersistentDataSource.NewLocalHsqlInstance();
			appCtr = CdmApplicationController.NewInstance(ds);
			List<?> l = appCtr.getNameService().list(null,5, 1,null,null);
			System.out.println(l);
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			if (appCtr != null){
			    appCtr.close();
			}

		} catch (DataSourceNotFoundException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
		}
	}

	private void testLocalH2(){

		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		ICdmDataSource ds =
			CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "", null);
//			ds =
//				 CdmPersistentDataSource.NewInstance("localH2");
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);

		boolean exists = appCtr.getUserService().userExists("admin");
		try {
			IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null);
			String nameCache = "testName";
			name.setNameCache(nameCache);
			name.setTitleCache(nameCache, true);
			Reference ref = ReferenceFactory.newGeneric();
			ref.setTitleCache("mySec", true);
			Taxon taxon = Taxon.NewInstance(name, ref);
			TaxonDescription description = TaxonDescription.NewInstance();
			taxon.addDescription(description);
			NamedArea area1 = appCtr.getTermService().getAreaByTdwgAbbreviation("GER");
			Distribution distribution = Distribution.NewInstance(area1, PresenceAbsenceTerm.PRESENT());
			description.addElement(distribution);

			List<Distribution> distrList = new ArrayList<Distribution>();
			distrList.add(distribution);
			List<NamedArea> areaList = new ArrayList<NamedArea>();
			areaList.add(area1);

		//	distribution.getInDescription().get
			appCtr.getTaxonService().save(taxon);

			System.out.println(taxon.getDescriptions().size());

			TransactionStatus txStatus = appCtr.startTransaction();

			Session session = appCtr.getSessionFactory().getCurrentSession();

			//String hqlQuery = "from DescriptionBase d join d.elements  as e "
//				String hqlQuery = "from Taxon t join t.descriptions  as d "+
//				 " inner join d.elements e on e member of d "
//				+
//				"";//" where e.area = :namedArea " ;
			String hqlQuery = "Select t from Distribution e join e.inDescription d join d.taxon t join t.name n "+
				" WHERE e.area in (:namedArea) AND n.nameCache = :nameCache ";
			Query query = session.createQuery(hqlQuery);

			//query.setEntity("namedArea", area1);
			query.setParameter("nameCache", nameCache);
			query.setParameterList("namedArea", areaList);
			List resultList = query.list();
			//List list = appCtr.getCommonService().getHqlResult(hqlQuery);

			for (Object o:resultList){
				System.out.println(o);
			}
			appCtr.commitTransaction(txStatus);

			//System.out.println(l);
			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();

		}
	}

	private boolean testWritableResourceDirectory() throws IOException{
		CdmApplicationUtils.getWritableResourceDir();
		return true;
	}

	private boolean testH2(){
//		testLocalH2();
//		if (true)return true;

		DbSchemaValidation validation = DbSchemaValidation.CREATE;
		ICdmDataSource ds =
			CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "", null);
			//CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
//		ds =
//			 CdmPersistentDataSource.NewInstance("localH2");
		CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds, validation);
		try {
		    TaxonName botName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
			TaxonName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
			IBotanicalName hybridName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
			botName1.addRelationshipToName(botName2, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), null);
			UUID uuid1 = botName1.getUuid();
			UUID uuid2 = botName2.getUuid();
			try {
				Logger loggerTrace = Logger.getLogger("org.hibernate.type");
				//loggerTrace.setLevel(Level.TRACE);
				System.out.println(logger.getName());

				appCtr.getNameService().save(botName1);
				ResultSet rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				int c = rs.getInt("n");
				System.out.println("Begin :" + c);

				botName1.removeRelationToTaxonName(botName2);
				botName1.setSpecificEpithet("DELETED");
				botName2.addHybridParent(hybridName, HybridRelationshipType.FIRST_PARENT(), null);

				TransactionStatus tx = appCtr.startTransaction();
				appCtr.getNameService().saveOrUpdate(botName2);
				rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				c = rs.getInt("n");
				System.out.println("End: " + c);

				appCtr.commitTransaction(tx);

				appCtr.getNameService().saveOrUpdate(botName1);

				rs = ds.executeQuery("Select count(*) as n FROM NameRelationship");
				rs.next();
				c = rs.getInt("n");
				System.out.println("End: " + c);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			//Agent agent = new Agent();
			//appCtr.getAgentService().saveAgent(agent);
			appCtr.close();
			return true;
		} catch (RuntimeException e) {
			logger.error("Runtime Exception");
			e.printStackTrace();
			appCtr.close();

		}
		return false;
	}

	private void test(){
		System.out.println("Start Datasource");
		testNewConfigControler();
    	//testDatabaseChange();

		//testSqlServer();

		//CdmUtils.findLibrary(au.com.bytecode.opencsv.CSVReader.class);
		//testPostgreServer();
		//testLocalHsql();
		//testLocalH2();
		//testWritableResourceDirectory();
//		testH2();
		System.out.println("\nEnd Datasource");
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		Datasource cc = new Datasource();
    	cc.test();
	}

}
