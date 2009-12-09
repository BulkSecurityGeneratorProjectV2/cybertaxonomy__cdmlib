// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
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
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{

	/**
	 * Computes all taxon bases.
	 * FIXME could substitute with list(Synonym.class, limit, start)
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<Synonym> getAllSynonyms(int limit, int start);
	
	/**
	 * Computes all taxon bases.
	 * FIXME could substitute with list(Taxon.class, limit,start)
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<Taxon> getAllTaxa(int limit, int start);	
	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @param sec The concept reference that the taxon belongs to
	 * @return The List<Taxon> of root taxa.
	 * @deprecated obsolete when using taxonomicTree
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec);
	

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * 
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 * @deprecated obsolete when using taxonomicTree
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, boolean onlyWithChildren);

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisapplications if false taxa that have at least one misapplied name relationship in which they are
	 * the misapplied name are not returned.<Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 * @deprecated obsolete when using taxonomicTree
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, boolean onlyWithChildren, boolean withMisapplications);

	/**
	 * Computes all Taxon instances which name is of a certain Rank.
	 * @param rank The rank of the taxon name
	 * @param sec The concept reference that the taxon belongs to
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisapplications if false taxa that have at least one misapplied name relationship in which they are
	 * the misapplied name are not returned.<Br>Default: true.
	 * @param propertyPaths
	 *            properties to be initialized, For detailed description and
	 *            examples <b>please refer to:</b>
	 *            {@link BeanInitializer#initialize(Object, List)}. <Br>
	 *            Default: true.
	 * @return The List<Taxon> of root taxa.
	 * @deprecated obsolete when using taxonomicTree
	 */
	public List<Taxon> getRootTaxa(Rank rank, ReferenceBase sec, boolean onlyWithChildren, boolean withMisapplications, List<String> propertyPaths);
	
	/**
	 * Computes all relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    public List<RelationshipBase> getAllRelationships(int limit, int start);

	/**
	 * Returns TaxonRelationshipType vocabulary
	 * @return
	 * @deprecated use TermService#getVocabulary(VocabularyType) instead
	 */
	public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary();

	/** */
	public List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
		
	/**
	 * Changes an accepted taxon to a synonym of another taxon. 
	 * 
	 * @param oldTaxon
	 * 				the accepted taxon that will be changed into a synonym
	 * @param newAcceptedTaxon
	 * 				the accepted taxon, the old taxon will become a synonym of
	 * @param synonymType
	 * 				<code>SynonymRelationshipType</code> to indicate wether hetero or homotypic
	 * @param citation
	 * 				the reference for this assumption
	 * @param citationMicroReference
	 * 				the micro citation
	 * @return
	 * 				the newly created synonym
	 */
	public Synonym makeTaxonSynonym (Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference);
	
	/**
	 * Sets the synonyms name as the accepted taxons name. Deletes the synonym from the accepted taxon synonym list
	 * and attaches a new synonym created with the former name of the accepted taxon 
	 * 
	 * @param synonym 
	 * @param acceptedTaxon
	 * @param synonymRelationshipType the relationship type the newly created synonym will have. Defaults to SYNONYM_OF
	 */
	public void swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, SynonymRelationshipType synonymRelationshipType );
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
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
	public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
		
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
	 * @return a Pager of TaxonRelationship instances
	 */
	public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonRelationship instances
	 */
	public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of TaxonRelationship instances
	 */
	public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied synonym is relatedFrom.
	 * 
	 * @param taxon The synonym that is relatedFrom
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of SynonymRelationship instances
	 */
	public Pager<SynonymRelationship> getSynonyms(Synonym synonym, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of SynonymRelationship instances
	 */
	public Pager<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths);
	
	public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths);
	
	/**
	 * Returns a Paged List of TaxonBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all TaxonBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager Taxon instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<TaxonBase> search(Class<? extends TaxonBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a list of TaxonBase instances where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param clazz optionally filter by class 
	 * @param uninomial 
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TaxonBase instances
	 */
	public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a list of IdentifiableEntity instances (in particular, TaxonNameBase and TaxonBase instances)
	 * that match the properties specified in the configurator.
	 * @param configurator
	 * @return
	 */
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator);
	
	/**
	 * 
	 * @param taxon
	 * @param size
	 * @param height
	 * @param widthOrDuration
	 * @param mimeTypes
	 * @return
	 */
	public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes);
}
