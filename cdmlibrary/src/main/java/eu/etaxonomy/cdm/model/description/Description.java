/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.location.NamedArea;
import etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:44
 */
public class Description extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Description.class);

	//in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.
	@Description("in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.")
	private String label;
	private ArrayList sources;
	private ArrayList geoScopes;
	private ArrayList scopes;
	private ArrayList features;

	public ArrayList getSources(){
		return sources;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSources(ArrayList newVal){
		sources = newVal;
	}

	public ArrayList getGeoScopes(){
		return geoScopes;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setGeoScopes(ArrayList newVal){
		geoScopes = newVal;
	}

	public ArrayList getScopes(){
		return scopes;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setScopes(ArrayList newVal){
		scopes = newVal;
	}

	public ArrayList getFeatures(){
		return features;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFeatures(ArrayList newVal){
		features = newVal;
	}

	public String getLabel(){
		return label;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLabel(String newVal){
		label = newVal;
	}

}