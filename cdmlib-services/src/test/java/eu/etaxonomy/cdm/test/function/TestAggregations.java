/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationConfigurationBase.AggregationMode;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationConfigurationBase.SourceMode;
import eu.etaxonomy.cdm.api.service.description.DistributionAggregationConfiguration;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.config.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * This test is purely for development purpose. It does not run in maven.
 * It is for testing factual data aggregation tasks.
 *
 * @author a.mueller
 * @since 15.11.2019
 */
public class TestAggregations {

	private static final Logger logger = Logger.getLogger(TestAggregations.class);

	private void testNewConfigControler(){
	    logger.debug("start");
		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;

		String server;
		String database;
		String username;
		ICdmDataSource dataSource;

//      List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
//     System.out.println(lsDataSources);
//     dataSource = lsDataSources.get(1);

//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;

//		String server = "160.45.63.171";
//		String database = "cdm_production_xxx";
//		String username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));


		server = "test.e-taxonomy.eu";
		database = "cdm_edit_cichorieae";
		username = "edit";
		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		String server = "localhost";
//		String database = "testCDM";
//		String username = "postgres";
//		dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.PostgreSQL, server, database, DatabaseTypeEnum.PostgreSQL.getDefaultPort(), username, AccountStore.readOrStorePassword(server, database, username, null));


//		//SQLServer
//		server = "BGBM-PESISQL";
//		database = "cdm36";
//		int port = 1433;
//		username = "cdmupdater";
//		dataSource = CdmDataSource.NewSqlServer2012Instance(server, database, port, username, AccountStore.readOrStorePassword(server, database, username, null));
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

		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource, schema);

//		TransactionStatus tx = appCtr.startTransaction(true);

		UUID targetAreaLevelUuid = UUID.fromString("25b563b6-6a6c-401b-b090-c9498886c50b");
		NamedAreaLevel targetAreaLevel = (NamedAreaLevel)appCtr.getTermService().load(targetAreaLevelUuid);
		Pager<NamedArea> areaPager = appCtr.getTermService().list(targetAreaLevel, (NamedAreaType) null,
                null, null, (List<OrderHint>) null, null);
		IProgressMonitor monitor = DefaultProgressMonitor.NewInstance();
		UUID uuidCichorieae = UUID.fromString("2343071c-d5f4-4434-89b4-cdf7b2ff7f39");
		UUID uuidCichoriinae = UUID.fromString("2b05bf1a-950e-43ad-8367-41fe8d3e6c92");
		UUID uuidCichorium = UUID.fromString("6a7ac1ad-2fd9-4218-8132-12dd463d04b9");
		UUID uuidArnoseris = UUID.fromString("0f71555c-676b-4d66-8a0c-281787ac72f6");
		TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(uuidArnoseris);

		DistributionAggregationConfiguration config = DistributionAggregationConfiguration
		        .NewInstance(AggregationMode.byAreasAndRanks(), areaPager.getRecords(), filter, monitor);
		config.setToParentSourceMode(SourceMode.DESCRIPTION);
        config.setWithinTaxonSourceMode(SourceMode.ALL_SAMEVALUE);
		try {
            config.getTaskInstance().invoke(config, appCtr);
        } catch (Exception e) {
            e.printStackTrace();
        }

		appCtr.close();

	}

	private void test(){
		System.out.println("Start Datasource");
		testNewConfigControler();

		System.out.println("\nEnd Datasource");
	}


	public static void  main(String[] args) {
		TestAggregations cc = new TestAggregations();
    	cc.test();
    	System.exit(0);
	}

}
