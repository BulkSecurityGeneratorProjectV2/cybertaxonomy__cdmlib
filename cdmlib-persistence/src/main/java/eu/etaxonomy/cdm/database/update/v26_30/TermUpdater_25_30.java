// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v26_30;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v24_25.TermUpdater_24_25;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.occurrence.DeterminationModifier;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_25_30 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_25_30.class);
	
	public static final String startTermVersion = "2.5.0.0.201009211255";
	private static final String endTermVersion = "3.0.0.0.201011170000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_25_30 NewInstance(){
		return new TermUpdater_25_30(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_25_30(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<SingleTermUpdater> getUpdaterList() {
		List<SingleTermUpdater> list = new ArrayList<SingleTermUpdater>();

		// cf.
		UUID uuidTerm = DeterminationModifier.uuidConfer;
		String description = "Confer";
		String label = "confer";
		String abbrev = "cf.";
		String dtype = DeterminationModifier.class.getSimpleName();
		UUID uuidVocabulary = UUID.fromString("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77");
		UUID uuidAfterTerm = null ; //UUID.fromString("");
		list.add( SingleTermUpdater.NewInstance("Add 'confer (cf.)' determination modifier", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidLatin, true, uuidAfterTerm));

		
		// aff.
		uuidTerm = DeterminationModifier.uuidAffinis;
		description = "Affinis";
		label = "affinis";
		abbrev = "aff.";
		dtype = DeterminationModifier.class.getSimpleName();
		uuidVocabulary = UUID.fromString("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77");
		uuidAfterTerm = DeterminationModifier.uuidConfer;
		list.add( SingleTermUpdater.NewInstance("Add 'affinis (aff.)' determination modifier", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidLatin, true, uuidAfterTerm));
//		
//		//Habitat
//		uuidTerm = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
//		description = "Habitat";
//		label = "Habitat";
//		abbrev = "Habitat";
//		dtype = Feature.class.getSimpleName();
//		uuidVocabulary = uuidFeatureVocabulary;
//		uuidAfterTerm = null;
//		list.add( SingleTermUpdater.NewInstance("Add habitat feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));
//
//		//Habitat & Ecology
//		uuidTerm = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
//		description = "Habitat & Ecology";
//		label = "Habitat & Ecology";
//		abbrev = "Hab. & Ecol.";
//		dtype = Feature.class.getSimpleName();
//		uuidVocabulary = uuidFeatureVocabulary;
//		uuidAfterTerm = null;
//		list.add( SingleTermUpdater.NewInstance("Add habitat & ecology feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));
//
//		//Chromosome Numbers
//		uuidTerm = UUID.fromString("6f677e98-d8d5-4bc5-80bf-affdb7e3945a");
//		description = "Chromosome Numbers";
//		label = "Chromosome Numbers";
//		abbrev = "Chromosome Numbers";
//		dtype = Feature.class.getSimpleName();
//		uuidVocabulary = uuidFeatureVocabulary;
//		uuidAfterTerm = null;
//		list.add( SingleTermUpdater.NewInstance("Add chromosome number feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));
//		
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_24_25.NewInstance();
	}

}
