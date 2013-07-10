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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbUriMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * The export class for {@link eu.etaxonomy.cdm.model.reference.Reference References}.<p>
 * Inserts into DataWarehouse database table <code>Source</code>.
 * @author e.-m.lee
 * @date 11.02.2010
 *
 */
@Component
public class PesiSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = Reference.class;

	private static int modCount = 1000;
	public static final String dbTableName = "Source";
	private static final String pluralString = "Sources";
	List<Integer> storedSourceIds = new ArrayList<Integer>();

	public PesiSourceExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.pesi.out.PesiExportBase#getStandardMethodParameter()
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

	/**
	 * Checks whether a sourceId was stored already.
	 * @param sourceId
	 * @return
	 */
	protected boolean isStoredSourceId(Integer sourceId) {
		if (storedSourceIds.contains(sourceId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a sourceId to the list of storedSourceIds.
	 * @param sourceId
	 */
	protected void addToStoredSourceIds(Integer sourceId) {
		if (! storedSourceIds.contains(sourceId)) {
			this.storedSourceIds.add(sourceId);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(PesiExportState state) {
		try{
			logger.info("*** Started Making " + pluralString + " ...");

			PesiExportConfigurator pesiExportConfigurator = state.getConfig();
			
			// Get the limit for objects to save within a single transaction.
			int limit = pesiExportConfigurator.getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true ;

			// PESI: Clear the database table Source.
			//doDelete(state);  -> done by stored procedure
			
			// Get specific mappings: (CDM) Reference -> (PESI) Source
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// Create the Sources
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<Reference> list = null;

//			logger.error("PHASE 1...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getReferenceService().list(null, limit, count, null, null)).size() > 0) {

				logger.debug("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (Reference<?> reference : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(reference);
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.debug("Committed transaction.");
				logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			}
			if (list.size() == 0) {
				logger.info("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.info("Committed transaction.");
			
			logger.info("*** Finished Making " + pluralString + " ..." + getSuccessString(success));

			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>Source</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear Occurrences
		sql = "DELETE FROM Occurrence";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Taxa
		sql = "DELETE FROM Taxon";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Sources
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		
		return true;
	}
	
	/**
	 * Returns the <code>IMIS_Id</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>IMIS_Id</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getIMIS_Id(Reference<?> reference) {
		return null;
	}
	
	/**
	 * Returns the <code>SourceCategoryFK</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceCategoryFK</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceCategoryFK(Reference<?> reference) {
		Integer result = null;
		try {
		result = PesiTransformer.reference2SourceCategoryFK(reference);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceCategoryCache</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceCategoryCache(Reference<?> reference, PesiExportState state) {
		return state.getTransformer().getCacheByReference(reference);
	}

	/**
	 * Returns the <code>Name</code> attribute. The corresponding CDM attribute is <code>title</code>.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>Name</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getName(Reference<?> reference) {
		if (reference != null) {
			return reference.getTitleCache(); // was getTitle()
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the <code>AuthorString</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> of an <code>authorTeam</code>.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAuthorString(Reference<?> reference) {
		String result = null;

		try {
		if (reference != null) {
			TeamOrPersonBase team = reference.getAuthorTeam();
			if (team != null) {
				result = team.getTitleCache();
//				result = team.getNomenclaturalTitle();
			} else {
				result = null;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * Returns the <code>NomRefCache</code> attribute. The corresponding CDM attribute is <code>titleCache</code>.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>NomRefCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefCache(Reference<?> reference) {
		return null;
//		if (reference != null) {
//			return reference.getTitleCache();
//		} else {
//			return null;
//		}
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(Reference<?> reference) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>RefIdInSource</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>RefIdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRefIdInSource(Reference<?> reference) {
		String result = null;

		try {
		if (reference != null) {
			Set<IdentifiableSource> sourceAll = reference.getSources();
			Set<IdentifiableSource> sourceCandidates = filterOriginalPesiDbSources(sourceAll);
			
			if (sourceCandidates.size() == 1) {
				result = sourceCandidates.iterator().next().getIdInSource();
			} else if (sourceCandidates.size() > 1) {
				logger.warn("Reference for RefIdInSource has multiple IdentifiableSources which are candidates for a PESI originalDbSource. RefIdInSource can't be determined correctly and will be left out: " + reference.getUuid() + " (" + reference.getTitleCache() + ")");
				int count = 1;
//				for (IdentifiableSource source : sources) {
//					result += source.getIdInSource();
//					if (count < sources.size()) {
//						result += "; ";
//					}
//					count++;
//				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private static Set<IdentifiableSource> filterOriginalPesiDbSources(
			Set<IdentifiableSource> sourceAll) {
		Set<IdentifiableSource> sourceCandidates = new HashSet<IdentifiableSource>();
		for (IdentifiableSource source : sourceAll){
			if (isOriginalPesiDbSource(source)){
				sourceCandidates.add(source);
			}
		}
		return sourceCandidates;
	}

	private static boolean isOriginalPesiDbSource(IdentifiableSource source) {
		return (source.getCitation() != null) &&
				source.getCitation().getType().equals(ReferenceType.Database);
	}

	/**
	 * Returns the <code>OriginalDB</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> of a <code>citation</code>.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOriginalDB(Reference<?> reference) {
		String result = "";

		try {
		if (reference != null) {
			Set<IdentifiableSource> sourcesAll = reference.getSources();
			Set<IdentifiableSource> sourceCandidates = filterOriginalPesiDbSources(sourcesAll); 
			
			if (sourceCandidates.size() == 1) {
				Reference citation = sourceCandidates.iterator().next().getCitation();
				if (citation != null) {
					result = PesiTransformer.databaseString2Abbreviation(citation.getTitleCache()); //or just title
				} else {
					logger.warn("OriginalDB can not be determined because the citation of this source is NULL: " + sourceCandidates.iterator().next().getUuid());
				}
			} else if (sourceCandidates.size() > 1) {
				logger.warn("Taxon has multiple IdentifiableSources: " + reference.getUuid() + " (" + reference.getTitleCache() + ")");
				int count = 1;
				for (IdentifiableSource source : sourceCandidates) {
					Reference citation = source.getCitation();
					if (citation != null) {
						result += PesiTransformer.databaseString2Abbreviation(citation.getTitleCache());
						if (count < sourceCandidates.size()) {
							result += "; ";
						}
						count++;
					}
				}
			} else {
				result = null;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! state.getConfig().getDoReferences().equals(DO_REFERENCES.ALL);
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		ExtensionType extensionType = null;
		
		mapping.addMapper(IdMapper.NewInstance("SourceId"));
		
		// IMIS_Id
		extensionType = (ExtensionType)getTermService().find(ErmsTransformer.IMIS_UUID);
		if (extensionType != null) {
			mapping.addMapper(DbExtensionMapper.NewInstance(extensionType, "IMIS_Id"));
		} else {
			mapping.addMapper(MethodMapper.NewInstance("IMIS_Id", this));
		}
		
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryFK", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryCache", this, Reference.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("Name", this));
		mapping.addMapper(DbStringMapper.NewInstance("referenceAbstract", "Abstract"));
		mapping.addMapper(DbStringMapper.NewInstance("title", "Title"));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("datePublished", "RefYear"));
		mapping.addMapper(MethodMapper.NewInstance("NomRefCache", this));
		mapping.addMapper(DbUriMapper.NewInstance("uri", "Link"));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));
		mapping.addMapper(MethodMapper.NewInstance("RefIdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));

		return mapping;
	}

}
