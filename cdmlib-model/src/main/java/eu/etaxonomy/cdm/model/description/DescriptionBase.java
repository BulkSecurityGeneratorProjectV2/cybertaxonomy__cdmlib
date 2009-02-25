/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The upmost (abstract) class for a description as a whole (with possibly
 * several {@link DescriptionElementBase elementary information data})
 * for a {@link SpecimenOrObservationBase specimen}, a {@link Taxon taxon}
 * or even a {@link TaxonNameBase taxon name}.
 * <P>
 * This class corresponds to: <ul>
 * <li> DescriptionsSectionType according to the the SDD schema
 * <li> MeasurementOrFact according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionBase", propOrder = {
    "describedSpecimenOrObservations",
    "descriptionSources",
    "descriptiveSystem",
    "elements"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Indexed
public abstract class DescriptionBase extends IdentifiableEntity {
	private static final long serialVersionUID = 5504218413819040193L;
	private static final Logger logger = Logger.getLogger(DescriptionBase.class);
	
	@XmlElementWrapper(name = "DescribedSpecimenOrObservations")
	@XmlElement(name = "DescribedSpecimenOrObservation")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	private Set<SpecimenOrObservationBase> describedSpecimenOrObservations = new HashSet<SpecimenOrObservationBase>();
	
	@XmlElementWrapper(name = "DescriptionSources")
	@XmlElement(name = "DescriptionSource")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)  //FIXME what is the difference between this and IdentifiableEntity.sources
	private Set<ReferenceBase> descriptionSources = new HashSet<ReferenceBase>();
	
	@XmlElementWrapper(name = "DescriptiveSystem")
	@XmlElement(name = "Feature")
	@ManyToMany(fetch = FetchType.LAZY)  //FIXME
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinTable(name = "DescriptionBase_Feature")
	private Set<Feature> descriptiveSystem = new HashSet<Feature>();
	
	@XmlElementWrapper(name = "DescriptionElements")
    @XmlElements({
        @XmlElement(name = "CategorialData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CategoricalData.class),
        @XmlElement(name = "CommonTaxonName", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = CommonTaxonName.class),
        @XmlElement(name = "Distribution", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Distribution.class),
        @XmlElement(name = "IndividualsAssociation", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = IndividualsAssociation.class),
        @XmlElement(name = "QuantitativeData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = QuantitativeData.class),
        @XmlElement(name = "TaxonInteraction", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TaxonInteraction.class),
        @XmlElement(name = "TextData", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextData.class)
    })
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "inDescription")
	@Cascade( { CascadeType.SAVE_UPDATE })
	private Set<DescriptionElementBase> elements = new HashSet<DescriptionElementBase>();

	/**
	 * Returns the set of {@link SpecimenOrObservationBase specimens or observations} involved in
	 * <i>this</i> description as a whole. {@link TaxonDescription Taxon descriptions} are also often based
	 * on concrete specimens or observations. For {@link TaxonNameDescription taxon name descriptions}
	 * this set should be empty.
	 * 
	 * @see    #addDescribedSpecimenOrObservations(SpecimenOrObservationBase)
	 * @see    #removeDescribedSpecimenOrObservations(SpecimenOrObservationBase)
	 */
	public Set<SpecimenOrObservationBase> getDescribedSpecimenOrObservations() {
		return describedSpecimenOrObservations;
	}
	
	/**
	 * Adds an existing {@link SpecimenOrObservationBase specimen or observation} to the set of
	 * {@link #getDescribedSpecimenOrObservations() specimens or observations} described in <i>this</i>
	 * description or which <i>this</i> description is based on.<BR>
	 * Due to bidirectionality if <i>this</i> description is a {@link SpecimenDescription specimen description},
	 * <i>this</i> description will also be added to the set of specimen
	 * descriptions corresponding to the given additional specimen or observation.
	 * 
	 * @param describedSpecimenOrObservation	the specimen or observation to be added to <i>this</i> description
	 * @see    	   								#getDescribedSpecimenOrObservations()
	 * @see    	   								SpecimenOrObservationBase#addDescription(DescriptionBase)
	 */
	public void addDescribedSpecimenOrObservations(SpecimenOrObservationBase describedSpecimenOrObservation) {
		logger.debug("addDescribedSpecimenOrObservations");
		this.describedSpecimenOrObservations.add(describedSpecimenOrObservation);
	}
	
	/** 
	 * Removes one element from the set of {@link #getDescribedSpecimenOrObservations() specimens or observations} involved
	 * in <i>this</i> description.<BR>
	 * Due to bidirectionality if <i>this</i> description is a {@link SpecimenDescription specimen description},
	 * <i>this</i> description will also be removed from the set of specimen
	 * descriptions corresponding to the given specimen or observation.
	 *
	 * @param  describedSpecimenOrObservation   the specimen or observation which should be removed
	 * @see     		  						#getDescribedSpecimenOrObservations()
	 * @see     		  						#addDescribedSpecimenOrObservations(SpecimenOrObservationBase)
	 * @see     		  						SpecimenOrObservationBase#removeDescription(DescriptionBase)
	 */
	public void removeDescribedSpecimenOrObservations(SpecimenOrObservationBase describedSpecimenOrObservation) {
		this.describedSpecimenOrObservations.remove(describedSpecimenOrObservation);
	}

	/**
	 * Returns the set of {@link ReferenceBase references} used as sources for <i>this</i> description as a
	 * whole. More than one source can be used for a general description without
	 * assigning for each data element of the description one of those sources. 
	 * 
	 * @see    #addDescriptionSource(ReferenceBase)
	 * @see    #removeDescriptionSource(ReferenceBase)
	 */
	public Set<ReferenceBase> getDescriptionSources() {
		return this.descriptionSources;
	}
	
	/**
	 * Adds an existing {@link ReferenceBase reference} to the set of
	 * {@link #getDescriptionSources() references} used as sources for <i>this</i>
	 * description.
	 * 
	 * @param descriptionSource	the reference source to be added to <i>this</i> description
	 * @see    	   				#getDescriptionSources()
	 */
	public void addDescriptionSource(ReferenceBase descriptionSource) {
		this.descriptionSources.add(descriptionSource);
	}
	
	/** 
	 * Removes one element from the set of {@link #getDescriptionSources() references} used as
	 * sources for <i>this</i> description.
	 *
	 * @param  descriptionSource	the reference source which should be deleted
	 * @see     		  			#getDescriptionSources()
	 * @see     		  			#addDescriptionSource(ReferenceBase)
	 */
	public void removeDescriptionSource(ReferenceBase descriptionSource) {
		this.descriptionSources.remove(descriptionSource);
	}

	/**
	 * Returns the set of {@link Feature feature} used as 
	 * features/characters/descriptors for <i>this</i> description.
	 * 
	 * @see    #addFeature(Feature)
	 * @see    #removeFeature(Feature)
	 */
	@ManyToMany(fetch = FetchType.LAZY)  //FIXME
	@Cascade( { CascadeType.SAVE_UPDATE })
	@JoinTable(name = "DescriptionBase_Feature")
	public Set<Feature> getDescriptiveSystem() {
		return this.descriptiveSystem;
	}
	
	/** 
	 * @see    #getDescriptiveSystem()
	 * @see    #addDescriptiveSystem(Feature)
	 */
	public void setDescriptiveSystem(Set<Feature> descriptiveSystem) {
		this.descriptiveSystem = descriptiveSystem;
	}
	
	/**
	 * Adds an existing {@link Feature feature} to the set of
	 * {@link #getDescriptiveSystem() descriptiveSystem} used as features for
	 * <i>this</i> description.
	 * 
	 * @param feature	the feature to be added to the descriptive system
	 * @see     #getDescriptiveSystem()
	 */
	public void addFeature(Feature feature) {
		this.descriptiveSystem.add(feature);
	}
	
	/** 
	 * Removes one element from the set of {@link #getDescriptiveSystem() features} used as
	 * features for <i>this</i> description.
	 *
	 * @param  feature	the feature which should be deleted
	 * @see     #getDescriptiveSystem()
	 * @see     addFeature(Feature)
	 */
	public void removeFeature(Feature feature) {
		this.descriptiveSystem.remove(feature);
	}
	
	/**
	 * Returns the set of {@link DescriptionElementBase elementary description data} which constitute
	 * <i>this</i> description as a whole. 
	 * 
	 * @see    #addElement(DescriptionElementBase)
	 * @see    #removeElement(DescriptionElementBase)
	 */
	public Set<DescriptionElementBase> getElements() {
		return this.elements;
	}

	/**
	 * Adds an existing {@link DescriptionElementBase elementary description} to the set of
	 * {@link #getElements() elementary description data} which constitute <i>this</i>
	 * description as a whole.
	 * 
	 * @param element	the elementary description to be added to <i>this</i> description
	 * @see    	   		#getDescriptionSources()
	 */
	public void addElement(DescriptionElementBase element) {
		element.setInDescription(this);
		this.elements.add(element);
	}

	/** 
	 * Removes one element from the set of {@link #getElements() elementary description data} which
	 * constitute <i>this</i> description as a whole.
	 *
	 * @param  element	the reference source which should be deleted
	 * @see     		#getElements()
	 * @see     		#addElement(DescriptionElementBase)
	 */
	public void removeElement(DescriptionElementBase element) {
		this.elements.remove(element);
		element.setInDescription(null);
	}
	
	/**
	 * Returns the number of {@link DescriptionElementBase elementary description data} which constitute
	 * <i>this</i> description as a whole. This is the cardinality of the set of
	 * elementary description data.
	 * 
	 * @see		#getElements()
	 * @return	the number of elements of the elementary description data set
	 */
	public int size(){
		return this.elements.size();
	}
	
	/**
	 * Generates a string that identifies <i>this</i> description.
	 * This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link IdentifiableEntity IdentifiableEntity}.
	 *
	 * @return  the string identifying <i>this</i> description
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 */
	@Override
	public String generateTitle() {
		//TODO generate title "generate Title not yet implemented"
		return this.toString();
	}
}
