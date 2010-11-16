// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v26_30;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MapTableCreator;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.v24_25.SchemaUpdater_24_25;


/**
 * @author a.mueller
 * @created Nov 08, 2010
 * @version 1.0
 */
public class SchemaUpdater_26_30 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_26_30.class);
	private static final String startSchemaVersion = "2.5.0.0.201009211255";
//	private static final String startSchemaVersion = "2.6.0.0.201010231255";
	private static final String endSchemaVersion = "3.0.0.0.201011090000";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_26_30 NewInstance(){
		return new SchemaUpdater_26_30();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_26_30() {
		super(startSchemaVersion, endSchemaVersion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getUpdaterList()
	 */
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		String stepName;
		
		//add feature tree attribute to feature node table
		stepName = "Add feature tree addtribue to feature node";
		//TODO defaultValue & not null
		ColumnAdder featureTreeColAdder = ColumnAdder.NewIntegerInstance(stepName, "featureNode", "featuretree_id", INCLUDE_AUDIT, false, "FeatureTree");
		stepList.add(featureTreeColAdder);
		
		//compute feature tree column
		stepName = "Update feature node tree column";
		FeatureNodeTreeColumnUpdater fntcu = FeatureNodeTreeColumnUpdater.NewInstance(stepName, INCLUDE_AUDIT);
		stepList.add(fntcu);
		
		//Key statement
		stepName = "Create KeyStatement tables";
		TableCreator tableCreator = TableCreator.NewInstance(stepName, "KeyStatement", new String[]{}, new String[]{}, new String[]{}, INCLUDE_AUDIT, INCLUDE_CDM_BASE);
		stepList.add(tableCreator);
		
		//KeyStatement_LanguageString
		stepName = "Create KeyStatement label";
		tableCreator = MapTableCreator.NewMapTableInstance(stepName,  "KeyStatement", null,  "LanguageString", "label", "DefinedTermBase", SchemaUpdaterBase.INCLUDE_AUDIT);
		stepList.add(tableCreator);

		
		//PolytomousKey
		stepName = "Create PolytomousKey tables";
		tableCreator = TableCreator.NewIdentifiableInstance(stepName, "PolytomousKey", new String[]{"root_id"}, new String[]{"int"}, new String[]{"PolytomousKeyNode"}, INCLUDE_AUDIT);
		stepList.add(tableCreator);
		
		//create table PolytomousKeyNode_PolytomousKeyNode_AUD (REV integer not null, parent_id integer not null, id integer not null, sortIndex integer not null, revtype tinyint, primary key (REV, parent_id, id, sortIndex)) ENGINE=MYISAM DEFAULT CHARSET=utf8
		tableCreator = TableCreator.NewInstance(stepName, "PolytomousKeyNode_PolytomousKeyNode_AUD", new String[]{"REV", "parent_id", "id", "sortIndex", "revtype"}, new String[]{"int","int","int","int","tinyint"}, new String[]{null, "PolytomousKeyNode", null, null, null},! INCLUDE_AUDIT, ! INCLUDE_CDM_BASE);
		tableCreator.setPrimaryKeyParams("REV, parent_id, id, sortIndex", null);
		tableCreator.setUniqueParams(null, null);
		stepList.add(tableCreator);
		
		//covered taxa
		stepName= "Add polytomous key covered taxa";
		tableCreator = MnTableCreator.NewMnInstance(stepName, "PolytomousKey", null, "TaxonBase", "coveredtaxa", SchemaUpdaterBase.INCLUDE_AUDIT, false, true);
		stepList.add(tableCreator);

		//Polytomous key node
		stepName = "Create PolytomousKeyNode tables";
		tableCreator = TableCreator.NewInstance(stepName, "PolytomousKeyNode", new String[]{"nodeNumber", "sortindex", "key_id", "othernode_id", "question_id", "statement_id", "feature_id", "subkey_id" , "taxon_id", "parent_id"}, new String[]{"int", "int", "int", "int", "int","int", "int", "int", "int", "int"}, new String[]{null, null, "PolytomousKey", "PolytomousKeyNode", "KeyStatement", "KeyStatement", "DefinedTermBase", "PolytomousKey" , "TaxonBase", "PolytomousKeyNode"}, INCLUDE_AUDIT, INCLUDE_CDM_BASE);
		stepList.add(tableCreator);

		//modifying text
		stepName = "Create PolytomousKeyNode modifying text";
		tableCreator = MapTableCreator.NewMapTableInstance(stepName,  "PolytomousKeyNode", null,  "LanguageString", "modifyingtext", "DefinedTermBase", SchemaUpdaterBase.INCLUDE_AUDIT);
		stepList.add(tableCreator);
		
		//rename named area featureTree_id
		stepName = "Rename polytomouskey_namedarea.featureTree_id -> polytomouskey_id";
		ColumnNameChanger colChanger = ColumnNameChanger.NewIntegerInstance(stepName, "PolytomousKey_NamedArea", "FeatureTree_id", "PolytomousKey_id", INCLUDE_AUDIT);
		stepList.add(colChanger);
		
		//rename polytomouskey_scope featureTree_id
		stepName = "Rename polytomouskey_scope.featureTree_id -> polytomouskey_id";
		colChanger = ColumnNameChanger.NewIntegerInstance(stepName, "PolytomousKey_Scope", "FeatureTree_id", "PolytomousKey_id", INCLUDE_AUDIT);
		stepList.add(colChanger);

		//move PolytomousKey data to new tables
		stepName = "Move polytomous key data from feature tree to polytomous key";
		PolytomousKeyDataMover dataMover = PolytomousKeyDataMover.NewInstance(stepName, INCLUDE_AUDIT);
		stepList.add(dataMover);
		
		//remove DTYPE from feature node
		stepName = "Remove feature tree DTYPE column";
		ColumnRemover colRemover = ColumnRemover.NewInstance(stepName, "FeatureTree", "DTYPE", INCLUDE_AUDIT);
		stepList.add(colRemover);
		
		//remove feature node taxon column
		stepName = "Remove feature node taxon column";
		colRemover = ColumnRemover.NewInstance(stepName, "FeatureNode", "taxon_id", INCLUDE_AUDIT);
		stepList.add(colRemover);

		//Remove featureNode_representation
		stepName = "Remove FeatureNode_representation MN";
		TableDroper tableDropper = TableDroper.NewInstance(stepName, "featurenode_representation", INCLUDE_AUDIT);
		stepList.add(tableDropper);
		
		
		//add exsiccatum
		stepName = "Add exsiccatum to specimen";
		ColumnAdder exsiccatumAdder = ColumnAdder.NewStringInstance(stepName, "SpecimenOrObservationBase", "exsiccatum", INCLUDE_AUDIT);
		stepList.add(exsiccatumAdder);
		
		//add primary collector
		stepName = "Add primary collector to field observation";
		ColumnAdder primaryCollectorAdder = ColumnAdder.NewIntegerInstance(stepName, "SpecimenOrObservationBase", "primaryCollector_id", INCLUDE_AUDIT, false, "AgentBase");
		stepList.add(primaryCollectorAdder);

		
		//taxonomic tree -> classification
		stepName = "Rename taxonomic tree to classification";
		TableNameChanger tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree", "Classification", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);
		
		//TaxonomicTree_Annotation -> classification_Annotation
		stepName = "Rename TaxonomicTree_Annotation to Classification_Annotation";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_Annotation", "Classification_Annotation", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);
		
		stepName = "Rename taxonomicTree_id column in Classification_Annotation";
		ColumnNameChanger columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_Annotation", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);
		
		
		//TaxonomicTree_Credit -> classification_Credit
		stepName = "Rename TaxonomicTree_Credit to Classification_Credit";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_Credit", "Classification_Credit", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_Credit";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_Credit", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);
		
		
		//TaxonomicTree_Extension -> classification_Extension
		stepName = "Rename TaxonomicTree_Extension to Classification_Extension";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_Extension", "Classification_Extension", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_Extension";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_Extension", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		
		//TaxonomicTree_Marker -> classification_Marker
		stepName = "Rename TaxonomicTree_Marker to Classification_Marker";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_Marker", "Classification_Marker", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_Marker";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_Marker", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		
		//TaxonomicTree_OriginalSourceBase -> classification_OriginalSourceBase
		stepName = "Rename TaxonomicTree_OriginalSourceBase to Classification_OriginalSourceBase";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_OriginalSourceBase", "Classification_OriginalSourceBase", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_OriginalSourceBase";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_OriginalSourceBase", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		
		//TaxonomicTree_Rights -> classification_Rights
		stepName = "Rename TaxonomicTree_Rights to Classification_Rights";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_Rights", "Classification_Rights", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_Rights";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_Rights", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		
		//TaxonomicTree_TaxonNode -> classification_TaxonNode
		stepName = "Rename TaxonomicTree_TaxonNode to Classification_TaxonNode";
		tableNameChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree_TaxonNode", "Classification_TaxonNode", INCLUDE_AUDIT);
		stepList.add(tableNameChanger);

		stepName = "Rename taxonomicTree_id column in Classification_TaxonNode";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "Classification_TaxonNode", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		//Rename taxonomictree column in TaxonNode
		stepName = "Rename taxonomicTree_id column in TaxonNode";
		columnNameChanger = ColumnNameChanger.NewIntegerInstance(stepName, "TaxonNode", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);
		stepList.add(columnNameChanger);

		
		
		//add the table hibernate_sequences
		stepName = "Add the table hibernate_sequences to store the table specific sequences in";
		SequenceTableCreator step = SequenceTableCreator.NewInstance(stepName);
		stepList.add(step);
		
		return stepList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getNextUpdater()
	 */
	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getPreviousUpdater()
	 */
	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_24_25.NewInstance();
	}

}
