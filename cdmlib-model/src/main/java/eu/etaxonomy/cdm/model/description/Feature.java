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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class for individual properties (also designed as character, type or
 * category) of observed phenomena able to be described or measured. It also
 * covers categories of informations on {@link TaxonNameBase taxon names} not
 * taken in account in {@link NomenclaturalCode nomenclature}.<BR>
 * Descriptions require features in order to be structured and disaggregated 
 * in {@link DescriptionElementBase description elements}.<BR>
 * Experts do not use the word feature for the actual description
 * but only for the property itself. Therefore naming this class FeatureType
 * would have leaded to confusion.
 * <P>
 * Since features are {@link DefinedTermBase defined terms} they have a hierarchical
 * structure that allows to specify ("kind of") or generalize
 * ("generalization of") features. "Kind of" / "generalization of" relations
 * are bidirectional (a feature F1 is a "Kind of" a feature F2 if and only
 * if the feature F2 is a "generalization of" the feature F1. This hierarchical
 * structure has nothing in common with {@link FeatureTree feature trees} used for determination.
 * <P> 
 * A standard set of feature instances will be automatically
 * created as the project starts. But this class allows to extend this standard
 * set by creating new instances of additional features if needed.<BR>
 * <P>
 * This class corresponds to DescriptionsSectionType according to the SDD
 * schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name="Feature", factoryMethod="NewInstance", propOrder = {
		"kindOf",
		"generalizationOf",
		"partOf",
		"includes",
	    "supportsTextData",
	    "supportsQuantitativeData",
	    "supportsDistribution",
	    "supportsIndividualAssociation",
	    "supportsTaxonInteraction",
	    "supportsCommonTaxonName",
	    "supportsCategoricalData",
	    "recommendedModifierEnumeration",
	    "recommendedStatisticalMeasures",
	    "supportedCategoricalEnumerations",
	    "recommendedMeasurementUnits"
})
@XmlRootElement(name = "Feature")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Feature extends DefinedTermBase<Feature> {
	private static final long serialVersionUID = 6754598791831848704L;
	private static final Logger logger = Logger.getLogger(Feature.class);
	private static Feature IMAGE;
	private static Feature CULTIVATION;
	private static Feature CONSERVATION;
	private static Feature USES;
	private static Feature ADDITIONAL_PUBLICATION;
	private static Feature CITATION;
	private static Feature OCCURRENCE;
	private static Feature PHENOLOGY;
	private static Feature COMMON_NAME;
	private static Feature PROTOLOG;
	private static Feature INTRODUCTION;
	private static Feature DIAGNOSIS;
	private static Feature ETYMOLOGY;
	private static Feature MATERIALS_METHODS;
	private static Feature MATERIALS_EXAMINED;
	private static Feature KEY;
	private static Feature BIOLOGY_ECOLOGY;
	private static Feature ECOLOGY;
	private static Feature DISCUSSION;
	private static Feature DISTRIBUTION;
	private static Feature DESCRIPTION;
	private static Feature UNKNOWN;
	private static Feature ANATOMY;
	private static Feature HOSTPLANT;
	private static Feature PATHOGEN_AGENT;
	private static Feature INDIVIDUALS_ASSOCIATION;
	private static Feature SPECIMEN;
	private static Feature OBSERVATION;

	private boolean supportsTextData;
	
	private boolean supportsQuantitativeData;
	
	private boolean supportsDistribution;
	
	private boolean supportsIndividualAssociation;
	
	private boolean supportsTaxonInteraction;
	
	private boolean supportsCategoricalData;
	
	/*
	 * FIXME Should this be Many-To-Many or do we expect each Feature to have its own unique modifier enums?
	 */
	@OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_RecommendedModifierEnumeration")
	private Set<TermVocabulary<Modifier>> recommendedModifierEnumeration = new HashSet<TermVocabulary<Modifier>>();
	
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_StatisticalMeasure")
	private Set<StatisticalMeasure> recommendedStatisticalMeasures = new HashSet<StatisticalMeasure>();
	
	/*
	 * FIXME Should this be Many-To-Many or do we expect each Feature to have its own unique state enums?
	 */
	@OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_SupportedCategoricalEnumeration")
	private Set<TermVocabulary<State>> supportedCategoricalEnumerations = new HashSet<TermVocabulary<State>>();
	
	private boolean supportsCommonTaxonName;

	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_MeasurementUnit")
	private Set<MeasurementUnit> recommendedMeasurementUnits = new HashSet<MeasurementUnit>();
	
/* ***************** CONSTRUCTOR AND FACTORY METHODS **********************************/
	

	/** 
	 * Class constructor: creates a new empty feature instance.
	 * 
	 * @see #Feature(String, String, String)
	 */
	public Feature() {
	}
	
	/** 
	 * Class constructor: creates a new feature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new feature to be created 
	 * @param	label  		 the string identifying the new feature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new feature to be created
	 * @see 				 #Feature()
	 */
	protected Feature(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty feature instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static Feature NewInstance() {
		return new Feature();
	}
	
	/** 
	 * Creates a new feature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new feature to be created 
	 * @param	label  		 the string identifying the new feature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new feature to be created
	 * @see 				 #readCsvLine(List, Language)
	 * @see 				 #NewInstance()
	 */
	public static Feature NewInstance(String term, String label, String labelAbbrev){
		return new Feature(term, label, labelAbbrev);
	}

/* *************************************************************************************/
	
	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link QuantitativeData quantitative data} (true)
	 * or not (false). If this flag is set <i>this</i> feature can only apply to
	 * {@link TaxonDescription taxon descriptions} or {@link SpecimenDescription specimen descriptions}.
	 *  
	 * @return  the boolean value of the supportsQuantitativeData flag
	 */
	@XmlElement(name = "SupportsQuantitativeData")
	public boolean isSupportsQuantitativeData() {
		return supportsQuantitativeData;
	}

	/**
	 * @see	#isSupportsQuantitativeData() 
	 */
	public void setSupportsQuantitativeData(boolean supportsQuantitativeData) {
		this.supportsQuantitativeData = supportsQuantitativeData;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link TextData text data} (true)
	 * or not (false).
	 *  
	 * @return  the boolean value of the supportsTextData flag
	 */
	@XmlElement(name = "SupportsTextData")
	public boolean isSupportsTextData() {
		return supportsTextData;
	}

	/**
	 * @see	#isSupportsTextData() 
	 */
	public void setSupportsTextData(boolean supportsTextData) {
		this.supportsTextData = supportsTextData;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link Distribution distribution} objects
	 * (true) or not (false). This flag is set if and only if <i>this</i> feature
	 * is the {@link #DISTRIBUTION() distribution feature}.
	 *  
	 * @return  the boolean value of the supportsDistribution flag
	 */
	@XmlElement(name = "SupportsDistribution")
	public boolean isSupportsDistribution() {
		return supportsDistribution;
	}

	/**
	 * @see	#isSupportsDistribution() 
	 */
	public void setSupportsDistribution(boolean supportsDistribution) {
		this.supportsDistribution = supportsDistribution;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link IndividualsAssociation individuals associations}
	 * (true) or not (false).
	 *  
	 * @return  the boolean value of the supportsIndividualAssociation flag
	 */
	@XmlElement(name = "SupportsIndividualAssociation")
	public boolean isSupportsIndividualAssociation() {
		return supportsIndividualAssociation;
	}

	/**
	 * @see	#isSupportsIndividualAssociation() 
	 */
	public void setSupportsIndividualAssociation(
			boolean supportsIndividualAssociation) {
		this.supportsIndividualAssociation = supportsIndividualAssociation;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link TaxonInteraction taxon interactions}
	 * (true) or not (false).
	 *  
	 * @return  the boolean value of the supportsTaxonInteraction flag
	 */
	@XmlElement(name = "SupportsTaxonInteraction")
	public boolean isSupportsTaxonInteraction() {
		return supportsTaxonInteraction;
	}

	/**
	 * @see	#isSupportsTaxonInteraction() 
	 */
	public void setSupportsTaxonInteraction(boolean supportsTaxonInteraction) {
		this.supportsTaxonInteraction = supportsTaxonInteraction;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link CommonTaxonName common names}
	 * (true) or not (false). This flag is set if and only if <i>this</i> feature
	 * is the {@link #COMMON_NAME() common name feature}.
	 *  
	 * @return  the boolean value of the supportsCommonTaxonName flag
	 */
	@XmlElement(name = "SupportsCommonTaxonName")
	public boolean isSupportsCommonTaxonName() {
		return supportsCommonTaxonName;
	}

	/**
	 * @see	#isSupportsTaxonInteraction() 
	 */
	public void setSupportsCommonTaxonName(boolean supportsCommonTaxonName) {
		this.supportsCommonTaxonName = supportsCommonTaxonName;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link CategoricalData categorical data}
	 * (true) or not (false).
	 *  
	 * @return  the boolean value of the supportsCategoricalData flag
	 */
	@XmlElement(name = "SupportsCategoricalData")
	public boolean isSupportsCategoricalData() {
		return supportsCategoricalData;
	}

	/**
	 * @see	#supportsCategoricalData() 
	 */
	public void setSupportsCategoricalData(boolean supportsCategoricalData) {
		this.supportsCategoricalData = supportsCategoricalData;
	}

	
	/**
	 * Returns the set of {@link TermVocabulary term vocabularies} containing the
	 * {@link Modifier modifiers} recommended to be used for {@link DescriptionElementBase description elements}
	 * with <i>this</i> feature.
	 *  
	 */
	@XmlElementWrapper(name = "RecommendedModifierEnumerations")
	@XmlElement(name = "RecommendedModifierEnumeration")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Set<TermVocabulary<Modifier>> getRecommendedModifierEnumeration() {
		return recommendedModifierEnumeration;
	}

	/**
	 * Adds a {@link TermVocabulary term vocabulary} (with {@link Modifier modifiers}) to the set of
	 * {@link #getRecommendedModifierEnumeration() recommended modifier vocabularies} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param recommendedModifierEnumeration	the term vocabulary to be added
	 * @see    	   								#getRecommendedModifierEnumeration()
	 */
	public void addRecommendedModifierEnumeration(
			TermVocabulary<Modifier> recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.add(recommendedModifierEnumeration);
	}
	/** 
	 * Removes one element from the set of {@link #getRecommendedModifierEnumeration() recommended modifier vocabularies}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  recommendedModifierEnumeration	the term vocabulary which should be removed
	 * @see     								#getRecommendedModifierEnumeration()
	 * @see     								#addRecommendedModifierEnumeration(TermVocabulary)
	 */
	public void removeRecommendedModifierEnumeration(
			TermVocabulary<Modifier> recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.remove(recommendedModifierEnumeration);
	}

	/**
	 * Returns the set of {@link StatisticalMeasure statistical measures} recommended to be used
	 * in case of {@link QuantitativeData quantitative data} with <i>this</i> feature.
	 */
	@XmlElementWrapper(name = "RecommendedStatisticalMeasures")
	@XmlElement(name = "RecommendedStatisticalMeasure")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Set<StatisticalMeasure> getRecommendedStatisticalMeasures() {
		return recommendedStatisticalMeasures;
	}

	/**
	 * Adds a {@link StatisticalMeasure statistical measure} to the set of
	 * {@link #getRecommendedStatisticalMeasures() recommended statistical measures} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param recommendedStatisticalMeasure	the statistical measure to be added
	 * @see    	   							#getRecommendedStatisticalMeasures()
	 */
	public void addRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.add(recommendedStatisticalMeasure);
	}
	/** 
	 * Removes one element from the set of {@link #getRecommendedStatisticalMeasures() recommended statistical measures}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  recommendedStatisticalMeasure	the statistical measure which should be removed
	 * @see     								#getRecommendedStatisticalMeasures()
	 * @see     								#addRecommendedStatisticalMeasure(StatisticalMeasure)
	 */
	public void removeRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.remove(recommendedStatisticalMeasure);
	}

	/**
	 * Returns the set of {@link StatisticalMeasure statistical measures} recommended to be used
	 * in case of {@link QuantitativeData quantitative data} with <i>this</i> feature.
	 */
	@XmlElementWrapper(name = "RecommendedMeasurementUnits")
	@XmlElement(name = "RecommendedMeasurementUnit")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Set<MeasurementUnit> getRecommendedMeasurementUnits() {
		return recommendedMeasurementUnits;
	}

	/**
	 * Adds a {@link StatisticalMeasure statistical measure} to the set of
	 * {@link #getRecommendedStatisticalMeasures() recommended statistical measures} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param recommendedStatisticalMeasure	the statistical measure to be added
	 * @see    	   							#getRecommendedStatisticalMeasures()
	 */
	public void addRecommendedMeasurementUnit(
			MeasurementUnit recommendedMeasurementUnit) {
		this.recommendedMeasurementUnits.add(recommendedMeasurementUnit);
	}
	/** 
	 * Removes one element from the set of {@link #getRecommendedStatisticalMeasures() recommended statistical measures}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  recommendedStatisticalMeasure	the statistical measure which should be removed
	 * @see     								#getRecommendedStatisticalMeasures()
	 * @see     								#addRecommendedStatisticalMeasure(StatisticalMeasure)
	 */
	public void removeRecommendedMeasurementUnit(
			MeasurementUnit recommendedMeasurementUnit) {
		this.recommendedMeasurementUnits.remove(recommendedMeasurementUnit);
	}
	
	/**
	 * Returns the set of {@link TermVocabulary term vocabularies} containing the list of
	 * possible {@link State states} to be used in {@link CategoricalData categorical data}
	 * with <i>this</i> feature.
	 * 
	 */
	@XmlElementWrapper(name = "SupportedCategoricalEnumerations")
	@XmlElement(name = "SupportedCategoricalEnumeration")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Set<TermVocabulary<State>> getSupportedCategoricalEnumerations() {
		return supportedCategoricalEnumerations;
	}

	/**
	 * Adds a {@link TermVocabulary term vocabulary} to the set of
	 * {@link #getSupportedCategoricalEnumerations() supported state vocabularies} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param supportedCategoricalEnumeration	the term vocabulary which should be removed
	 * @see    	   								#getSupportedCategoricalEnumerations()
	 */
	public void addSupportedCategoricalEnumeration(
			TermVocabulary<State> supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.add(supportedCategoricalEnumeration);
	}
	/** 
	 * Removes one element from the set of {@link #getSupportedCategoricalEnumerations() supported state vocabularies}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  supportedCategoricalEnumeration	the term vocabulary which should be removed
	 * @see     								#getSupportedCategoricalEnumerations()
	 * @see     								#addSupportedCategoricalEnumeration(TermVocabulary)
	 */
	public void removeSupportedCategoricalEnumeration(
			TermVocabulary<State> supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.remove(supportedCategoricalEnumeration);
	}
	
	@XmlElement(name = "KindOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Override
	public Feature getKindOf(){
		return super.getKindOf();
	}

	public void setKindOf(Feature kindOf){
		super.setKindOf(kindOf);
	}
	
	@XmlElement(name = "PartOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public Feature getPartOf(){
		return super.getPartOf();
	}
	
	public void setPartOf(Feature partOf){
		super.setPartOf(partOf);
	}
	
	@XmlElementWrapper(name = "Generalizations", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlElement(name = "GeneralizationOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public Set<Feature> getGeneralizationOf(){
		return super.getGeneralizationOf();
	}
	
	protected void setGeneralizationOf(Set<Feature> value){
		super.setGeneralizationOf(value);
	}
	
	@XmlElementWrapper(name = "Includes", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlElement(name = "Include", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public Set<Feature> getIncludes(){
		return super.getIncludes();
	}
	
	protected void setIncludes(Set<Feature> includes) {
		super.setIncludes(includes);
	}
	
	private static final UUID uuidUnknown = UUID.fromString("910307f1-dc3c-452c-a6dd-af5ac7cd365c");
	private static final UUID uuidDescription = UUID.fromString("9087cdcd-8b08-4082-a1de-34c9ba9fb493");
	private static final UUID uuidDistribution = UUID.fromString("9fc9d10c-ba50-49ee-b174-ce83fc3f80c6");
	private static final UUID uuidEcology = UUID.fromString("aa923827-d333-4cf5-9a5f-438ae0a4746b");
	private static final UUID uuidBiologyEcology = UUID.fromString("9832e24f-b670-43b4-ac7c-20a7261a1d8c");
	private static final UUID uuidKey = UUID.fromString("a677f827-22b9-4205-bb37-11cb48dd9106");
	private static final UUID uuidMaterialsExamined = UUID.fromString("7c0c7571-a864-47c1-891d-01f59000dae1");
	private static final UUID uuidMaterialsMethods = UUID.fromString("1e87d9c3-0844-4a03-9686-773e2ccb3ab6");
	private static final UUID uuidEtymology = UUID.fromString("dd653d48-355c-4aec-a4e7-724f6eb29f8d");
	private static final UUID uuidDiagnosis = UUID.fromString("d43d8501-ceab-4caa-9e51-e87138528fac");
	private static final UUID uuidProtolog = UUID.fromString("7f1fd111-fc52-49f0-9e75-d0097f576b2d");
	private static final UUID uuidCommonName = UUID.fromString("fc810911-51f0-4a46-ab97-6562fe263ae5");
	private static final UUID uuidPhenology = UUID.fromString("a7786d3e-7c58-4141-8416-346d4c80c4a2");
	private static final UUID uuidOccurrence = UUID.fromString("5deff505-1a32-4817-9a74-50e6936fd630");
	private static final UUID uuidCitation = UUID.fromString("99b2842f-9aa7-42fa-bd5f-7285311e0101");
	private static final UUID uuidAdditionalPublication = UUID.fromString("cb2eab09-6d9d-4e43-8ad2-873f23400930");
	private static final UUID uuidUses = UUID.fromString("e5374d39-b210-47c7-bec1-bee05b5f1cb6");
	private static final UUID uuidConservation = UUID.fromString("4518fc20-2492-47de-b345-777d2b83c9cf");
	private static final UUID uuidCultivation = UUID.fromString("e28965b2-a367-48c5-b954-8afc8ac2c69b");
	private static final UUID uuidIntroduction = UUID.fromString("e75255ca-8ff4-4905-baad-f842927fe1d3");
	private static final UUID uuidDiscussion = UUID.fromString("d3c4cbb6-0025-4322-886b-cd0156753a25");
	private static final UUID uuidImage = UUID.fromString("84193b2c-327f-4cce-90ef-c8da18fd5bb5");
	private static final UUID uuidAnatomy = UUID.fromString("94213b2c-e67a-4d37-25ef-e8d316edfba1");
	private static final UUID uuidHostPlant = UUID.fromString("6e9de1d5-05f0-40d5-8786-2fe30d0d894d");
	private static final UUID uuidPathogenAgent = UUID.fromString("002d05f2-fd72-49f1-ba4d-196cf09240b5");
	private static final UUID uuidIndividualsAssociation = UUID.fromString("e2308f37-ddc5-447d-b483-5e2171dd85fd");
	private static final UUID uuidSpecimen = UUID.fromString("8200e050-d5fd-4cac-8a76-4b47afb13809");
	private static final UUID uuidObservation = UUID.fromString("f59e747d-0b4f-4bf7-b69a-cbd50bc78595");
	
//	private static final UUID uuidDistribution = UUID.fromString("");
//	private static final UUID uuidDistribution = UUID.fromString("");
//	private static final UUID uuidDistribution = UUID.fromString("");

//	"86bd920d-f8c5-48b9-af1d-03f63c31de5c",,"Abstract","Abstract"
//	"489bf358-b78a-45e2-a691-f9f3f10446ce",,"Synopsis","Synopsis"
//	"89d3b005-9876-4923-89d9-60eb75b9583b",,"Multiple","Multiple"
//	"555a46bc-211a-476f-a022-c472970d6f8b",,"Acknowledgments","Acknowledgments"
	
	
	/** 
	 * Creates and returns a new feature instance on the basis of a given string
	 * list (containing an UUID, an URI, a label and a description) and a given
	 * {@link Language language} to be associated with the description. Furthermore
	 * the flags concerning the supported subclasses of {@link DescriptionElementBase description elements}
	 * are set according to a particular string belonging to the given
	 * string list.<BR>
	 * This method overrides the readCsvLine method from {@link DefinedTermBase#readCsvLine(List, Language) DefinedTermBase}.
	 *
	 * @param  csvLine	the string list with elementary information for attributes
	 * @param  lang		the language in which the description has been formulated
	 * @see     		#NewInstance(String, String, String)
	 */
	@Override
	public Feature readCsvLine(Class<Feature> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		Feature newInstance = super.readCsvLine(termClass, csvLine, terms); 
		String text = (String)csvLine.get(4);
		if (text != null && text.length() >= 6){
			if ("1".equals(text.substring(0, 1))){newInstance.setSupportsTextData(true);};
			if ("1".equals(text.substring(1, 2))){newInstance.setSupportsQuantitativeData(true);};
			if ("1".equals(text.substring(2, 3))){newInstance.setSupportsDistribution(true);};
			if ("1".equals(text.substring(3, 4))){newInstance.setSupportsIndividualAssociation(true);};
			if ("1".equals(text.substring(4, 5))){newInstance.setSupportsTaxonInteraction(true);};
			if ("1".equals(text.substring(5, 6))){newInstance.setSupportsCommonTaxonName(true);};
			// if ("1".equals(text.substring(6, 7))){newInstance.setSupportsCategoricalData(true);};
		}
		return newInstance;
	}
	
	/**
	 * Returns the "unknown" feature. This feature allows to store values of
	 * {@link DescriptionElementBase description elements} even if it is momentarily
	 * not known what they mean.
	 */
	public static final Feature UNKNOWN(){
		return UNKNOWN;
	}
	
	/**
	 * Returns the "description" feature. This feature allows to handle global
	 * {@link DescriptionElementBase description elements} for a global {@link DescriptionBase description}.<BR>
	 * The "description" feature is the highest level feature. 
	 */
	public static final Feature DESCRIPTION(){
		return DESCRIPTION;
	}

	/**
	 * Returns the "distribution" feature. This feature allows to handle only
	 * {@link Distribution distributions}.
	 * 
	 * @see	#isSupportsDistribution()
	 */
	public static final Feature DISTRIBUTION(){
		return DISTRIBUTION;
	}

	/**
	 * Returns the "discussion" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature DISCUSSION(){
		return DISCUSSION;
	}
	
	/**
	 * Returns the "ecology" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "ecology" feature generalizes all other possible features concerning
	 * ecological matters.
	 */
	public static final Feature ECOLOGY(){
		return ECOLOGY;
	}	
	
	/**
	 * Returns the "biology_ecology" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "biology_ecology" feature generalizes all possible features concerning
	 * biological aspects of ecological matters.
	 * 
	 * @see #ECOLOGY()
	 */
	public static final Feature BIOLOGY_ECOLOGY(){
		return BIOLOGY_ECOLOGY;
	}
	
	/**
	 * Returns the "key" feature. This feature is the "upper" feature generalizing
	 * all features being used within an identification key.
	 */
	public static final Feature KEY(){
		return KEY;
	}		
	
	
	/**
	 * Returns the "materials_examined" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * mentioning which material has been examined in order to accomplish
	 * the description. This feature applies only to
	 * {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature MATERIALS_EXAMINED(){
		return MATERIALS_EXAMINED;
	}
	
	/**
	 * Returns the "materials_methods" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * mentioning which methods have been adopted to analyze the material in
	 * order to accomplish the description. This feature applies only to
	 * {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature MATERIALS_METHODS(){
		return MATERIALS_METHODS;
	}
	
	/**
	 * Returns the "etymology" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * giving some information about the history of the taxon name. This feature applies only to
	 * {@link TaxonNameDescription taxon name descriptions}.
	 */
	public static final Feature ETYMOLOGY(){
		return ETYMOLOGY;
	}
		
	/**
	 * Returns the "diagnosis" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}.
	 * This feature applies only to {@link SpecimenDescription specimen descriptions} or to
	 * {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature DIAGNOSIS(){
		return DIAGNOSIS;
	}

	
	/**
	 * Returns the "introduction" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature INTRODUCTION(){
		return INTRODUCTION;
	}

	/**
	 * Returns the "protologue" feature. This feature can only be described
	 * with {@link TextData text data} reproducing the content of the protologue 
	 * (or some information about it) of the taxon name. This feature applies only to
	 * {@link TaxonNameDescription taxon name descriptions}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature PROTOLOG(){
		return PROTOLOG;
	}
	
	/**
	 * Returns the "common_name" feature. This feature allows to handle only
	 * {@link CommonTaxonName common names}.
	 * 
	 * @see	#isSupportsCommonTaxonName()
	 */
	public static final Feature COMMON_NAME(){
		return COMMON_NAME;
	}
	
	/**
	 * Returns the "phenology" feature. This feature can only be described
	 * with {@link CategoricalData categorical data} or eventually with {@link TextData text data}
	 * containing information time about recurring natural phenomena.
	 * This feature only applies to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "phenology" feature generalizes all other possible features
	 * concerning time information about particular natural phenomena
	 * (such as "first flight of butterflies").
	 */
	public static final Feature PHENOLOGY(){
		return PHENOLOGY;
	}

	/**
	 * Returns the "occurrence" feature.
	 */
	public static final Feature OCCURRENCE(){
		return OCCURRENCE;
	}

	/**
	 * Returns the "anatomy" feature.
	 */
	public static final Feature ANATOMY(){
		return ANATOMY;
	}
	/**
	 * Returns the "hostplant" feature.
	 */
	public static final Feature HOSTPLANT(){
		return HOSTPLANT;
	}
	/**
	 * Returns the "pathogen agent" feature.
	 */
	public static final Feature PATHOGEN_AGENT(){
		return PATHOGEN_AGENT;
	}

	/**
	 * Returns the "citation" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature CITATION(){
		return CITATION;
	}
	
	/**
	 * Returns the "additional_publication" feature. This feature can only be
	 * described with {@link TextData text data} with information about a
	 * publication where a {@link TaxonNameBase taxon name} has also been published
	 * but which is not the {@link TaxonNameBase#getNomenclaturalReference() nomenclatural reference}.
	 * This feature applies only to {@link TaxonNameDescription taxon name descriptions}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature ADDITIONAL_PUBLICATION(){
		return ADDITIONAL_PUBLICATION;
	}
	
	
	/**
	 * Returns the "uses" feature. This feature only applies
	 * to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "uses" feature generalizes all other possible features concerning
	 * particular uses (for instance "industrial use of seeds").
	 */
	public static final Feature USES(){
		return USES;
	}
	 
	
	/**
	 * Returns the "conservation" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} and generalizes
	 * methods and conditions for the conservation of {@link Specimen specimens}.<BR>
	 */
	public static final Feature CONSERVATION(){
		return CONSERVATION;
	}
	
	
	/**
	 * Returns the "cultivation" feature.
	 */
	public static final Feature CULTIVATION(){
		return CULTIVATION;
	}
	
	
	/**
	 * Returns the "cultivation" feature.
	 */
	public static final Feature IMAGE(){
		return IMAGE;
	}
	
	/**
	 * Returns the "individuals association" feature.
	 */
	public static final Feature INDIVIDUALS_ASSOCIATION(){
		Feature individuals_association = INDIVIDUALS_ASSOCIATION;
		Set<Feature> generalizationOf = new HashSet<Feature>();
		generalizationOf.add(SPECIMEN());
		generalizationOf.add(OBSERVATION());
		individuals_association.setGeneralizationOf(generalizationOf);
		return individuals_association;
		
	}
	
	public static final Feature SPECIMEN(){
		return SPECIMEN;
	}
	
	public static final Feature OBSERVATION(){
		return OBSERVATION;
	}
	/**
	 * Returns the "hybrid_parent" feature. This feature can only be used
	 * by {@link TaxonInteraction taxon interactions}.<BR>
	 * <P>
	 * Note: It must be distinguished between hybrid relationships as
	 * relevant nomenclatural relationships between {@link BotanicalName plant names}
	 * on the one side and the biological relation between two {@link Taxon taxa}
	 * as it is here the case on the other one. 
	 * 
	 * @see	#isSupportsTaxonInteraction()
	 * @see	HybridRelationshipType
	 */
	public static final Feature HYBRID_PARENT(){
		//TODO
		logger.warn("HYBRID_PARENT not yet implemented");
		return null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<Feature> termVocabulary) {
		Feature.ADDITIONAL_PUBLICATION = termVocabulary.findTermByUuid(Feature.uuidAdditionalPublication);
		Feature.BIOLOGY_ECOLOGY = termVocabulary.findTermByUuid(Feature.uuidBiologyEcology);
		Feature.CITATION = termVocabulary.findTermByUuid(Feature.uuidCitation);
		Feature.COMMON_NAME = termVocabulary.findTermByUuid(Feature.uuidCommonName);
		Feature.CONSERVATION = termVocabulary.findTermByUuid(Feature.uuidConservation);
		Feature.CULTIVATION = termVocabulary.findTermByUuid(Feature.uuidCultivation);
		Feature.DESCRIPTION = termVocabulary.findTermByUuid(Feature.uuidDescription);
		Feature.DIAGNOSIS = termVocabulary.findTermByUuid(Feature.uuidDiagnosis);
		Feature.DISCUSSION = termVocabulary.findTermByUuid(Feature.uuidDiscussion);
		Feature.DISTRIBUTION = termVocabulary.findTermByUuid(Feature.uuidDistribution);
		Feature.ECOLOGY = termVocabulary.findTermByUuid(Feature.uuidEcology);
		Feature.ETYMOLOGY = termVocabulary.findTermByUuid(Feature.uuidEtymology);
		Feature.IMAGE = termVocabulary.findTermByUuid(Feature.uuidImage);
		Feature.INTRODUCTION = termVocabulary.findTermByUuid(Feature.uuidIntroduction);
		Feature.KEY = termVocabulary.findTermByUuid(Feature.uuidKey);
		Feature.MATERIALS_EXAMINED = termVocabulary.findTermByUuid(Feature.uuidMaterialsExamined);
		Feature.MATERIALS_METHODS = termVocabulary.findTermByUuid(Feature.uuidMaterialsMethods);
		Feature.OCCURRENCE = termVocabulary.findTermByUuid(Feature.uuidOccurrence);
		Feature.PHENOLOGY = termVocabulary.findTermByUuid(Feature.uuidPhenology);
		Feature.PROTOLOG = termVocabulary.findTermByUuid(Feature.uuidProtolog);
		Feature.UNKNOWN = termVocabulary.findTermByUuid(Feature.uuidUnknown);
		Feature.USES = termVocabulary.findTermByUuid(Feature.uuidUses);
		Feature.ANATOMY = termVocabulary.findTermByUuid(Feature.uuidAnatomy);
		Feature.PATHOGEN_AGENT = termVocabulary.findTermByUuid(Feature.uuidPathogenAgent);
		Feature.HOSTPLANT = termVocabulary.findTermByUuid(uuidHostPlant); 
		Feature.INDIVIDUALS_ASSOCIATION = termVocabulary.findTermByUuid(uuidIndividualsAssociation); 
		Feature.SPECIMEN = termVocabulary.findTermByUuid(uuidSpecimen);
		Feature.OBSERVATION = termVocabulary.findTermByUuid(uuidObservation);
	}

}