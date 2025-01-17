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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public abstract class MultipleAttributeMapperBase<SINGLE_MAPPER extends CdmSingleAttributeMapperBase> extends CdmAttributeMapperBase {
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MultipleAttributeMapperBase.class);


//******************************* ATTRIBUTES ***************************************/

	protected List<SINGLE_MAPPER> singleMappers = new ArrayList<>();

//********************************* CONSTRUCTOR ****************************************/

	public MultipleAttributeMapperBase() {
		singleMappers = new ArrayList<SINGLE_MAPPER>();
	}

//************************************ METHODS *******************************************/

	@Override
	public List<String> getDestinationAttributeList() {
		List<String> result = new ArrayList<>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.addAll(singleMapper.getDestinationAttributeList());
		}
		return result;
	}

	@Override
	public Set<String> getDestinationAttributes() {
		Set<String> result = new HashSet<>();
		result.addAll(getDestinationAttributeList());
		return result;
	}

	@Override
	public List<String> getSourceAttributeList() {
		List<String> result = new ArrayList<>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.addAll(singleMapper.getSourceAttributeList());
		}
		return result;
	}

	@Override
	public Set<String> getSourceAttributes() {
		Set<String> result = new HashSet<>();
		result.addAll(getSourceAttributeList());
		return result;
	}

    /**
	 * Returns the value of a result set attribute in its String representation.
	 * Better move this to a subclass for DbImportMappers (does not exist yet)
	 */
	protected String getStringDbValue(ResultSet rs, String attribute) throws SQLException {
		if (StringUtils.isBlank(attribute)){
			return null;
		}
		Object oId = rs.getObject(attribute);
		if (oId == null){
			return null;
		}
		String id = String.valueOf(oId);
		return id;
	}
}
