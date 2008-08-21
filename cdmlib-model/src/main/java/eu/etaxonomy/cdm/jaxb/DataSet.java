package eu.etaxonomy.cdm.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.reference.Article;
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
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"agents",
		"agentData",
	    "terms",
	    "termVocabularies",
	    "references",
	    "taxonomicNames",
	    "taxa",
	    "synonyms",
	    "relationships",
	    "homotypicalGroups"
})
@XmlRootElement(name = "DataSet", namespace = "http://etaxonomy.eu/cdm/model/1.0")
public class DataSet {

    // Some fields are of type List and some are of type Set. 
	// This is mainly because
	// the service classes return lists, i.e.
    // TaxonServiceImpl.getRootTaxa() returns List<Taxon>
	// and the Taxon methods return sets, i.e.
    // Taxon.getTaxonomicChildren() returns Set<Taxon>.

    @XmlElementWrapper(name = "Agents")
    @XmlElements({
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
    protected List<Agent> agents;
    
    @XmlElementWrapper(name = "AgentData")
    @XmlElements({
    @XmlElement(name = "Address", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Address.class),
    @XmlElement(name = "Contact", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Contact.class),
    @XmlElement(name = "InstitutionalMembership", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = InstitutionalMembership.class)
    })
    protected List<VersionableEntity> agentData;

    @XmlElementWrapper(name = "Terms")
    @XmlElements({
    	@XmlElement(name = "Continent", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = Continent.class),
    	@XmlElement(name = "DerivationEventType", namespace = "http://etaxonomy.eu/cdm/model/occurrence/1.0", type = DerivationEventType.class),
    	@XmlElement(name = "Feature", namespace = "http://etaxonomy.eu/cdm/model/description/1.0", type = Feature.class),
    	@XmlElement(name = "HybridRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = HybridRelationshipType.class),
        @XmlElement(name = "Keyword", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Keyword.class),
    	@XmlElement(name = "Language", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = Language.class),
    	@XmlElement(name = "MarkerType", namespace = "http://etaxonomy.eu/cdm/model/common/1.0", type = MarkerType.class),
    	@XmlElement(name = "NamedArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedArea.class),
    	@XmlElement(name = "NamedAreaLevel", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaLevel.class),
    	@XmlElement(name = "NamedAreaType", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = NamedAreaType.class),
    	@XmlElement(name = "NameRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameRelationshipType.class),
    	@XmlElement(name = "NomenclaturalCode", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalCode.class),
    	@XmlElement(name = "NomenclaturalStatusType", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NomenclaturalStatusType.class),
        @XmlElement(name = "Rank", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = Rank.class),
    	@XmlElement(name = "SynonymRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = SynonymRelationshipType.class),
    	@XmlElement(name = "TaxonRelationshipType", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationshipType.class),
    	@XmlElement(name = "TdwgArea", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = TdwgArea.class),
    	@XmlElement(name = "TypeDesignationStatus", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = TypeDesignationStatus.class),
    	@XmlElement(name = "WaterbodyOrCountry", namespace = "http://etaxonomy.eu/cdm/model/location/1.0", type = WaterbodyOrCountry.class)
    })
    protected List<? extends TermBase> terms;

    @XmlElementWrapper(name = "TermVocabularies")
    @XmlElement(name = "TermVocabulary", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    protected List<TermVocabulary> termVocabularies;

    @XmlElementWrapper(name = "References")
    @XmlElements({
    	@XmlElement(name = "Article", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Article.class),
    	@XmlElement(name = "Generic", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Generic.class),
    	@XmlElement(name = "Patent", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Patent.class),
    	@XmlElement(name = "PersonalCommunication", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PersonalCommunication.class),
    	@XmlElement(name = "CdDvd", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = CdDvd.class),
    	@XmlElement(name = "Database", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Database.class),
    	@XmlElement(name = "Journal", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Journal.class),
    	@XmlElement(name = "Map", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Map.class),
    	@XmlElement(name = "Book", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Book.class),
    	@XmlElement(name = "Proceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Proceedings.class),
    	@XmlElement(name = "PrintSeries", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = PrintSeries.class),
    	@XmlElement(name = "Report", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Report.class),
    	@XmlElement(name = "Thesis", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = Thesis.class),
    	@XmlElement(name = "WebPage", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = WebPage.class),
    	@XmlElement(name = "BookSection", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = BookSection.class),
    	@XmlElement(name = "InProceedings", namespace = "http://etaxonomy.eu/cdm/model/reference/1.0", type = InProceedings.class)
    })
    protected List<ReferenceBase> references;

    @XmlElementWrapper(name = "TaxonomicNames")
    @XmlElements({
    	@XmlElement(name = "ZoologicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = ZoologicalName.class),
    	@XmlElement(name = "CultivarPlantName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = CultivarPlantName.class),
    	@XmlElement(name = "BotanicalName", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = BotanicalName.class)
    })
    protected List<TaxonNameBase> taxonomicNames;

    @XmlElementWrapper(name = "Taxa")
    @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Taxon> taxa;
	
    @XmlElementWrapper(name = "Synonyms")
    @XmlElement(name = "Synonym", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Synonym> synonyms;

    @XmlElementWrapper(name = "Relationships")
    @XmlElements({
    	@XmlElement(name = "TaxonRelationship", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = TaxonRelationship.class),
    	@XmlElement(name = "SynonymRelationship", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0", type = SynonymRelationship.class),
    	@XmlElement(name = "NameRelationship", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = NameRelationship.class),
    	@XmlElement(name = "HybridRelationship", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = HybridRelationship.class)
    })
    protected Set<RelationshipBase> relationships;

    @XmlElementWrapper(name = "HomotypicalGroups")
    @XmlElement(name = "HomotypicalGroup", namespace = "http://etaxonomy.eu/cdm/model/name/1.0")
    protected Set<HomotypicalGroup> homotypicalGroups;

    public DataSet () {
    }

    /**
     * Gets the value of the agents property.
     * 
     * @return
     *     possible object is
     *     {@link List<Agent> }
     *     
     */
    public List<Agent> getAgents() {
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
    public void setAgents(List<Agent> value) {
        this.agents = value;
    }

    /**
     * Gets the value of the agentData property.
     * 
     * @return
     *     possible object is
     *     {@link List<VersionableEntity> }
     *     
     */
    public List<VersionableEntity> getAgentData() {
        return agentData;
    }

    /**
     * Sets the value of the agentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<VersionableEntity> }
     *     
     */
    public void setAgentData(List<VersionableEntity> value) {
        this.agentData = value;
    }

    /**
     * Gets the value of the terms property.
     * 
     * @return
     *     possible object is
     *     {@link List<TermBase> }
     *     
     */
    
    public List<? extends TermBase> getTerms() {
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
    public void setTerms(List<? extends TermBase> value) {
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
    
    public List<TermVocabulary> getTermVocabularies() {
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
    public void setTermVocabularies(List<TermVocabulary> value) {
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
     * Gets the value of the taxonomicNames property.
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
     * Sets the value of the taxonomicNames property.
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
     * Gets the value of the taxa property.
     * 
     * @return
     *     possible object is
     *     {@link List<Taxon> }
     *     
     */
//    public List<Taxon> getTaxa() {
//        return taxa;
//    }

    /**
     * Gets the value of the taxa property as {@link Collection<TaxonBase> }
     * 
     * @return
     *     possible object is
     *     {@link Collection<TaxonBase> }
     *     
     */
    public Collection<TaxonBase> getTaxonBases() {
    	
    	Object obj = taxa;
        // FIXME: This is a dirty trick. Replace by getTaxonBases_().
    	Collection<TaxonBase> taxonBases = (Collection<TaxonBase>)obj;
        return taxonBases;
    }

    public Collection<TaxonBase> getTaxonBases_() {
    	
    	Collection<TaxonBase> taxonBases = new HashSet<TaxonBase>();
    	
    	if (taxa != null) {
    		for (Taxon taxon: taxa) {
    			taxonBases.add(taxon);
    		}
    	}
    	if (synonyms != null) {
    		for (Synonym synonym: synonyms) {
    			taxonBases.add(synonym);
    		}
    	}
        return taxonBases;
    }

    /**
     * Sets the value of the taxa property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Taxon> }
     *     
     */
    public void setTaxa(List<Taxon> value) {
        this.taxa = value;
    }

    /**
     * Adds the taxon in value to the taxa property list.
     * 
     * @param value
     *     
     */
    public void addTaxon(Taxon value) {
    		this.taxa.add(value);
    }

    /**
     * Adds the taxa in value to the taxa property list.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection<Taxon> }
     *     
     */
    public void addTaxa(Collection<Taxon> value) {
    	for (Taxon taxon: value) {
    		this.taxa.add(taxon);
    	}
    }

    /**
     * Gets the value of the synonyms property.
     * 
     * @return
     *     possible object is
     *     {@link List<Synonym> }
     *     
     */
    public List<Synonym> getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the value of the synonyms property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Synonym> }
     *     
     */
    public void setSynonyms(List<Synonym> value) {
        this.synonyms = value;
    }
    
    /**
     * Adds the synonym in value to the taxa property list.
     * 
     * @param value
     *     
     */
    public void addSynonym(Synonym value) {
    		this.synonyms.add(value);
    }
    
    /**
     * Adds the synonym in value to the synonyms property list.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection<Synonym> }
     *     
     */
    public void addSynonyms(Collection<Synonym> value) {
    	for (Synonym synonym: value) {
    		this.synonyms.add(synonym);
    	}
    }

    /**
     * Gets the value of the relationships property.
     * 
     * @return
     *     possible object is
     *     {@link Set<RelationshipBase> }
     *     
     */
    public Set<RelationshipBase> getRelationships() {
        return relationships;
    }

    /**
     * Sets the value of the relationships property.
     * 
     * @param value
     *     allowed object is
     *     {@link Set<RelationshipBase> }
     *     
     */
    public void setRelationships(Set<RelationshipBase> value) {
        this.relationships = value;
    }

    /**
     * Adds the relationship in value to the relationships property list.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection<RelationshipBase> }
     *     
     */
    public void addRelationships(Collection<? extends RelationshipBase> value) {
    	for (RelationshipBase relationship: value) {
    		this.relationships.add(relationship);
    	}
    }

    /**
     * Gets the value of the synonyms property.
     * 
     * @return
     *     possible object is
     *     {@link List<Synonym> }
     *     
     */
    public Set<HomotypicalGroup> getHomotypicalGroups() {
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
    public void setHomotypicalGroups(Set<HomotypicalGroup> value) {
        this.homotypicalGroups = value;
    }
    
}
