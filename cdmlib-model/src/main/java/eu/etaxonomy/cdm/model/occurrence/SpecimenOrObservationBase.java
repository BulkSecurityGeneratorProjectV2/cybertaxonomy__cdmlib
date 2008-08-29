/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenOrObservationBase", propOrder = {
	"sex",
    "individualCount",
    "lifeStage",
    "descriptions",
    "determinations"
})
@XmlRootElement(name = "SpecimenOrObservationBase")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="SpecimenOrObservationBase", indexes = { @Index(name = "specimenOrObservationBaseTitleCacheIndex", columnNames = { "persistentTitleCache" }) })
public abstract class SpecimenOrObservationBase extends IdentifyableMediaEntity{
	
	private static final Logger logger = Logger.getLogger(SpecimenOrObservationBase.class);
	
	@XmlElementWrapper(name = "Descriptions")
	@XmlElement(name = "Description")
	private Set<DescriptionBase> descriptions = new HashSet<DescriptionBase>();
	
	@XmlElementWrapper(name = "Determinations")
	@XmlElement(name = "Determination")
	private Set<DeterminationEvent> determinations = new HashSet<DeterminationEvent>();
	
	@XmlElement(name = "Sex")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Sex sex;
	
	@XmlElement(name = "LifeStage")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Stage lifeStage;
	
	@XmlElement(name = "IndividualCount")
	private Integer individualCount;
	
	// the verbatim description of this occurrence. Free text usable when no atomised data is available.
	// in conjunction with titleCache which serves as the "citation" string for this object
	private MultilanguageSet description;
	
	// events that created derivedUnits from this unit
	private Set<DerivationEvent> derivationEvents = new HashSet();

	/**
	 * Constructor
	 */
	protected SpecimenOrObservationBase(){
		super();
	}
	
//	@ManyToMany   //FIXME
//	@Cascade( { CascadeType.SAVE_UPDATE })
	@Transient
	public Set<DescriptionBase> getDescriptions() {
		return this.descriptions;
	}
	protected void setDescriptions(Set<DescriptionBase> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescription(DescriptionBase description) {
		this.descriptions.add(description);
	}
	public void removeDescription(DescriptionBase description) {
		this.descriptions.remove(description);
	}
	
	@ManyToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<DerivationEvent> getDerivationEvents() {
		return this.derivationEvents;
	}
	protected void setDerivationEvents(Set<DerivationEvent> derivationEvents) {
		this.derivationEvents = derivationEvents;
	}
	public void addDerivationEvent(DerivationEvent derivationEvent) {
		if (! this.derivationEvents.contains(derivationEvent)){
			this.derivationEvents.add(derivationEvent);
			derivationEvent.addOriginal(this);
		}
	}
	public void removeDerivationEvent(DerivationEvent derivationEvent) {
		this.derivationEvents.remove(derivationEvent);
	}
	


	@OneToMany(mappedBy="identifiedUnit")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DeterminationEvent> getDeterminations() {
		return this.determinations;
	}
	protected void setDeterminations(Set<DeterminationEvent> determinations) {
		this.determinations = determinations;
	}
	public void addDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.add(determination);
	}
	public void removeDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.remove(determination);
	}
	
	
	@ManyToOne
	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	@ManyToOne
	public Stage getLifeStage() {
		return lifeStage;
	}

	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}
	
	
	@Override
	public String generateTitle(){
		return "";
	}


	public Integer getIndividualCount() {
		return individualCount;
	}

	public void setIndividualCount(Integer individualCount) {
		this.individualCount = individualCount;
	}


	public MultilanguageSet getDefinition(){
		return this.description;
	}
	private void setDefinition(MultilanguageSet description){
		this.description = description;
	}
	public void addDefinition(LanguageString description){
		this.description.add(description);
	}
	public void addDefinition(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	public void removeDefinition(Language lang){
		this.description.remove(lang);
	}
	
	
	/**
	 * for derived units get the single next higher parental/original unit.
	 * If multiple original units exist throw error
	 * @return
	 */
	@Transient
	public SpecimenOrObservationBase getOriginalUnit(){
		return null;
	}

	@Transient
	public abstract GatheringEvent getGatheringEvent();
	
}