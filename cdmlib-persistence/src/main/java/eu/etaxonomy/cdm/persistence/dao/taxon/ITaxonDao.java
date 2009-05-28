/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.SelectMode;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase>, ISearchableDao<TaxonBase> {
	
	/**
	 * Returns a count of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString
	 * 
	 * @param queryString
	 * @param accepted
	 * @param sec
	 * @return a count of the matching taxa
	 */
	public int countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec);

	/**
	 * Returns a count of TaxonBase instances where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param queryString search string
	 * @param matchMode way how search string shall be matched: exact, beginning, or anywhere
	 * @param selectMode either all taxon bases, or all taxa, or all synonyms
	 * @param sec reference
	 */ 
	public Integer countTaxaByName(String queryString, 
			MatchMode matchMode, SelectMode selectMode, ReferenceBase sec);

	/** 
	 * Returns a list of TaxonBase instances where the taxon.titleCache property matches the name parameter, 
	 * and taxon.sec matches the sec parameter.
	 * @param name
	 * @param sec
	 * @return
	 */
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	/** 
	 * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString, and taxon.sec matches the sec parameter.
	 * @param name
	 * @param sec
	 * @return
	 */
	public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, ReferenceBase sec);

	/** 
	 * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString.
	 * @param queryString
	 * @param matchMode
	 * @param accepted
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, 
			Boolean accepted, Integer pageSize, Integer pageNumber);
	
	
	/** 
	 * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString.
	 * @param queryString
	 * @param matchMode
	 * @param selectMode
	 * @param pageSize
	 * @param pageNumber
	 * @return list of found taxa
	 */
	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode,
			ReferenceBase sec, Integer pageSize, Integer pageNumber);

	/**
	 * @param queryString
	 * @param matchMode
	 * @param accepted
	 * @return
	 */
	public Integer countTaxaByName(String queryString, MatchMode matchMode, 
			Boolean accepted);
		
	/**
	 * Returns a count of TaxonBase instances where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param queryString search string
	 * @param matchMode way how search string shall be matched: exact, beginning, or anywhere
	 * @param selectModel all taxon base, taxa, or synonyms
	 */ 
	public Integer countTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode);

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec);

	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * @param cdmFetch not used yet !! TODO
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisaplications if false only taxa are returned that have no isMisappliedNameFor relationship. 
	 * <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications);
	
	
	/**
	 * Computes all Taxon instances which name is of a certain Rank.
	 * 
	 * @param rank
	 *            The rank of the taxon name
	 * @param sec
	 *            The concept reference that the taxon belongs to
	 * @param cdmFetch
	 *            not used yet !! TODO
	 * @param onlyWithChildren
	 *            if true only taxa are returned that have taxonomic children. <Br>
	 *            Default: true.
	 * @param withMisaplications
	 *            if false only taxa are returned that have no
	 *            isMisappliedNameFor relationship.
	 * @param propertyPaths
	 *            properties to be initialized, For detailed description and
	 *            examples <b>please refer to:</b>
	 *            {@link BeanInitializer#initialize(Object, List)}. <Br>
	 *            Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> 
	getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications, List<String> propertyPaths);

		/**
	 * TODO necessary? 
	 * @param pagesize max maximum number of returned taxa
	 * @param page page to start, with 0 being first page 
	 * @return
	 */
	public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page);
	
	
	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	public List<Taxon> getAllTaxa(Integer limit, Integer start);

	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	public List<Synonym> getAllSynonyms(Integer limit, Integer start);

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start); 

	/**
	 * Find taxa by searching for @{link NameBase}
	 * @param queryString
	 * @param matchMode
	 * @param page
	 * @param pagesize
	 * @param onlyAcccepted
	 * @return
	 */
	public List<Taxon> findByName(String queryString, MatchMode matchMode, int page, int pagesize, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @return
	 */
	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @param criteria
	 * @return
	 */
	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria);
	
	/**
	 * Returns a count of the TaxonRelationships (of where relationship.type == type,
	 *  if this arguement is supplied) where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @return the number of TaxonRelationship instances
	 */
	public int countRelatedTaxa(Taxon taxon, TaxonRelationshipType type);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonRelationship instances
	 */
	public List<TaxonRelationship> getRelatedTaxa(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a count of the SynonymRelationships (of where relationship.type == type,
	 *  if this arguement is supplied) where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @return the number of SynonymRelationship instances
	 */
	public int countSynonyms(Taxon taxon, SynonymRelationshipType type);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of SynonymRelationship instances
	 */
	public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a count of TaxonBase instances where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param clazz 
	 * @param uninomial
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @return a count of TaxonBase instances
	 */
	public int countTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet,String specificEpithet, String infraspecificEpithet, Rank rank);
	
	/**
	 * Returns a list of TaxonBase instances where the
	 * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of 
	 * <i>null</i> will search for those taxa with a value of null in that field
	 * 
	 * @param clazz optionally filter by class (can be null to return all taxa)
	 * @param uninomial 
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a list of TaxonBase instances
	 */
	public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);
	
	
}
