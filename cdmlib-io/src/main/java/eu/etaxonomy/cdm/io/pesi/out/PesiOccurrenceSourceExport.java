// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 15.03.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiOccurrenceSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiOccurrenceSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = AnnotatableEntity.class;

	private static int modCount = 1000;
	private static final String dbTableName = "OccurrenceSource";
	private static final String pluralString = "OccurrenceSources";
	private static final String parentPluralString = "Taxa";
	private static Taxon taxon = null;
	private static ArrayList processedList = new ArrayList();

	public PesiOccurrenceSourceExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(PesiExportState state) {
		boolean result = true;
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doInvoke(PesiExportState state) {
		try {
			logger.error("*** Started Making " + pluralString + " ...");
	
			String occurrenceSql = "Insert into OccurrenceSource (OccurrenceFk, SourceFk, SourceNameCache, OldTaxonName) " +
			"values (?, ?, ?, ?)";
			Connection con = state.getConfig().getDestination().getConnection();
			PreparedStatement stmt = con.prepareStatement(occurrenceSql);

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table OccurrenceSource.
			doDelete(state);
	
			// Get specific mappings: (CDM) ? -> (PESI) OccurrenceSource
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the OccurrenceSource
			int count = 0;
			int taxonCount = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonBase> list = null;
			
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			while ((list = getTaxonService().list(null, limit, taxonCount, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + parentPluralString + ".");
				taxonCount += list.size();
				for (TaxonBase taxonBase : list) {
					if (taxonBase.isInstanceOf(Taxon.class)) {

						// Set the current Taxon
						taxon = CdmBase.deproxy(taxonBase, Taxon.class);

						// Determine the TaxonDescriptions
						Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions();

						// Determine the DescriptionElements (Citations) for the current Taxon
						for (TaxonDescription taxonDescription : taxonDescriptions) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								Set<DescriptionElementSource> elementSources = descriptionElement.getSources();
								
								// Focus on descriptionElements with sources.
								if (elementSources.size() > 0) {
									for (DescriptionElementSource elementSource : elementSources) {
										ReferenceBase reference = elementSource.getCitation();

										// Citations can be empty (null): Is it wrong data or just a normal case?
										if (reference != null) {
											doCount(count++, modCount, pluralString);

											// Lookup sourceFk by using getSourceFk()
											Integer sourceFk = getSourceFk(reference, state);
											
											if (sourceFk != null && ! allreadyProcessed(sourceFk)) {
												// Add to processed sourceFk's since sourceFk's can be scanned more than once.
												addToProcessed(sourceFk);
												
												// Query the database for all entries in table 'Occurrence' with the sourceFk just determined.
												Set<Integer> occurrenceIds = getOccurrenceIds(sourceFk, state);
	
												// Insert as many entries in database table 'OccurrenceSource' as occurrenceId's were determined.
												insertColumns(stmt, reference, sourceFk, occurrenceIds);
											}

										}
									}
								}
								
							}
						}
					}
				}
				
				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			}
			if (list.size() == 0) {
				logger.error("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");
	
			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Inserts columns in the database table OccurrenceSource.
	 * @param stmt
	 * @param reference
	 * @param sourceFk
	 * @param occurrenceIds
	 */
	private void insertColumns(PreparedStatement stmt, ReferenceBase reference,
			Integer sourceFk, Set<Integer> occurrenceIds) {
		for (Integer occurrenceId : occurrenceIds) {
			try {
				stmt.setInt(1, occurrenceId);
				stmt.setInt(2, sourceFk);
				stmt.setString(3, getSourceNameCache(reference));
				stmt.setString(4, null); // TODO
				stmt.execute();
			} catch (SQLException e) {
				logger.error("SQLException during getOccurrenceId invoke.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns whether the given sourceFk was processed before or not.
	 * @param sourceFk
	 * @return
	 */
	private static boolean allreadyProcessed(Integer sourceFk) {
		if (processedList.contains(sourceFk)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Add given sourceFk to the list of processed sourceFk's.
	 * @param sourceFk
	 */
	private static void addToProcessed(Integer sourceFk) {
		processedList.add(sourceFk);
	}
	
	/**
	 * Returns a Set of OccurrenceId's associated to a given SourceFk.
	 * @param state
	 * @return
	 */
	private static Set<Integer> getOccurrenceIds(Integer sourceFk, PesiExportState state) {
		String occurrenceSql = "Select OccurrenceId From Occurrence where SourceFk = ?";
		Connection con = state.getConfig().getDestination().getConnection();
		PreparedStatement stmt = null;
		Set<Integer> occurrenceIds = new HashSet();
		
		try {
			stmt = con.prepareStatement(occurrenceSql);
			stmt.setInt(1, sourceFk);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				occurrenceIds.add(resultSet.getInt(1));
			}
		} catch (SQLException e) {
			logger.error("SQLException during getOccurrenceId invoke. (2)");
			e.printStackTrace();
		}

		return occurrenceIds;
	}

	/**
	 * Deletes all entries of database tables related to <code>OccurrenceSource</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear OccurrenceSource
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		// TODO
//		return ! state.getConfig().isDoOccurrenceSource();
		return false;
	}

	/**
	 * Returns the <code>OccurrenceFk</code> attribute.
	 * @param description The {@link DescriptionElementBase DescriptionElement}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>OccurrenceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getOccurrenceFk(AnnotatableEntity entity, PesiExportState state) {
		Integer result = null;
		return result;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param entity
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceFk(AnnotatableEntity entity, PesiExportState state) {
		Integer result = null;
		if (state != null && entity != null && entity.isInstanceOf(ReferenceBase.class)) {
			ReferenceBase reference = CdmBase.deproxy(entity, ReferenceBase.class);
			result = state.getDbId(reference);
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceCache</code> attribute.
	 * @param entity
	 * @return The <code>SourceCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(AnnotatableEntity entity) {
		String result = null;
		if (entity != null && entity.isInstanceOf(ReferenceBase.class)) {
			ReferenceBase reference = CdmBase.deproxy(entity, ReferenceBase.class);
			result = reference.getTitle();
		}
		return result;
	}
	
	/**
	 * Returns the <code>OldTaxonName</code> attribute.
	 * @param description The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>OldTaxonName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOldTaxonName(AnnotatableEntity entity) {
		// TODO: What does "old" mean in this context?
		return null;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);

		mapping.addMapper(MethodMapper.NewInstance("OccurrenceFk", this.getClass(), "getOccurrenceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("OldTaxonName", this));

		return mapping;
	}

}
