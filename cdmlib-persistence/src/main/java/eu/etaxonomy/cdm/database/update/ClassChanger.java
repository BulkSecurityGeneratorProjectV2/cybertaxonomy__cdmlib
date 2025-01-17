/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.List;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 *
 */
public class ClassChanger
       extends AuditedSchemaUpdaterStepBase{

    private static final Logger logger = LogManager.getLogger(ClassChanger.class);

	private final String newClassName;
	private final String[] oldClassNames;
	private final boolean isIdentifiable;
	private final boolean isAnnotatable;
	private final boolean isSourcable;

	public static final ClassChanger NewIdentifiableInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String newClassNamePath, String[] oldClassNames, boolean includeAudTable){
		return new ClassChanger(stepList, stepName, tableName, newClassNamePath, oldClassNames, includeAudTable, true, true, true);
	}
	public static final ClassChanger NewAnnotatableInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String newClassNamePath, String[] oldClassNames, boolean includeAudTable){
		return new ClassChanger(stepList, stepName, tableName, newClassNamePath, oldClassNames, includeAudTable, true, false, false);
	}
	public static final ClassChanger NewDescriptionElementInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String newClassNamePath, String[] oldClassNames, boolean includeAudTable){
		return new ClassChanger(stepList, stepName, tableName, newClassNamePath, oldClassNames, includeAudTable, true, true, false);
	}


	protected ClassChanger(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String newClassName, String[] oldClassNames, boolean includeAudTable, boolean isAnnotatable, boolean isSourcable, boolean isIdentifiable) {
		super(stepList, stepName, tableName, includeAudTable);
		this.newClassName = newClassName;
		this.oldClassNames = oldClassNames;
		this.isIdentifiable = isIdentifiable;
		this.isAnnotatable = isAnnotatable;
		this.isSourcable = isSourcable;
	}

	@Override
	protected void invokeOnTable(String tableName, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
		try {
			if (true){
				String updateQuery = getDtypeUpdateQueryString(tableName, datasource, monitor);
				datasource.executeUpdate(updateQuery);
			}

//			if (isAnnotatable){
//				updateAnnotatables(tableName, datasource, monitor, caseType);
//			}
//			if (isSourcable){
//				updateSourcable(tableName, datasource, monitor, caseType);
//			}
//
//			if (isIdentifiable){
//				updateIdentifiables(tableName, datasource, monitor, caseType);
//			}

			return;
		} catch ( Exception e) {
		    String message = "Unhandled exception " + e.getMessage() + " in invokeOnTable";
			monitor.warning(message, e);
			logger.error(e);
			result.addException(e, message, "ClassChanger.invokeOnTable");
			return;
		}
	}

// not required anymore since #5743
//	private void updateSourcable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
//		updateSingleExtension("OriginalSourceBase", "sourcedObj_type" , datasource, monitor, caseType);
//	}
//	private void updateIdentifiables(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
//		updateSingleExtension("Extension", "extendedObj_type" , datasource, monitor, caseType);
//	}
//	private void updateAnnotatables(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
//		updateSingleExtension("Marker", "markedObj_type" , datasource, monitor, caseType);
//		updateSingleExtension("Annotation", "annotatedObj_type" , datasource, monitor, caseType);
//	}
//	private void updateSingleExtension(String extensionClass, String typeAttr, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException{
//		String sql = " UPDATE %s " +
//				" SET %s = '%s' " +
//				" WHERE %s = '%s'";
//
//		for (String oldClassPath : oldClassNames){
//			String query = String.format(sql, caseType.transformTo(extensionClass),
//					typeAttr, newClassName,
//					typeAttr, oldClassPath);
//			datasource.executeUpdate(query);
//		}
//	}


	public String getDtypeUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		updateQuery = " UPDATE @tableName " +
				" SET DTYPE = '@newTableName' " +
				" WHERE (1=0 @dtypes)";

		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@newTableName", getSimpleName(newClassName));
		String dtypes = "";
		for (String oldClassName : oldClassNames){
			dtypes += String.format(" OR DTYPE = '%s' ", getSimpleName(oldClassName)) ;
		}
		updateQuery = updateQuery.replace("@dtypes", dtypes);

		return updateQuery;
	}
	private String getSimpleName(String className) {
		String result = className;
		while (result.contains(".")){
			result = result.replaceAll(".*\\.", "");
		}
		return result;
	}

}
