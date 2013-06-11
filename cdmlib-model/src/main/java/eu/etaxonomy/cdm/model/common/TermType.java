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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;



/**
 * The term type is used to define the type of a {@link TermBase term}, may it be a vocabulary or a defined term.<BR>
 * It is used to define in which context a term may be used. From v3.3 on this replaces the semantic part of the subclasses
 * of the class {@link DefinedTermBase}. E.g. before v3.3 a term defining a sex and a term defining a stage had to different
 * classes Sex and Stage. With v3.3 they both became {@link DefinedTerm}s but with different types.<BR>
 * The type of a term and the type of its vocabulary should be the same. Before v3.3 it was not always possible to define
 * what the context of an (empty) vocabulary is.
 *  * @author a.mueller
 * @created 11.06.2013
 */
@XmlEnum
public enum TermType implements IDefinedTerm<TermType>, Serializable{
	
	//0
	/**
	 * Unknown term type is the type to be used if no information is available about the type.
	 * In the current model this type should never be used. However, it is a placeholder in case
	 * we find an appropriate usage in future.
	 */
	@XmlEnumValue("Unknown")
	Unknown(UUID.fromString("b2836c89-3b1d-4758-ba6d-568ef8d6fbc4"), "Unknown term type"),

	
	@XmlEnumValue("AnnotationType")
	AnnotationType(UUID.fromString("c3aabb64-6174-4152-95b1-7cec57e485cf"), "Annotation type"),
	
	@XmlEnumValue("DerivationEventType")
	DerivationEventType(UUID.fromString("ba8e4b10-c792-42e7-a3f5-874708f10094"), "Derivation event type"),
	
	@XmlEnumValue("ExtensionType")
	ExtensionType(UUID.fromString("12f5c03b-528a-4909-b81b-e525feabc97c"), "Extension type"),

	@XmlEnumValue("Feature")
	Feature(UUID.fromString("b866a1d6-f962-4c23-bb8e-a3b66d33aedc"), "Feature"),

	@XmlEnumValue("InstitutionType")
	InstitutionType(UUID.fromString("09d78265-18b5-4352-b154-d2f39e84d3f3"), "Institution type"),

	@XmlEnumValue("MarkerType")
	MarkerType(UUID.fromString(""), "MarkerType"),

	@XmlEnumValue("MeasurementUnit")
	MeasurementUnit(UUID.fromString(""), "Measurement unit"),

	@XmlEnumValue("NamedAreaType")
	NamedAreaType(UUID.fromString(""), "Named area type"),

	@XmlEnumValue("NaturalLanguageTerm")
	NaturalLanguageTerm(UUID.fromString(""), "Natural language term"),

	@XmlEnumValue("PreservationMethod")
	PreservationMethod(UUID.fromString(""), "Preservation method"),

	@XmlEnumValue("ReferenceSystem")
	ReferenceSystem(UUID.fromString(""), "Reference system"),

	@XmlEnumValue("RightsTerm")
	RightsTerm(UUID.fromString(""), "Rights term"),

	@XmlEnumValue("StatisticalMeasure")
	StatisticalMeasure(UUID.fromString(""), "Statistical measure"),

	@XmlEnumValue("TextFormat")
	TextFormat(UUID.fromString(""), "Text format"),

	@XmlEnumValue("NamedArea")
	NamedArea(UUID.fromString(""), "Named area"),

	@XmlEnumValue("NamedAreaLevel")
	NamedAreaLevel(UUID.fromString(""), "Named area level"),

	@XmlEnumValue("NomenclaturalStatusType")
	NomenclaturalStatusType(UUID.fromString(""), "Nomenclatural status type"),

	@XmlEnumValue("PresenceAbsenceTerm")
	PresenceAbsenceTerm(UUID.fromString(""), "Presence or absence term"),

	@XmlEnumValue("Rank")
	Rank(UUID.fromString(""), "Rank"),

	@XmlEnumValue("HybridRelationshipType")
	HybridRelationshipType(UUID.fromString(""), "Hybrid relationship type"),

	@XmlEnumValue("NameRelationshipType")
	NameRelationshipType(UUID.fromString(""), "Name relationship type"),

	@XmlEnumValue("SynonymRelationshipType")
	SynonymRelationshipType(UUID.fromString(""), "Synonym relationship type"),
	
	@XmlEnumValue("TaxonRelationshipType")
	TaxonRelationshipType(UUID.fromString(""), "Taxon relationship type"),

	@XmlEnumValue("State")
	State(UUID.fromString(""), "State"),

	@XmlEnumValue("NameTypeDesignationStatus")
	NameTypeDesignationStatus(UUID.fromString(""), "Name type designation status"),

	@XmlEnumValue("SpecimenTypeDesignationStatus")
	SpecimenTypeDesignationStatus(UUID.fromString(""), "Specimen type designation status"),

	/**
	 * TODO
	 */
	@XmlEnumValue("Modifier")
	Modifier(UUID.fromString(""), "Modifier"),
	
	/**
	 * TODO
	 * 
	 * A determination modifier is a specification of a Modifier.
	 */
	@XmlEnumValue("DeterminationModifier")
	DeterminationModifier(UUID.fromString(""), "Determination modifier"),
	
	@XmlEnumValue("Scope")
	Scope(UUID.fromString("8862b66e-9059-4ea4-885e-47a373357075"), "Scope"),

	
	/** The stage type represents the restriction (scope) concerning the life stage for
	 * the applicability of {@link TaxonDescription taxon descriptions}. The life stage of a
	 * {@link SpecimenOrObservationBase specimen or observation}
	 * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
	 * the specimen itself.<BR>
	 * A stage is a specification of Scope.
	 */
	@XmlEnumValue("Stage")
	Stage(UUID.fromString("cf411ef0-8eee-4461-99e9-c03f4f0a1656"), "Stage"),

	/**
	 * TODO
	 * 
	 * A sex is a specification of Scope.
	 */
	@XmlEnumValue("Sex")
	Sex(UUID.fromString(""), "Sex"),
	
	
	;
	
	
	private static final Logger logger = Logger.getLogger(TermType.class);

	private String readableString;
	private UUID uuid;

	private TermType(UUID uuid, String defaultString){
		this.uuid = uuid;
		readableString = defaultString;
	}

	@Transient
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
	

	@Override
    public TermType readCsvLine(Class<TermType> termClass,
			List<String> csvLine, java.util.Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
    public void writeCsvLine(CSVWriter writer, TermType term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public TermType getByUuid(UUID uuid) {
		for (TermType type : TermType.values()){
			if (type.getUuid().equals(uuid)){
				return type;
			}
		}
		return null;
	}


	@Override
    public TermType getKindOf() {
		return null;
	}


	@Override
    public Set<TermType> getGeneralizationOf() {
		return new HashSet<TermType>();
	}


	@Override
    public TermType getPartOf() {
		return null;
	}


	@Override
    public Set<TermType> getIncludes() {
		return new HashSet<TermType>();
	}


	@Override
    public Set<Media> getMedia() {
		return new HashSet<Media>();
	}

}
