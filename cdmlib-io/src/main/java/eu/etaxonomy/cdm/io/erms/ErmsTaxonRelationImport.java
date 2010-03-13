// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportNameTypeDesignationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportSynonymMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTaxIncludedInMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformed;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 09.03.2010
 * @version 1.0
 */
@Component
public class ErmsTaxonRelationImport extends ErmsImportBase<TaxonBase> implements ICheckIgnoreMapper, IDbImportTransformed{
	private static final Logger logger = Logger.getLogger(ErmsTaxonRelationImport.class);
	
private DbImportMapping mapping;
	
	private int modCount = 10000;
	private static final String pluralString = "taxon relations";
	private static final String dbTableName = "tu";

	private Class cdmTargetClass = TaxonBase.class;

	public ErmsTaxonRelationImport(){
		super(pluralString, dbTableName);
	}

	/**
	 * @return
	 */
	private DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportTaxIncludedInMapper.NewInstance("id", "tu_parent", ErmsTaxonImport.TAXON_NAMESPACE, null)); //there is only one tree
			mapping.addMapper(DbImportSynonymMapper.NewInstance("id", "tu_acctaxon", ErmsTaxonImport.TAXON_NAMESPACE, null)); 			
			mapping.addMapper(DbImportNameTypeDesignationMapper.NewInstance("id", "tu_typetaxon", ErmsTaxonImport.NAME_NAMESPACE, "tu_typedesignationstatus"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_acctaxon"));
		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getRecordQuery(eu.etaxonomy.cdm.io.erms.ErmsImportConfigurator)
	 */
	protected String getRecordQuery(ErmsImportConfigurator config) {
		//TODO get automatic by second path mappers
		String selectAttributes = "id, tu_parent, tu_typetaxon, tu_typetaxon, tu_typedesignation, tu_acctaxon, tu_status"; 
		String strRecordQuery = 
			" SELECT  " + selectAttributes + 
			" FROM tu " +
			" WHERE ( tu.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.common.ResultSetPartitioner, eu.etaxonomy.cdm.io.common.IPartitionedState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		boolean success = true ;
		Set taxaToSave = new HashSet<TaxonBase>();
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs, taxaToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getTaxonService().save(taxaToSave);
		return success;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> nameIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "tu_parent");
				handleForeignKey(rs, nameIdSet, "tu_typetaxon");
				handleForeignKey(rs, taxonIdSet, "tu_acctaxon");					
			}
			
			//name map
			nameSpace = ErmsTaxonImport.NAME_NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);
			
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ICheckIgnoreMapper#checkIgnoreMapper(eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper, java.sql.ResultSet)
	 */
	public boolean checkIgnoreMapper(IDbImportMapper mapper, ResultSet rs) throws SQLException{
		boolean result = false;
		if (mapper instanceof DbImportTaxIncludedInMapper){
			int tu_status = rs.getInt("tu_status");
			if (tu_status != 1){
				result = true;
			}
		}else if (mapper instanceof DbImportSynonymMapper){
			int tu_status = rs.getInt("tu_status");
			if (tu_status == 1){
				result = true;
			}
		}else if (mapper instanceof DbImportNameTypeDesignationMapper){
			Object tu_typeTaxon = rs.getObject("tu_typetaxon");
			if (tu_typeTaxon == null){
				return true;
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public TaxonBase createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		// not needed
		return null;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.ITransformed#getTransformer()
	 */
	public IDbImportTransformer getTransformer() {
		//TODO class variable
		return new ErmsTransformer();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
//		IOValidator<ErmsImportState> validator = new ErmsTaxonImportValidator();
//		return validator.validate(state);
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}



}
