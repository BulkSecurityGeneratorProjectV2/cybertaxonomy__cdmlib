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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{

    /**
     * Computes all taxon bases.
     * @param limit
     * @param start
     * @return
     *
     * FIXME could substitute with list(Synonym.class, limit, start)
     */
    public List<Synonym> getAllSynonyms(int limit, int start);

    /**
     * Computes all taxon bases.
     * @param limit
     * @param start
     * @return
     *
     * FIXME could substitute with list(Taxon.class, limit,start)
     */
    public List<Taxon> getAllTaxa(int limit, int start);

    /**
     * Computes all Taxon instances that do not have a taxonomic parent.
     * @param sec The concept reference that the taxon belongs to
     *
     * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
     * @return The List<Taxon> of root taxa.
     * @deprecated obsolete when using classification
     */
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, boolean onlyWithChildren);

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
     * @deprecated obsolete when using classification
     */
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, boolean onlyWithChildren, boolean withMisapplications, List<String> propertyPaths);

    /**
     * Computes all relationships.
     * @param limit
     * @param start
     * @return
     * FIXME candidate for harmonization - rename to listRelationships
     */
    public List<RelationshipBase> getAllRelationships(int limit, int start);

    /**
     * Returns TaxonRelationshipType vocabulary
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary();

    /**
     * Returns a list of taxa that matches the name string and the sec reference
     * @param name the name string to search for
     * @param sec the taxons sec reference
     * @return a list of taxa matching the name and the sec reference
     */
    public List<TaxonBase> searchTaxaByName(String name, Reference sec);

    /**
     * Swaps given synonym and accepted taxon.
     * In particular:
     * <ul>
     * 		<li>A new accepted taxon with the synonyms name is created</li>
     * 		<li>The synonym is deleted from the old accepted taxons synonym list</li>
     * 		<li>A new synonym with the name of the old accepted taxon is created</li>
     * 		<li>The newly created synonym get related to the newly created accepted taxon</li>
     * </ul>
     *
     * @param synonym
     * @param acceptedTaxon
     * @param synonymRelationshipType the relationship type the newly created synonym will have. Defaults to SYNONYM_OF
     */
    public void swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon);

    /**
     * Changes a synonym into an accepted taxon and removes
     * the synonym relationship to the given accepted taxon.
     * Other synonyms homotypic to the synonym to change are
     * moved to the same new accepted taxon as homotypic
     * synonyms. The new accepted taxon has the same name and
     * the same sec reference as the old synonym.<BR>
     * If the given accepted taxon and the synonym are homotypic
     * to each other an exception may be thrown as taxonomically it doesn't
     * make sense to have two accepted taxa in the same homotypic group
     * but also it is than difficult to decide how to handle other names
     * in the homotypic group. It is up to the implementing class to
     * handle this situation via an exception or in another way.
     * TODO Open issue: does the old synonym need to be deleted from the database?
     *
     * @param synonym
     * 				the synonym to change into an accepted taxon
     * @param acceptedTaxon
     * 				an accepted taxon, the synonym had a relationship to
     * @param deleteSynonym
     * 			if true the method tries to delete the old synonym from the database
     * @param copyCitationInfo
     * 			if true the citation and the microcitation of newly created synonyms
     * 			is taken from the old synonym relationships.
     * @param citation
     * 			if given this citation is added to the newly created synonym
     * 			relationships as citation. Only used if copyCitationInfo is <code> false</code>
     * @param microCitation
     * 			if given this microCitation is added to the newly created synonym
     * 			relationships as microCitation.Only used if copyCitationInfo is <code> false</code>
     * @return
     * 			the newly created accepted taxon
     * @throws IllegalArgumentException
     * 			if the given accepted taxon and the synonym are homotypic
     * 		    to each other an exception may be thrown as taxonomically it doesn't
     * 			make sense to have two accepted taxa in the same homotypic group
     *          but also it is than difficult to decide how to handle other names
     *          in the homotypic group. It is up to the implementing class to
     *          handle this situation via an exception or in another way.
     */
    public Taxon changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym, boolean copyCitationInfo, Reference citation, String microCitation) throws HomotypicalGroupChangeException;

    /**
     * TODO still needed and correct?
     * Change a synonym into a related concept
     *
     * @param synonym
     * 				the synonym to change into the concept taxon
     * @param toTaxon
     * 				the taxon the newly created concept should be related to
     * @param taxonRelationshipType
     * 				the type of relationship
     * @param reference
     * @param microReference
     * @return
     * 				the newly created concept
     */
    public Taxon changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference reference, String microReference);


    /**
     * Changes the homotypic group of a synonym into the new homotypic group.
     * All relations to taxa are updated correctly depending on the homotypic
     * group of the accepted taxon. <BR>
     * All existing basionym relationships to and from this name are removed.<BR>
     * If the parameter <code>targetTaxon</code> is defined, the synonym is
     * added to this taxon irrespctive of if it has been related to this
     * taxon before.<BR>
     * If <code>removeFromOtherTaxa</code> is true and <code>targetTaxon</code> is
     * defined all relationships to other taxa are deleted.<BR>
     * If <code>setBasionymRelationIfApplicable</code> is true a basionym relationship
     * between the existing basionym(s) of the new homotypic group and the synonyms name
     * is added.<BR>
     *
     * @param synonym
     * @param newHomotypicalGroup
     * @param taxon
     * @param setBasionymRelationIfApplicable
     */
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup, Taxon targetTaxon,
                        boolean removeFromOtherTaxa, boolean setBasionymRelationIfApplicable);


    /**
     * Moves a synonym to another taxon and removes the old synonym relationship.
     *
     * @param oldSynonymRelation the old synonym relationship defining the synonym to move and the old accepted taxon.
     * @param newTaxon the taxon the synonym will be moved to
     * @param moveHomotypicGroup if the synonym belongs to a homotypical group with other synonyms and
     * 		<code>moveHomotypicGroup</code> is <code>true</code> all these synonyms are moved to the new taxon,
     * 		if <code>false</code> a {@link HomotypicalGroupChangeException} is thrown.
     * 		<code>MoveHomotypicGroup</code> has no effect if the synonym is the only synonym in it's homotypic group.
     * @param newSynonymRelationshipType the synonym relationship type of the new synonym relations. Default is
     * 		{@link SynonymRelationshipType#HETEROTYPIC_SYNONYM_OF() heterotypic}.
     * @param newReference The reference for the new synonym relation(s).
     * @param newReferenceDetail The reference detail for the new synonym relation(s).
     * @param keepReference if no <code>newReference</code> and/or no <code>newReferenceDetail</code>
     * 		is defined they are taken from the old synonym relation(s) if <code>keepReference</code> is
     * 		<code>true</code>. If <code>false</code> the reference and the reference detail will be taken
     * 		only from the <code>newReference</code> and <code>newReferenceDetail</code>.
     * @return The new synonym relationship. If <code>moveHomotypicGroup</code> is <code>true</code> additionally
     * 		created new synonym relationships must be retrieved separately from the new taxon.
     * @throws HomotypicalGroupChangeException Exception is thrown if (1) synonym is homotypic to the old accepted taxon or
     * 		(2) synonym is in homotypic group with other synonyms and <code>moveHomotypicGroup</code> is false
     */
    public SynonymRelationship moveSynonymToAnotherTaxon(SynonymRelationship oldSynonymRelation, Taxon newTaxon, boolean moveHomotypicGroup,
            SynonymRelationshipType newSynonymRelationshipType, Reference newReference, String newReferenceDetail, boolean keepReference) throws HomotypicalGroupChangeException;

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

    /**
     * Returns the list of all synonyms that share the same homotypical group with the given taxon.
     * Only those homotypic synonyms are returned that do have a synonym relationship with the accepted taxon.
     * @param taxon
     * @param propertyPaths
     * @return
     */
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths);

    /**
     * Returns the ordered list of all {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups}
     * that contain {@link Synonym synonyms} that are heterotypic to the given taxon.
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase Taxon names} of heterotypic synonyms
     * belong to a homotypical group which cannot be the homotypical group to which the
     * taxon name of the given taxon belongs. This method does not return the homotypic group the given
     * taxon belongs to.<BR>
     * This method does neglect the type of synonym relationship that is defined between the given taxon
     * and the synonym. So the synonym relationship may be homotypic however a synonym is returned
     * in one of the result lists as long as the synonym does not belong to the same homotypic group as
     * the given taxon.<BR>
     * The list returned is ordered according to the date of publication of the
     * first published name within each homotypical group.
     *
     * @see			#getHeterotypicSynonymyGroups()
     * @see			#getSynonyms()
     * @see			SynonymRelationshipType#HETEROTYPIC_SYNONYM_OF()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup

     * @param taxon
     * @param propertyPaths
     * @return
     */
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
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

    /**
     * Returns a list of TaxonBase instances where the
     * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of
     * <i>null</i> will search for those taxa with a value of null in that field
     *
     * @param clazz optionally filter by class
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a List of TaxonBase instances
     */
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

    /**
     * Returns a list of IdentifiableEntity instances (in particular, TaxonNameBase and TaxonBase instances)
     * that match the properties specified in the configurator.
     * @param configurator
     * @return
     */
    public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator);

    /**
     * <h4>This is an experimental feature, it may be moved, modified, or even removed in future releases!!!</h4>
     *
     * @param clazz
     * @param queryString the query string to filter by
     * @param pageSize The maximum number of objects returned (can be null for all objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return a paged list of instances of type T matching the queryString
     * @throws IOException
     * @throws CorruptIndexException
     * @throws ParseException
     */
    public Pager<SearchResult<TaxonBase>> findByDescriptionElementFullText(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException;


    /**
     *
     * @param taxon
     * @param size
     * @param height
     * @param widthOrDuration
     * @param mimeTypes
     * @return
     *
     * FIXME candidate for harmonization - rename to listMedia()
     */
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes);

    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs);
    
    public int countAllRelationships();

    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath);
    public List<TaxonNameBase> findIdenticalTaxonNameIds(List<String> propertyPath);
    public String getPhylumName(TaxonNameBase name);

    public long deleteSynonymRelationships(Synonym syn);


    /**
     * Removes a synonym.<BR><BR>
     *
     * In detail it removes
     *  <li>all synonym relationship to the given taxon or to all taxa if taxon is <code>null</code></li>
     *  <li>the synonym concept if it is not referenced by any synonym relationship anymore</li>
     *  <BR><BR>
     *  If <code>removeNameIfPossible</code> is true
     *  it also removes the synonym name if it is not used in any other context
     *  (part of a concept, in DescriptionElementSource, part of a name relationship, used inline, ...)<BR><BR>
     *  If <code>newHomotypicGroupIfNeeded</code> is <code>true</code> and the synonym name is not deleted and
     *  the name is homotypic to the taxon the name is moved to a new homotypical group.<BR><BR>
     *
     *  If synonym is <code>null</code> the method has no effect.
     *
     * @param taxon
     * @param synonym
     * @param removeNameIfPossible
     * @throws DataChangeNoRollbackException
     */
    public void deleteSynonym(Synonym synonym, Taxon taxon, boolean removeNameIfPossible, boolean newHomotypicGroupIfNeeded);


    /**
     * Returns the SynonymRelationships (of where relationship.type == type, if this argument is supplied)
     * depending on direction, where the supplied taxon is relatedTo or the supplied synonym is relatedFrom.
     *
     * @param taxonBase The taxon or synonym that is relatedTo or relatedFrom
     * @param type The type of SynonymRelationship (can be null)
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
     * @param direction The direction of the relationship
     * @return a List of SynonymRelationship instances
     */
    public List<SynonymRelationship> listSynonymRelationships(
            TaxonBase taxonBase, SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths, Direction direction);

    /**
     * @param tnb
     * @return
     */
    public Taxon findBestMatchingTaxon(String taxonName);

    public Taxon findBestMatchingTaxon(MatchingTaxonConfigurator config);

    public Synonym findBestMatchingSynonym(String taxonName);

    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon();

    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym();

    public List<UuidAndTitleCache<TaxonBase>> findTaxaAndNamesForEditor(ITaxonServiceConfigurator configurator);
    
    /**
     * Creates the specified inferred synonyms for the taxon in the classification, but do not insert it to the database
     * @param taxon
     * @param tree
     * @return list of inferred synonyms
     */
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification tree, SynonymRelationshipType type);
    
    /**
     * Creates all inferred synonyms for the taxon in the classification, but do not insert it to the database
     * @param taxon
     * @param tree
     * @return list of inferred synonyms
     */
    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree);

       


}
