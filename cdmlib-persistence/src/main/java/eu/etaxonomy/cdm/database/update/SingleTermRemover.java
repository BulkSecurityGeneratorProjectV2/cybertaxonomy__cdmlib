// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Removes a given term if it is not in use.
 * TODO does not yet check all DefinedTermBase_XXX tables except for representations.
 * Does also not handle AUD tables
 * 
 * @author a.mueller
 * @date 06.09.2013
 *
 */
public class SingleTermRemover extends SchemaUpdaterStepBase<SingleTermRemover> implements ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleTermRemover.class);
	
	public static final SingleTermRemover NewInstance(String stepName, String uuidTerm, List<String> checkUsedQueries){
		return new SingleTermRemover(stepName, uuidTerm, checkUsedQueries);	
	}
	
	/**
	 * @param firstCheckUsedQuery The first query to check if this term is used. Must return a single int value > 0 
	 * if this term is used at the given place.
	 * @return
	 */
	public static final SingleTermRemover NewInstance(String stepName, String uuidTerm, String firstCheckUsedQuery){
		List<String> checkUsedQueries = new ArrayList<String>();
		checkUsedQueries.add(firstCheckUsedQuery);
		return new SingleTermRemover(stepName, uuidTerm, checkUsedQueries);	
	}
	
	
	private String uuidTerm ;
	private List<String> checkUsedQueries = new ArrayList<String>();
	

	private SingleTermRemover(String stepName, String uuidTerm, List<String> checkUsedQueries) {
		super(stepName);
		this.uuidTerm = uuidTerm;
		this.checkUsedQueries = checkUsedQueries; 
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException{
 		//get term id
		String sql = " SELECT id FROM DefinedTermBase WHERE uuid = '%s'";
		Integer id = (Integer)datasource.getSingleValue(String.format(sql, this.uuidTerm));
		if (id == null || id == 0){
			return 0;
		}
		
		//check if in use
		if (! checkTermInUse(datasource, monitor, id)){
			return 0;
		}
		
		//if not ... remove
		removeTerm(datasource, monitor, id);
		
		return 0;
	}

	private void removeTerm(ICdmDataSource datasource, IProgressMonitor monitor, int id) throws SQLException {
		
		//get representation ids
		List<Integer> repIDs = new ArrayList<Integer>();
		getRepIds(datasource, id, repIDs, "representations_id", "DefinedTermBase_Representation");
		getRepIds(datasource, id, repIDs, "inverserepresentations_id", "RelationshipTermBase_inverseRepresentation");
		
		//remove MN table
		String sql = " DELETE FROM DefinedTermBase_Representation WHERE DefinedTermBase_id = " + id;
		datasource.executeUpdate(sql);
		sql = " DELETE FROM RelationshipTermBase_inverseRepresentation WHERE DefinedTermBase_id = " +id;
		datasource.executeUpdate(sql);
		
		//remove representations
		for (Integer repId : repIDs){
			sql = " DELETE FROM Representation WHERE id = " + repId;
			datasource.executeUpdate(sql);
		}
		
		//remove term
		sql = " DELETE FROM DefinedTermBase WHERE id = " + id;
		datasource.executeUpdate(sql);
	}

	private void getRepIds(ICdmDataSource datasource, int id,
			List<Integer> repIDs, String mnRepresentationIdAttr, String mnTableName) throws SQLException {
		String sql = " SELECT DISTINCT %s as repId FROM %s WHERE @mnTermIdName = %d";
		sql = String.format(sql, mnRepresentationIdAttr, mnTableName, id);
		ResultSet rs = datasource.executeQuery(sql);
		while (rs.next()){
			Integer repId = rs.getInt("repId");  //TODO nullSafe, but should not happen
			if (repId != null){  
				repIDs.add(repId);
			}
		}
	}

	private boolean checkTermInUse(ICdmDataSource datasource, IProgressMonitor monitor, int id) throws SQLException {
		for (String query : checkUsedQueries){
			query = String.format(query, id);
			Integer i = (Integer)datasource.getSingleValue(query);
			if (i != null && i>0){
				return true;
			}
		}
		return false;
	}

	public SingleTermRemover addCheckUsedQuery(String query){
		this.checkUsedQueries.add(query);
		return this;
	}
	


}
