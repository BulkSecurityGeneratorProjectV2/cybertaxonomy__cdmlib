/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

/**
 * Other names/labels/titles (abreviated or not) for the same object (person,
 * reference, source, etc.)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalSource", propOrder = {
    "idInSource",
    "idNamespace"
})
@XmlRootElement(name = "OriginalSource")
@Entity
public class OriginalSource extends ReferencedEntityBase implements Cloneable{
	
	static Logger logger = Logger.getLogger(OriginalSource.class);
	
	//The object's ID in the source, where the alternative string comes from
	@XmlElement(name = "IdInSource")
	private String idInSource;
	
	@XmlElement(name = "IdNamespace")
	private String idNamespace;
	
	@XmlTransient
	private IdentifiableEntity sourcedObj;

	/**
	 * Factory method
	 * @return
	 */
	public static OriginalSource NewInstance(){
		return new OriginalSource();
	}
	
	public static OriginalSource NewInstance(String id){
		OriginalSource result = new OriginalSource();
		result.setIdInSource(id);
		return result;
	}

	public static OriginalSource NewInstance(String id, String idNamespace){
		OriginalSource result = new OriginalSource();
		result.setIdInSource(id);
		result.setIdNamespace(idNamespace);
		return result;
	}

	
	/**
	 * Constructor
	 */
	public OriginalSource(){
		super();
	}

/*************** GETTER /SETTER ************************************/
	
	public String getIdInSource(){
		return this.idInSource;
	}
	public void setIdInSource(String idInSource){
		this.idInSource = idInSource;
	}


	/**
	 * Returns the id namespace. The id namespace is a String that further defines the origin of
	 * the original record. In the combination with the id it should be unique within one a source. 
	 * E.g. if a record comes from table ABC and has the id 345, 'ABC' is a suitable namespace and the 
	 * combination of 'ABC' and 345 is a unique id for this source. 
	 * The namespace is meant to distinguish import records that come from two different tables, elements, objects, ... 
	 * and end up in the same CDM class. In this case the id may not be enough to identify the original record. 
	 * @return the idNamespace
	 */
	public String getIdNamespace() {
		return idNamespace;
	}

	/**
	 * @param idNamespace the idNamespace to set
	 */
	public void setIdNamespace(String idNamespace) {
		this.idNamespace = idNamespace;
	}


//	@ManyToOne
//	@Cascade(CascadeType.SAVE_UPDATE)
	@Transient //beacause IdentifiableEntity is MappedSuperclass
	public IdentifiableEntity getSourcedObj() {
		return sourcedObj;
	}
	public void setSourcedObj(IdentifiableEntity sourcedObj) {
		this.sourcedObj = sourcedObj;
	}


	
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		OriginalSource result = (OriginalSource)super.clone();
		
		//no changes to: idInSource, sourcedObj
		return result;
	}
	
	/**
	 * Clones this original source and sets the clones sourced object to 'sourceObj'
	 * @see java.lang.Object#clone()
	 */
	public OriginalSource clone(IdentifiableEntity sourcedObj) throws CloneNotSupportedException{
		OriginalSource result = (OriginalSource)clone();
		result.setSourcedObj(sourcedObj);
		return result;
	}

}