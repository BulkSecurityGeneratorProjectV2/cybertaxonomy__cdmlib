// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v31_33;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v30_31.SchemaUpdater_30_301;


/**
 * NOT YET USED
 * @author a.mueller
 * @created Oct 11, 2011
 */
public class SchemaUpdater_31_33 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_31_33.class);
	private static final String startSchemaVersion = "3.0.1.0.201104190000";
	private static final String endSchemaVersion = "3.3.0.0.201306010000";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_31_33 NewInstance(){
		return new SchemaUpdater_31_33();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_31_33() {
		super(startSchemaVersion, endSchemaVersion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getUpdaterList()
	 */
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		
		//TODO still needed? Does it throw exception if table does not exist?
		//drop TypeDesignationBase_TaxonNameBase   //from schemaUpdater 301_31
		String stepName = "Drop duplicate TypeDesignation-TaxonName table";
		String tableName = "TypeDesignationBase_TaxonNameBase";
		ISchemaUpdaterStep step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create original source type column
		stepName = "Create original source type column";
		tableName = "OriginalSourceBase";
		String columnName = "type";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, true, null);
		stepList.add(step);
		
		//TODO ?? update original source type
		updateOriginalSourceType(stepList);
		
		//create and update elevenation max, remove error column
		updateElevationMax(stepList);
		
		//create taxon node tree index
		stepName = "Create taxon node tree index";
		tableName = "TaxonNode";
		columnName = "treeIndex";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO update tree index
		
		//create original source type column
		stepName = "Create taxon node sort index column";
		tableName = "TaxonNode";
		columnName = "sortIndex";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, null);
		stepList.add(step);
		
		//TODO implement sorted behaviour in model first !!
		//TODO update sortindex (similar updater exists already for FeatureNode#sortIndex in schema update 25_30 
		
		//create feature node tree index
		stepName = "Create feature node tree index";
		tableName = "FeatureNode";
		columnName = "treeIndex";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
				
		//TODO update tree index for feature node
		
		//update introduced: adventitious (casual) label
		//#3540
		stepName = "Update introduced: adventitious (casual) label";
		String query = " UPDATE representation r " + 
				" SET r.abbreviatedlabel = ia " +
				" WHERE r.abbreviatedlabel = 'id' AND r.label = 'introduced: adventitious (casual)' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//TODO update idInVocabulary for DefinedTerms
		
		//TODO update termType for DefinedTerms and TermVocabulary, no type must be null
		
		//TODO update DTYPE for modifiers (Stage, Scope, Sex, DeterminationModifier, Modifier -> DefinedTerm)
		
		
		//TODO update rankClass
		
		//TODO change column type for DistanceToWaterSurface und DistanceToGround
		
		//TODO add column for DistanceToWaterSurfaceMax/Text und DistanceToGroundMax/Text
		
		//TODO update datatype of sequence.sequence (keeping data not necessary #3325)

		
		
		return stepList;
	}

	private void updateElevationMax(List<ISchemaUpdaterStep> stepList) {
		//create column
		String stepName = "Create absoluteElevationMax column";
		String tableName = "GatheringEvent";
		String columnName = "absoluteElevationMax";
		ISchemaUpdaterStep step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, null);
		stepList.add(step);
		
		
		//update max
		stepName = "Update gathering elevation max";
		//all audits to unknown type
		String query = " UPDATE GatheringEvent ge " + 
				" SET ge.absoluteElevationMax = ge.elevation + ge.elevationErrorRadius,  " +
				"     ge.absoluteElevation =  ge.elevationErrorRadius - ge.elevationErrorRadius" +
				" WHERE ge.elevationErrorRadius is not null ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		//TODO same for AUD
		
		//remove error column
		stepName = "Remove elevationErrorRadius column";
		tableName = "GatheringEvent";
		columnName = "elevationErrorRadius";
		step = ColumnRemover.NewInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		
	}

	/**
	 * @param stepList
	 */
	private void updateOriginalSourceType(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		ISchemaUpdaterStep step;
		stepName = "Create original source type column";
		//all audits to unknown type
		String query = "UPDATE OriginalSourceBase_AUD SET type = 0 ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all data to unknown
		query = "UPDATE OriginalSourceBase SET type = 0 ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all imports recognized by idInSOurce and by missing nameInSource
		query = "UPDATE OriginalSourceBase SET type = 3 WHERE " +
				"((idInSource IS NOT NULL) OR (idNamespace IS NOT NULL))  AND " +
				"( nameUsedInSource IS NULL AND originalNameString IS NULL ) ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		 //all imports recognized by idInSOurce and by missing nameInSource
		query = "UPDATE OriginalSourceBase SET type = 1 WHERE " +
				"(idInSource IS NULL AND idNamespace IS NULL) AND " +
				"( citation IS NOT NULL ) ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
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
		return SchemaUpdater_30_301.NewInstance();
	}

}
