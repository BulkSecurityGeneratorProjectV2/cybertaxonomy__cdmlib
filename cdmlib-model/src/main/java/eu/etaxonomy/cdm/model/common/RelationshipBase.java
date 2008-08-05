/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipBase", propOrder = {
	"relatedFrom",
	"relatedTo",
    "type"
})
@XmlRootElement(name = "RelationshipBase")
@MappedSuperclass
public abstract class RelationshipBase<FROM extends IRelated, TO extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase {

	static Logger logger = Logger.getLogger(RelationshipBase.class);

	@XmlElement(name = "RelatedFrom", type = Taxon.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private FROM relatedFrom;

	@XmlElement(name = "RelatedTo", type = Taxon.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private TO relatedTo;

	@XmlElement(name = "RelationshipType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private TYPE type;

	protected RelationshipBase(){
		super();
	}
	
	/**
	 * Creates a relationship between 2 objects and adds it to the respective
	 * relation sets of both objects.
	 * 
	 * @param from
	 * @param to
	 * @param type
	 * @param citation
	 * @param citationMicroReference
	 */
	protected RelationshipBase(FROM from, TO to, TYPE type, ReferenceBase citation, String citationMicroReference) {
		super(citation, citationMicroReference, null);
		setRelatedFrom(from);
		setRelatedTo(to);
		setType(type);
		from.addRelationship(this);
		to.addRelationship(this);
	}
	
	@ManyToOne
	public TYPE getType(){
		return this.type;
	}
	protected void setType(TYPE type){
		this.type = type;
	}
	
	
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected FROM getRelatedFrom() {
		return relatedFrom;
	}
	protected void setRelatedFrom(FROM relatedFrom) {
		this.relatedFrom = relatedFrom;
	}

	
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected TO getRelatedTo() {
		return relatedTo;
	}
	protected void setRelatedTo(TO relatedTo) {
		this.relatedTo = relatedTo;
	}

	
// TODO
//	UUID toUuid; 
//	UUID fromUuid;
//	
//	@Transient
//	public UUID getToUuidCache(){
//		return relationTo.getUuid();
//	}
//	protected void setToUuid(UUID uuid){
//		toUuid = uuid;
//	}
//	
//	public UUID getFromUuid(){
//		return relationTo.getUuid();
//	}
//	protected void setFromUuid(UUID uuid){
//		fromUuid = uuid;
//	}
}