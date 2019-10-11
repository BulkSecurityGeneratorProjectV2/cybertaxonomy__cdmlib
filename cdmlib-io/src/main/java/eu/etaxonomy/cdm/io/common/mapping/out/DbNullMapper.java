/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbNullMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbNullMapper.class);

    private int sqlType;

	public static DbNullMapper NewInstance(String dbIdAttributeString, int sqlType){
		return new DbNullMapper(dbIdAttributeString, sqlType);
	}
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbNullMapper(String dbAttributeString, int sqlType) {
		super(null, dbAttributeString, null);
		this.sqlType = sqlType;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		return null;
	}

	@Override
	protected int getSqlType() {
		return this.sqlType;
	}
}
