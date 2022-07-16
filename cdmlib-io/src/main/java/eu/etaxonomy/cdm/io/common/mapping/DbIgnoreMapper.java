/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This mapper does not change any import object, but keeps the information that a database
 * attribute does not need to be mapped.
 *
 * @author a.mueller
 * @since 25.02.2010
 */
public class DbIgnoreMapper
        extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase> {

    private static final Logger logger = LogManager.getLogger();

//*************************** FACTORY ***************************************************************//

	public static DbIgnoreMapper NewInstance(String dbAttributeToIgnore){
		return new DbIgnoreMapper(dbAttributeToIgnore, null, null, null);
	}

	public static DbIgnoreMapper NewInstance(String dbAttributeToIgnore, String ignoreReason){
		return new DbIgnoreMapper(dbAttributeToIgnore, null, null, ignoreReason);
	}

//*************************** VARIABLES ***************************************************************//

	private String ignoreReason;

//*************************** CONSTRUCTOR ***************************************************************//

	protected DbIgnoreMapper(String dbAttributString, String cdmAttributeString, Object defaultValue, String ignoreReason) {
		super(dbAttributString, cdmAttributeString, defaultValue);
		this.ignoreReason = ignoreReason;
	}

	@Override
	public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		return cdmBase; //do nothing
	}

	@Override
	public Class getTypeClass() {
		return null;  //not needed
	}

	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		String localIgnoreReason = "";
		if (StringUtils.isNotBlank(ignoreReason)){
			localIgnoreReason = "(" + ignoreReason +")";
	}
		logger.warn(this.getSourceAttribute() +  " ignored" +  localIgnoreReason);
	}
}