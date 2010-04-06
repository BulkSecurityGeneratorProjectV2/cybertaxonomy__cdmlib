// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/


// NOT YET IN USE //
package eu.etaxonomy.cdm.model.reference;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 30.06.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceIdentity", propOrder = {
	"preferredLongForm",
	"preferredAbbreviation",
    "references"
})
@Audited
//@Entity
public class ReferenceIdentity extends VersionableEntity {
	private static final long serialVersionUID = -6114973116800471106L;
	private static final Logger logger = Logger.getLogger(ReferenceIdentity.class);
	
	@XmlElementWrapper(name = "References")
    @XmlElement(name = "Reference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="name", fetch= FetchType.LAZY)
	private Set<ReferenceBase> references;
	
	@XmlElement(name = "PreferredLongForm")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	//@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private ReferenceBase preferredLongForm;


	@XmlElement(name = "PreferredAbbreviation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	//@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private ReferenceBase preferredAbbreviation;
	
	
	/**
	 * @return the references
	 */
	public Set<ReferenceBase> getReferences() {
		return references;
	}


//	/**
//	 * Adds the reference to this reference identity
//	 * @param reference
//	 * @return
//	 */
//	public boolean addReference(ReferenceBase reference){
//		if (reference == null){
//			return false;
//		}
//		if (reference.getReferenceIdentity() != null){
//			reference.setReferenceIdentity(this);
//		}
//		return this.references.add(reference);
//	}
//	
//	public boolean removeReference(ReferenceBase reference){
//		if (reference == null){
//			return false;
//		}
//		reference.setReferenceIdentity(null);
//		return this.references.remove(reference);
//	}
	

	/**
	 * @return the preferredLongForm
	 */
	public ReferenceBase getPreferredLongForm() {
		return preferredLongForm;
	}

	/**
	 * @param preferredLongForm the preferredLongForm to set
	 */
	public void setPreferredLongForm(ReferenceBase preferredLongForm) {
		this.preferredLongForm = preferredLongForm;
	}

	/**
	 * @return the preferredAbbreviation
	 */
	public ReferenceBase getPreferredAbbreviation() {
		return preferredAbbreviation;
	}

	/**
	 * @param preferredAbbreviation the preferredAbbreviation to set
	 */
	public void setPreferredAbbreviation(ReferenceBase preferredAbbreviation) {
		this.preferredAbbreviation = preferredAbbreviation;
	}
	
	
	
}
