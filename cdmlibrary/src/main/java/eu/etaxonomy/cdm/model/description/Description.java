/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:20
 */
@Entity
public class Description extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Description.class);
	//in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.
	private String label;
	private ArrayList<FeatureBase> features;
	private ArrayList scopes;
	private ArrayList<ReferenceBase> sources;
	private ArrayList geoScopes;

	public ArrayList<ReferenceBase> getSources(){
		return this.sources;
	}

	/**
	 * 
	 * @param sources    sources
	 */
	public void addSource(ReferenceBase source){
		this.sources.add(source);
	}
	public void removeSource(ReferenceBase source){
		this.sources.remove(source);
	}

	public ArrayList getGeoScopes(){
		return this.geoScopes;
	}

	/**
	 * 
	 * @param geoScopes    geoScopes
	 */
	public void setGeoScopes(ArrayList geoScopes){
		this.geoScopes = geoScopes;
	}

	public ArrayList getScopes(){
		return this.scopes;
	}

	/**
	 * 
	 * @param scopes    scopes
	 */
	public void setScopes(ArrayList scopes){
		this.scopes = scopes;
	}

	public ArrayList<FeatureBase> getFeatures(){
		return this.features;
	}

	/**
	 * 
	 * @param features    features
	 */
	public void addFeature(FeatureBase feature){
		this.features.add(feature);
	}
	public void removeFeature(FeatureBase feature){
		this.features.remove(feature);
	}

	public String getLabel(){
		return this.label;
	}

	/**
	 * 
	 * @param label    label
	 */
	public void setLabel(String label){
		this.label = label;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}