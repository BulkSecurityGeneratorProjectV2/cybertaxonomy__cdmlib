/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

/**
 * @author a.babadshanjan
 * @created 03.03.2009
 * @version 1.0
 */
public enum MatchMode {
	EXACT,
	BEGINNING,
	ANYWHERE;
	
	public String queryStringFrom(String queryString){
		queryString = queryString.replace('*', '%');
		switch(this){	
			case BEGINNING:
				return "'" + queryString + "%'";			
			case ANYWHERE:
				return "'%" + queryString + "%'";
			default:
				return "'" + queryString + "'";
		}
	}
}
