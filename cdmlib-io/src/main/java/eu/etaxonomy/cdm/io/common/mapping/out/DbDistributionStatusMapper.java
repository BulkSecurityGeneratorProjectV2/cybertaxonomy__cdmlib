/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * Maps distribution to a database foreign key or cache field.
 * Requires according transformer implementation.
 * @author a.mueller
 * @since 06.02.2012
 */
public class DbDistributionStatusMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private boolean isCache = false;

	public static DbDistributionStatusMapper NewInstance(String dbAttributeString, boolean isCache){
		return new DbDistributionStatusMapper(dbAttributeString, isCache, null);
	}

	protected DbDistributionStatusMapper(String dbAttributeString, boolean isCache, Object defaultValue) {
		super("status", dbAttributeString, defaultValue);
		this.isCache = isCache;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(Distribution.class)){
			Distribution distribution = CdmBase.deproxy(cdmBase, Distribution.class);
			PresenceAbsenceTerm status = distribution.getStatus();
			IExportTransformer transformer = getState().getTransformer();
			try {
				if (isCache){
					return transformer.getCacheByPresenceAbsenceTerm(status);
				}else{
					return transformer.getKeyByPresenceAbsenceTerm(status);
				}
			} catch (UndefinedTransformerMethodException e) {
				throw new RuntimeException(e);
			}

		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type Distribution, but was " + cdmBase.getClass());
		}
	}

	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}

	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return Integer.class;
		}
	}
}
