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
import java.util.Set;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportMapping<STATE extends DbImportStateBase, CONFIG extends IImportConfigurator>
            extends CdmIoMapping {

    private static final Logger logger = LogManager.getLogger(DbImportMapping.class);

	private boolean isInitialized = false;
	private Class<? extends CdmBase> destinationClass;
	private DbImportMapping<STATE, CONFIG> secondPathMapping;
	private boolean blankToNull = false;

	public boolean initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass){
		if (!isInitialized){
			//	this.dbTableName = tableName;
			this.destinationClass = destinationClass;
			for (CdmMapperBase mapper: this.mapperList){
				if (mapper instanceof IDbImportMapper){
					((IDbImportMapper<DbImportStateBase<?,?>,? extends CdmBase>) mapper ).initialize(state, destinationClass);
				}else{
					logger.warn("Mapper type " + mapper.getClass().getSimpleName() + " not yet implemented for DB import mapping");
				}
			}
			isInitialized = true;
			if (secondPathMapping != null){
				secondPathMapping.initialize(state, destinationClass);
			}
		}
		return true;
	}

	@Override
    public void addMapper(CdmAttributeMapperBase mapper){
		super.addMapper(mapper);
		if (mapper instanceof DbStringMapper){
			((DbStringMapper)mapper).setBlankToNull(isBlankToNull());
		}
	}

	/**
	 * Invokes the second path mapping if one has been defined
	 * @param rs
	 * @param objectsToSave
	 * @return
	 * @throws SQLException
	 */
	public boolean invoke(ResultSet rs, Set<CdmBase> objectsToSave) throws SQLException{
		return invoke(rs, objectsToSave, false);
	}

	/**
	 * Invokes the mapping. If secondPath is true, the secondPath mapping is invoked if it exists.
	 */
	public boolean invoke(ResultSet rs, Set<CdmBase> objectsToSave, boolean secondPath) throws SQLException{
		boolean result = true;
		if (secondPath == true && secondPathMapping != null){
			return secondPathMapping.invoke(rs, objectsToSave);
		} else {
			CdmBase objectToSave = null;
			//		try {
			for (CdmMapperBase mapper : this.mapperList){
				if (mapper instanceof IDbImportMapper){
					@SuppressWarnings("unchecked")
                    IDbImportMapper<DbImportStateBase<?,?>,CdmBase> dbMapper = (IDbImportMapper<DbImportStateBase<?,?>,CdmBase>)mapper;
					try {
						objectToSave = dbMapper.invoke(rs, objectToSave);
					} catch (Exception e) {
						result = false;
						logger.error("Error occurred in mapping.invoke of mapper " + dbMapper.toString());
						e.printStackTrace();
						continue;
					}
				}else{
					logger.warn("mapper is not of type " + IDbImportMapper.class.getSimpleName());
				}
			}
			if (objectToSave != null){
				objectsToSave.add(objectToSave);
			}else{
				logger.warn("The objectToSave was (null). Please check that your mappers work correctly.");
			}
			return result;
		}
	}

	public void setSecondPathMapping(DbImportMapping<STATE, CONFIG> secondPathMapping){
		this.secondPathMapping = secondPathMapping;
	}

	/**
	 * If <code>true</code> all {@link DbStringMapper} map blank strings to <code>null</code>
	 * @return
	 */
	public boolean isBlankToNull() {
		return blankToNull;
	}

	/**
	 * @see #isBlankToNull()
	 * @param blankToNull
	 */
	public void setBlankToNull(boolean blankToNull) {
		this.blankToNull = blankToNull;
	}
}
