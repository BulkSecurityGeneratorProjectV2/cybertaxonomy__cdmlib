/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.IDatabaseType;

/**
 * @author a.mueller
 * @created 18.12.2008
 * @version 1.0
 */
abstract class CdmDataSourceBase implements ICdmDataSource {
	private static final Logger logger = Logger.getLogger(CdmDataSourceBase.class);
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#testConnection()
	 */
	public boolean testConnection() {
		try {
			IDatabaseType dbType = getDatabaseType().getDatabaseType();
			String classString = dbType.getClassString();
			Class.forName(classString);
			
			String mUrl = dbType.getConnectionString(this);
			Connection mConn = DriverManager.getConnection(mUrl, getUsername(), getPassword());
			if (mConn != null){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return false;
		}
	}
	
	// TODO: Harmonize methods (DRY)
	
    /**
     * Executes a query and returns the ResultSet.
     * @return ResultSet for the query.
     */
    public ResultSet executeQuery (String query) {
    	ResultSet rs;
    	try {
    		IDatabaseType dbType = getDatabaseType().getDatabaseType();
    		String classString = dbType.getClassString();
    		Class.forName(classString);

    		String mUrl = dbType.getConnectionString(this);
    		Connection mConn = DriverManager.getConnection(mUrl, getUsername(), getPassword());
    		if (query == null){
    			return null;
    		}
    		Statement mStmt = mConn.createStatement();
    		rs = mStmt.executeQuery(query);
    		return rs;
    	} catch(SQLException e) {
    		logger.error("Problems when creating Resultset for query \n  " + query + " \n" + "Exception: " + e);
    		return null;
    	} catch (ClassNotFoundException e) {
    		logger.error("Database driver class could not be loaded\n" + "Exception: " + e.toString());
    		return null;
    	}
    }
	
    /**
     * Executes an update
     * @return return code
     */
    public int executeUpdate (String sqlUpdate) {
    	int result;
    	try {
    		IDatabaseType dbType = getDatabaseType().getDatabaseType();
    		String classString = dbType.getClassString();
    		Class.forName(classString);

    		String mUrl = dbType.getConnectionString(this);
    		Connection mConn = DriverManager.getConnection(mUrl, getUsername(), getPassword());
    		if (sqlUpdate == null){
    			return 0;
    		}
    		Statement mStmt = mConn.createStatement();
    		result = mStmt.executeUpdate(sqlUpdate);
    		return result;
    	} catch(SQLException e) {
    		logger.error("Problems when creating executing update\n  " + sqlUpdate + " \n" + "Exception: " + e);
    		return 0;
    	} catch (ClassNotFoundException e) {
    		logger.error("Database driver class could not be loaded\n" + "Exception: " + e.toString());
    		return 0;
    	}
    }
}
