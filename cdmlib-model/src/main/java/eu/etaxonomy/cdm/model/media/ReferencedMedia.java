/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
 * @version 1.0
 * @created 08-Nov-2007 13:06:48
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferencedMedia", propOrder = {
    "citationMicroReference",
    "citation"
})
@XmlRootElement(name = "ReferencedMedia")
@Entity
public abstract class ReferencedMedia extends Media implements IReferencedEntity {
	
	static Logger logger = Logger.getLogger(ReferencedMedia.class);
	
	@XmlElement(name = "CitationMicroReference")
	private String citationMicroReference;
	
	@XmlElement(name = "Citation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private ReferenceBase citation;
	

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public ReferenceBase getCitation(){
		return this.citation;
	}
	public void setCitation(ReferenceBase citation){
		this.citation = citation;
	}

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}

}