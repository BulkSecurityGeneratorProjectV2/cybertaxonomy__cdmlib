/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@Entity
public class Taxon extends TaxonBase {
	static Logger logger = Logger.getLogger(Taxon.class);
	private Set<TaxonDescription> descriptions = new HashSet();
	private Set<SynonymRelationship> synonymRelations = new HashSet();
	private Set<TaxonRelationship> taxonRelations = new HashSet();

	public static Taxon NewInstance(TaxonNameBase taxonName, ReferenceBase sec){
		Taxon result = new Taxon();
		result.setName(taxonName);
		result.setSec(sec);
		return result;
	}
	
	//TODO should be private, but still produces Spring init errors
	public Taxon(){
	}

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonDescription> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<TaxonDescription> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescriptions(TaxonDescription description) {
		this.descriptions.add(description);
	}
	public void removeDescriptions(DescriptionBase description) {
		this.descriptions.remove(description);
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	public void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.remove(synonymRelation);
	}
	

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getTaxonRelations() {
		return taxonRelations;
	}
	protected void setTaxonRelations(Set<TaxonRelationship> taxonRelations) {
		this.taxonRelations = taxonRelations;
	}
	public void addTaxonRelation(TaxonRelationship taxonRelation) {
		this.taxonRelations.add(taxonRelation);
	}
	public void removeTaxonRelation(TaxonRelationship taxonRelation) {
		this.taxonRelations.remove(taxonRelation);
	}

	@Transient
	public Set<TaxonRelationship> getIncomingTaxonRelations() {
		// FIXME: filter relations
		return taxonRelations;
	}
	@Transient
	public Set<TaxonRelationship> getOutgoingTaxonRelations() {
		// FIXME: filter relations
		return taxonRelations;
	}


	@Override
	public String generateTitle(){
		return "";
	}

	@Transient
	public Taxon getTaxonomicParent() {
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getFromTaxon().equals(this)){
				return rel.getToTaxon();
			}
		}
		return null;
	}
	@Transient
	public Set<Taxon> getTaxonomicChildren() {
		Set<Taxon> taxa = new HashSet();
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getToTaxon().equals(this)){
				taxa.add(rel.getFromTaxon());
			}
		}
		return taxa;
	}
	@Transient
	public boolean hasTaxonomicChildren(){
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getToTaxon().equals(this)){
				return true;
			}
		}
		return false;
	}
	
	@Transient
	public Set<Synonym> getSynonyms(){
		Set<Synonym> taxa = new HashSet();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			taxa.add(rel.getSynoynm());
		}
		return taxa;
	}
	@Transient
	public Set<Synonym> getSynonymsSortedByType(){
		// FIXME: need to sort synonyms according to type!!!
		return getSynonyms();
	}
	

}