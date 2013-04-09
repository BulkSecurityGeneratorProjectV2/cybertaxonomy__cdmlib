/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.hibernate.dialect.MySQL5MyISAMUtf8Dialect;

import eu.etaxonomy.cdm.database.ICdmDataSource;


/**
 * @author a.mueller
 *
 */
public class MySQLDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "MySQL";

	//class
	protected String classString = "com.mysql.jdbc.Driver";

	//url
    protected String urlString = "jdbc:mysql://";

    //port
    private final int defaultPort = 3306;

    private static String dbSeparator = "/";

    //hibernate dialect
    // TODO switch to MySQLInnoDBUtf8Dialect !!
    // see #3371 (switch cdm to MySQL InnoDB)
    //private String hibernateDialect = MySQLInnoDBUtf8Dialect.class.getSimpleName();
    private final String hibernateDialect = MySQL5MyISAMUtf8Dialect.class.getSimpleName();

    //connection String
	@Override
    public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() + ":" + port + dbSeparator + ds.getDatabase() + "?useUnicode=true&characterEncoding=utf8" + "&zeroDateTimeBehavior=convertToNull";
        //return urlString + ds.getServer() + ":" + port + "/" + ds.getDatabase() + "?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8_general_ci&characterSetResults=utf8&jdbcCompliantTruncation=false";
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)
     */
    @Override
    public String getDatabaseNameByConnectionString(String connectionString){
    	String result;
    	result = getDatabasePartOfConnectionString(connectionString, dbSeparator);
    	int posParams = result.indexOf("?");
    	if (posParams != -1){
    		result = result.substring(0, posParams);
    	}
     	return result;
    }

    /**
     * Constructor
     */
    public MySQLDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}


}
