/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase.SourceType;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class NormalExplicitRow extends ExcelRowBase {
	
	private int id;
	private int parentId;
	private String rank;
	private String scientificName;
	private String author;
	private String nameStatus;
	private String commonName;
	private String language;
	private String reference;
	
	//Sets
	private TreeMap<Integer, String> distributions = new TreeMap<Integer, String>();
	
	private TreeMap<Integer, String> protologues = new TreeMap<Integer, String>();
	
	private TreeMap<Integer, String> images = new TreeMap<Integer, String>();
	
	private Map<UUID, TreeMap<Integer, String>> featureTexts = new HashMap<UUID, TreeMap<Integer, String>>();
	
	private Map<UUID, TreeMap<Integer, SourceDataHolder>> textSources = new HashMap<UUID, TreeMap<Integer, SourceDataHolder>>();

	
	
	public NormalExplicitRow() {
		this.id = 0;
		this.parentId = 0;
		this.rank = "";
		this.scientificName = "";
		this.author =  "";
		this.nameStatus =  "";
		this.commonName =  "";
		this.language =  "";
		this.reference =  "";
	}
	
	public NormalExplicitRow(String name, int parentId) {
		this(name, parentId, null);
	}
	
	public NormalExplicitRow(String scientificName, int parentId, String reference) {
		this.parentId = parentId;
		this.scientificName = scientificName;
		this.reference = reference;
	}
	
// **************************** GETTER / SETTER *********************************/	
	
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	/**
	 * @return the name
	 */
	public String getScientificName() {
		return scientificName;
	}
	/**
	 * @param name the name to set
	 */
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the rank
	 */
	public String getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the nameStatus
	 */
	public String getNameStatus() {
		return nameStatus;
	}

	/**
	 * @param nameStatus the nameStatus to set
	 */
	public void setNameStatus(String nameStatus) {
		this.nameStatus = nameStatus;
	}

	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	public void putDistribution(int key, String distribution){
		this.distributions.put(key, distribution);
	}
	
	public List<String> getDistributions() {
		return getOrdered(distributions);
	}

	public void putProtologue(int key, String protologue){
		this.protologues.put(key, protologue);
	}
	
	public List<String> getProtologues() {
		return getOrdered(protologues);
	}

	public void putImage(int key, String image){
		this.images.put(key, image);
	}
	
	public List<String> getImages() {
		return getOrdered(images);
	}
	

	public void putFeature(UUID featureUuid, int index, String value) {
		TreeMap<Integer, String> featureMap = featureTexts.get(featureUuid);
		if (featureMap == null){
			featureMap = new TreeMap<Integer, String>();
			featureTexts.put(featureUuid, featureMap);
		}
		featureMap.put(index, value);
	}

	public Set<UUID> getFeatures() {
		return featureTexts.keySet();
	}
	
	public List<String> getFeatureTexts(UUID featureUuid) {
		TreeMap<Integer, String> map = featureTexts.get(featureUuid);
		if (map != null){
			return getOrdered(map);
		}else{
			return null;
		}
	}
	

	public void putFeatureSource(UUID featureUuid,	int featureIndex, SourceType refType, String value, int refIndex) {
		//feature Map
		TreeMap<Integer, SourceDataHolder> featureMap = textSources.get(featureUuid);
		if (featureMap == null){
			featureMap = new TreeMap<Integer, SourceDataHolder>();
			textSources.put(featureUuid, featureMap);
		}
		//sourcedText
		SourceDataHolder sourceDataHolder = featureMap.get(featureIndex);
		if (sourceDataHolder == null){
			sourceDataHolder = new SourceDataHolder();
			featureMap.put(featureIndex, sourceDataHolder);
		}
		//
		sourceDataHolder.putSource(refIndex, refType, value);
	}
	

	public SourceDataHolder getFeatureTextReferences(UUID featureUuid, int index) {
		TreeMap<Integer, SourceDataHolder> textMap = textSources.get(featureUuid);
		if (textMap == null){
			return new SourceDataHolder();
		}else{
			SourceDataHolder sourceMap = textMap.get(index);
			return sourceMap;
		}
		
	}

	private List<String> getOrdered(TreeMap<Integer, String> tree) {
		List<String> result = new ArrayList<String>();
		for (String distribution : tree.values()){
			result.add(distribution);
		}
		return result;
	}



	
}
