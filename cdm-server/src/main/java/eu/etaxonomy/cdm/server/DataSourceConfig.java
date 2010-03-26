// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.server;

import org.apache.log4j.Logger;

public class DataSourceConfig {
 
	public static final Logger logger = Logger.getLogger(DataSourceConfig.class);

	private String dataSourceName;
	private String password;
	private String username;
	private String url;
	private String driverClass;
	public String getDataSourceName() {
		return dataSourceName;
	}
	public String getJdbcJndiName() {
		return "jdbc/"+dataSourceName;
	}
	
	public String getHibernateDialectName() {
		if(url.contains("mysql")){
			return "org.hibernate.dialect.MySQLDialect";
		}
		logger.error("hibernate dialect mapping for "+url+ " not jet implemented or unavailable");
		return null;
	}
	
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDriverClass() {
		return driverClass;
	}
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}
	
	@Override
	public String toString(){
		return dataSourceName ;
		
	}
}
