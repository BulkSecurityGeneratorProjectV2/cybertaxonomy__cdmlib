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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbOriginalNameMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private boolean isCache = false;

	public static DbOriginalNameMapper NewInstance(String dbAttributeString, boolean isCache, Object defaultValue){
		return new DbOriginalNameMapper(dbAttributeString, isCache, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbOriginalNameMapper(String dbAttributeString, boolean isCache, Object defaultValue) {
		super("originalName", dbAttributeString, defaultValue);
		this.isCache = isCache;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(OriginalSourceBase.class)){
			OriginalSourceBase source = CdmBase.deproxy(cdmBase, OriginalSourceBase.class);
			String nameString = source.getOriginalNameString();
			TaxonName name = null;
			if (source.isInstanceOf(NamedSourceBase.class)){
			    NamedSourceBase descSource = CdmBase.deproxy(source, NamedSourceBase.class);
				name = descSource.getNameUsedInSource();
			}

			if (name != null){
				if (isCache){
					return name.getTitleCache();
				}else{
					return getState().getDbId(name);
				}
			}else{
				if (isCache){
					return nameString;
				}else{
					return null;
				}
			}

		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type OriginalSourceBase, but was " + cdmBase.getClass());
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return  Integer.class;
		}
	}
}
