/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmImporterTest extends CdmTransactionalIntegrationTest{
	
	@SpringBeanByType
	JaxbImport jaxbImport;
	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest-input.xml");
		configurator = JaxbImportConfigurator.NewInstance(url.toString(),null);
	}
	
	
	@Test
	public void testInit() {
		assertNotNull("jaxbImport should not be null",jaxbImport);
	}
	
	@Test
	@DataSet
	public void testImport() throws Exception {
		jaxbImport.doInvoke(configurator, null);
		testExpectedDataSet(this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest.testImport-result.xml"));
	}

	protected void testExpectedDataSet(InputStream dataSet) {
		
		final String dbVersionTable = "DB_VERSION";
		try {
			IDatabaseConnection databaseConnection = getConnection();
			
			IDataSet expectedDataSet = new FlatXmlDataSet(dataSet, this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/dataset.dtd"));
			
            // Filter table DB_VERSION since column VERSION_TIMESTAMP has different value for different users
			IDataSet filteredActualDataSet = 
				new FilteredDataSet(new ExcludeTableFilter(new String[]{dbVersionTable}), databaseConnection.createDataSet());
			Assertion.assertEquals(expectedDataSet, filteredActualDataSet);

//          Instead of filtering the entire table DB_VERSION as above,
//			it might be better to filter the column VERSION_TIMESTAMP only (see commented code below).
//          Need to add the filtered table back to the actual data set.
			
//			ITable actualDbVersionTable = actualDataSet.getTable(dbVersionTable);
//			ITable filteredTable = 
//				DefaultColumnFilter.excludedColumnsTable(actualDbVersionTable, new String[]{dbVersionTable});
//			filteredTable.getTableMetaData();
			
		} catch (Exception e) {
			System.out.println(e);
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
			fail("No exception expected in database validation method");
		} 
	}
}
