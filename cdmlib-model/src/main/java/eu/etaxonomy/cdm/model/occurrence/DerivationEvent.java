/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.EventBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivationEvent", propOrder = {
    "originals",
    "derivatives",
    "type"
})
@XmlRootElement(name = "DerivationEvent")
@Entity
public class DerivationEvent extends EventBase implements Cloneable{
	
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	@XmlElementWrapper(name = "Originals")
	@XmlElement(name = "Original")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<SpecimenOrObservationBase> originals = getNewOriginalsSet();
	
	@XmlElementWrapper(name = "Derivatives")
	@XmlElement(name = "Derivative")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Set<DerivedUnitBase> derivatives = getNewDerivatesSet();
	
	@XmlElement(name = "DerivationEventType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private DerivationEventType type;
	
	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEvent NewInstance(){
		return new DerivationEvent();
	}
	
	/**
	 * Constructor
	 */
	protected DerivationEvent() {
		super();
	}
	
	@ManyToMany(mappedBy="derivationEvents")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenOrObservationBase> getOriginals() {
		return originals;
	}
	protected void setOriginals(Set<SpecimenOrObservationBase> originals) {
		this.originals = originals;
	}
	public void addOriginal(SpecimenOrObservationBase original) {
		if (! this.originals.contains(original)){
			this.originals.add(original);
			original.addDerivationEvent(this);
		}
	}
	public void removeOriginal(SpecimenOrObservationBase original) {
		this.originals.remove(original);
	}
	
	
	@OneToMany(mappedBy="derivationEvent")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DerivedUnitBase> getDerivatives() {
		return derivatives;
	}
	protected void setDerivatives(Set<DerivedUnitBase> derivatives) {
		this.derivatives = derivatives;
	}
	public void addDerivative(DerivedUnitBase derivative) {
		if (derivative != null){
			derivative.setDerivedFrom(this);
		}
	}
	public void removeDerivative(DerivedUnitBase derivative) {
		if (derivative != null){
			derivative.setDerivedFrom(null);
		}
	}

	
	@ManyToOne
	public DerivationEventType getType() {
		return type;
	}
	public void setType(DerivationEventType type) {
		this.type = type;
	}
	
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> derivation event. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> derivation event
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link EventBase EventBase}.
	 * 
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DerivationEvent clone(){
		try{
			DerivationEvent result = (DerivationEvent)super.clone();
			//type
			result.setType(this.getType());
			//derivates
			Set<DerivedUnitBase> derivates = getNewDerivatesSet();
			derivates.addAll(this.derivatives);
			result.setDerivatives(derivates);
			//originals
			Set<SpecimenOrObservationBase> originals = getNewOriginalsSet();
			originals.addAll(this.originals);
			result.setOriginals(this.getOriginals());
			//no changes to: -
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
	
	@Transient
	private static Set<DerivedUnitBase> getNewDerivatesSet(){
		return new HashSet<DerivedUnitBase>();
	}

	@Transient
	private static Set<SpecimenOrObservationBase> getNewOriginalsSet(){
		return new HashSet<SpecimenOrObservationBase>();
	}
	
}
