/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionType;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationModifier;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BibtexEntryType;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.CdDvd;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.InProceedings;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Map;
import eu.etaxonomy.cdm.model.reference.Patent;
import eu.etaxonomy.cdm.model.reference.PersonalCommunication;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.Proceedings;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.Report;
import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.babadshanjan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSet", propOrder = {
	    "terms",
	    "termVocabularies",
		"agents",
		"collections",
		"occurrences",
		"eventBases",
	    "references",
	    "typeDesignations",
	    "featureTrees",
	    "taxonomicNames",
	    "homotypicalGroups",
	    "taxonBases",
	    "media"
})
@XmlRootElement(name = "DataSet")
public class DataSet {

    @XmlElementWrapper(name = "Agents")
    @XmlElements({             
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
    protected List<AgentBase> agents = new ArrayList<AgentBase>();

    @XmlElementWrapper(name = "FeatureTrees")
    @XmlElement(name = "FeatureTree", namespace = "http://etaxonomy.eu/cdm/model/description/1.0")
    protected List<FeatureTree> featureTrees = new ArrayList<FeatureTree>();
    
    @XmlElementWrapper(name = "Terms")
    @XmlElements({
    	@XmlElement(name = "AbsenceTerm", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = AbsenceTerm.class),
    	@XmlElement(name = "AnnotationType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = AnnotationType.class),
    	@XmlElement(name = "BibtexEntryType", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = BibtexEntryType.class),
    	@XmlElement(name = "Continent", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = Continent.class),
    	@XmlElement(name = "DerivationEventType", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivationEventType.class),
    	@XmlElement(name = "DeterminationModifier", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DeterminationModifier.class),
    	@XmlElement(name = "ExtensionType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = ExtensionType.class),
    	@XmlElement(name = "Feature", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Feature.class),
    	@XmlElement(name = "HybridRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = HybridRelationshipType.class),
    	@XmlElement(name = "InstitutionType", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = InstitutionType.class),
        @XmlElement(name = "Keyword", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Keyword.class),
    	@XmlElement(name = "Language", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Language.class),
    	@XmlElement(name = "MarkerType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = MarkerType.class),
    	@XmlElement(name = "MeasurementUnit", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = MeasurementUnit.class),
    	@XmlElement(name = "Modifier", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Modifier.class),
    	@XmlElement(name = "NamedArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedArea.class),
    	@XmlElement(name = "NamedAreaLevel", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaLevel.class),
    	@XmlElement(name = "NamedAreaType", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaType.class),
    	@XmlElement(name = "NameRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameRelationshipType.class),
    	@XmlElement(name = "NomenclaturalCode", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalCode.class),
    	@XmlElement(name = "NomenclaturalStatusType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalStatusType.class),
    	@XmlElement(name = "PresenceTerm", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = PresenceTerm.class),
    	@XmlElement(name = "PreservationMethod", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = PreservationMethod.class),
        @XmlElement(name = "Rank", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = Rank.class),
    	@XmlElement(name = "ReferenceSystem", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = ReferenceSystem.class),
    	@XmlElement(name = "RightsTerm", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = RightsTerm.class),
    	@XmlElement(name = "Scope", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Scope.class),
    	@XmlElement(name = "Sex", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Sex.class),
    	@XmlElement(name = "Stage", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Stage.class),
    	@XmlElement(name = "State", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = State.class),
    	@XmlElement(name = "StatisticalMeasure", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = StatisticalMeasure.class),
    	@XmlElement(name = "SynonymRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = SynonymRelationshipType.class),
    	@XmlElement(name = "TaxonRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationshipType.class),
    	@XmlElement(name = "TextFormat", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = TextFormat.class),
    	@XmlElement(name = "TypeDesignationStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = TypeDesignationStatus.class),
    	@XmlElement(name = "WaterbodyOrCountry", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = WaterbodyOrCountry.class)
    })
    protected List<DefinedTermBase> terms = new ArrayList<DefinedTermBase>();

    @XmlElementWrapper(name = "TermVocabularies")
    @XmlElements({
        @XmlElement(name = "TermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = TermVocabulary.class),
        @XmlElement(name = "OrderedTermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = OrderedTermVocabulary.class)
    })
    protected List<TermVocabulary<DefinedTermBase>> termVocabularies = new ArrayList<TermVocabulary<DefinedTermBase>>();

    @XmlElementWrapper(name = "Collections")
    @XmlElement(name = "Collection", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0")
    protected List<eu.etaxonomy.cdm.model.occurrence.Collection> collections = new ArrayList<eu.etaxonomy.cdm.model.occurrence.Collection>();
    
    @XmlElementWrapper(name = "Occurrences")
    @XmlElements({
    	@XmlElement(name = "DerivedUnit", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivedUnit.class),
    	@XmlElement(name = "DnaSample", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DnaSample.class),
    	@XmlElement(name = "FieldObservation", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = FieldObservation.class),
    	@XmlElement(name = "Fossil", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Fossil.class),
    	@XmlElement(name = "LivingBeing", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = LivingBeing.class),
    	@XmlElement(name = "Observation", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Observation.class),
    	@XmlElement(name = "Specimen", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = Specimen.class)
    })
    protected List<SpecimenOrObservationBase> occurrences = new ArrayList<SpecimenOrObservationBase>();
    
    @XmlElementWrapper(name = "EventBases")
    @XmlElements({
    	@XmlElement(name = "DerivationEvent", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivationEvent.class),
    	@XmlElement(name = "GatheringEvent", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = GatheringEvent.class)
    })
    protected List<EventBase> eventBases = new ArrayList<EventBase>();
    
    @XmlElementWrapper(name = "References")
    @XmlElements({
    	@XmlElement(name = "Article", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Article.class),
    	@XmlElement(name = "Book", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Book.class),
    	@XmlElement(name = "BookSection", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = BookSection.class),
    	@XmlElement(name = "CdDvd", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = CdDvd.class),
    	@XmlElement(name = "Database", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Database.class),
    	@XmlElement(name = "Generic", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Generic.class),
    	@XmlElement(name = "InProceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = InProceedings.class),
    	@XmlElement(name = "Journal", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Journal.class),
    	@XmlElement(name = "Map", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Map.class),
    	@XmlElement(name = "Patent", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Patent.class),
    	@XmlElement(name = "PersonalCommunication", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PersonalCommunication.class),
    	@XmlElement(name = "PrintSeries", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PrintSeries.class),
    	@XmlElement(name = "Proceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Proceedings.class),
    	@XmlElement(name = "Report", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Report.class),
    	@XmlElement(name = "Thesis", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Thesis.class),
    	@XmlElement(name = "WebPage", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = WebPage.class)
    })
    protected List<ReferenceBase> references = new ArrayList<ReferenceBase>();

    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElements({
    	@XmlElement(name = "NameTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameTypeDesignation.class),
    	@XmlElement(name = "SpecimenTypeDesignation", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = SpecimenTypeDesignation.class)
    })
    protected List<TypeDesignationBase> typeDesignations = new ArrayList<TypeDesignationBase>();
    	
    @XmlElementWrapper(name = "TaxonomicNames")
    @XmlElements({
    	@XmlElement(name = "BacterialName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BacterialName.class),
    	@XmlElement(name = "BotanicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BotanicalName.class),
    	@XmlElement(name = "CultivarPlantName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = CultivarPlantName.class),
    	@XmlElement(name = "NonViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NonViralName.class),
    	@XmlElement(name = "ViralName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ViralName.class),
    	@XmlElement(name = "ZoologicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ZoologicalName.class)
    })
    protected List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();

    @XmlElementWrapper(name = "TaxonBases")
    @XmlElements({
      @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Taxon.class),
      @XmlElement(name = "Synonym", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = Synonym.class)
    })
    protected List<TaxonBase> taxonBases = new ArrayList<TaxonBase>();

    @XmlElementWrapper(name = "Media")
    @XmlElements({
      @XmlElement(name = "Media", namespace = "http://etaxonomy.eu/cdm/model/media/1.0"),
      @XmlElement(name = "IdentificationKey", namespace = "http://etaxonomy.eu/cdm/model/description/1.0")
    })
    protected List<Media> media = new ArrayList<Media>();
    
    @XmlElementWrapper(name = "HomotypicalGroups")
    @XmlElement(name = "HomotypicalGroup", namespace = "http://etaxonomy.eu/cdm/model/name/1.0")
    protected List<HomotypicalGroup> homotypicalGroups = new ArrayList<HomotypicalGroup>();

    /**
     * Gets the value of the agents property.
     * 
     * @return
     *     possible object is
     *     {@link List<Agent> }
     *     
     */
    public List<AgentBase> getAgents() {
        return agents;
    }

    /**
     * Sets the value of the agents property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Agent> }
     *     
     */
    public void setAgents(List<AgentBase> value) {
        this.agents = value;
    }

    /**
     * Gets the value of the collections property.
     * 
     * @return
     *     possible object is
     *     {@link List<eu.etaxonomy.cdm.model.occurrence.Collection> }
     *     
     */
    public List<eu.etaxonomy.cdm.model.occurrence.Collection> getCollections() {
        return collections;
    }

    /**
     * Sets the value of the collections property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<eu.etaxonomy.cdm.model.occurrence.Collection> }
     *     
     */
    public void setCollections(List<eu.etaxonomy.cdm.model.occurrence.Collection> value) {
        this.collections = value;
    }

    /**
     * Gets the value of the terms property.
     * 
     * @return
     *     possible object is
     *     {@link List<TermBase> }
     *     
     */
    public List<DefinedTermBase> getTerms() {
        return terms;
    }

    /**
     * Sets the value of the terms property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<TermBase> }
     *     
     */
    public void setTerms(List<DefinedTermBase> value) {
        this.terms = value;
    }

    /**
     * Gets the value of the term vocabularies property.
     * 
     * @return
     *     possible object is
     *     {@link List<TermVocabulary> }
     *     
     */
    
    public List<TermVocabulary<DefinedTermBase>> getTermVocabularies() {
        return termVocabularies;
    }

    /**
     * Sets the value of the term vocabularies property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<TermVocabulary> }
     *     
     */
    public void setTermVocabularies(List<TermVocabulary<DefinedTermBase>> value) {
        this.termVocabularies = value;
    }

    /**
     * Gets the value of the taxonomicNames property.
     * 
     * @return
     *     possible object is
     *     {@link List<axonNameBase> }
     *     
     */
    public List<TaxonNameBase> getTaxonomicNames() {
        return taxonomicNames;
    }

    /**
     * Sets the value of the taxonomicNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<TaxonNameBase> }
     *     
     */
    public void setTaxonomicNames(List<TaxonNameBase> value) {
        this.taxonomicNames = value;
    }
    
    /**
     * Gets the value of the eventBases property.
     * 
     * @return
     *     possible object is
     *     {@link List<EventBase> }
     *     
     */
    public List<EventBase> getEventBases() {
        return eventBases;
    }

    /**
     * Sets the value of the eventBases property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<EventBase> }
     *     
     */
    public void setEventBases(List<EventBase> value) {
        this.eventBases = value;
    }

    /**
     * Gets the value of the occurrences property.
     * 
     * @return
     *     possible object is
     *     {@link List<SpecimenOrObservationBase> }
     *     
     */
    public List<SpecimenOrObservationBase> getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the value of the occurrences property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<SpecimenOrObservationBase> }
     *     
     */
    public void setOccurrences(List<SpecimenOrObservationBase> value) {
        this.occurrences = value;
    }

    /**
     * Gets the value of the references property.
     * 
     * @return
     *     possible object is
     *     {@link List<ReferenceBase> }
     *     
     */
    public List<ReferenceBase> getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<ReferenceBase> }
     *     
     */
    public void setReferences(List<ReferenceBase> value) {
        this.references = value;
    }

    /**
     * Gets the value of the featureTrees property.
     * 
     * @return
     *     possible object is
     *     {@link List<FeatureTree> }
     *     
     */
    public List<FeatureTree> getFeatureTrees() {
        return featureTrees;
    }

    /**
     * Sets the value of the featureTrees property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<FeatureTree> }
     *     
     */
    public void setFeatureTrees(List<FeatureTree> value) {
    	this.featureTrees = value;
    }
    
    
    /**
     * Adds the taxonBases in value to the taxonBases property list.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection<TaxonBase> }
     *     
     */
    public void addTaxonBases(Collection<TaxonBase> value) {
    	this.taxonBases.addAll(value);
    }

    /**
     * Gets the value of the taxonBases property as {@link Collection<TaxonBase> }
     * 
     * @return
     *     possible object is
     *     {@link List<TaxonBase> }
     *     
     */
    public List<TaxonBase> getTaxonBases() {
    	return taxonBases;
    }

    /**
     * Sets the value of the taxonBases property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<TaxonBase> }
     *     
     */
    public void setTaxonBases(List<TaxonBase> value) {
        this.taxonBases = value;
    }

    /**
     * Adds the taxonBase in value to the taxonBases property list.
     * 
     * @param value
     *     
     */
    public void addTaxonBase(TaxonBase value) {
    		this.taxonBases.add(value);
    }

    /**
     * Adds the media in value to the media property list.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection<VersionableEntity> }
     *     
     */
    public <T extends Media> void addMedia(Collection<T> value) {
    	for (T medium: value) {
    		this.media.add(medium);
    	}
    }

    /**
     * Gets the value of the  property.
     * 
     * @return
     *     possible object is
     *     {@link List<ReferencedEntityBase> }
     *     
     */
    public List<Media> getMedia() {
        return media;
    }

    /**
     * Sets the value of the referencedEntities property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<ReferencedEntityBase> }
     *     
     */
    public void setMedia(List<Media> value) {
        this.media = new ArrayList<Media>();
        media.addAll(value);
    }
    
    /**
     * Gets the value of the synonyms property.
     * 
     * @return
     *     possible object is
     *     {@link List<Synonym> }
     *     
     */
    public List<HomotypicalGroup> getHomotypicalGroups() {
        return homotypicalGroups;
    }

    /**
     * Sets the value of the synonyms property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Synonym> }
     *     
     */
    public void setHomotypicalGroups(List<HomotypicalGroup> value) {
        this.homotypicalGroups = value;
    }

    public List<TypeDesignationBase> getTypeDesignations() {
    	return typeDesignations;
    }
    
	public void addTypeDesignations(List<TypeDesignationBase> typeDesignations) {
		this.typeDesignations.addAll(typeDesignations);
	}
    
}
