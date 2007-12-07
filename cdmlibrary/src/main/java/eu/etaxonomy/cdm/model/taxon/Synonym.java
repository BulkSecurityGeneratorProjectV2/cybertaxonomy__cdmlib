/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@Entity
public class Synonym extends TaxonBase {
	static Logger logger = Logger.getLogger(Synonym.class);
	private Set<SynonymRelationship> synonymRelations = new HashSet();


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	public void addSynoynmRelations(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	public void removeSynoynmRelations(SynonymRelationship synonymRelation) {
		this.synonymRelations.remove(synonymRelation);
	}


	@Override
	public String generateTitle(){
		return "";
	}

	@Transient
	public Set<Taxon> getAcceptedTaxa() {
		Set<Taxon>taxa=new HashSet();
		for (SynonymRelationship rel:getSynonymRelations()){
			taxa.add(rel.getAcceptedTaxon());
		}
		return taxa;
	}

}