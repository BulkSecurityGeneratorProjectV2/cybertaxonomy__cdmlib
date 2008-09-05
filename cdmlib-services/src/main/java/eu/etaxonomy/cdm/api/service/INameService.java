/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;

public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	/**
	 * @param uuid
	 * @return
	 */
	public abstract TaxonNameBase getTaxonNameByUuid(UUID uuid);

	/**
	 * @param taxonName
	 * @return
	 */
	public abstract UUID saveTaxonName(TaxonNameBase taxonName);

	/**
	 * Saves a collection of  TaxonNames and return its UUID@param taxonCollection
	 * @return
	 */
	public abstract Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<TaxonNameBase> taxonCollection);

	public Map<UUID, ReferencedEntityBase> saveTypeDesignationAll(Collection<ReferencedEntityBase> typeDesignationCollection);

		/**
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<TaxonNameBase> getAllNames(int limit, int start);

	public abstract List<ReferencedEntityBase> getAllNomenclaturalStatus(int limit, int start);

	public abstract List<ReferencedEntityBase> getAllTypeDesignations(int limit, int start);
	
	/**
	 * @param name
	 * @return
	 */
	public abstract List<TaxonNameBase> getNamesByName(String name);

	/**
	 * @param name
	 * @param sessionObject An object that is attached to the session before executing the query
	 * @return
	 */
	public abstract List getNamesByName(String name, CdmBase sessionObject);
		
	
	/**
	 * Returns all Ranks.
	 * @return
	 */
	public abstract OrderedTermVocabulary<Rank> getRankVocabulary();
	
	/**
	 * Returns all NomenclaturalStatusTypes.
	 * @return
	 */
	public abstract TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary();
	
	
	/**
	 * Returns all NameRelationshipTypes.
	 * @return
	 */
	public abstract TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary();
}