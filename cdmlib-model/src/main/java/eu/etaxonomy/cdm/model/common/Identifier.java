// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Field;

import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * @author a.mueller
 * @date 2014-06-30
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identifier", propOrder = {
    "identifier",
    "type",
    "identifiedObj"    
})
@Entity
@Audited
@Table(appliesTo="Identifier", indexes = { @Index(name = "identifierIndex", columnNames = { "identifier" }) })
public class Identifier<T extends IdentifiableEntity<?>> extends AnnotatableEntity implements Cloneable {
	private static final long serialVersionUID = 3337567049024506936L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Identifier.class);


	@XmlElement(name ="Identifier" )
	@Column(length=800, name="identifier")
	@Field
    @NullOrNotEmpty
	private String identifier;
	
    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded(depth=1)
	private DefinedTerm type;

    @XmlElement(name = "IdentifiedObject")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Any(metaDef = "CdmBase",
	    	 metaColumn=@Column(name = "identifiedObj_type"),
	    	 fetch = FetchType.LAZY,
	    	 optional = false)
	@JoinColumn(name = "identifiedObj_id")
	@NotAudited
	private T identifiedObj;
    
// **************************** FACTORY ******************************/    
    
    public static <T extends IdentifiableEntity<?>> Identifier<T> NewInstance(T entity, String identifier, DefinedTerm type){
    	return new Identifier<T>(entity, identifier, type);
    }

// ************************* CONSTRUCTOR ************************************    
    
    @Deprecated  //for hibernate use only
    protected Identifier(){};
    
    public Identifier (T entity, String identifier, DefinedTerm type){
    	this.identifier = identifier;
    	this.type = type;
//    	logger.warn("Identified object not yet implemented");
    	this.identifiedObj = entity;
    	identifiedObj.addIdentifier(this);
    }

    
// ****************** GETTER / SETTER **********************/    
    
	public String getIdentifier() {
		return identifier;
	}


	public void setIdentifier(String identifier) {
		this.identifier = StringUtils.isBlank(identifier) ? null : identifier;
	}


	/**
	 * The identifier type. E.g. DOI, LSID, Barcode, Sample Designation, ...
	 * @see TermType#IdentifierType 
	 * @return
	 */
	public DefinedTerm getType() {
		return type;
	}


	public void setType(DefinedTerm identifierType) {
		this.type = identifierType;
	}
	
	public T getIdentifiedObj() {
		return identifiedObj;
	}
	protected void setIdentifiedObj(T identifiedObj) {
		this.identifiedObj = identifiedObj;
	}
	
	//****************** CLONE ************************************************/
	 
		@Override
		public Object clone() throws CloneNotSupportedException{
			Identifier<?> result = (Identifier<?>)super.clone();	
			//no changes to: type, value
			return result;
		}
	
	/**
	 * Clones this extension and sets the clone's extended object to 'extendedObject'
	 * @see java.lang.Object#clone()
	 */
	public <T extends IdentifiableEntity<?>> Identifier<T> clone(T identifiedObject) throws CloneNotSupportedException{
		Identifier<T> result = (Identifier<T>)clone();
		result.setIdentifiedObj(identifiedObject);
		return result;
	}
	
}
