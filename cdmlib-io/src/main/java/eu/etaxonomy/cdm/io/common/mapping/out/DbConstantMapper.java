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
 * Maps to a constant value.
 *
 * TODO deduplicate with {@link DbFixedIntegerMapper} and {@link DbFixedStringMapper}.
 *
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbConstantMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbConstantMapper.class);

	private int sqlType;
	private Object value;

	public static DbConstantMapper NewInstance(String dbIdAttributeString, int sqlType, Object value){
		return new DbConstantMapper(dbIdAttributeString, sqlType, value);
	}

	protected DbConstantMapper(String dbAttributeString, int sqlType, Object value) {
		super(null, dbAttributeString, null);
		this.sqlType = sqlType;
		this.value = value;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		return value;
	}

	@Override
	protected int getSqlType() {
		return this.sqlType;
	}
}
