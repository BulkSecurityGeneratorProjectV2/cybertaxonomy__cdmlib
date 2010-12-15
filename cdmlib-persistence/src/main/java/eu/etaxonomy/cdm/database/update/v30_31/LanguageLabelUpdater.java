// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_31;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 15.12.2010
 */
public class LanguageLabelUpdater extends SchemaUpdaterStepBase implements ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LanguageLabelUpdater.class);

	private static final String stepName = "Update language labels by full language name";
	
// **************************** STATIC METHODS ********************************/

	public static final LanguageLabelUpdater NewInstance(){
		return new LanguageLabelUpdater(stepName);	
	}

	protected LanguageLabelUpdater(String stepName) {
		super(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		
		//update representation label
		String sql;
		sql = " UPDATE Representation " + 
			" SET label = text " +
			" WHERE id IN ( SELECT MN.representations_id " +
				" FROM DefinedTermBase lang " +
				" INNER JOIN DefinedTermBase_Representation MN ON lang.id = MN.DefinedTermBase_id " +
				" WHERE lang.DTYPE = 'Language' " +
				" )";
		datasource.executeUpdate(sql);
		
		//update term titleCache
		//FIXME only for English representations
		sql = " UPDATE DefinedTermBase dtb " + 
			  " SET titleCache = " +  
					" ( " +
					" SELECT rep.label  " +
					" FROM DefinedTermBase_Representation MN " + 
					" INNER JOIN Representation rep ON MN.representations_id = rep.id " +
					" WHERE dtb.id = MN.DefinedTermBase_id) " + 
				" WHERE dtb.DTYPE = 'Language'";
		datasource.executeUpdate(sql);
		
		return 0;
	}

	
}
