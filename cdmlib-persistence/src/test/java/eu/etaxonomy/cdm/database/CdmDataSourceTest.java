/**
 * 
 */
package eu.etaxonomy.cdm.database;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.NoCacheProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.DbProperties;
import eu.etaxonomy.cdm.database.DbSchemaValidation;

/**
 * @author a.mueller
 *
 */
public class CdmDataSourceTest {
	private static final Logger logger = Logger.getLogger(CdmDataSourceTest.class);
	
	private static CdmPersistentDataSource dataSource;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	//@Before
	public void setUp() throws Exception {
		dataSource = CdmPersistentDataSource.NewInstance("default");
		//delete tmp
		String dataSourceString = "tmp";
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			//;
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));

	}

	/**
	 * @throws java.lang.Exception
	 */
	//@After
	public void tearDown() throws Exception {
		//delete tmp
		String dataSourceString = "tmp";
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			//;
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));

	}
	
//********************** TESTS ***********************************************/

	@Test
	public void testDummy() {
		assertEquals(1,1);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewDefaultInstance()}.
	 */
	//@Test
	public void testNewDefaultInstance() {
		try {
			assertEquals(CdmPersistentDataSource.NewInstance("default"), CdmPersistentDataSource.NewDefaultInstance());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewLocalHsqlInstance()}.
	 */
	//@Test
	public void testNewLocalHsqlInstance() {
		try {
			assertEquals(CdmPersistentDataSource.NewInstance("localDefaultHsql"), CdmPersistentDataSource.NewLocalHsqlInstance());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#NewInstance(java.lang.String)}.
	 */
	//@Test
	public void testNewInstance() {
		assertNotNull(dataSource);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getName()}.
	 */
	//@Test
	public void testGetName() {
		assertEquals("default", dataSource.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDatabaseType()}.
	 */
	//@Test
	public void testGetDatabaseType() {
		assertEquals(DatabaseTypeEnum.MySQL, dataSource.getDatabaseType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDbProperty(eu.etaxonomy.cdm.database.CdmPersistentDataSource.DbProperties)}.
	 */
	//@Test
	public void testGetDbProperty() {
		assertEquals("com.mysql.jdbc.Driver", dataSource.getDatasourceProperty(DbProperties.DRIVER_CLASS));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDatasourceBean()}.
	 */
	//@Test
	public void testGetDatasourceBean() {
		BeanDefinition beanDef = dataSource.getDatasourceBean();
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "driverClassName"; 
		assertEquals("com.mysql.jdbc.Driver", propValues.getPropertyValue(propName).getValue());
		propName =  "url"; 
		assertEquals("testUrl", propValues.getPropertyValue(propName).getValue());
		propName =  "username"; 
		assertEquals("testUser", propValues.getPropertyValue(propName).getValue());
		propName =  "password"; 
		assertEquals("testPassword", propValues.getPropertyValue(propName).getValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL)}.
	 */
	//@Test
	public void testGetHibernatePropertiesBeanHBM2DDL() {
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		BeanDefinition beanDef = dataSource.getHibernatePropertiesBean(hbm2dll);
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "properties"; 
		PropertyValue propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		Properties properties = (Properties)propValue.getValue();
		assertEquals(hbm2dll.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
		assertEquals(dataSource.getDatabaseType().getHibernateDialect(), properties.getProperty("hibernate.dialect"));
		assertEquals(NoCacheProvider.class.getName(), properties.getProperty("hibernate.cache.provider_class"));
		assertEquals(String.valueOf(false), properties.getProperty("hibernate.show_sql"));
		assertEquals(String.valueOf(false), properties.getProperty("hibernate.format_sql"));

		//null
		beanDef = dataSource.getHibernatePropertiesBean(null);
		propValues = beanDef.getPropertyValues();
		propName =  "properties"; 
		propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		properties = (Properties)propValue.getValue();
		assertEquals(DbSchemaValidation.VALIDATE.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL, java.lang.Boolean, java.lang.Boolean, java.lang.Class)}.
	 */
	//@Test
	public void testGetHibernatePropertiesBeanHBM2DDLBooleanBooleanClassOfQextendsCacheProvider() {
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		boolean showSql = false;
		boolean formatSql = false;
		Class<? extends CacheProvider> cacheProviderClass = NoCacheProvider.class;
		
		BeanDefinition beanDef = dataSource.getHibernatePropertiesBean(hbm2dll, showSql, formatSql, cacheProviderClass);
		PropertyValues propValues = beanDef.getPropertyValues();
		String propName =  "properties"; 
		PropertyValue propValue =  propValues.getPropertyValue(propName);
		assertNotNull(propValue);
		assertTrue( propValue.getValue() instanceof Properties);
		
		Properties properties = (Properties)propValue.getValue();
		assertEquals(hbm2dll.toString(), properties.getProperty("hibernate.hbm2ddl.auto"));
		assertEquals(dataSource.getDatabaseType().getHibernateDialect(), properties.getProperty("hibernate.dialect"));
		assertEquals(cacheProviderClass.getName(), properties.getProperty("hibernate.cache.provider_class"));
		assertEquals(String.valueOf(showSql), properties.getProperty("hibernate.show_sql"));
		assertEquals(String.valueOf(formatSql), properties.getProperty("hibernate.format_sql"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#exists(java.lang.String)}.
	 */
	//@Test
	public void testExists() {
		assertTrue(CdmPersistentDataSource.exists("default"));
		assertTrue(CdmPersistentDataSource.exists("localDefaultHsql"));
		assertFalse(CdmPersistentDataSource.exists("xlsj�dfl"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#save(java.lang.String, eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	//@Test
	public void testSaveStringDatabaseTypeEnumStringStringIntStringString() {
		String dataSourceString = "tmp";
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));
		
		DatabaseTypeEnum databaseType = DatabaseTypeEnum.SqlServer2005;
		String servername = "server";
		String db = "database";
		String username = "username";
		String password = "password";
		int port = 1234;
		
		CdmPersistentDataSource.save(dataSourceString, databaseType, servername, db, port, username, password);
		assertTrue(CdmPersistentDataSource.exists(dataSourceString));
		
		CdmPersistentDataSource dataSource = null;
		try {
			dataSource = CdmPersistentDataSource.NewInstance(dataSourceString);
		} catch (DataSourceNotFoundException e1) {
			fail();
		}
		assertEquals(databaseType, dataSource.getDatabaseType());
		assertEquals(DatabaseTypeEnum.SqlServer2005.getDriverClassName(), dataSource.getDatasourceProperty(DbProperties.DRIVER_CLASS));
		assertEquals("jdbc:sqlserver://server:1234;databaseName=database;SelectMethod=cursor", dataSource.getDatasourceProperty(DbProperties.URL));
		assertEquals(username, dataSource.getDatasourceProperty(DbProperties.USERNAME));
		assertEquals(password, dataSource.getDatasourceProperty(DbProperties.PASSWORD));
		//delete
		try {
			CdmPersistentDataSource.delete(CdmPersistentDataSource.NewInstance(dataSourceString));
		} catch (DataSourceNotFoundException e) {
			fail();
		}
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#save(java.lang.String, eu.etaxonomy.cdm.database.DatabaseTypeEnum, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	//@Test
	public void testSaveStringDatabaseTypeEnumStringStringStringString() {
		//see testSaveStringDatabaseTypeEnumStringStringIntStringString
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#saveLocalHsqlDb(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	//@Test
	public void testSaveLocalHsqlDb() {
		String dataSourceString = "tmp";
		assertFalse(CdmPersistentDataSource.exists(dataSourceString));
		
		String servername = "server";
		String db = "testHsqlDb";
		String username = "username";
		String password = "password";
		
		CdmPersistentDataSource.saveLocalHsqlDb(dataSourceString, CdmApplicationUtils.getWritableResourceDir().getAbsolutePath(), db, username, password);
		assertTrue(CdmPersistentDataSource.exists(dataSourceString));
		
		CdmPersistentDataSource dataSource = null;
		try {
			dataSource = CdmPersistentDataSource.NewInstance(dataSourceString);
		} catch (DataSourceNotFoundException e1) {
			fail();
		}
		assertEquals(DatabaseTypeEnum.HSqlDb, dataSource.getDatabaseType());
		assertEquals(DatabaseTypeEnum.HSqlDb.getDriverClassName(), dataSource.getDatasourceProperty(DbProperties.DRIVER_CLASS));
		assertEquals("jdbc:hsqldb:hsql://localhost:9001/testHsqlDb", dataSource.getDatasourceProperty(DbProperties.URL));
		assertEquals(username, dataSource.getDatasourceProperty(DbProperties.USERNAME));
		assertEquals(password, dataSource.getDatasourceProperty(DbProperties.PASSWORD));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#delete(eu.etaxonomy.cdm.database.CdmPersistentDataSource)}.
	 */
	//@Test
	public void testDelete() {
		testSaveStringDatabaseTypeEnumStringStringStringString();
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getAllDataSources()}.
	 */
	//@Test
	public void testGetAllDataSources() {
		assertEquals(2, CdmPersistentDataSource.getAllDataSources().size());
		assertEquals("default", CdmPersistentDataSource.getAllDataSources().get(0).getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#toString()}.
	 */
	//@Test
	public void testToString() {
		String dataSourceName = "default";
		try {
			assertEquals(dataSourceName, CdmPersistentDataSource.NewInstance(dataSourceName).toString());
		} catch (DataSourceNotFoundException e) {
			fail();
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDataSourceInputStream()}.
	 */
	//@Test
	public void testGetDataSourceInputStream() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.database.CdmPersistentDataSource#getDataSourceOutputStream()}.
	 */
	//@Test
	public void testGetDataSourceOutputStream() {
		logger.warn("Not yet implemented");
	}

}
