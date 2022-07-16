/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Changes the object to be handled (usually the object to be mapped stays the same for all mappers of a mapping).
 * @author a.mueller
 * @since 12.05.2009
 */
public class ObjectChangeMapper
        extends CdmAttributeMapperBase
        implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {

    private static final Logger logger = LogManager.getLogger();

	private DbExportStateBase<?, IExportTransformer> state;  //for possible later use

	private Class<? extends CdmBase> oldClass;
	private Class<? extends CdmBase> newClass;
	private String cdmAttribute;

	private Method method;
//	private Class<?>[] parameterTypes;

	public static ObjectChangeMapper NewInstance(Class<? extends CdmBase>  oldClass, Class<? extends CdmBase> newClass, String cdmAttribute){
		String methodName = "get" + cdmAttribute;
		return new ObjectChangeMapper(oldClass, newClass, methodName, (Class<?>[])null);
	}

	protected ObjectChangeMapper(Class<? extends CdmBase>oldClazz,Class<? extends CdmBase>newClazz, String methodName, Class<?>... parameterTypes) {
		try {
//			this.parameterTypes = parameterTypes;
			oldClass = oldClazz;
			newClass = newClazz;
			method = oldClazz.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException", e);
		}
	}

	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase<?, IExportTransformer> state, String tableName) {
		this.state = state;
	}

	@Override
    public boolean invoke(CdmBase cdmBase) {
		throw new RuntimeException("Invoke must not be called for " + this.getClass().getSimpleName() + ".  Return type is still incompatible. Use getNewObject instead.");
	}

	public CdmBase getNewObject(CdmBase oldCdmBase){
		try {
			if (oldCdmBase.isInstanceOf(oldClass)){
				return  (CdmBase)method.invoke(oldCdmBase, (Object[])null);
			}else if (oldCdmBase.isInstanceOf(newClass)){
				return oldCdmBase;
			}else{
				logger.warn("ObjectChangeMapper "+this.toString()+"not applicable for CdmBase " + oldCdmBase.getClass());
				return oldCdmBase;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> getSourceAttributes() {
		Set<String> result = new HashSet<>();
		if (cdmAttribute != null){
			result.add(cdmAttribute);
		}
		return result;
	}

	@Override
	public Set<String> getDestinationAttributes() {
		return new HashSet<>();
	}

	@Override
	public List<String> getSourceAttributeList() {
		ArrayList<String> result = new ArrayList<>();
		if (cdmAttribute != null){
			result.add(cdmAttribute);
		}
		return result;
	}

	@Override
	public List<String> getDestinationAttributeList() {
		return new ArrayList<>();
	}
}
