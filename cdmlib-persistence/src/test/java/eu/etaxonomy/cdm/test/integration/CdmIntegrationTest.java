package eu.etaxonomy.cdm.test.integration;

import java.io.OutputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.spring.annotation.SpringApplicationContext;

/**
 * Abstract base class for integration testing a spring / hibernate application using
 * the unitils testing framework and dbunit, against an in-memory HSQL database.
 * 
 * @author ben.clark
 * @see <a href="http://www.unitils.org">unitils home page</a>
 */
@SpringApplicationContext("classpath:eu/etaxonomy/cdm/applicationContext-test.xml")
public abstract class CdmIntegrationTest extends UnitilsJUnit4 {
	protected static final Logger logger = Logger.getLogger(CdmIntegrationTest.class);

	@TestDataSource
	protected DataSource dataSource;

	protected IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(dataSource.getConnection());

			DatabaseConfig config = connection.getConfig();

			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
					new HsqldbDataTypeFactory());
		} catch (Exception e) {
			logger.error(e);
		}
		return connection;
	}

	/**
	 * Prints the data set to an output stream, using dbunit's
	 * {@link org.dbunit.dataset.xml.FlatXmlDataSet}. 
	 * <p>
	 * Remember, if you've just called save() or
	 * update(), the data isn't written to the database until the 
	 * transaction is committed, and that isn't until after the 
	 * method exits. Consequently, if you want to test writing to 
	 * the database, either use the {@literal @ExpectedDataSet} 
	 * annotation (that executes after the test is run), or use
	 * {@link CdmTransactionalIntegrationTest}.
	 *  
	 * @param out The OutputStream to write to.
	 * @see org.dbunit.dataset.xml.FlatXmlDataSet
	 */
	public void printDataSet(OutputStream out) {
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet actualDataSet = connection.createDataSet();
			FlatXmlDataSet.write(actualDataSet, out);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException sqle) {
				logger.error(sqle);
			}
		}
	}

}
