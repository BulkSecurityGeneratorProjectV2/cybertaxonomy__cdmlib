/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.common;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @date 13.07.2011
 */
public abstract class ExcelRowBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelRowBase.class);

	private UUID cdmUuid = null; 

	private String ecology;
	private String plantDescription;


//	private String family;
//	private String genus;
//	private String specificEpithet;
	
	
	private TreeMap<Integer, IdentifiableSource> sources = new TreeMap<Integer, IdentifiableSource>();
	private TreeMap<Integer, SpecimenTypeDesignation> types = new TreeMap<Integer, SpecimenTypeDesignation>();
	private List<PostfixTerm> extensions  = new ArrayList<PostfixTerm>(); 
	

	
	public ExcelRowBase() {
	}
	
	public class PostfixTerm{
		public PostfixTerm(){}
		public String term;
		public String postfix;
	}
	

	
// **************************** GETTER / SETTER *********************************/	
	

	public void setCdmUuid(UUID cdmUuid) {
		this.cdmUuid = cdmUuid;
	}


	public UUID getCdmUuid() {
		return cdmUuid;
	}

//	
//	/**
//	 * @return the author
//	 */
//	public String getAuthor() {
//		return author;
//	}
//
//
//	/**
//	 * @param author the author to set
//	 */
//	public void setAuthor(String author) {
//		this.author = author;
//	}



	/**
	 * @return the ecology
	 */
	public String getEcology() {
		return ecology;
	}


	/**
	 * @param ecology the ecology to set
	 */
	public void setEcology(String ecology) {
		this.ecology = ecology;
	}


	/**
	 * @return the plantDescription
	 */
	public String getPlantDescription() {
		return plantDescription;
	}


	/**
	 * @param plantDescription the plantDescription to set
	 */
	public void setPlantDescription(String plantDescription) {
		this.plantDescription = plantDescription;
	}

	public void putIdInSource(int key, String id){
		IdentifiableSource source = getOrMakeSource(key);
		source.setIdInSource(id);
	}
	public void putSourceReference(int key, Reference<?> reference){
		IdentifiableSource source = getOrMakeSource(key);
		source.setCitation(reference);
	}

	public List<IdentifiableSource> getSources() {
		return getOrdered(sources);
	}
	
	
	/**
	 * @param key
	 * @return
	 */
	private IdentifiableSource getOrMakeSource(int key) {
		IdentifiableSource  source = sources.get(key);
		if (source == null){
			source = IdentifiableSource.NewInstance();
			sources.put(key, source);
		}
		return source;
	}
	

	public void putTypeCategory(int key, SpecimenTypeDesignationStatus status){
		SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
		designation.setTypeStatus(status);
	}
	public void putTypifiedName(int key, TaxonNameBase<?,?> name){
		if (name != null){
			SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
			name.addTypeDesignation(designation, false);
		}
	}

	public List<SpecimenTypeDesignation> getTypeDesignations() {
		return getOrdered(types);
	}


	private SpecimenTypeDesignation getOrMakeTypeDesignation(int key) {
		SpecimenTypeDesignation designation = types.get(key);
		if (designation == null){
			designation = SpecimenTypeDesignation.NewInstance();
			types.put(key, designation);
		}
		return designation;
	}

	private<T extends Object> List<T> getOrdered(TreeMap<?, T> tree) {
		List<T> result = new ArrayList<T>();
		for (T value : tree.values()){
			result.add(value);
		}
		return result;
	}

	public void addExtension(String levelPostfix, String value) {
		PostfixTerm term = new PostfixTerm();
		term.term = value;
		term.postfix = levelPostfix;
		this.extensions.add(term);
	}
	
	public List<PostfixTerm> getExtensions(){
		return extensions;
	}

	
}
