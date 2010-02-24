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
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.pesi.PesiTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 11.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiSourceExport extends PesiExportBase<ReferenceBase> {
	private static final Logger logger = Logger.getLogger(PesiSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = ReferenceBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Source";
	private static final String pluralString = "Sources";

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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doInvoke(PesiExportState state) {
		try{
			logger.info("Start: Make " + pluralString + " ...");

			// Stores whether this invoke was successful or not.
			boolean success = true ;

			// PESI: Clear the database table Source.
			doDelete(state);

			// Start transaction
			TransactionStatus txStatus = startTransaction(true);

			// CDM: Get all References
			List<ReferenceBase> list = getReferenceService().list(null, 100000000, 0, null, null);

			// Get specific mappings: (CDM) Reference -> (PESI) Source
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the Sources
			// TODO: Store CDM2PESI identifier pairs for later use in other export classes - PesiExportState
			int count = 0;
			for (ReferenceBase<?> reference : list) {
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(reference);
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.info("End: Make " + pluralString + " ..." + getSuccessString(success));

			return success;
		} catch(SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
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

		// Clear Sources
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}
	
	/**
	 * Returns the IMIS_Id.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The IMIS_Id
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getIMIS_Id(ReferenceBase<?> reference) {
		// TODO
		// Where is the IMIS_Id from an ERMS import stored in CDM?
		return null;
	}
	
	/**
	 * Returns the <code>SourceCategoryFK</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>SourceCategoryFK</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceCategoryFK(ReferenceBase<?> reference) {
		return PesiTransformer.reference2SourceCategoryFK(reference);
	}
	
	/**
	 * Returns the <code>SourceCategoryCache</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>SourceCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceCategoryCache(ReferenceBase<?> reference) {
		return PesiTransformer.getSourceCategoryCache(reference);
	}

	/**
	 * Returns the <code>Name</code> attribute. The corresponding CDM attribute is <code>title</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>Name</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getName(ReferenceBase<?> reference) {
		return reference.getTitle();
	}

	/**
	 * Returns the <code>AuthorString</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> or an <code>authorTeam</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAuthorString(ReferenceBase<?> reference) {
		TeamOrPersonBase team = reference.getAuthorTeam();
		if (team != null) {
			return team.getTitleCache();
			//team.getNomenclaturalTitle();
		} else {
			return null;
		}
	}

	/**
	 * Returns the <code>NomRefCache</code> attribute. The corresponding CDM attribute is <code>titleCache</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>NomRefCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefCache(ReferenceBase<?> reference) {
		return reference.getTitleCache();
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(ReferenceBase<?> reference) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>RefIdInSource</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>RefIdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRefIdInSource(ReferenceBase<?> reference) {
		String result = null;
		
		// For sets of size bigger than one, this isn't good at all.
		for (IdentifiableSource source : reference.getSources()) {
			if (source != null) {
				result = source.getIdInSource();
			}
		}
		return result;
	}

	/**
	 * Returns the <code>OriginalDB</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> of a <code>citation</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOriginalDB(ReferenceBase<?> reference) {
		String result = null;
		for (IdentifiableSource source : reference.getSources()) {
			if (source != null) {
				result = source.getCitation().getTitleCache();  //or just title
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! (((PesiExportConfigurator) state.getConfig()).getDoReferences().equals(DO_REFERENCES.ALL));
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
	//	mapping.addMapper(IdMapper.NewInstance("SourceId"));
		mapping.addMapper(MethodMapper.NewInstance("IMIS_Id", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryFK", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Name", this));
		mapping.addMapper(DbStringMapper.NewInstance("referenceAbstract", "Abstract"));
		mapping.addMapper(DbStringMapper.NewInstance("title", "Title"));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("datePublished", "RefYear"));
		mapping.addMapper(MethodMapper.NewInstance("NomRefCache", this));
		mapping.addMapper(DbStringMapper.NewInstance("uri", "Link"));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));
		mapping.addMapper(MethodMapper.NewInstance("RefIdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));

		return mapping;
	}

}
