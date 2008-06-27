/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "TaxonRelationship")
@Entity
public class TaxonRelationship extends RelationshipBase<Taxon, Taxon, TaxonRelationshipType> {

	static private final Logger logger = Logger.getLogger(TaxonRelationship.class);

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private TaxonRelationshipType type;
	
	//for hibernate, don't use
	@Deprecated
	private TaxonRelationship(){		
	}
	
	/**
	 * Constructor, creates a new TaxonRelationship and adds it to the "to"-Taxon and the "from"-Taxon. 
	 * @param from Taxon this relationship starts at.
	 * @param to Taxon this relationship points to.
	 * @param type The TaxonRelationshipType this relationship represents.
	 * @param citation This relationship is referenced in this citation
	 * @param citationMicroReference The microreference (page, figur, ...) of the citation 
	 */
	protected TaxonRelationship(Taxon from, Taxon to, TaxonRelationshipType type, ReferenceBase citation, String citationMicroReference) {
		super(from, to, type, citation, citationMicroReference);
	}
	
	
	@Transient
	public Taxon getFromTaxon(){
		return getRelatedFrom();
	}
	public void setFromTaxon(Taxon fromTaxon){
		setRelatedFrom(fromTaxon);
	}

	@Transient
	public Taxon getToTaxon(){
		return getRelatedTo();
	}

	public void setToTaxon(Taxon toTaxon){
		setRelatedTo(toTaxon);
	}

}