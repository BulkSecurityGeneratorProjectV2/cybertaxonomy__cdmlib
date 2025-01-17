/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v30_40;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ClassChanger;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermMover;
import eu.etaxonomy.cdm.database.update.TreeIndexUpdater;
import eu.etaxonomy.cdm.database.update.VocabularyCreator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author a.mueller
 * @since Jun 06, 2013
 */
public class SchemaUpdater_31_33 extends SchemaUpdaterBase {

	private static final Logger logger = LogManager.getLogger(SchemaUpdater_31_33.class);
	private static final String startSchemaVersion = "3.0.1.0.201104190000";
	private static final String endSchemaVersion = "3.3.0.0.201309240000";

	// ********************** FACTORY METHOD*************************************

	public static SchemaUpdater_31_33 NewInstance() {
		return new SchemaUpdater_31_33();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_31_33() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		// CHECKS

		// remove SpecimenOrObservationBase_Media #3597
		// TODO check if Description -Specimen Relation has M:M data
		if (false) {
			throw new RuntimeException(
					"Required check for SpecimenOrObservationBase_Media");
		} else {
			logger.info("CHECKS for inconsistent data not running !!!!");
		}

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		// Was in Schemaupdater_301_31 which was never used and later deleted
		// (r18331).
		// drop TypeDesignationBase_TaxonNameBase //from schemaUpdater 301_31
		stepName = "Drop duplicate TypeDesignation-TaxonName table";
		tableName = "TypeDesignationBase_TaxonNameBase";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// create original source type column
		stepName = "Create original source type column";
		tableName = "OriginalSourceBase";
		columnName = "sourceType";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				4, INCLUDE_AUDIT).setNotNull(true);

		// update original source type
		updateOriginalSourceType(stepList);

		// create and update elevenation max, remove error column
		updateElevationMax(stepList);

		// create TaxonNode tree index
		stepName = "Create taxon node tree index";
		tableName = "TaxonNode";
		columnName = "treeIndex";
		// TODO NOT NULL unclear //see also columnTypeChanger
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				255, INCLUDE_AUDIT);

		// update treeindex for taxon nodes
		stepName = "Update TaxonNode treeindex";
		tableName = "TaxonNode";
		String treeIdColumnName = "classification_id";
		TreeIndexUpdater.NewInstance(stepList, stepName, tableName,
				treeIdColumnName, columnName, ! INCLUDE_AUDIT);   //update does no yet wok for ANSI SQL (e.g. PosGres / H2 with multiple entries for same id in AUD table)

		// create TaxonNode sort index column
		stepName = "Create taxon node sort index column";
		tableName = "TaxonNode";
		columnName = "sortIndex";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, null);

		// update sortindex
		stepName = "Update sort index on TaxonNode children";
		tableName = "TaxonNode";
		String parentIdColumn = "parent_id";
		String sortIndexColumn = "sortIndex";
		SortIndexUpdater.NewInstance(
		        stepList, stepName, tableName, parentIdColumn, sortIndexColumn,
				INCLUDE_AUDIT);

		// Classification root nodes sort index
		stepName = "Create classification root node sort index column";
		tableName = "Classification_TaxonNode";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, null);

		stepName = "Update sort index on classification child nodes";
		parentIdColumn = "Classification_id";
		String idColumn = "rootnodes_id";
		SortIndexUpdater.NewInstance(stepList, stepName, tableName,
				parentIdColumn, sortIndexColumn, idColumn, INCLUDE_AUDIT);

		// create feature node tree index
		stepName = "Create feature node tree index";
		tableName = "FeatureNode";
		columnName = "treeIndex";
		// TODO NOT NULL unclear
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				255, INCLUDE_AUDIT);

		// update tree index for feature node
		stepName = "Update FeatureNode treeindex";
		tableName = "FeatureNode";
		treeIdColumnName = "featuretree_id";
		TreeIndexUpdater.NewInstance(stepList, stepName, tableName,
				treeIdColumnName, columnName, ! INCLUDE_AUDIT);  // see comment for TaxonTree

		// update introduced: adventitious (casual) label
		// #3540
		stepName = "Update introduced: adventitious (casual) label";
		String query = " UPDATE @@Representation@@ "
				+ " SET abbreviatedlabel = 'ia' "
				+ " WHERE abbreviatedlabel = 'id' AND label = 'introduced: adventitious (casual)' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing("Representation");

		// termType for DefinedTerms and TermVocabulary, no type must be null
		stepName = "Create termType column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "termType";
		// TODO NOT NULL unclear
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				255, INCLUDE_AUDIT);

		stepName = "Create termType column in TermVocabulary";
		tableName = "TermVocabulary";
		columnName = "termType";
		// TODO NOT NULL unclear
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				4, INCLUDE_AUDIT);

		// update termType for DefinedTerms, no type must be null
		updateTermTypesForTerms(stepList);

		// update termType for TermVocabulary, no type must be null
		updateTermTypesForVocabularies(stepList);

		// update DTYPE of DefinedTerms
		updateDtypeOfDefinedTerms(stepList);

		// idInVocabulary for DefinedTerms
		stepName = "Create idInVocabulary column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "idInVocabulary";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				255, INCLUDE_AUDIT);

		// update idInVocabulary
		updateIdInVocabulary(stepList);

		// rankClass (#3521)
		stepName = "Create rankClass column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "rankClass";
		// TODO NOT NULL unclear
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				255, INCLUDE_AUDIT);

		// update rankClass (#3521)
		RankClassUpdater.NewInstance(stepList);

		// update datatype->CLOB for URIs. (DefinedTerms, TermVocabulary,
		// Reference, Rights, MediaRepresentationPart )
		// #3345, TODO2 adapt type to <65k -> see #3954
		// sequence.sequence has been changed #3360
		changeUriType(stepList);

		// Annotation.linkbackUri change name #3374
		stepName = "Update url to uri (->clob) for Annotation.linkbackUri";
		columnName = "linkbackUrl";
		String newColumnName = "linkbackUri";
		tableName = "Annotation";
		// TODO check non MySQL and with existing data (probably does not exist)
		ColumnNameChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, newColumnName, INCLUDE_AUDIT);

		// update Sicilia -> Sicily
		// #3540
		stepName = "Update Sicilia -> Sicily";
		query = " UPDATE @@Representation@@ "
				+ " SET label = 'Sicily', text = 'Sicily' "
				+ " WHERE (abbreviatedlabel = 'SIC-SI'  OR abbreviatedlabel = 'SIC')  AND label = 'Sicilia' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing("Representation");

		// remove homotypical group form type designation base
		stepName = "Remove column homotypical group in type designation base";
		tableName = "TypeDesignationBase";
		String oldColumnName = "homotypicalgroup_id";
		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName,
				INCLUDE_AUDIT);

		// add publish flag #1780
		addPublishFlag(stepList);

		// add columns abbrevTitle, abbrevTitleCache and
		// protectedAbbrevTitleCache to Reference
		stepName = "Add abbrevTitle to Reference";
		tableName = "Reference";
		columnName = "abbrevTitle";
		int length = 255;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		stepName = "Add abbrevTitleCache to Reference";
		tableName = "Reference";
		columnName = "abbrevTitleCache";
		length = 1023;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		stepName = "Add protectedAbbrevTitleCache to Reference";
		tableName = "Reference";
		columnName = "protectedAbbrevTitleCache";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false);

		// update abbrevTitle, protectedAbbrevTitle and abbrevTitleCache in
		// Reference
		updateAbbrevTitle(stepList);

		//remove figure #2539
		stepName = "Remove Figure class";
		query = "UPDATE @@Media@@ SET DTYPE = 'Media' WHERE DTYPE = 'Figure'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "Media", 99);

		// add doi to reference
		stepName = "Add doi to Reference";
		tableName = "Reference";
		columnName = "doi";
		length = 255;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);


		// add start number to PolytomousKey
		stepName = "Add start number column to PolytomousKey";
		tableName = "PolytomousKey";
		columnName = "startNumber";
		Integer defaultValue = 1;
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, defaultValue, true);

		// add recordBasis to specimenOrObservationBase
		stepName = "Add recordBasis to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "recordBasis";
		length = 4; // TODO needed?
		// TODO NOT NULL
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		// update recordBasis
		updateRecordBasis(stepList);

		// update specimenOrObservationBase DTYPE with DerivedUnit where
		// necessary
		stepName = "Update Specimen -> DerivedUnit";
		query = " UPDATE @@SpecimenOrObservationBase@@ "
				+ " SET DTYPE = 'DerivedUnit' "
				+ " WHERE DTYPE = 'Specimen' OR DTYPE = 'Fossil' OR DTYPE = 'LivingBeing' OR DTYPE = 'Observation' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing("SpecimenOrObservationBase");

		stepName = "Update Specimen -> DerivedUnit";
		String newClass = "eu.etaxonomy.cdm.model.occurrence.DerivedUnit";
		String[] oldClassPaths = new String[] {
				"eu.etaxonomy.cdm.model.occurrence.Specimen",
				"eu.etaxonomy.cdm.model.occurrence.Fossil",
				"eu.etaxonomy.cdm.model.occurrence.LivingBeing",
				"eu.etaxonomy.cdm.model.occurrence.Observation" };
		ClassChanger.NewIdentifiableInstance(stepList, stepName, tableName,
				newClass, oldClassPaths, INCLUDE_AUDIT);

		// update DTYPE FieldObservation -> FieldUnit #3351
		stepName = "Update FieldObservation -> FieldUnit";
		query = " UPDATE @@SpecimenOrObservationBase@@ "
				+ " SET DTYPE = 'FieldUnit' "
				+ " WHERE DTYPE = 'FieldObservation' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing("SpecimenOrObservationBase");

		stepName = "Update Specimen -> DerivedUnit";
		newClass = "eu.etaxonomy.cdm.model.occurrence.FieldUnit";
		oldClassPaths = new String[] { "eu.etaxonomy.cdm.model.occurrence.FieldObservation" };
		ClassChanger.NewIdentifiableInstance(stepList, stepName, tableName,
				newClass, oldClassPaths, INCLUDE_AUDIT);

		// add kindOfUnit to SpecimenOrObservationBase
		stepName = "Add kindOfUnit column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "kindOfUnit_id";
		String relatedTable = "DefinedTermBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, relatedTable);

		// remove citation_id and citation micro-reference columns from Media
		// table #2541
		// FIXME first check if columns are always empty
		stepName = "Remove citation column from Media";
		tableName = "Media";
		columnName = "citation_id";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		stepName = "Remove citation microreference column from Media";
		tableName = "Media";
		columnName = "citationMicroReference";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// update length of all title caches and full title cache in names #1592
		updateTitleCacheLength(stepList);

		// rename FK column states_id -> stateData_id in
		// DescriptionElementBase_StateData(+AUD) #2923
		stepName = "Update states_id to stateData_id in DescriptionElementBase_StateData";
		tableName = "DescriptionElementBase_StateData";
		oldColumnName = "states_id";
		newColumnName = "stateData_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName,
				oldColumnName, newColumnName, INCLUDE_AUDIT);

		// specimen descriptions #3571
		// add column DescriptionBase.Specimen_ID #3571
		stepName = "Add specimen_id column to DescriptionBase";
		tableName = "DescriptionBase";
		columnName = "specimen_id";
		boolean notNull = false;
		String referencedTable = "SpecimenOrObservationBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, notNull, referencedTable);

		// update DescriptionBase.Specimen_ID data #3571
		updateDescriptionSpecimenRelation(stepList);

		// remove tables DescriptionBase_SpecimenOrObservationBase(_AUD) #3571
		stepName = "Remove table DescriptionBase_SpecimenOrObservationBase";
		tableName = "DescriptionBase_SpecimenOrObservationBase";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// change column type for reference type
		stepName = "Change column type for Reference.type";
		tableName = "Reference";
		columnName = "refType";
		Integer defaultValueStr = -1;
		notNull = true;
		int size = 3;
		ColumnTypeChanger.NewInt2StringInstance(stepList, stepName, tableName,
				columnName, size, true, defaultValueStr, notNull);

		// update reference type
		updateReferenceType(stepList);

		// create table CdmPreference #3555
		stepName = "Create table 'CdmPreference'";
		tableName = "CdmPreference";
		TableCreator stepPref = TableCreator.NewInstance(stepList, stepName, tableName,
				new String[] { "key_subject", "key_predicate", "value" }, // colNames
				new String[] { "string_100", "string_100", "string_1023", }, // columnTypes
				new String[] { null, "DefinedTermBase", null }, // referencedTables
				!INCLUDE_AUDIT, false);
		stepPref.setPrimaryKeyParams("key_subject, key_predicate", null);

		// update RightsTerm to RightsType #1306
		stepName = "Update RightsTerm -> RightsType";
		String updateSql = "UPDATE @@DefinedTermBase@@ SET DTYPE = 'RightsType'  WHERE DTYPE = 'RightsTerm'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, updateSql, 99)
				.setDefaultAuditing("DefinedTermBase");

		// update Rights table to RightsInfo
		updateRights2RightsInfo(stepList);

		// Remove column isDescriptionSeparated from FeatureTree #3678
		stepName = "Remove column isDescriptionSeparated from FeatureTree";
		tableName = "FeatureTree";
		columnName = "descriptionSeparated";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove table Sequence_GenBankAccession #3552
		stepName = "Remove table Sequence_GenBankAccession";
		tableName = "Sequence_GenBankAccession";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove table GenBankAccession #3552
		stepName = "Remove table GenBankAccession";
		tableName = "GenBankAccession";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove table Sequence_Credit #3360
		stepName = "Remove table Sequence_Credit";
		tableName = "Sequence_Credit";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove table Sequence_Extension #3360
		stepName = "Remove table Sequence_Extension";
		tableName = "Sequence_Extension";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		//remove table Sequence_Media #3360
		stepName = "Remove table Sequence_Media";
		tableName = "Sequence_Media";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove table Sequence_OriginalSourceBase #3360
		stepName = "Remove table Sequence_OriginalSourceBase";
		tableName = "Sequence_OriginalSourceBase";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove table Sequence_OriginalSourceBase #3360
		stepName = "Remove table Sequence_Rights";
		tableName = "Sequence_Rights";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// remove old sequence columns
		removeOldSequenceColumns(stepList);

		// add MediaSpecimen column #3614
		stepName = "Add mediaSpecimen column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "mediaSpecimen_id";
		notNull = false;
		referencedTable = "Media";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, notNull, referencedTable);

		// remove DescriptionBase_Feature #2202
		stepName = "Remove table DescriptionBase_Feature";
		tableName = "DescriptionBase_Feature";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// add timeperiod to columns to description element base #3312
		addTimeperiodToDescriptionElement(stepList);

		// move specimen images
		stepName = "Move images from SpecimenOrObservationBase_Media to image gallery";
		SpecimenMediaMoverUpdater.NewInstance(stepList);

		// SpecimenOrObservationBase_Media #3597
		stepName = "Remove table SpecimenOrObservationBase_Media";
		tableName = "SpecimenOrObservationBase_Media";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		// all molecular (#3360) and related updates
		updateMolecularAndRelated(stepList);

		// update vocabulary representaitons
		TermVocabularyRepresentationUpdater.NewInstance(stepList);

		return stepList;
	}

	private void updateMolecularAndRelated(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		String columnName;
		int length;
		Integer defaultValue;
		String referencedTable;

		// Primer #3360
		stepName = "Create table 'Primer'";
		tableName = "Primer";
		TableCreator.NewAnnotatableInstance(stepList, stepName, tableName,
				new String[] { "label", "sequence_id", "publishedIn_id" }, // colNames
				new String[] { "string_255", "int", "int" }, // columnTypes
				new String[] { null, Sequence.class.getSimpleName(),
						Reference.class.getSimpleName() }, // referencedTables
				INCLUDE_AUDIT);

		// MaterialOrMethod #3360
		stepName = "Create table 'MaterialOrMethodEvent'";
		tableName = MaterialOrMethodEvent.class.getSimpleName();
		TableCreator.NewEventInstance(stepList, stepName, tableName, new String[] {
				"DTYPE", "strain", "temperature", "definedMaterialOrMethod_id",
				"forwardPrimer_id", "reversePrimer_id", "medium_id" }, // colNames
				new String[] { "string_255", "string_255", "double", "int",
						"int", "int", "int" }, // columnTypes
				new String[] { null, null, null, "DefinedTermBase", "Primer",
						"Primer", "DefinedTermBase" }, // referencedTables
				INCLUDE_AUDIT);

		stepName = "Remove preservation column from SpecimenOrObservationBase";
		// to fully remove all foreign keys, maybe there is a better way to do so
		// we don't expect any preservation information to exist in any CDM
		// database
		tableName = "SpecimenOrObservationBase";
		String oldColumnName = "preservation_id";
		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName,
				INCLUDE_AUDIT);

		stepName = "Add new preservation column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		String newColumnName = "preservation_id";
		boolean notNull = false;
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName,
				newColumnName, INCLUDE_AUDIT, notNull, "MaterialOrMethodEvent");

		// Amplification #3360
		stepName = "Create table 'Amplification'";
		tableName = "Amplification";
		TableCreator.NewEventInstance(stepList, stepName, tableName,
				new String[] { "dnaSample_id", "dnaMarker_id",
						"forwardPrimer_id", "reversePrimer_id",
						"purification_id", "cloning_id", "gelPhoto_id",
						"successful", "successText", "ladderUsed",
						"electrophoresisVoltage", "gelRunningTime",
						"gelConcentration" }, // colNames
				new String[] { "int", "int", "int", "int", "int", "int", "int",
						"bit", "string_255", "string_255", "double", "double",
						"double" }, // columnTypes
				new String[] { "SpecimenOrObservationBase", "DefinedTermBase",
						"Primer", "Primer", "MaterialOrMethodEvent",
						"MaterialOrMethodEvent", "Media", null, null, null,
						null, null, null }, // referencedTables
				INCLUDE_AUDIT);

		// SingleRead #3360
		stepName = "Create table 'SingleRead'";
		tableName = "SingleRead";
		TableCreator.NewEventInstance(stepList, stepName, tableName, new String[] {
				"amplification_id", "materialOrMethod_id", "primer_id",
				"pherogram_id", "direction", "sequence_length" }, // colNames
				new String[] { "int", "int", "int", "int", "string_3", "int" }, // columnTypes
				new String[] { "Amplification", "MaterialOrMethodEvent",
						"Primer", "Media", null, null }, // referencedTables
				INCLUDE_AUDIT);

		// sequence - consensussequence_string #3360
		stepName = "Add sequence_string to single read";
		columnName = "sequence_string";
		ColumnAdder.NewClobInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// amplification - single reads #3360
		stepName = "Add single reads to amplification";
		String firstTable = "Amplification";
		String secondTable = "SingleRead";
		MnTableCreator
				.NewMnInstance(stepList, stepName, firstTable, null, secondTable, null, null,
						SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);

		// sequence - single reads #3360
		stepName = "Add single reads to sequence";
		firstTable = "Sequence";
		secondTable = "SingleRead";
		MnTableCreator
				.NewMnInstance(stepList, stepName, firstTable, null, secondTable, null, null,
						SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);

		// sequence - barcode #3360
		stepName = "Add barcodesequencepart_length to sequence";
		tableName = "Sequence";
		columnName = "barcodeSequencePart_length";
		defaultValue = null;
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, defaultValue, false);

		// sequence - barcode #3360
		stepName = "Add barcodesequencepart_string to sequence";
		tableName = "Sequence";
		columnName = "barcodeSequencePart_string";
		ColumnAdder.NewClobInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// sequence - consensussequence_length #3360
		stepName = "Add consensusSequence_length to sequence";
		tableName = "Sequence";
		columnName = "consensusSequence_length";
		defaultValue = null;
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, defaultValue, false);

		// sequence - consensussequence_string #3360
		stepName = "Add consensusSequence_string to sequence";
		tableName = "Sequence";
		columnName = "consensusSequence_string";
		ColumnAdder.NewClobInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// sequence - contigFile #3360
		stepName = "Add contigFile to sequence";
		tableName = "Sequence";
		columnName = "contigFile_id";
		referencedTable = "Media";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, referencedTable);

		// sequence - boldprocessid #3360
		stepName = "Add boldprocessId to sequence";
		tableName = "Sequence";
		columnName = "boldProcessId";
		length = 20;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		// sequence - boldprocessid #3360
		stepName = "Add geneticAccessionNumber to sequence";
		tableName = "Sequence";
		columnName = "geneticAccessionNumber";
		length = 20;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		// sequence - haplotype #3360
		stepName = "Add haplotype to sequence";
		tableName = "Sequence";
		columnName = "haplotype";
		length = 100;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				length, INCLUDE_AUDIT);

		// sequence - isBarcode #3360
		stepName = "Add isBarcode to sequence";
		tableName = "Sequence";
		columnName = "isBarcode";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false);

		// sequence - dnaMarker #3360
		stepName = "Add dnaMarker to sequence";
		tableName = "Sequence";
		columnName = "dnaMarker_id";
		referencedTable = "DefinedTermBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, referencedTable);

		// sequence - dnaSample #3360
		stepName = "Add dnaSample to sequence";
		tableName = "Sequence";
		columnName = "dnaSample_id";
		referencedTable = "SpecimenOrObservationBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false, referencedTable);
	}

	private void addPublishFlag(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		String columnName;
		String query;

		// TaxonBase

		// add publish flag to taxon
		stepName = "Add publish flag column to taxon base";
		tableName = "TaxonBase";
		columnName = "publish";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, true);

		// update publish with existing publish false markers
		stepName = "update TaxonBase publish if publish false markers exist";
		query = " UPDATE @@TaxonBase@@ "
				+ " SET publish = @FALSE@ "
				+ " WHERE id IN ( "
				 + " SELECT DISTINCT MN.TaxonBase_id "
				 + " FROM @@Marker@@ m INNER JOIN @@TaxonBase_Marker@@ MN ON MN.markers_id = m.id "
				 + " INNER JOIN @@DefinedTermBase@@ markerType ON m.markertype_id = markerType.id "
				 + " WHERE m.flag = @FALSE@ AND markerType.uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc' "
				+ ")";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "TaxonBase", 99);

		// remove publish marker MN table
		stepName = "Remove existing TaxonBase publish markers MN";
		query = " DELETE "
				+ " FROM @@TaxonBase_Marker@@ "
				+ " WHERE markers_id IN ( "
				 + " SELECT m.id "
				 + " FROM @@Marker@@ m INNER JOIN @@DefinedTermBase@@ mType ON m.markertype_id = mType.id "
				 + " WHERE mType.uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc'  "
				+ ")";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "TaxonBase_Marker", 99);

		// update publish with existing publish false markers
		stepName = "Remove existing TaxonBase publish markers";
		query = " DELETE "
				+ " FROM @@Marker@@ "
				+ " WHERE id NOT IN "
				+ " (SELECT MN.markers_id FROM @@TaxonBase_Marker@@ MN) "
				+ " AND (markedObj_type = 'eu.etaxonomy.cdm.model.taxon.Synonym' OR markedObj_type = 'eu.etaxonomy.cdm.model.taxon.Taxon') "
				+ " AND markertype_id IN ( "
				+ "SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc' "
				+ ")";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99); // AUD does not have markedObj_type

		// SpecimenOrObservationBase

		// add publish flag to specimen
		stepName = "Add publish flag column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "publish";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, true);

		// update publish with existing publish false markers
		stepName = "update SpecimenOrObservationBase publish if publish false markers exist";
		query = " UPDATE @@SpecimenOrObservationBase@@ "
				+ " SET publish = @FALSE@ "
				+ " WHERE id IN ( "
				+ " SELECT DISTINCT MN.SpecimenOrObservationBase_id "
				+ " FROM @@Marker@@ m INNER JOIN @@SpecimenOrObservationBase_Marker@@ MN ON MN.markers_id = m.id "
				+ " INNER JOIN @@DefinedTermBase@@ markerType ON m.markertype_id = markerType.id "
				+ " WHERE m.flag = @FALSE@ AND markerType.uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc' "
				+ ")";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "SpecimenOrObservationBase", 99);

		// remove publish marker MN table
		stepName = "Remove existing SpecimenOrObservationBase publish markers MN";
		query = " DELETE "
				+ " FROM @@SpecimenOrObservationBase_Marker@@ "
				+ " WHERE markers_id IN ( "
				+ " SELECT m.id "
				+ " FROM @@Marker@@ m INNER JOIN @@DefinedTermBase@@ mType ON m.markertype_id = mType.id "
				+ " WHERE mType.uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc'  "
				+ ")";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "SpecimenOrObservationBase_Marker", 99);

		// update publish with existing publish false markers
		stepName = "Remove existing SpecimenOrObservationBase publish markers";
		query = " DELETE "
				+ " FROM @@Marker@@ "
				+ " WHERE id NOT IN "
				+ " (SELECT MN.markers_id FROM @@SpecimenOrObservationBase_Marker@@ MN) "
				+ " AND (markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.DerivedUnit' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.FieldObservation' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.FieldUnit' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.Specimen' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.Fossil' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.LivingBeing' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.Observation' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.MediaSpecimen' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.occurrence.TissueSample' "
				+ "OR markedObj_type = 'eu.etaxonomy.cdm.model.molecular.DnaSample') "
				+ " AND markertype_id IN ( "
				+ "SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc' "
				+ ")";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99);

		// remove all audited markers if no current markers exist
		// this may remove more audited markers then expected but we do accept
		// this here
		stepName = "Remove all audited markers if no current markers exist";
		query = " DELETE "
				+ " FROM @@Marker_AUD@@ "
				+ " WHERE id NOT IN (SELECT id FROM @@Marker@@ ) "
				+ " AND markertype_id IN ( "
				+ "SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '0522c2b3-b21c-400c-80fc-a251c3501dbc' "
				+ ")";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99);

	}

	private void updateRights2RightsInfo(List<ISchemaUpdaterStep> stepList) {
		// #2945
		String stepName = "Update Rights to RightsInfo";
		String tableName = "Rights";
		String newTableName = "RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName,
				tableName, newTableName, INCLUDE_AUDIT);

		stepName = "Update AgentBase_Rights to RightsInfo";
		tableName = "AgentBase_Rights";
		newTableName = "AgentBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Rights_Annotation to RightsInfo";
		tableName = "Rights_Annotation";
		newTableName = "RightsInfo_Annotation";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Rights_id column in RightsInfo_Annotation";
		tableName = "RightsInfo_Annotation";
		String columnName = "Rights_Id";
		String newColumnName = "RightsInfo_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName,
				columnName, newColumnName, INCLUDE_AUDIT);

		stepName = "Update Rights_Marker to RightsInfo";
		tableName = "Rights_Marker";
		newTableName = "RightsInfo_Marker";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Rights_id column in RightsInfo_Marker";
		tableName = "RightsInfo_Marker";
		columnName = "Rights_Id";
		newColumnName = "RightsInfo_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName,
				columnName, newColumnName, INCLUDE_AUDIT);

		stepName = "Update Classification_Rights to RightsInfo";
		tableName = "Classification_Rights";
		newTableName = "Classification_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Collection_Rights to RightsInfo";
		tableName = "Collection_Rights";
		newTableName = "Collection_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update DefinedTermBase_Rights to RightsInfo";
		tableName = "DefinedTermBase_Rights";
		newTableName = "DefinedTermBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update DescriptionBase_Rights to RightsInfo";
		tableName = "DescriptionBase_Rights";
		newTableName = "DescriptionBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update FeatureTree_Rights to RightsInfo";
		tableName = "FeatureTree_Rights";
		newTableName = "FeatureTree_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Media_Rights to RightsInfo";
		tableName = "Media_Rights";
		newTableName = "Media_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update PolytomousKey_Rights to RightsInfo";
		tableName = "PolytomousKey_Rights";
		newTableName = "PolytomousKey_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update Reference_Rights to RightsInfo";
		tableName = "Reference_Rights";
		newTableName = "Reference_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update SpecimenOrObservationBase_Rights to RightsInfo";
		tableName = "SpecimenOrObservationBase_Rights";
		newTableName = "SpecimenOrObservationBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update TaxonBase_Rights to RightsInfo";
		tableName = "TaxonBase_Rights";
		newTableName = "TaxonBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update TaxonNameBase_Rights to RightsInfo";
		tableName = "TaxonNameBase_Rights";
		newTableName = "TaxonNameBase_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);

		stepName = "Update TermVocabulary_Rights to RightsInfo";
		tableName = "TermVocabulary_Rights";
		newTableName = "TermVocabulary_RightsInfo";
		TableNameChanger.NewInstance(stepList, stepName, tableName, newTableName,
				INCLUDE_AUDIT);
	}

	private void updateReferenceType(List<ISchemaUpdaterStep> stepList) {

		String baseQuery = " UPDATE @@Reference@@ " + " SET refType = '%s' "
				+ " WHERE refType = '%s' ";
		Integer index = 0;
		String tableName = "Reference";

		// 0-Article
		String stepName = "Update reference refType for Article";
		String query = String.format(baseQuery, ReferenceType.Article.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99).setDefaultAuditing(tableName);

		// 1-Book
		stepName = "Update reference refType for Book";
		query = String.format(baseQuery, ReferenceType.Book.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 2-Book Section
		stepName = "Update reference refType for Book Section";
		query = String.format(baseQuery, ReferenceType.BookSection.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 3-CD / DVD
		stepName = "Update reference refType for CD";
		query = String.format(baseQuery, ReferenceType.CdDvd.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 4-Database
		stepName = "Update reference refType for Database";
		query = String.format(baseQuery, ReferenceType.Database.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 5-Generic
		stepName = "Update reference refType for Generic";
		query = String.format(baseQuery, ReferenceType.Generic.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 6-InProceedings
		stepName = "Update reference refType for InProceedings";
		query = String.format(baseQuery, ReferenceType.InProceedings.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 7-Journal
		stepName = "Update reference refType for Journal";
		query = String.format(baseQuery, ReferenceType.Journal.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 8-Map
		stepName = "Update reference refType for Map";
		query = String.format(baseQuery, ReferenceType.Map.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 9-Patent
		stepName = "Update reference refType for Patent";
		query = String.format(baseQuery, ReferenceType.Patent.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 10-Personal Communication
		stepName = "Update reference refType for Personal Communication";
		query = String.format(baseQuery,
				ReferenceType.PersonalCommunication.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 11-PrintSeries
		stepName = "Update reference refType for PrintSeries";
		query = String.format(baseQuery, ReferenceType.PrintSeries.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// 12-Proceedings
		stepName = "Update reference refType for Proceedings";
		query = String.format(baseQuery, ReferenceType.Proceedings.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
		;

		// 13-Report
		stepName = "Update reference refType for Report";
		query = String.format(baseQuery, ReferenceType.Report.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
		;

		// 14-Thesis
		stepName = "Update reference refType for Thesis";
		query = String.format(baseQuery, ReferenceType.Thesis.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
		;

		// 15-WebPage
		stepName = "Update reference refType for WebPage";
		query = String.format(baseQuery, ReferenceType.WebPage.getKey(),
				String.valueOf(index++));
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
		;
	}

	private void updateRecordBasis(List<ISchemaUpdaterStep> stepList) {
		String stepName = "Update recordBasis for SpecimenOrObservationBase";
		String tableName = "@@SpecimenOrObservationBase@@";

		// Field Unit
		String query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.FieldUnit.getKey() + "' "
				+ " WHERE DTYPE = 'FieldUnit' OR DTYPE = 'FieldObservation'";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99)
					.setDefaultAuditing(tableName);

		// DerivedUnit
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.DerivedUnit.getKey() + "' "
				+ " WHERE DTYPE = '" + DerivedUnit.class.getSimpleName() + "'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Living Being
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.LivingSpecimen.getKey() + "' "
				+ " WHERE DTYPE = 'LivingBeing'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Observation
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.Observation.getKey() + "' "
				+ " WHERE DTYPE = 'Observation'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Preserved Specimen
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.PreservedSpecimen.getKey() + "' "
				+ " WHERE DTYPE = 'Specimen'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Fossil
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.Fossil.getKey() + "' "
				+ " WHERE DTYPE = 'Fossil'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// DnaSample
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.DnaSample.getKey() + "' "
				+ " WHERE DTYPE = 'DnaSample'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Unknown as default (if not yet handled before)
		query = " UPDATE " + tableName + " SET recordBasis = '"
				+ SpecimenOrObservationType.Unknown.getKey() + "' "
				+ " WHERE recordBasis IS NULL ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
	}

	// update length of all title caches and full title cache in names
	// TODO test for H2, Postgres, SqlServer
	// https://dev.e-taxonomy.eu/redmine/issues/1592
	private void updateTitleCacheLength(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		String columnName;
		int size = 800;

		stepName = "Change length of TaxonName fullTitleCache";
		tableName = "TaxonNameBase";
		columnName = "fullTitleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of TaxonName title cache";
		tableName = "TaxonNameBase";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of TaxonBase title cache";
		tableName = "TaxonBase";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of Classification title cache";
		tableName = "Classification";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of DescriptionBase title cache";
		tableName = "DescriptionBase";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of FeatureTree title cache";
		tableName = "FeatureTree";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of Collection title cache";
		tableName = "Collection";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of Reference title cache";
		tableName = "Reference";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of Media title cache";
		tableName = "Media";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of PolytomousKey title cache";
		tableName = "PolytomousKey";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of SpecimenOrObservationBase title cache";
		tableName = "SpecimenOrObservationBase";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of DefinedTermBase title cache";
		tableName = "DefinedTermBase";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

		stepName = "Change length of TermVocabulary title cache";
		tableName = "TermVocabulary";
		columnName = "titleCache";
		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
				columnName, size, INCLUDE_AUDIT);

	}

	private void updateDescriptionSpecimenRelation(
			List<ISchemaUpdaterStep> stepList) {
		// TODO warn if multiple entries for 1 description exists -> won't do, as this is currently not expected
		String sqlCount = " SELECT count(*) as n "
				+ " FROM DescriptionBase_SpecimenOrObservationBase MN "
				+ " GROUP BY MN.descriptions_id "
				+ " HAVING count(*) > 1 "
				+ " ORDER BY MN.descriptions_id, MN.describedspecimenorobservations_id ";

		// TODO ... and log the concrete records
		// FROM DescriptionBase_SpecimenOrObservationBase ds
		// WHERE ds.descriptions_id IN (
		// SELECT MN.descriptions_id
		// FROM DescriptionBase_SpecimenOrObservationBase MN
		// GROUP BY MN.descriptions_id
		// HAVING count(*) > 1
		// )
		// ORDER BY descriptions_id, describedspecimenorobservations_id

		// TODO test for H2, Postgresql AND SQLServer (later will need TOP 1)
		String stepName = "UPDATE Description - Specimen relation data  ";
		String sql = " UPDATE @@DescriptionBase@@ " + " SET specimen_id =  "
				+ " (SELECT  MN.describedspecimenorobservations_id "
				+ " FROM @@DescriptionBase_SpecimenOrObservationBase@@ MN "
				+ " WHERE MN.descriptions_id = @@DescriptionBase@@.id " + " LIMIT 1 " + ")";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, sql, 99);

	}

	private void updateAbbrevTitle(List<ISchemaUpdaterStep> stepList) {
		String tableName = "Reference";

		String stepName = "Update abbrevTitleCache for protected title caches with title";
		String query = " UPDATE @@Reference@@ "
				+ " SET abbrevTitle = left(title, 255), abbrevTitleCache = titleCache, protectedAbbrevTitleCache = protectedTitleCache";
		// + " WHERE r.title IS NOT NULL AND r.protectedTitleCache = 1 ";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99)
					.setDefaultAuditing(tableName);

		// stepName =
		// "Update abbrevTitleCache for protected title caches with no title";
		// query = " UPDATE Reference r " +
		// " SET r.abbrevTitleCache = r.titleCache, r.protectedAbbrevTitleCache = r.protectedTitleCache"
		// +
		// " WHERE r.title IS NULL AND r.protectedTitleCache = 1 ";
		// SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName,
		// query).setAuditing("Reference");

		// stepName =
		// "Update abbrevTitleCache for protected title caches with title";
		// query = " UPDATE Reference r " +
		// " SET r.abbrevTitle = r.title, r.abbrevTitleCache = r.titleCache, r.protectedAbbrevTitleCache = r.protectedTitleCache"
		// +
		// " WHERE r.title IS NOT NULL AND r.protectedTitleCache = 0 ";
		// SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName,
		// query).setAuditing("Reference");

		stepName = "Update reference title, set null where abbrev title very likely";
		query = " UPDATE @@Reference@@ "
				+ " SET title = NULL "
				+ " WHERE title IS NOT NULL AND protectedTitleCache = @FALSE@ AND "
				+ " ( LENGTH(title) <= 15 AND title like '%.%.%' OR LENGTH(title) < 30 AND title like '%.%.%.%' OR LENGTH(title) < 45 AND title like '%.%.%.%.%' OR LENGTH(title) < 60 AND title like '%.%.%.%.%.%' "
				+ ")";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		stepName = "Update reference abbrevTitle, set null where abbrev title very unlikely";
		query = " UPDATE @@Reference@@ "
				+ " SET abbrevTitle = NULL "
				+ " WHERE title IS NOT NULL AND protectedTitleCache = @FALSE@ AND "
				+ " ( title NOT like '%.%' OR LENGTH(title) > 30 AND title NOT like '%.%.%' "
				+ ")";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

	}

	private void removeOldSequenceColumns(List<ISchemaUpdaterStep> stepList) {

		// remove citation microreference
		String stepName = "Remove citationmicroreference column";
		String tableName = "Sequence";
		String columnName = "citationMicroReference";
		ColumnRemover.NewInstance(stepList, stepName,
				tableName, columnName, INCLUDE_AUDIT);

		// remove datesequenced
		stepName = "Remove datesequenced column";
		columnName = "datesequenced";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove length
		stepName = "Remove length column";
		columnName = "length";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove sequence
		stepName = "Remove sequence column";
		columnName = "sequence";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove locus_id
		stepName = "Remove locus_id column";
		columnName = "locus_id";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove publishedin_id
		stepName = "Remove publishedin_id column";
		columnName = "publishedin_id";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove barcode
		stepName = "Remove barcode column";
		columnName = "barcode";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// identifiable columns
		// remove lsid_authority
		stepName = "Remove lsid_authority";
		columnName = "lsid_authority";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove lsid_lsid
		stepName = "Remove lsid_lsid";
		columnName = "lsid_lsid";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove lsid_namespace
		stepName = "Remove lsid_namespace";
		columnName = "lsid_namespace";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove lsid_object
		stepName = "Remove lsid_object";
		columnName = "lsid_object";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove lsid_revision
		stepName = "Remove lsid_revision";
		columnName = "lsid_revision";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove protectedTitleCache
		stepName = "Remove protectedTitleCache";
		columnName = "protectedTitleCache";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// remove titleCache
		stepName = "Remove titleCache";
		columnName = "titleCache";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

	}

	private void updateIdInVocabulary(List<ISchemaUpdaterStep> stepList) {
		String tableName = "DefinedTermBase";

		//NOT ANSI  - works with MySQL http://stackoverflow.com/questions/1293330/how-can-i-do-an-update-statement-with-join-in-sql
//		String queryVocUuid = " UPDATE @@DefinedTermBase@@ dtb INNER JOIN @@TermVocabulary@@ voc ON voc.id = dtb.vocabulary_id"
//				+ " SET dtb.idInVocabulary = (SELECT abbreviatedlabel "
//				+ " FROM @@DefinedTermBase_Representation@@ MN "
//				+ " INNER JOIN @@Representation@@ r ON r.id = MN.representations_id "
//				+ " WHERE MN.DefinedTermBase_id = dtb.id) "
//				+ " WHERE voc.uuid = '%s'";

		//ANSI - SQL
		String queryVocUuid = " UPDATE @@DefinedTermBase@@ "
				+ " SET idInVocabulary = " +
					" (SELECT abbreviatedlabel "
					+ " FROM @@DefinedTermBase_Representation@@ MN "
					+ " INNER JOIN @@Representation@@ r ON r.id = MN.representations_id "
					+ " WHERE MN.DefinedTermBase_id = @@DefinedTermBase@@.id) "
				+ " WHERE EXISTS (SELECT * FROM @@TermVocabulary@@ voc WHERE voc.id = @@DefinedTermBase@@.vocabulary_id " +
						" AND voc.uuid = '%s') ";


		// Languages (ISO)
		String stepName = "Update idInVocabulary for Languages ";
//		String query = "UPDATE @@DefinedTermBase@@ dtb INNER JOIN @@TermVocabulary@@ voc ON voc.id = dtb.vocabulary_id "
//				+ " SET dtb.idInVocabulary = dtb.iso639_2 "
//				+ " WHERE voc.uuid = '45ac7043-7f5e-4f37-92f2-3874aaaef2de' ";
		String query = "UPDATE @@DefinedTermBase@@ "
				+ " SET idInVocabulary = iso639_2 "
				+ " WHERE EXISTS (SELECT * FROM @@TermVocabulary@@ voc WHERE voc.id = @@DefinedTermBase@@.vocabulary_id " +
						" AND voc.uuid = '45ac7043-7f5e-4f37-92f2-3874aaaef2de') ";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99).setDefaultAuditing(
						tableName); // not fully correct as we should join with
									// TermVoc_AUD but good enough for this usecase

		// Undefined Languages => all
		stepName = "Update idInVocabulary for undefined languages";
		String uuid = "7fd1e6d0-2e76-4dfa-bad9-2673dd042c28";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// Waterbody & Country => all
		stepName = "Update idInVocabulary for WaterbodyOrCountries";
		uuid = "006b1870-7347-4624-990f-e5ed78484a1a";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// TdwgAreas => all
		stepName = "Update idInVocabulary for TDWG areas";
		uuid = NamedArea.uuidTdwgAreaVocabulary.toString();
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// Rank => some
		stepName = "Update idInVocabulary for ranks";
		uuid = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// avoid duplicate for section (bot.)
		stepName = "Update idInVoc for section (bot.)";
		String sql = " UPDATE @@DefinedTermBase@@ " +
				" SET idInVocabulary = 'sect.(bot.)' " +
				" WHERE uuid = '3edff68f-8527-49b5-bf91-7e4398bb975c'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// avoid duplicate for subsection (bot.)
		stepName = "Update idInVoc for subsection (bot.)";
		sql = " UPDATE @@DefinedTermBase@@ " +
				" SET idInVocabulary = 'subsect.(bot.)' " +
				" WHERE uuid = 'd20f5b61-d463-4448-8f8a-c1ff1f262f59'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// avoid duplicate for section (zool.)
		stepName = "Update idInVoc for section (zool.)";
		sql = " UPDATE @@DefinedTermBase@@ SET idInVocabulary = 'sect.(zool.)' WHERE uuid = '691d371e-10d7-43f0-93db-3d7fa1a62c54'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// avoid duplicate for subsection (zool.)
		stepName = "Update idInVoc for subsection (zool.)";
		sql = " UPDATE @@DefinedTermBase@@ SET idInVocabulary = 'subsect.(zool.)' WHERE uuid = '0ed32d28-adc4-4303-a9ca-68e2acd67e33'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// SpecimenTypeDesignationStatus => all
		stepName = "Update idInVocabulary for SpecimenTypeDesignationStatus";
		uuid = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// avoid duplicate for PT
		stepName = "Update idInVoc for Phototype (PhT) to avoid duplicate for PT";
		sql = " UPDATE @@DefinedTermBase@@ SET idInVocabulary = 'PhT' WHERE uuid = 'b7807acc-f559-474e-ad4a-e7a41e085e34'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// NameTypeDesignationStatus => all
		stepName = "Update idInVocabulary for NameTypeDesignationStatus";
		uuid = "ab60e738-4d09-4c24-a1b3-9466b01f9f55";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// NomenclaturalStatusType => all, abbrevs.
		stepName = "Update idInVocabulary for NomenclaturalStatusType";
		uuid = "bb28cdca-2f8a-4f11-9c21-517e9ae87f1f";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// TaxonRelationshipType, all but 2 (Invalid Designation for, Misapplied
		// Name for)
		stepName = "Update idInVocabulary for TaxonRelationshipType";
		uuid = "15db0cf7-7afc-4a86-a7d4-221c73b0c9ac";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// avoid duplicate for Misapplied Name (remove '-')
		stepName = "Update idInVoc for Misapplied Name Relationship";
		sql = " UPDATE @@DefinedTermBase@@ SET idInVocabulary = NULL WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// avoid duplicate for Invalid designation (remove '-')
		stepName = "Update idInVoc for Invalid Designation";
		sql = " UPDATE @@DefinedTermBase@@ SET idInVocabulary = NULL WHERE uuid = '605b1d01-f2b1-4544-b2e0-6f08def3d6ed'";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// PresenceTerm => all
		stepName = "Update idInVocabulary for PresenceTerm";
		uuid = "adbbbe15-c4d3-47b7-80a8-c7d104e53a05";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// AbsenceTerm => all
		stepName = "Update idInVocabulary for AbsenceTerm";
		uuid = "5cd438c8-a8a1-4958-842e-169e83e2ceee";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// Sex => all
		stepName = "Update idInVocabulary for Sex";
		uuid = "9718b7dd-8bc0-4cad-be57-3c54d4d432fe";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// ExtensionType => all
		stepName = "Update idInVocabulary for ExtensionType";
		uuid = "117cc307-5bd4-4b10-9b2f-2e14051b3b20";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// ReferenceSystem => all
		stepName = "Update idInVocabulary for ReferenceSystem";
		uuid = "ec6376e5-0c9c-4f5c-848b-b288e6c17a86";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// DeterminationModifier => all
		stepName = "Update idInVocabulary for DeterminationModifier";
		uuid = "fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid), 99)
				.setDefaultAuditing(tableName);

		// InstitutionType, MeasurementUnit, Scope, Stage, State, TextFormat,
		// Modifier, PreservationMethod => dummies
		stepName = "Update idInVocabulary for dummy terms in several vocabularies";
		query = " UPDATE @@DefinedTermBase@@ "
				+ " SET idInVocabulary = (SELECT abbreviatedlabel "
					+ " FROM @@DefinedTermBase_Representation@@ MN "
					+ " INNER JOIN @@Representation@@ r ON r.id = MN.representations_id "
					+ " WHERE MN.DefinedTermBase_id = @@DefinedTermBase@@.id) "
				+ " WHERE termType IN ('%s','%s','%s','%s','%s','%s','%s','%s')";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList,
				stepName,
				String.format(query, TermType.InstitutionType.getKey(),
						TermType.MeasurementUnit.getKey(),
						TermType.Scope.getKey(), TermType.Stage.getKey(),
						TermType.State.getKey(), TermType.TextFormat.getKey(),
						TermType.Modifier.getKey(), TermType.Method.getKey()), 99)
				.setDefaultAuditing(tableName);

		stepName = "Update idInVocabulary for dummy state";
		query = " UPDATE @@DefinedTermBase@@ "
				+ " SET idinvocabulary = 'std' "
				+ " WHERE uuid = '881b9c80-626d-47a6-b308-a63ee5f4178f' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99);

		stepName = "Update idInVocabulary for dummy stage";
		query = " UPDATE @@DefinedTermBase@@ "
				+ " SET idinvocabulary = 'sgd' "
				+ " WHERE uuid = '48f8e8a7-a2ac-4974-9ce8-6944afc5095e' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99);

		stepName = "Update idInVocabulary for dummy modifier";
		query = " UPDATE @@DefinedTermBase@@ "
				+ " SET idinvocabulary = 'md' "
				+ " WHERE uuid = 'efc38dad-205c-4028-ad9d-ae509a14b37a' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99);

		// Remove state dummy
		stepName = "Remove state dummy if possible";
		uuid = "881b9c80-626d-47a6-b308-a63ee5f4178f";
		String checkUsed = " SELECT count(*) as n FROM @@StateData@@ sd "
				+ " WHERE sd.state_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Remove institution type dummy
		stepName = "Remove institution type dummy term";
		uuid = "bea94a6c-472b-421c-abc1-52f797c51dbf";
		checkUsed = " SELECT count(*) as n FROM @@AgentBase_DefinedTermBase@@ MN "
				+ " WHERE MN.types_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Remove measurement unit dummy
		stepName = "Remove measurement unit dummy term";
		uuid = "e19dd590-5be8-4c93-978f-b78554116289";
		checkUsed = " SELECT count(*) as n FROM @@DescriptionElementBase@@ deb "
				+ " WHERE deb.unit_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Remove scope dummy
		stepName = "Remove scope dummy term";
		uuid = "2ace7f1f-4ce6-47e1-8a65-e3f6b724876c";
		checkUsed = " SELECT count(*) as n FROM @@DescriptionBase_Scope@@ MN "
				+ " WHERE MN.scopes_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Remove stage dummy
		stepName = "Remove stage dummy term";
		uuid = "48f8e8a7-a2ac-4974-9ce8-6944afc5095e";
		checkUsed = " SELECT count(*) as n FROM @@DescriptionBase_Scope@@ MN "
				+ " WHERE MN.scopes_id = %d ";
		String checkUsed2 = " SELECT count(*) as n FROM @@SpecimenOrObservationBase@@ osb "
				+ " WHERE osb.lifestage_id =  %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99)
				.addCheckUsedQuery(checkUsed2, 99);

		// Remove text format dummy
		stepName = "Remove text format dummy if possible";
		uuid = "5d095782-d99c-46bc-a158-edb2e47c9b63";
		checkUsed = " SELECT count(*) as n FROM @@DescriptionElementBase@@ deb "
				+ " WHERE deb.format_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Remove modifier dummy
		stepName = "Remove modifier dummy if possible";
		uuid = "efc38dad-205c-4028-ad9d-ae509a14b37a";
		checkUsed = " SELECT count(*) as n FROM @@DescriptionElementBase_Modifier@@ MN "
				+ " WHERE MN.modifiers_id = %d ";
		checkUsed2 = " SELECT count(*) as n FROM @@StateData_DefinedTermBase@@ MN "
				+ " WHERE MN.modifiers_id = %d ";
		String checkUsed3 = " SELECT count(*) as n FROM @@StatisticalMeasurementValue_DefinedTermBase@@ MN "
				+ " WHERE MN.modifiers_id = %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99)
				.addCheckUsedQuery(checkUsed2, 99).addCheckUsedQuery(checkUsed3, 99);

		// Remove text preservation method dummy
		stepName = "Remove preservation method dummy if possible";
		uuid = "3edc2633-365b-4a9b-bc3a-f3f85f59dbdf";
		checkUsed = " SELECT count(*) as n FROM @@SpecimenOrObservationBase@@ osb "
				+ " WHERE osb.preservation_id =  %d ";
		SingleTermRemover.NewInstance(stepList, stepName, uuid, checkUsed, 99);

		// Split Country Vocabulary #3700
		stepName = "Create Waterbody vocabulary";
		UUID uuidVocabulary = UUID
				.fromString("35a62b25-f541-4f12-a7c7-17d90dec3e03");
		String description = "Major Waterbodies of the World";
		String label = "Waterbody";
		String abbrev = null;
		boolean isOrdered = false;
		TermType termType = TermType.NamedArea;
		Class<?> termClass = NamedArea.class;
		VocabularyCreator.NewVocabularyInstance(stepList, uuidVocabulary,
				description, label, abbrev, isOrdered, termClass, termType);

		stepName = "Move waterbodies to new vocabulary";
		UUID newVocabulary = UUID.fromString("35a62b25-f541-4f12-a7c7-17d90dec3e03");
		TermMover
				.NewInstance(stepList, stepName, newVocabulary,
						"aa96ca19-46ab-6365-af29-e4842f13eb4c")
				.addTermUuid(UUID.fromString("36aea55c-46ab-6365-af29-e4842f13eb4c"))
				.addTermUuid(UUID.fromString("36aea55c-892c-6365-af29-e4842f13eb4c"))
				.addTermUuid(UUID.fromString("36aea55c-892c-4114-af29-d4b287f76fab"))
				.addTermUuid(UUID.fromString("aa96ca19-892c-4114-af29-d4b287f76fab"))
				.addTermUuid(UUID.fromString("aa96ca19-892c-4114-a494-d4b287f76fab"))
				.addTermUuid(UUID.fromString("d4cf6c57-892c-4114-bf57-96886eb7108a"))
				.addTermUuid(UUID.fromString("d4cf6c57-892c-c953-a494-96886eb7108a"))
				.addTermUuid(UUID.fromString("aa96ca19-46ab-c953-a494-96886eb7108a"))
				.addTermUuid(UUID.fromString("aa96ca19-46ab-4114-a494-96886eb7108a"));

		// update waterbody uuids #3705 AND waterbody DTYPE to NamedArea and
		// sortindex new #3700
		stepName = "Update waterbody uuids";
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 1, uuid = 'af4271e5-8897-4e6f-9db7-54ea4f28cfc0' WHERE uuid = 'aa96ca19-46ab-6365-af29-e4842f13eb4c' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, "DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 2, uuid = '77e79804-1b17-4c99-873b-933fe216e3da' WHERE uuid = '36aea55c-46ab-6365-af29-e4842f13eb4c' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 3, uuid = '3d68a327-104c-49d5-a2d8-c71c6600181b' WHERE uuid = '36aea55c-892c-6365-af29-e4842f13eb4c' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 4, uuid = 'ff744a37-5990-462c-9c20-1e85a9943851' WHERE uuid = '36aea55c-892c-4114-af29-d4b287f76fab' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 5, uuid = 'ef04f363-f67f-4a2c-8d98-110de4c5f654' WHERE uuid = 'aa96ca19-892c-4114-af29-d4b287f76fab' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 6, uuid = '8811a47e-29d6-4455-8f83-8916b78a692f' WHERE uuid = 'aa96ca19-892c-4114-a494-d4b287f76fab' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 7, uuid = '4cb4bbae-9aab-426c-9025-e34f809165af' WHERE uuid = 'd4cf6c57-892c-4114-bf57-96886eb7108a' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 8, uuid = '598fec0e-b93a-4947-a1f3-601e380797f7' WHERE uuid = 'd4cf6c57-892c-c953-a494-96886eb7108a' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 9, uuid = 'ee69385e-6c80-405c-be6e-974e9fd1e297' WHERE uuid = 'aa96ca19-46ab-c953-a494-96886eb7108a' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'NamedArea', orderindex = 10, uuid = '8dc16e70-74b8-4143-95cf-a659a319a854' WHERE uuid = 'aa96ca19-46ab-4114-a494-96886eb7108a' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// update DTYPE for country
		stepName = "Update DTYPE for Countries";
		sql = " UPDATE @@DefinedTermBase@@ SET DTYPE = 'Country' WHERE DTYPE = 'WaterbodyOrCountry' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql,
				"DefinedTermBase", 99);

		// Rename tables
		stepName = "Rename DefinedTermBase_WaterbodyOrCountry";
		String oldName = "DefinedTermBase_WaterbodyOrCountry";
		String newName = "DefinedTermBase_Country";
		TableNameChanger.NewInstance(stepList, stepName, oldName, newName,
				INCLUDE_AUDIT);

		// rename column
		stepName = "Rename DefinedTermBase_Country.waterbodiesorcountries_id";
		tableName = "DefinedTermBase_Country";
		String oldColumnName = "waterbodiesorcountries_id";
		String newColumnName = "countries_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName,
				oldColumnName, newColumnName, INCLUDE_AUDIT);

		// NULL for empty strings
		stepName = "Update idInVocabulary, replace empty strings by null";
		query = "Update @@DefinedTermBase@@ dtb SET idInVocabulary = NULL WHERE idInVocabulary = ''";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// MarkerType, AnnotationType, NamedAreaType, NamedAreaLevel, Feature,
		// Continent, DerivationEventType, StatisticalMeasure,
		// RightsType,SynonymType & HybridRelationshipType &
		// NameRelationshipType
		// => none

		// DnaMarker => yes but no entries

		// Clean up empty abbreviated labels in representations
		stepName = "Update abbreviated label, replace empty strings by null";
		query = "UPDATE @@Representation@@ SET abbreviatedLabel = NULL WHERE abbreviatedLabel = ''";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing("Representation"); // AUD not needed

	}

	private void updateTermTypesForVocabularies(
			List<ISchemaUpdaterStep> stepList) {
		// vocabularies with terms
		for (TermType termType : TermType.values()) {
			updateTermTypeForVocabularies(stepList, termType);
		}

		String tableName = "TermVocabulary";
		// Natural Language Terms
		String stepName = "Updater termType for NaturalLanguageTerms";
		String query = "UPDATE @@TermVocabulary@@ " + " SET termType = '"
				+ TermType.NaturalLanguageTerm.getKey() + "' "
				+ " WHERE uuid = 'fdaba4b0-5c14-11df-a08a-0800200c9a66'";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99).setDefaultAuditing(
						tableName);

		// remaining vocabularies
		stepName = "Updater termType for remaining vocabularies";
		query = "UPDATE @@TermVocabulary@@ " + " SET termType = '"
				+ TermType.Unknown.getKey() + "' "
				+ " WHERE termType IS NULL";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

	}

	private void updateTermTypeForVocabularies(
			List<ISchemaUpdaterStep> stepList, TermType termType) {
		String stepName = "Updater vocabulary termType for "
				+ termType.toString();
		String query = "UPDATE @@TermVocabulary@@ "
				+ " SET termType = '"
				+ termType.getKey()
				+ "' "
				+ " WHERE Exists (SELECT * FROM @@DefinedTermBase@@ dtb WHERE dtb.termType = '"
				+ termType.getKey() + "' AND dtb.vocabulary_id = @@TermVocabulary@@.id)";
		SimpleSchemaUpdaterStep
				.NewNonAuditedInstance(stepList, stepName, query, 99).setDefaultAuditing(
						"TermVocabulary"); // AUD not fully correct as subselect
							// should also work on AUD, good enough for our purposes
	}

	/**
	 * @param stepList
	 * @param stepName
	 */
	private void updateTermTypesForTerms(List<ISchemaUpdaterStep> stepList) {
		String stepName = "Update termType for NamedAreas";
		String tableName = "DefinedTermBase";

		//NamedArea
		String query = " UPDATE @@DefinedTermBase@@ " +
				" SET termType = '" + TermType.NamedArea.getKey() + "' " +
				" WHERE DTYPE = '" + NamedArea.class.getSimpleName() + "' OR DTYPE = 'TdwgArea' " +
						"OR DTYPE = 'WaterbodyOrCountry' OR DTYPE = '"+ Country.class.getSimpleName() + "' OR DTYPE = 'Continent' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99).setDefaultAuditing(tableName);

		// Lanugage
		stepName = "Update termType for Language";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Language.getKey() + "' " + " WHERE DTYPE = '"
				+ Language.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// RANK
		stepName = "Update termType for Rank";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Rank.getKey() + "' " + " WHERE DTYPE = '"
				+ Rank.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Feature
		stepName = "Update termType for Feature";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Feature.getKey() + "' " + " WHERE DTYPE = '"
				+ Feature.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// AnnotationType
		stepName = "Update termType for Annotation Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.AnnotationType.getKey() + "' " + " WHERE DTYPE = '"
				+ AnnotationType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// MarkerType
		stepName = "Update termType for Marker Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.MarkerType.getKey() + "' " + " WHERE DTYPE = '"
				+ MarkerType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// ExtensionType
		stepName = "Update termType for Extension Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.ExtensionType.getKey() + "' " + " WHERE DTYPE = '"
				+ ExtensionType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// DerivationEventType
		stepName = "Update termType for DerivationEvent Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.DerivationEventType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ DerivationEventType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// PresenceAbsenceTerm
		stepName = "Update termType for PresenceAbsence Term";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.PresenceAbsenceTerm.getKey() + "' "
				+ " WHERE DTYPE = 'PresenceTerm' OR DTYPE = 'AbsenceTerm'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NomenclaturalStatusType
		stepName = "Update termType for NomenclaturalStatusType";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NomenclaturalStatusType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ NomenclaturalStatusType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NameRelationshipType
		stepName = "Update termType for NameRelationship Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NameRelationshipType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ NameRelationshipType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// HybridRelationshipType
		stepName = "Update termType for HybridRelationship Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.HybridRelationshipType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ HybridRelationshipType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// SynonymType
		stepName = "Update termType for SynonymRelationship Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.SynonymType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ SynonymType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// TaxonRelationshipType
		stepName = "Update termType for TaxonRelationship Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.TaxonRelationshipType.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ TaxonRelationshipType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NameTypeDesignationStatus
		stepName = "Update termType for NameTypeDesignationStatus";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NameTypeDesignationStatus.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ NameTypeDesignationStatus.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// SpecimenTypeDesignationStatus
		stepName = "Update termType for SpecimenTypeDesignationStatus";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.SpecimenTypeDesignationStatus.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ SpecimenTypeDesignationStatus.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// InstitutionType
		stepName = "Update termType for Institution Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.InstitutionType.getKey() + "' "
				+ " WHERE DTYPE = 'InstitutionType' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NamedAreaType
		stepName = "Update termType for NamedArea Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NamedAreaType.getKey() + "' " + " WHERE DTYPE = '"
				+ NamedAreaType.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NamedAreaLevel
		stepName = "Update termType for NamedArea Level";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NamedAreaLevel.getKey() + "' " + " WHERE DTYPE = '"
				+ NamedAreaLevel.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// RightsType
		stepName = "Update termType for Rights Type";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.RightsType.getKey() + "' "
				+ " WHERE DTYPE = 'RightsType' OR DTYPE = 'RightsTerm' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// MeasurementUnit
		stepName = "Update termType for MeasurementUnit";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.MeasurementUnit.getKey() + "' " + " WHERE DTYPE = '"
				+ MeasurementUnit.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// StatisticalMeasure
		stepName = "Update termType for Statistical Measure";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.StatisticalMeasure.getKey() + "' "
				+ " WHERE DTYPE = '" + StatisticalMeasure.class.getSimpleName()
				+ "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// PreservationMethod
		stepName = "Update termType for Preservation Method";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Method.getKey() + "' " + " WHERE DTYPE = '"
				+ PreservationMethod.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Modifier
		stepName = "Update termType for Modifier";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Modifier.getKey() + "' "
				+ " WHERE DTYPE = 'Modifier' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Scope
		stepName = "Update termType for Scope";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Scope.getKey() + "' " + " WHERE DTYPE = 'Scope' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Stage
		stepName = "Update termType for Stage";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Stage.getKey() + "' " + " WHERE DTYPE = 'Stage' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// Sex
		stepName = "Update termType for Sex";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.Sex.getKey() + "' " + " WHERE DTYPE = 'Sex' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// ReferenceSystem
		stepName = "Update termType for ReferenceSystem";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.ReferenceSystem.getKey() + "' " + " WHERE DTYPE = '"
				+ ReferenceSystem.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// State
		stepName = "Update termType for State";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.State.getKey() + "' " + " WHERE DTYPE = '"
				+ State.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// NaturalLanguageTerm
		stepName = "Update termType for NaturalLanguageTerm";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.NaturalLanguageTerm.getKey() + "' "
				+ " WHERE DTYPE = '"
				+ NaturalLanguageTerm.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// TextFormat
		stepName = "Update termType for TextFormat";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.TextFormat.getKey() + "' " + " WHERE DTYPE = '"
				+ TextFormat.class.getSimpleName() + "' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// DeterminationModifier
		stepName = "Update termType for DeterminationModifier";
		query = " UPDATE @@DefinedTermBase@@ " + " SET termType = '"
				+ TermType.DeterminationModifier.getKey() + "' "
				+ " WHERE DTYPE = 'DeterminationModifier' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

	}

	/**
	 * @param stepList
	 */
	private void updateDtypeOfDefinedTerms(List<ISchemaUpdaterStep> stepList) {
		String tableName = "DefinedTermBase";

		// update DTYPE for institution type and modifiers (Stage, Scope, Sex,
		// DeterminationModifier, Modifier) -> DefinedTerm
		String stepName = "Update DTYPE for TDWG Areas";
		String query = " UPDATE @@DefinedTermBase@@ "
				+ " SET DTYPE = 'DefinedTerm' "
				+ " WHERE DTYPE = 'Stage' OR DTYPE = 'Scope' OR DTYPE = 'Sex' OR DTYPE = 'DeterminationModifier'  "
				+ " OR DTYPE = 'Modifier' OR DTYPE = 'InstitutionType' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// update DTYPE for TDWG Areas and Continents -> NamedArea
		stepName = "Update DTYPE for TDWG Areas and Continents";
		query = " UPDATE @@DefinedTermBase@@ " + " SET DTYPE = 'NamedArea' "
				+ " WHERE DTYPE = 'TdwgArea' OR DTYPE = 'Continent' ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

	}

	/**
	 * @param stepList
	 */
	private void changeUriType(List<ISchemaUpdaterStep> stepList) {
		// #3345
		String stepName;
		String tableName;
		String columnName;

		stepName = "Update uri to clob for DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		stepName = "Update uri to clob for TermVocabulary";
		tableName = "TermVocabulary";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		// are uri and termsourceuri needed -> see #3955
		stepName = "Update termsourceuri to clob for TermVocabulary";
		tableName = "TermVocabulary";
		columnName = "termsourceuri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		stepName = "Update uri to clob for Reference";
		tableName = "Reference";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		stepName = "Update uri to clob for Rights";
		tableName = "Rights";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		stepName = "Update uri to clob for MediaRepresentationPart";
		tableName = "MediaRepresentationPart";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		// still needed??
		stepName = "Update uri to clob for FeatureTree";
		tableName = "FeatureTree";
		columnName = "uri";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

		// Annotation.linkbackUri (change from URL to URI)
		stepName = "Update url to uri (->clob) for Annotation.linkbackUri";
		tableName = "Annotation";
		columnName = "linkbackUrl";
		ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
				columnName, INCLUDE_AUDIT);

	}

	/**
	 * @param stepList
	 * @return
	 */
	private void addTimeperiodToDescriptionElement(
			List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		String columnName;
		// start #3312
		stepName = "Create time period start column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_start";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// end #3312
		stepName = "Create time period end column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_end";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// freetext #3312
		stepName = "Create time period freetext column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_freetext";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		return;
	}

	private void updateElevationMax(List<ISchemaUpdaterStep> stepList) {
		// create column
		String stepName = "Create absoluteElevationMax column";
		String tableName = "GatheringEvent";
		String columnName = "absoluteElevationMax";
		ColumnAdder.NewIntegerInstance(stepList, stepName,
				tableName, columnName, INCLUDE_AUDIT, false, null);

		String audTableName = "GatheringEvent";
		// update max
		stepName = "Update gathering elevation max";
		// all audits to unknown type
		String query = " UPDATE @@GatheringEvent@@ "
				+ " SET absoluteElevationMax = absoluteElevation + absoluteElevationError,  "
				+ "     absoluteElevation =  absoluteElevation - absoluteElevationError"
				+ " WHERE absoluteElevationError is not null ";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(audTableName);
		// TODO same for AUD

		// remove error column
		stepName = "Remove elevationErrorRadius column";
		tableName = "GatheringEvent";
		columnName = "absoluteElevationError";
		ColumnRemover.NewInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT);

		// create column absoluteElevationText
		stepName = "Create absoluteElevationText column";
		tableName = "GatheringEvent";
		columnName = "absoluteElevationText";
		int size = 30;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				size, INCLUDE_AUDIT);

		// retype distanceToGround
		stepName = "Rname distanceToGround column";
		tableName = "GatheringEvent";
		String strOldColumnName = "distanceToGround";
		ColumnTypeChanger.NewInt2DoubleInstance(stepList, stepName, tableName,
				strOldColumnName, INCLUDE_AUDIT);

		// create column distanceToGroundMax
		stepName = "Create distanceToGroundMax column";
		tableName = "GatheringEvent";
		columnName = "distanceToGroundMax";
		ColumnAdder.NewDoubleInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false);

		// create column distanceToGroundText
		stepName = "Create distanceToGroundText column";
		tableName = "GatheringEvent";
		columnName = "distanceToGroundText";
		size = 30;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				size, INCLUDE_AUDIT);

		// retype distanceToGround
		stepName = "Rname distanceToWaterSurface column";
		tableName = "GatheringEvent";
		strOldColumnName = "distanceToWaterSurface";
		ColumnTypeChanger.NewInt2DoubleInstance(stepList, stepName, tableName,
				strOldColumnName, INCLUDE_AUDIT);

		// create column distanceToWaterSurface
		stepName = "Create distanceToWaterSurfaceMax column";
		tableName = "GatheringEvent";
		columnName = "distanceToWaterSurfaceMax";
		ColumnAdder.NewDoubleInstance(stepList, stepName, tableName, columnName,
				INCLUDE_AUDIT, false);

		// create column distanceToGroundText
		stepName = "Create distanceToWaterSurfaceText column";
		tableName = "GatheringEvent";
		columnName = "distanceToWaterSurfaceText";
		size = 30;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName,
				size, INCLUDE_AUDIT);

	}

	/**
	 * @param stepList
	 */
	private void updateOriginalSourceType(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String typeAttrName = "sourceType";
		String tableName = "OriginalSourceBase";

		// all data to unknown
		stepName = "Update original source type column: set all to unknown";
		String query = String.format("UPDATE @@OriginalSourceBase@@ "
				+ " SET %s = '%s' ", typeAttrName,
				OriginalSourceType.Unknown.getKey());
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// all IMPORTS recognized by idInSOurce and by missing nameInSource
		stepName = "Update original source type column: set to 'import' where possible";
		query = String
				.format("UPDATE @@OriginalSourceBase@@ "
						+ " SET %s = '%s' "
						+ " WHERE "
						+ "((idInSource IS NOT NULL) OR (idNamespace IS NOT NULL))  AND "
						+ "( nameUsedInSource_id IS NULL AND originalNameString IS NULL ) ",
						typeAttrName, OriginalSourceType.Import.getKey());
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);

		// all PRIMARY TAXONOMIC SOURCES recognized by missing idInSource and
		// namespace and by existing citation
		stepName = "Update original source type column: set to 'primary taxonomic source' where possible";
		query = String.format("UPDATE @@OriginalSourceBase@@ SET  %s = '%s' WHERE "
				+ "(idInSource IS NULL AND idNamespace IS NULL) AND "
				+ "( citation_id IS NOT NULL ) ", typeAttrName,
				OriginalSourceType.PrimaryTaxonomicSource.getKey());
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, 99)
				.setDefaultAuditing(tableName);
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_30_301.NewInstance();
	}

}
