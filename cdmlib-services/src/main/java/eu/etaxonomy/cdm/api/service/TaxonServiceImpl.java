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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.SortField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.SynonymDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
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
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


/**
 * @author a.kohlbecker
 * @date 10.09.2010
 *
 */
@Service
@Transactional(readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService{
    private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

    public static final String POTENTIAL_COMBINATION_NAMESPACE = "Potential combination";

    public static final String INFERRED_EPITHET_NAMESPACE = "Inferred epithet";

    public static final String INFERRED_GENUS_NAMESPACE = "Inferred genus";


    @Autowired
    private ITaxonNameDao nameDao;

    @Autowired
    private INameService nameService;
    
    @Autowired
    private ITaxonNodeService nodeService;
    

    @Autowired
    private ICdmGenericDao genericDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IOrderedTermVocabularyDao orderedVocabularyDao;

    @Autowired
    private IOccurrenceDao occurrenceDao;

    @Autowired
    private AbstractBeanInitializer beanInitializer;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    /**
     * Constructor
     */
    public TaxonServiceImpl(){
        if (logger.isDebugEnabled()) { logger.debug("Load TaxonService Bean"); }
    }

    /**
     * FIXME Candidate for harmonization
     * rename searchByName ?
     */
    @Override
    public List<TaxonBase> searchTaxaByName(String name, Reference sec) {
        return dao.getTaxaByName(name, sec);
    }

    /**
     * FIXME Candidate for harmonization
     * list(Synonym.class, ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllSynonyms(int, int)
     */
    @Override
    public List<Synonym> getAllSynonyms(int limit, int start) {
        return dao.getAllSynonyms(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * list(Taxon.class, ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllTaxa(int, int)
     */
    @Override
    public List<Taxon> getAllTaxa(int limit, int start) {
        return dao.getAllTaxa(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * merge with getRootTaxa(Reference sec, ..., ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference, boolean)
     */
    @Override
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
        if (cdmFetch == null){
            cdmFetch = CdmFetch.NO_FETCH();
        }
        return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.Reference, boolean, boolean)
     */
    @Override
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, boolean onlyWithChildren,boolean withMisapplications, List<String> propertyPaths) {
        return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications, propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllRelationships(int, int)
     */
    @Override
    public List<RelationshipBase> getAllRelationships(int limit, int start){
        return dao.getAllRelationships(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.TaxonRelationshipType) ?
     */
    @Override
    @Deprecated
    public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary() {

        String taxonRelTypeVocabularyId = "15db0cf7-7afc-4a86-a7d4-221c73b0c9ac";
        UUID uuid = UUID.fromString(taxonRelTypeVocabularyId);
        OrderedTermVocabulary<TaxonRelationshipType> taxonRelTypeVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return taxonRelTypeVocabulary;
    }



    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#swapSynonymWithAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym)
     */
    @Override
    @Transactional(readOnly = false)
    public void swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon){

        TaxonNameBase<?,?> synonymName = synonym.getName();
        synonymName.removeTaxonBase(synonym);
        TaxonNameBase<?,?> taxonName = acceptedTaxon.getName();
        taxonName.removeTaxonBase(acceptedTaxon);

        synonym.setName(taxonName);
        acceptedTaxon.setName(synonymName);

        // the accepted taxon needs a new uuid because the concept has changed
        // FIXME this leads to an error "HibernateException: immutable natural identifier of an instance of eu.etaxonomy.cdm.model.taxon.Taxon was altered"
        //acceptedTaxon.setUuid(UUID.randomUUID());
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    
    @Override
    @Transactional(readOnly = false)
    public Taxon changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym, boolean copyCitationInfo, Reference citation, String microCitation) throws HomotypicalGroupChangeException{
    	
        TaxonNameBase<?,?> acceptedName = acceptedTaxon.getName();
        TaxonNameBase<?,?> synonymName = synonym.getName();
        HomotypicalGroup synonymHomotypicGroup = synonymName.getHomotypicalGroup();

        //check synonym is not homotypic
        if (acceptedName.getHomotypicalGroup().equals(synonymHomotypicGroup)){
            String message = "The accepted taxon and the synonym are part of the same homotypical group and therefore can not be both accepted.";
            throw new HomotypicalGroupChangeException(message);
        }

        Taxon newAcceptedTaxon = Taxon.NewInstance(synonymName, acceptedTaxon.getSec());

        SynonymRelationshipType relTypeForGroup = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
        List<Synonym> heteroSynonyms = acceptedTaxon.getSynonymsInGroup(synonymHomotypicGroup);

        for (Synonym heteroSynonym : heteroSynonyms){
            if (synonym.equals(heteroSynonym)){
                acceptedTaxon.removeSynonym(heteroSynonym, false);
            }else{
                //move synonyms in same homotypic group to new accepted taxon
                heteroSynonym.replaceAcceptedTaxon(newAcceptedTaxon, relTypeForGroup, copyCitationInfo, citation, microCitation);
            }
        }

        //synonym.getName().removeTaxonBase(synonym);
       
        if (deleteSynonym){
//			deleteSynonym(synonym, taxon, false);
            try {
                this.dao.flush();
                this.deleteSynonym(synonym, acceptedTaxon, new SynonymDeletionConfigurator());

            } catch (Exception e) {
                logger.info("Can't delete old synonym from database");
            }
        }

        return newAcceptedTaxon;
    }


    @Override
    public Taxon changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference citation, String microcitation){

        // Get name from synonym
        TaxonNameBase<?, ?> synonymName = synonym.getName();

        // remove synonym from taxon
        toTaxon.removeSynonym(synonym);

        // Create a taxon with synonym name
        Taxon fromTaxon = Taxon.NewInstance(synonymName, null);

        // Add taxon relation
        fromTaxon.addTaxonRelation(toTaxon, taxonRelationshipType, citation, microcitation);

        // since we are swapping names, we have to detach the name from the synonym completely.
        // Otherwise the synonym will still be in the list of typified names.
        synonym.getName().removeTaxonBase(synonym);

        return fromTaxon;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeHomotypicalGroupOfSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.name.HomotypicalGroup, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup, Taxon targetTaxon,
                        boolean removeFromOtherTaxa, boolean setBasionymRelationIfApplicable){
        // Get synonym name
        TaxonNameBase synonymName = synonym.getName();
        HomotypicalGroup oldHomotypicalGroup = synonymName.getHomotypicalGroup();


        // Switch groups
        oldHomotypicalGroup.removeTypifiedName(synonymName);
        newHomotypicalGroup.addTypifiedName(synonymName);

        //remove existing basionym relationships
        synonymName.removeBasionyms();

        //add basionym relationship
        if (setBasionymRelationIfApplicable){
            Set<TaxonNameBase> basionyms = newHomotypicalGroup.getBasionyms();
            for (TaxonNameBase basionym : basionyms){
                synonymName.addBasionym(basionym);
            }
        }

        //set synonym relationship correctly
//			SynonymRelationship relToTaxon = null;
        boolean relToTargetTaxonExists = false;
        Set<SynonymRelationship> existingRelations = synonym.getSynonymRelations();
        for (SynonymRelationship rel : existingRelations){
            Taxon acceptedTaxon = rel.getAcceptedTaxon();
            boolean isTargetTaxon = acceptedTaxon != null && acceptedTaxon.equals(targetTaxon);
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType newRelationType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            rel.setType(newRelationType);
            //TODO handle citation and microCitation

            if (isTargetTaxon){
                relToTargetTaxonExists = true;
            }else{
                if (removeFromOtherTaxa){
                    acceptedTaxon.removeSynonym(synonym, false);
                }else{
                    //do nothing
                }
            }
        }
        if (targetTaxon != null &&  ! relToTargetTaxonExists ){
            Taxon acceptedTaxon = targetTaxon;
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType relType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            //TODO handle citation and microCitation
            Reference citation = null;
            String microCitation = null;
            acceptedTaxon.addSynonym(synonym, relType, citation, microCitation);
        }

    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends TaxonBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Override
    @Autowired
    protected void setDao(ITaxonDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * @param taxon
     * @param includeRelationships
     * @param maxDepth
     * @param limit
     * @param starts
     * @param propertyPaths
     * @return an List which is not specifically ordered
     */
    @Override
    public Set<Taxon> listRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Integer maxDepth,
            Integer limit, Integer start, List<String> propertyPaths) {

        Set<Taxon> relatedTaxa = collectRelatedTaxa(taxon, includeRelationships, new HashSet<Taxon>(), maxDepth);
        relatedTaxa.remove(taxon);
        beanInitializer.initializeAll(relatedTaxa, propertyPaths);
        return relatedTaxa;
    }


    /**
     * recursively collect related taxa for the given <code>taxon</code> . The returned list will also include the
     *  <code>taxon</code> supplied as parameter.
     *
     * @param taxon
     * @param includeRelationships
     * @param taxa
     * @param maxDepth can be <code>null</code> for infinite depth
     * @return
     */
    private Set<Taxon> collectRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Set<Taxon> taxa, Integer maxDepth) {

        if(taxa.isEmpty()) {
            taxa.add(taxon);
        }

        if(maxDepth != null) {
            maxDepth--;
        }
        if(logger.isDebugEnabled()){
            logger.debug("collecting related taxa for " + taxon + " with maxDepth=" + maxDepth);
        }
        List<TaxonRelationship> taxonRelationships = dao.getTaxonRelationships(taxon, null, null, null, null, null, null);
        for (TaxonRelationship taxRel : taxonRelationships) {

            // skip invalid data
            if (taxRel.getToTaxon() == null || taxRel.getFromTaxon() == null || taxRel.getType() == null) {
                continue;
            }
            // filter by includeRelationships
            for (TaxonRelationshipEdge relationshipEdgeFilter : includeRelationships) {
                if ( relationshipEdgeFilter.getTaxonRelationshipType().equals(taxRel.getType()) ) {
                    if (relationshipEdgeFilter.getDirections().contains(Direction.relatedTo) && !taxa.contains(taxRel.getToTaxon())) {
                        if(logger.isDebugEnabled()){
                            logger.debug(maxDepth + ": " + taxon.getTitleCache() + " --[" + taxRel.getType().getLabel() + "]--> " + taxRel.getToTaxon().getTitleCache());
                        }
                        taxa.add(taxRel.getToTaxon());
                        if(maxDepth == null || maxDepth > 0) {
                            taxa.addAll(collectRelatedTaxa(taxRel.getToTaxon(), includeRelationships, taxa, maxDepth));
                        }
                    }
                    if(relationshipEdgeFilter.getDirections().contains(Direction.relatedFrom) && !taxa.contains(taxRel.getFromTaxon())) {
                        taxa.add(taxRel.getFromTaxon());
                        if(logger.isDebugEnabled()){
                            logger.debug(maxDepth + ": " +taxRel.getFromTaxon().getTitleCache() + " --[" + taxRel.getType().getLabel() + "]--> " + taxon.getTitleCache() );
                        }
                        if(maxDepth == null || maxDepth > 0) {
                            taxa.addAll(collectRelatedTaxa(taxRel.getFromTaxon(), includeRelationships, taxa, maxDepth));
                        }
                    }
                }
            }
        }
        return taxa;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SynonymRelationship> getSynonyms(Synonym synonym,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(synonym, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(synonym, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHomotypicSynonymsByHomotypicGroup(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
     */
    @Override
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        return t.getHomotypicSynonymsByHomotypicGroup();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHeterotypicSynonymyGroups(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
     */
    @Override
    public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
        List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<List<Synonym>>(homotypicalGroups.size());
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            heterotypicSynonymyGroups.add(t.getSynonymsInGroup(homotypicalGroup));
        }
        return heterotypicSynonymyGroups;
    }

    @Override
    public List<UuidAndTitleCache<TaxonBase>> findTaxaAndNamesForEditor(IFindTaxaAndNamesConfigurator configurator){

        List<UuidAndTitleCache<TaxonBase>> result = new ArrayList<UuidAndTitleCache<TaxonBase>>();
//        Class<? extends TaxonBase> clazz = null;
//        if ((configurator.isDoTaxa() && configurator.isDoSynonyms())) {
//            clazz = TaxonBase.class;
//            //propertyPath.addAll(configurator.getTaxonPropertyPath());
//            //propertyPath.addAll(configurator.getSynonymPropertyPath());
//        } else if(configurator.isDoTaxa()) {
//            clazz = Taxon.class;
//            //propertyPath = configurator.getTaxonPropertyPath();
//        } else if (configurator.isDoSynonyms()) {
//            clazz = Synonym.class;
//            //propertyPath = configurator.getSynonymPropertyPath();
//        }


        result = dao.getTaxaByNameForEditor(configurator.isDoTaxa(), configurator.isDoSynonyms(), configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)
     */
    @Override
    public Pager<IdentifiableEntity> findTaxaAndNames(IFindTaxaAndNamesConfigurator configurator) {

        List<IdentifiableEntity> results = new ArrayList<IdentifiableEntity>();
        int numberOfResults = 0; // overall number of results (as opposed to number of results per page)
        List<TaxonBase> taxa = null;

        // Taxa and synonyms
        long numberTaxaResults = 0L;


        List<String> propertyPath = new ArrayList<String>();
        if(configurator.getTaxonPropertyPath() != null){
            propertyPath.addAll(configurator.getTaxonPropertyPath());
        }


       if (configurator.isDoMisappliedNames() || configurator.isDoSynonyms() || configurator.isDoTaxa()){
            if(configurator.getPageSize() != null){ // no point counting if we need all anyway
                numberTaxaResults =
                    dao.countTaxaByName(configurator.isDoTaxa(),configurator.isDoSynonyms(), configurator.isDoMisappliedNames(),
                        configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(),
                        configurator.getNamedAreas());
            }

            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){ // no point checking again if less results
                taxa = dao.getTaxaByName(configurator.isDoTaxa(), configurator.isDoSynonyms(),
                    configurator.isDoMisappliedNames(), configurator.getTitleSearchStringSqlized(), configurator.getClassification(),
                    configurator.getMatchMode(), configurator.getNamedAreas(),
                    configurator.getPageSize(), configurator.getPageNumber(), propertyPath);
            }
       }

        if (logger.isDebugEnabled()) { logger.debug(numberTaxaResults + " matching taxa counted"); }

        if(taxa != null){
            results.addAll(taxa);
        }

        numberOfResults += numberTaxaResults;

        // Names without taxa
        if (configurator.isDoNamesWithoutTaxa()) {
            int numberNameResults = 0;

            List<? extends TaxonNameBase<?,?>> names =
                nameDao.findByName(configurator.getTitleSearchStringSqlized(), configurator.getMatchMode(),
                        configurator.getPageSize(), configurator.getPageNumber(), null, configurator.getTaxonNamePropertyPath());
            if (logger.isDebugEnabled()) { logger.debug(names.size() + " matching name(s) found"); }
            if (names.size() > 0) {
                for (TaxonNameBase<?,?> taxonName : names) {
                    if (taxonName.getTaxonBases().size() == 0) {
                        results.add(taxonName);
                        numberNameResults++;
                    }
                }
                if (logger.isDebugEnabled()) { logger.debug(numberNameResults + " matching name(s) without taxa found"); }
                numberOfResults += numberNameResults;
            }
        }

        // Taxa from common names

        if (configurator.isDoTaxaByCommonNames()) {
            taxa = new ArrayList<TaxonBase>();
            numberTaxaResults = 0;
            if(configurator.getPageSize() != null){// no point counting if we need all anyway
                numberTaxaResults = dao.countTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
            }
            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){
                List<Object[]> commonNameResults = dao.getTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getPageSize(), configurator.getPageNumber(), configurator.getTaxonPropertyPath());
                for( Object[] entry : commonNameResults ) {
                    taxa.add((TaxonBase) entry[0]);
                }
            }
            if(taxa != null){
                results.addAll(taxa);
            }
            numberOfResults += numberTaxaResults;

        }

       return new DefaultPagerImpl<IdentifiableEntity>
            (configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
    }

    public List<UuidAndTitleCache<TaxonBase>> getTaxonUuidAndTitleCache(){
        return dao.getUuidAndTitleCache();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllMedia(eu.etaxonomy.cdm.model.taxon.Taxon, int, int, int, java.lang.String[])
     */
    @Override
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes){
        List<MediaRepresentation> medRep = new ArrayList<MediaRepresentation>();
        taxon = (Taxon)dao.load(taxon.getUuid());
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        for (TaxonDescription taxDesc: descriptions){
            Set<DescriptionElementBase> elements = taxDesc.getElements();
            for (DescriptionElementBase descElem: elements){
                for(Media media : descElem.getMedia()){

                    //find the best matching representation
                    medRep.add(MediaUtils.findBestMatchingRepresentation(media, null, size, height, widthOrDuration, mimeTypes));

                }
            }
        }
        return medRep;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listTaxonDescriptionMedia(eu.etaxonomy.cdm.model.taxon.Taxon, boolean)
     */
    @Override
    public List<Media> listTaxonDescriptionMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, boolean limitToGalleries, List<String> propertyPath){
        return listMedia(taxon, includeRelationships, limitToGalleries, true, false, false, propertyPath);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listMedia(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.Set, boolean, java.util.List)
     */
    @Override
    public List<Media> listMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships,
            Boolean limitToGalleries, Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            Boolean includeTaxonNameDescriptions, List<String> propertyPath) {

        Set<Taxon> taxa = new HashSet<Taxon>();
        List<Media> taxonMedia = new ArrayList<Media>();

        if (limitToGalleries == null) {
            limitToGalleries = false;
        }

        // --- resolve related taxa
        if (includeRelationships != null) {
            taxa = listRelatedTaxa(taxon, includeRelationships, null, null, null, null);
        }

        taxa.add((Taxon) dao.load(taxon.getUuid()));

        if(includeTaxonDescriptions != null && includeTaxonDescriptions){
            List<TaxonDescription> taxonDescriptions = new ArrayList<TaxonDescription>();
            // --- TaxonDescriptions
            for (Taxon t : taxa) {
                taxonDescriptions.addAll(descriptionService.listTaxonDescriptions(t, null, null, null, null, propertyPath));
            }
            for (TaxonDescription taxonDescription : taxonDescriptions) {
                if (!limitToGalleries || taxonDescription.isImageGallery()) {
                    for (DescriptionElementBase element : taxonDescription.getElements()) {
                        for (Media media : element.getMedia()) {
                            taxonMedia.add(media);
                        }
                    }
                }
            }
        }

        if(includeOccurrences != null && includeOccurrences) {
            Set<SpecimenOrObservationBase> specimensOrObservations = new HashSet<SpecimenOrObservationBase>();
            // --- Specimens
            for (Taxon t : taxa) {
                specimensOrObservations.addAll(occurrenceDao.listByAssociatedTaxon(null, t, null, null, null, null));
            }
            for (SpecimenOrObservationBase occurrence : specimensOrObservations) {

//            	direct media removed from specimen #3597
//              taxonMedia.addAll(occurrence.getMedia());

                // SpecimenDescriptions
                Set<SpecimenDescription> specimenDescriptions = occurrence.getSpecimenDescriptions();
                for (DescriptionBase specimenDescription : specimenDescriptions) {
                    if (!limitToGalleries || specimenDescription.isImageGallery()) {
                        Set<DescriptionElementBase> elements = specimenDescription.getElements();
                        for (DescriptionElementBase element : elements) {
                            for (Media media : element.getMedia()) {
                                taxonMedia.add(media);
                            }
                        }
                    }
                }

                // Collection
                //TODO why may collections have media attached? #
                if (occurrence.isInstanceOf(DerivedUnit.class)) {
                    DerivedUnit derivedUnit = CdmBase.deproxy(occurrence, DerivedUnit.class);
                    if (derivedUnit.getCollection() != null){
                        taxonMedia.addAll(derivedUnit.getCollection().getMedia());
                    }
                }

                // pherograms & gelPhotos
                if (occurrence.isInstanceOf(DnaSample.class)) {
                    DnaSample dnaSample = CdmBase.deproxy(occurrence, DnaSample.class);
                    Set<Sequence> sequences = dnaSample.getSequences();
                    //we do show only those gelPhotos which lead to a consensus sequence
                    for (Sequence sequence : sequences) {
                        Set<Media> dnaRelatedMedia = new HashSet<Media>();
                        for (SingleRead singleRead : sequence.getSingleReads()){
                            Amplification amplification = singleRead.getAmplification();
                            dnaRelatedMedia.add(amplification.getGelPhoto());
                            dnaRelatedMedia.add(singleRead.getPherogram());
                            dnaRelatedMedia.remove(null);
                        }
                        taxonMedia.addAll(dnaRelatedMedia);
                    }
                }

            }
        }

        if(includeTaxonNameDescriptions != null && includeTaxonNameDescriptions) {
            // --- TaxonNameDescription
            Set<TaxonNameDescription> nameDescriptions = new HashSet<TaxonNameDescription>();
            for (Taxon t : taxa) {
                nameDescriptions .addAll(t.getName().getDescriptions());
            }
            for(TaxonNameDescription nameDescription: nameDescriptions){
                if (!limitToGalleries || nameDescription.isImageGallery()) {
                    Set<DescriptionElementBase> elements = nameDescription.getElements();
                    for (DescriptionElementBase element : elements) {
                        for (Media media : element.getMedia()) {
                            taxonMedia.add(media);
                        }
                    }
                }
            }
        }

        beanInitializer.initializeAll(taxonMedia, propertyPath);
        return taxonMedia;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByID(java.util.Set)
     */
    @Override
    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs) {
        return this.dao.listByIds(listOfIDs, null, null, null, null);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxonByUuid(UUID uuid, List<String> propertyPaths)
     */
    @Override
    public TaxonBase findTaxonByUuid(UUID uuid, List<String> propertyPaths){
        return this.dao.findByUuid(uuid, null ,propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#countAllRelationships()
     */
    @Override
    public int countAllRelationships() {
        return this.dao.countAllRelationships();
    }




    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNames(java.util.List)
     */
    @Override
    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath) {
        return this.dao.findIdenticalTaxonNames(propertyPath);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator)
     */
    @Override
    public UUID deleteTaxon(Taxon taxon, TaxonDeletionConfigurator config, Classification classification) throws DataChangeNoRollbackException {
        if (config == null){
            config = new TaxonDeletionConfigurator();
        }

            //    	SynonymRelationShip
            if (config.isDeleteSynonymRelations()){
                boolean removeSynonymNameFromHomotypicalGroup = false;
                for (SynonymRelationship synRel : taxon.getSynonymRelations()){
                    Synonym synonym = synRel.getSynonym();
                    taxon.removeSynonymRelation(synRel, removeSynonymNameFromHomotypicalGroup);
                    if (config.isDeleteSynonymsIfPossible()){
                        //TODO which value
                        boolean newHomotypicGroupIfNeeded = true;
                        SynonymDeletionConfigurator synConfig = new SynonymDeletionConfigurator();
                        
                        deleteSynonym(synonym, taxon, synConfig);
                    }else{
                        deleteSynonymRelationships(synonym, taxon);
                    }
                }
            }

            //    	TaxonRelationship
            if (! config.isDeleteTaxonRelationships()){
                if (taxon.getTaxonRelations().size() > 0){
                    String message = "Taxon can't be deleted as it is related to another taxon. Remove taxon from all relations to other taxa prior to deletion.";
                    throw new ReferencedObjectUndeletableException(message);
                }
            } else{
            	for (TaxonRelationship taxRel: taxon.getTaxonRelations()){
            		    
        			
        			
        			if (config.isDeleteMisappliedNamesAndInvalidDesignations()){
        				if (taxRel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR()) || taxRel.getType().equals(TaxonRelationshipType.INVALID_DESIGNATION_FOR())){
        					if (taxon.equals(taxRel.getToTaxon())){
        						this.deleteTaxon(taxRel.getFromTaxon(), config, classification);
        					}
        				}
        			}
        			taxon.removeTaxonRelation(taxRel);
            		/*if (taxFrom.equals(taxon)){
            			try{
            				this.deleteTaxon(taxTo, taxConf, classification);
            			} catch(DataChangeNoRollbackException e){
            				logger.debug("A related taxon will not be deleted." + e.getMessage());
            			}
                	} else {
                		try{
                			this.deleteTaxon(taxFrom, taxConf, classification);
            			} catch(DataChangeNoRollbackException e){
            				logger.debug("A related taxon will not be deleted." + e.getMessage());
            			}
            			
            		}*/
            	}
            }


            
                   
            //    	TaxonDescription
            if (config.isDeleteDescriptions()){
                    Set<TaxonDescription> descriptions = taxon.getDescriptions();

                    for (TaxonDescription desc: descriptions){
                        //TODO use description delete configurator ?
                        //FIXME check if description is ALWAYS deletable
                    	if (desc.getDescribedSpecimenOrObservation() != null){
                    		String message = "Taxon can't be deleted as it is used in a TaxonDescription" +
                    				" which also describes specimens or abservations";
                            throw new ReferencedObjectUndeletableException(message);
                        }
                        descriptionService.delete(desc);
                        taxon.removeDescription(desc);
                    }
            }


            //check references with only reverse mapping
        String message = checkForReferences(taxon);
		if (message != null){
			throw new ReferencedObjectUndeletableException(message.toString());
		}
	         
		 if (! config.isDeleteTaxonNodes() || (!config.isDeleteInAllClassifications() && classification == null )){
                if (taxon.getTaxonNodes().size() > 0){
                    message = "Taxon can't be deleted as it is used in a classification node. Remove taxon from all classifications prior to deletion or define a classification where it should be deleted or adapt the taxon deletion configurator.";
                    throw new ReferencedObjectUndeletableException(message);
                }
            }else{
            	if (taxon.getTaxonNodes().size() != 0){
	            	Set<TaxonNode> nodes = taxon.getTaxonNodes();
	            	Iterator<TaxonNode> iterator = nodes.iterator();
	            	TaxonNode node = null;
	            	boolean deleteChildren;
	        		if (config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.DELETE)){
	        			deleteChildren = true;
	        		}else {
	        			deleteChildren = false;
	        		}
	        		boolean success = true;
	            	if (!config.isDeleteInAllClassifications() && !(classification == null)){
	            		while (iterator.hasNext()){
		            		node = iterator.next();
		            		if (node.getClassification().equals(classification)){
		            			break;
		            		}
		            		node = null;
		            	}
	            		if (node != null){
	            			success =taxon.removeTaxonNode(node, deleteChildren);
	            		} else {
	            			message = "Taxon is not used in defined classification";
	            			throw new DataChangeNoRollbackException(message);
	            		}
	            	} else if (config.isDeleteInAllClassifications()){
	            		List<TaxonNode> nodesList = new ArrayList<TaxonNode>();
		            	nodesList.addAll(taxon.getTaxonNodes());
		            	
			            	for (TaxonNode taxonNode: nodesList){
			            		if(deleteChildren){
			            			Object[] childNodes = taxonNode.getChildNodes().toArray();
			            			for (Object childNode: childNodes){
			            				TaxonNode childNodeCast = (TaxonNode) childNode;
			            				deleteTaxon(childNodeCast.getTaxon(), config, classification);
			            				
			            			}
			            			
			            			/*for (TaxonNode childNode: taxonNode.getChildNodes()){
				            			deleteTaxon(childNode.getTaxon(), config, classification);
				            			
				            		}*/
				            		//taxon.removeTaxonNode(taxonNode);
			            		} else{
			            			Object[] childNodes = taxonNode.getChildNodes().toArray();
			            			for (Object childNode: childNodes){
			            				TaxonNode childNodeCast = (TaxonNode) childNode;
			            				taxonNode.getParent().addChildNode(childNodeCast, childNodeCast.getReference(), childNodeCast.getMicroReference());
			            			}
			            			
			            			//taxon.removeTaxonNode(taxonNode);
			            		}
			            	}
		            	
	            		
	            		
	            		nodeService.deleteTaxonNodes(nodesList);
		            	
	            	}
	            	if (!success){
	            		 message = "The taxon node could not be deleted.";
	            		throw new DataChangeNoRollbackException(message);
	            	}
            	}
            }
            //TaxonNameBase
            if (config.isDeleteNameIfPossible()){
                try {
                	
                	//TaxonNameBase name = nameService.find(taxon.getName().getUuid());
                	TaxonNameBase name = (TaxonNameBase)HibernateProxyHelper.deproxy(taxon.getName());
                	//check whether taxon will be deleted or not
                	if (taxon.getTaxonNodes() == null || taxon.getTaxonNodes().size()== 0){
                		taxon = (Taxon) HibernateProxyHelper.deproxy(taxon);
                		name.removeTaxonBase(taxon);
                	    nameService.save(name);
                		nameService.delete(name, config.getNameDeletionConfig());
                	}
                } catch (ReferencedObjectUndeletableException e) {
                    //do nothing
                    if (logger.isDebugEnabled()){logger.debug("Name could not be deleted");}
                    
                }
            }
            
//        	TaxonDescription
           /* Set<TaxonDescription> descriptions = taxon.getDescriptions();

            for (TaxonDescription desc: descriptions){
                if (config.isDeleteDescriptions()){
                    //TODO use description delete configurator ?
                    //FIXME check if description is ALWAYS deletable
                	taxon.removeDescription(desc);
                    descriptionService.delete(desc);
                }else{
                    if (desc.getDescribedSpecimenOrObservations().size()>0){
                        String message = "Taxon can't be deleted as it is used in a TaxonDescription" +
                                " which also describes specimens or observations";
                            throw new ReferencedObjectUndeletableException(message);
    }
                    }
                }*/
            
           

            if (taxon.getTaxonNodes() == null || taxon.getTaxonNodes().size()== 0){
            	dao.delete(taxon);
            	return taxon.getUuid();
            } else{
            	message = "Taxon can't be deleted as it is used in another Taxonnode";
            	throw new ReferencedObjectUndeletableException(message);
            }
            

    }
    
    private String checkForReferences(Taxon taxon){
    	Set<CdmBase> referencingObjects = genericDao.getReferencingObjects(taxon);
        for (CdmBase referencingObject : referencingObjects){
            //IIdentificationKeys (Media, Polytomous, MultiAccess)
            if (HibernateProxyHelper.isInstanceOf(referencingObject, IIdentificationKey.class)){
                String message = "Taxon" + taxon.getTitleCache() + "can't be deleted as it is used in an identification key. Remove from identification key prior to deleting this name";
              
                return message;
            }


            //PolytomousKeyNode
            if (referencingObject.isInstanceOf(PolytomousKeyNode.class)){
                String message = "Taxon" + taxon.getTitleCache() + " can't be deleted as it is used in polytomous key node";
                return message;
            }

            //TaxonInteraction
            if (referencingObject.isInstanceOf(TaxonInteraction.class)){
                String message = "Taxon can't be deleted as it is used in taxonInteraction#taxon2";
                return message;
            }
        }
        referencingObjects = null;
        return null;
    }
    
    @Transactional(readOnly = false)
    public UUID delete(Synonym syn){
    	UUID result = syn.getUuid();
    	this.deleteSynonym(syn, null);
    	return result;
    }
    
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
	public void deleteSynonym(Synonym synonym, SynonymDeletionConfigurator config) {
    	deleteSynonym(synonym, null, config);
		
	}
    

	/* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
    public void deleteSynonym(Synonym synonym, Taxon taxon, SynonymDeletionConfigurator config) {
        if (synonym == null){
            return;
        }
        if (config == null){
        	config = new SynonymDeletionConfigurator();
        }
        synonym = CdmBase.deproxy(dao.merge(synonym), Synonym.class);

        //remove synonymRelationship
        Set<Taxon> taxonSet = new HashSet<Taxon>();
        if (taxon != null){
            taxonSet.add(taxon);
        }else{
            taxonSet.addAll(synonym.getAcceptedTaxa());
        }
        for (Taxon relatedTaxon : taxonSet){
//			dao.deleteSynonymRelationships(synonym, relatedTaxon);
            relatedTaxon.removeSynonym(synonym, config.isNewHomotypicGroupIfNeeded());
        }
        this.saveOrUpdate(synonym);

        //TODO remove name from homotypical group?

        //remove synonym (if necessary)
        
        
        if (synonym.getSynonymRelations().isEmpty()){
            TaxonNameBase<?,?> name = synonym.getName();
            synonym.setName(null);
            dao.delete(synonym);

            //remove name if possible (and required)
            if (name != null && config.isDeleteNameIfPossible()){
                try{
                    nameService.delete(name, config.getNameDeletionConfig());
                }catch (ReferencedObjectUndeletableException ex){
                	System.err.println("Name wasn't deleted as it is referenced");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Name wasn't deleted as it is referenced");
                    }
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNameIds(java.util.List)
     */
    @Override
    public List<TaxonNameBase> findIdenticalTaxonNameIds(List<String> propertyPath) {

        return this.dao.findIdenticalNamesNew(propertyPath);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getPhylumName(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    @Override
    public String getPhylumName(TaxonNameBase name){
        return this.dao.getPhylumName(name);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    @Override
    public long deleteSynonymRelationships(Synonym syn, Taxon taxon) {
        return dao.deleteSynonymRelationships(syn, taxon);
    }

/* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym)
     */
    @Override
    public long deleteSynonymRelationships(Synonym syn) {
        return dao.deleteSynonymRelationships(syn, null);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listSynonymRelationships(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction)
     */
    @Override
    public List<SynonymRelationship> listSynonymRelationships(
            TaxonBase taxonBase, SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths, Direction direction) {
        Integer numberOfResults = dao.countSynonymRelationships(taxonBase, type, direction);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonymRelationships(taxonBase, type, pageSize, pageNumber, orderHints, propertyPaths, direction);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingTaxon(java.lang.String)
     */
    @Override
    public Taxon findBestMatchingTaxon(String taxonName) {
        MatchingTaxonConfigurator config = MatchingTaxonConfigurator.NewInstance();
        config.setTaxonNameTitle(taxonName);
        return findBestMatchingTaxon(config);
    }



    @Override
    public Taxon findBestMatchingTaxon(MatchingTaxonConfigurator config) {

        Taxon bestCandidate = null;
        try{
            // 1. search for acceptet taxa
            List<TaxonBase> taxonList = dao.findByNameTitleCache(true, false, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
            boolean bestCandidateMatchesSecUuid = false;
            boolean bestCandidateIsInClassification = false;
            int countEqualCandidates = 0;
            for(TaxonBase taxonBaseCandidate : taxonList){
                if(taxonBaseCandidate instanceof Taxon){
                    Taxon newCanditate = CdmBase.deproxy(taxonBaseCandidate, Taxon.class);
                    boolean newCandidateMatchesSecUuid = isMatchesSecUuid(newCanditate, config);
                    if (! newCandidateMatchesSecUuid && config.isOnlyMatchingSecUuid() ){
                        continue;
                    }else if(newCandidateMatchesSecUuid && ! bestCandidateMatchesSecUuid){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateMatchesSecUuid = true;
                        continue;
                    }

                    boolean newCandidateInClassification = isInClassification(newCanditate, config);
                    if (! newCandidateInClassification && config.isOnlyMatchingClassificationUuid()){
                        continue;
                    }else if (newCandidateInClassification && ! bestCandidateIsInClassification){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateIsInClassification = true;
                        continue;
                    }
                    if (bestCandidate == null){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        continue;
                    }

                }else{  //not Taxon.class
                    continue;
                }
                countEqualCandidates++;

            }
            if (bestCandidate != null){
                if(countEqualCandidates > 1){
                    logger.info(countEqualCandidates + " equally matching TaxonBases found, using first accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                } else {
                    logger.info("using accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                }
            }


            // 2. search for synonyms
            if (config.isIncludeSynonyms()){
                List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
                for(TaxonBase taxonBase : synonymList){
                    if(taxonBase instanceof Synonym){
                        Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
                        Set<Taxon> acceptetdCandidates = synonym.getAcceptedTaxa();
                        if(!acceptetdCandidates.isEmpty()){
                            bestCandidate = acceptetdCandidates.iterator().next();
                            if(acceptetdCandidates.size() == 1){
                                logger.info(acceptetdCandidates.size() + " Accepted taxa found for synonym " + taxonBase.getTitleCache() + ", using first one: " + bestCandidate.getTitleCache());
                                return bestCandidate;
                            } else {
                                logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + "for synonym " + taxonBase.getTitleCache());
                                return bestCandidate;
                            }
                            //TODO extend method: search using treeUUID, using SecUUID, first find accepted then include synonyms until a matching taxon is found
                        }
                    }
                }
            }

        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }

        return bestCandidate;
    }

    private boolean isInClassification(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configClassificationUuid = config.getClassificationUuid();
        if (configClassificationUuid == null){
            return false;
        }
        for (TaxonNode node : taxon.getTaxonNodes()){
            UUID classUuid = node.getClassification().getUuid();
            if (configClassificationUuid.equals(classUuid)){
                return true;
            }
        }
        return false;
    }

    private boolean isMatchesSecUuid(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configSecUuid = config.getSecUuid();
        if (configSecUuid == null){
            return false;
        }
        UUID taxonSecUuid = (taxon.getSec() == null)? null : taxon.getSec().getUuid();
        return configSecUuid.equals(taxonSecUuid);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingSynonym(java.lang.String)
     */
    @Override
    public Synonym findBestMatchingSynonym(String taxonName) {
        List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, taxonName, null, MatchMode.EXACT, null, 0, null, null);
        if(! synonymList.isEmpty()){
            Synonym result = CdmBase.deproxy(synonymList.iterator().next(), Synonym.class);
            if(synonymList.size() == 1){
                logger.info(synonymList.size() + " Synonym found " + result.getTitleCache() );
                return result;
            } else {
                logger.info("Several matching synonyms found. Using first: " +  result.getTitleCache());
                return result;
            }
        }
        return null;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#moveSynonymToAnotherTaxon(eu.etaxonomy.cdm.model.taxon.SynonymRelationship, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, boolean)
     */
    @Override
    public SynonymRelationship moveSynonymToAnotherTaxon(SynonymRelationship oldSynonymRelation, Taxon newTaxon, boolean moveHomotypicGroup,
            SynonymRelationshipType newSynonymRelationshipType, Reference reference, String referenceDetail, boolean keepReference) throws HomotypicalGroupChangeException {

        Synonym synonym = oldSynonymRelation.getSynonym();
        Taxon fromTaxon = oldSynonymRelation.getAcceptedTaxon();
        //TODO what if there is no name ?? Concepts may be cached (e.g. via TCS import)
        TaxonNameBase<?,?> synonymName = synonym.getName();
        TaxonNameBase<?,?> fromTaxonName = fromTaxon.getName();
        //set default relationship type
        if (newSynonymRelationshipType == null){
            newSynonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
        }
        boolean newRelTypeIsHomotypic = newSynonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());

        HomotypicalGroup homotypicGroup = synonymName.getHomotypicalGroup();
        int hgSize = homotypicGroup.getTypifiedNames().size();
        boolean isSingleInGroup = !(hgSize > 1);

        if (! isSingleInGroup){
            boolean isHomotypicToAccepted = synonymName.isHomotypic(fromTaxonName);
            boolean hasHomotypicSynonymRelatives = isHomotypicToAccepted ? hgSize > 2 : hgSize > 1;
            if (isHomotypicToAccepted){
                String message = "Synonym is in homotypic group with accepted taxon%s. First remove synonym from homotypic group of accepted taxon before moving to other taxon.";
                String homotypicRelatives = hasHomotypicSynonymRelatives ? " and other synonym(s)":"";
                message = String.format(message, homotypicRelatives);
                throw new HomotypicalGroupChangeException(message);
            }
            if (! moveHomotypicGroup){
                String message = "Synonym is in homotypic group with other synonym(s). Either move complete homotypic group or remove synonym from homotypic group prior to moving to other taxon.";
                throw new HomotypicalGroupChangeException(message);
            }
        }else{
            moveHomotypicGroup = true;  //single synonym always allows to moveCompleteGroup
        }
//        Assert.assertTrue("Synonym can only be moved with complete homotypic group", moveHomotypicGroup);

        SynonymRelationship result = null;
        //move all synonyms to new taxon
        List<Synonym> homotypicSynonyms = fromTaxon.getSynonymsInGroup(homotypicGroup);
        for (Synonym syn: homotypicSynonyms){
            Set<SynonymRelationship> synRelations = syn.getSynonymRelations();
            for (SynonymRelationship synRelation : synRelations){
                if (fromTaxon.equals(synRelation.getAcceptedTaxon())){
                    Reference<?> newReference = reference;
                    if (newReference == null && keepReference){
                        newReference = synRelation.getCitation();
                    }
                    String newRefDetail = referenceDetail;
                    if (newRefDetail == null && keepReference){
                        newRefDetail = synRelation.getCitationMicroReference();
                    }
                    SynonymRelationship newSynRelation = newTaxon.addSynonym(syn, newSynonymRelationshipType, newReference, newRefDetail);
                    fromTaxon.removeSynonymRelation(synRelation, false);
//
                    //change homotypic group of synonym if relType is 'homotypic'
//                	if (newRelTypeIsHomotypic){
//                		newTaxon.getName().getHomotypicalGroup().addTypifiedName(syn.getName());
//                	}
                    //set result
                    if (synRelation.equals(oldSynonymRelation)){
                        result = newSynRelation;
                    }
                }
            }

        }
        saveOrUpdate(newTaxon);
        //Assert that there is a result
        if (result == null){
            String message = "Old synonym relation could not be transformed into new relation. This should not happen.";
            throw new IllegalStateException(message);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheTaxon()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon() {
        return dao.getUuidAndTitleCacheTaxon();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheSynonym()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym() {
        return dao.getUuidAndTitleCacheSynonym();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findByFullText(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, boolean, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SearchResult<TaxonBase>> findByFullText(
            Class<? extends TaxonBase> clazz, String queryString,
            Classification classification, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {


        LuceneSearch luceneSearch = prepareFindByFullTextSearch(clazz, queryString, classification, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByDistribution(List<NamedArea> areaFilter, List<PresenceAbsenceTermBase<?>> statusFilter,
            Classification classification,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, ParseException {

        LuceneSearch luceneSearch = prepareByDistributionSearch(areaFilter, statusFilter, classification);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param clazz
     * @param queryString
     * @param classification
     * @param languages
     * @param highlightFragments
     * @param directorySelectClass
     * @return
     */
    protected LuceneSearch prepareFindByFullTextSearch(Class<? extends CdmBase> clazz, String queryString, Classification classification, List<Language> languages,
            boolean highlightFragments) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);

        textQuery.add(taxonBaseQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
        textQuery.add(taxonBaseQueryFactory.newDefinedTermQuery("name.rank", queryString, languages), Occur.SHOULD);

        finalQuery.add(textQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(taxonBaseQueryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    /**
     * Uses org.apache.lucene.search.join.JoinUtil for query time joining, alternatively
     * the BlockJoinQuery could be used. The latter might be more memory save but has the
     * drawback of requiring to do the join an indexing time.
     * see  http://dev.e-taxonomy.eu/trac/wiki/LuceneNotes#JoinsinLucene for more information on this.
     *
     * Joins TaxonRelationShip with Taxon depending on the direction of the given edge:
     * <ul>
     * <li>direct, everted: {@link Direction.relatedTo}: TaxonRelationShip.relatedTo.id --&gt; Taxon.id </li>
     * <li>inverse: {@link Direction.relatedFrom}:  TaxonRelationShip.relatedFrom.id --&gt; Taxon.id </li>
     * <ul>
     *
     * @param queryString
     * @param classification
     * @param languages
     * @param highlightFragments
     * @return
     * @throws IOException
     */
    protected LuceneSearch prepareFindByTaxonRelationFullTextSearch(TaxonRelationshipEdge edge, String queryString, Classification classification, List<Language> languages,
            boolean highlightFragments) throws IOException {

        String fromField;
        String queryTermField;
        String toField = "id"; // TaxonBase.uuid

        if(edge.isBidirectional()){
            throw new RuntimeException("Bidirectional joining not supported!");
        }
        if(edge.isEvers()){
            fromField = "relatedFrom.id";
            queryTermField = "relatedFrom.titleCache";
        } else if(edge.isInvers()) {
            fromField = "relatedTo.id";
            queryTermField = "relatedTo.titleCache";
        } else {
            throw new RuntimeException("Invalid direction: " + edge.getDirections());
        }

        BooleanQuery finalQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        BooleanQuery joinFromQuery = new BooleanQuery();
        joinFromQuery.add(taxonBaseQueryFactory.newTermQuery(queryTermField, queryString), Occur.MUST);
        joinFromQuery.add(taxonBaseQueryFactory.newEntityIdQuery("type.id", edge.getTaxonRelationshipType()), Occur.MUST);
        Query joinQuery = taxonBaseQueryFactory.newJoinQuery(fromField, toField, joinFromQuery, TaxonRelationship.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
        luceneSearch.setSortFields(sortFields);

        finalQuery.add(joinQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(taxonBaseQueryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }




    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaAndNamesByFullText(java.util.EnumSet, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, java.util.Set, java.util.List, boolean, java.lang.Integer, java.lang.Integer, java.util.List, java.util.Map)
     */
    @Override
    public Pager<SearchResult<TaxonBase>> findTaxaAndNamesByFullText(
            EnumSet<TaxaAndNamesSearchMode> searchModes, String queryString, Classification classification,
            Set<NamedArea> namedAreas, Set<PresenceAbsenceTermBase<?>> distributionStatus, List<Language> languages,
            boolean highlightFragments, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)
            throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        // FIXME: allow taxonomic ordering
        //  hql equivalent:  order by t.name.genusOrUninomial, case when t.name.specificEpithet like '\"%\"' then 1 else 0 end, t.name.specificEpithet, t.name.rank desc, t.name.nameCache";
        // this require building a special sort column by a special classBridge
        if(highlightFragments){
            logger.warn("findTaxaAndNamesByFullText() : fragment highlighting is " +
                    "currently not fully supported by this method and thus " +
                    "may not work with common names and misapplied names.");
        }

        // convert sets to lists
        List<NamedArea> namedAreaList = null;
        List<PresenceAbsenceTermBase<?>>distributionStatusList = null;
        if(namedAreas != null){
            namedAreaList = new ArrayList<NamedArea>(namedAreas.size());
            namedAreaList.addAll(namedAreas);
        }
        if(distributionStatus != null){
            distributionStatusList = new ArrayList<PresenceAbsenceTermBase<?>>(distributionStatus.size());
            distributionStatusList.addAll(distributionStatus);
        }

        // set default if parameter is null
        if(searchModes == null){
            searchModes = EnumSet.of(TaxaAndNamesSearchMode.doTaxa);
        }

        boolean addDistributionFilter = namedAreas != null && namedAreas.size() > 0;

        List<LuceneSearch> luceneSearches = new ArrayList<LuceneSearch>();
        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();

        /*
          ======== filtering by distribution , HOWTO ========

           - http://www.javaranch.com/journal/2009/02/filtering-a-lucene-search.html
           - http://stackoverflow.com/questions/17709256/lucene-solr-using-complex-filters -> QueryWrapperFilter
          add Filter to search as http://lucene.apache.org/core/3_6_0/api/all/org/apache/lucene/search/Filter.html
          which will be put into a FilteredQuersy  in the end ?


          3. how does it work in spatial?
          see
           - http://www.nsshutdown.com/projects/lucene/whitepaper/locallucene_v2.html
           - http://www.infoq.com/articles/LuceneSpatialSupport
           - http://www.mhaller.de/archives/156-Spatial-search-with-Lucene.html
          ------------------------------------------------------------------------

          filter strategies:
          A) use a separate distribution filter per index sub-query/search:
           - byTaxonSyonym (query TaxaonBase):
               use a join area filter (Distribution -> TaxonBase)
           - byCommonName (query DescriptionElementBase): use an area filter on
               DescriptionElementBase !!! PROBLEM !!!
               This cannot work since the distributions are different entities than the
               common names and thus these are different lucene documents.
           - byMisaplliedNames (join query TaxonRelationship -> TaxaonBase):
               use a join area filter (Distribution -> TaxonBase)

          B) use a common distribution filter for all index sub-query/searches:
           - use a common join area filter (Distribution -> TaxonBase)
           - also implement the byCommonName as join query (CommonName -> TaxonBase)
           PROBLEM in this case: we are losing the fragment highlighting for the
           common names, since the returned documents are always TaxonBases
        */

        /* The QueryFactory for creating filter queries on Distributions should
         * The query factory used for the common names query cannot be reused
         * for this case, since we want to only record the text fields which are
         * actually used in the primary query
         */
        QueryFactory distributionFilterQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(Distribution.class);

        BooleanFilter multiIndexByAreaFilter = new BooleanFilter();


        // search for taxa or synonyms
        if(searchModes.contains(TaxaAndNamesSearchMode.doTaxa) || searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)) {
            Class taxonBaseSubclass = TaxonBase.class;
            if(searchModes.contains(TaxaAndNamesSearchMode.doTaxa) && !searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)){
                taxonBaseSubclass = Taxon.class;
            } else if (!searchModes.contains(TaxaAndNamesSearchMode.doTaxa) && searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)) {
                taxonBaseSubclass = Synonym.class;
            }
            luceneSearches.add(prepareFindByFullTextSearch(taxonBaseSubclass, queryString, classification, languages, highlightFragments));
            idFieldMap.put(CdmBaseType.TAXON, "id");
            /* A) does not work!!!!
            if(addDistributionFilter){
                // in this case we need a filter which uses a join query
                // to get the TaxonBase documents for the DescriptionElementBase documents
                // which are matching the areas in question
                Query taxonAreaJoinQuery = createByDistributionJoinQuery(
                        namedAreaList,
                        distributionStatusList,
                        distributionFilterQueryFactory
                        );
                multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);
            }
            */
            if(addDistributionFilter && searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)){
                // add additional area filter for synonyms
                String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index
                String toField = "accTaxon.id"; // id in TaxonBase index

                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);

                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);
                multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);

            }
        }

        // search by CommonTaxonName
        if(searchModes.contains(TaxaAndNamesSearchMode.doTaxaByCommonNames)) {
            // B)
            QueryFactory descriptionElementQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(DescriptionElementBase.class);
            Query byCommonNameJoinQuery = descriptionElementQueryFactory.newJoinQuery(
                    "inDescription.taxon.id",
                    "id",
                    QueryFactory.addTypeRestriction(
                                createByDescriptionElementFullTextQuery(queryString, classification, null, languages, descriptionElementQueryFactory)
                                , CommonTaxonName.class
                                ),
                    CommonTaxonName.class);
            logger.debug("byCommonNameJoinQuery: " + byCommonNameJoinQuery.toString());
            LuceneSearch byCommonNameSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);
            byCommonNameSearch.setCdmTypRestriction(Taxon.class);
            byCommonNameSearch.setQuery(byCommonNameJoinQuery);
            idFieldMap.put(CdmBaseType.TAXON, "id");

            luceneSearches.add(byCommonNameSearch);

            /* A) does not work!!!!
            luceneSearches.add(
                    prepareByDescriptionElementFullTextSearch(CommonTaxonName.class,
                            queryString, classification, null, languages, highlightFragments)
                        );
            idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");
            if(addDistributionFilter){
                // in this case we are able to use DescriptionElementBase documents
                // which are matching the areas in question directly
                BooleanQuery byDistributionQuery = createByDistributionQuery(
                        namedAreaList,
                        distributionStatusList,
                        distributionFilterQueryFactory
                        );
                multiIndexByAreaFilter.add(new QueryWrapperFilter(byDistributionQuery), Occur.SHOULD);
            } */
        }

        // search by misapplied names
        if(searchModes.contains(TaxaAndNamesSearchMode.doMisappliedNames)) {
            // NOTE:
            // prepareFindByTaxonRelationFullTextSearch() is making use of JoinUtil.createJoinQuery()
            // which allows doing query time joins
            // finds the misapplied name (Taxon B) which is an misapplication for
            // a related Taxon A.
            //
            luceneSearches.add(prepareFindByTaxonRelationFullTextSearch(
                    new TaxonRelationshipEdge(TaxonRelationshipType.MISAPPLIED_NAME_FOR(), Direction.relatedTo),
                    queryString, classification, languages, highlightFragments));
            idFieldMap.put(CdmBaseType.TAXON, "id");

            if(addDistributionFilter){
                String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index

                /*
                 * Here i was facing wired and nasty bug which took me bugging be really for hours until I found this solution.
                 * Maybe this is a but in java itself java.
                 *
                 * When the string toField is constructed by using the expression TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString()
                 * directly:
                 *
                 *    String toField = "relation." + TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString() +".to.id";
                 *
                 * The byDistributionQuery fails, however when the uuid is first stored in another string variable the query
                 * will execute as expected:
                 *
                 *    String misappliedNameForUuid = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString();
                 *    String toField = "relation." + misappliedNameForUuid +".to.id";
                 *
                 * Comparing both strings by the String.equals method returns true, so both String are identical.
                 *
                 * The bug occurs when running eu.etaxonomy.cdm.api.service.TaxonServiceSearchTest in eclipse and in maven and seems to to be
                 * dependent from a specific jvm (openjdk6  6b27-1.12.6-1ubuntu0.13.04.2, openjdk7 7u25-2.3.10-1ubuntu0.13.04.2,  oracle jdk1.7.0_25 tested)
                 * The bug is persistent after a reboot of the development computer.
                 */
//                String misappliedNameForUuid = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString();
//                String toField = "relation." + misappliedNameForUuid +".to.id";
                String toField = "relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id";
//                System.out.println("relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id".equals("relation." + misappliedNameForUuid +".to.id") ? " > identical" : " > different");
//                System.out.println("relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id".equals("relation." + TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString() +".to.id") ? " > identical" : " > different");

                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);
                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);
                QueryWrapperFilter filter = new QueryWrapperFilter(taxonAreaJoinQuery);

//                debug code for bug described above
                DocIdSet filterMatchSet = filter.getDocIdSet(luceneIndexToolProvider.getIndexReaderFor(Taxon.class));
//                System.err.println(DocIdBitSetPrinter.docsAsString(filterMatchSet, 100));

                multiIndexByAreaFilter.add(filter, Occur.SHOULD);
            }
        }

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneIndexToolProvider,
                luceneSearches.toArray(new LuceneSearch[luceneSearches.size()]));


        if(addDistributionFilter){

            // B)
            // in this case we need a filter which uses a join query
            // to get the TaxonBase documents for the DescriptionElementBase documents
            // which are matching the areas in question
            //
            // for toTaxa, doByCommonName
            Query taxonAreaJoinQuery = createByDistributionJoinQuery(
                    namedAreaList,
                    distributionStatusList,
                    distributionFilterQueryFactory
                    );
            multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);
        }

        if (addDistributionFilter){
            multiSearch.setFilter(multiIndexByAreaFilter);
        }
        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());


        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param namedAreaList at least one area must be in the list
     * @param distributionStatusList optional
     * @return
     * @throws IOException
     */
    protected Query createByDistributionJoinQuery(
            List<NamedArea> namedAreaList,
            List<PresenceAbsenceTermBase<?>> distributionStatusList,
            QueryFactory queryFactory
            ) throws IOException {

        String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index
        String toField = "id"; // id in TaxonBase index

        BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, queryFactory);

        Query taxonAreaJoinQuery = queryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);

        return taxonAreaJoinQuery;
    }

    /**
     * @param namedAreaList
     * @param distributionStatusList
     * @param queryFactory
     * @return
     */
    private BooleanQuery createByDistributionQuery(List<NamedArea> namedAreaList,
            List<PresenceAbsenceTermBase<?>> distributionStatusList, QueryFactory queryFactory) {
        BooleanQuery areaQuery = new BooleanQuery();
        // area field from Distribution
        areaQuery.add(queryFactory.newEntityIdsQuery("area.id", namedAreaList), Occur.MUST);

        // status field from Distribution
        if(distributionStatusList != null && distributionStatusList.size() > 0){
            areaQuery.add(queryFactory.newEntityIdsQuery("status.id", distributionStatusList), Occur.MUST);
        }

        logger.debug("createByDistributionQuery() query: " + areaQuery.toString());
        return areaQuery;
    }

    /**
     * This method has been primarily created for testing the area join query but might
     * also be useful in other situations
     *
     * @param namedAreaList
     * @param distributionStatusList
     * @param classification
     * @param highlightFragments
     * @return
     * @throws IOException
     */
    protected LuceneSearch prepareByDistributionSearch(
            List<NamedArea> namedAreaList, List<PresenceAbsenceTermBase<?>> distributionStatusList,
            Classification classification) throws IOException {

        BooleanQuery finalQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);

        // FIXME is this query factory using the wrong type?
        QueryFactory taxonQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(Taxon.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING, false)};
        luceneSearch.setSortFields(sortFields);


        Query byAreaQuery = createByDistributionJoinQuery(namedAreaList, distributionStatusList, taxonQueryFactory);

        finalQuery.add(byAreaQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(taxonQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }

        logger.info("prepareByAreaSearch() query: " + finalQuery.toString());
        luceneSearch.setQuery(finalQuery);

        return luceneSearch;
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findByDescriptionElementFullText(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, java.util.List, boolean, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SearchResult<TaxonBase>> findByDescriptionElementFullText(
            Class<? extends DescriptionElementBase> clazz, String queryString,
            Classification classification, List<Feature> features, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {


        LuceneSearch luceneSearch = prepareByDescriptionElementFullTextSearch(clazz, queryString, classification, features, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);

    }


    @Override
    public Pager<SearchResult<TaxonBase>> findByEverythingFullText(String queryString,
            Classification classification, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        LuceneSearch luceneSearchByDescriptionElement = prepareByDescriptionElementFullTextSearch(null, queryString, classification, null, languages, highlightFragments);
        LuceneSearch luceneSearchByTaxonBase = prepareFindByFullTextSearch(null, queryString, classification, languages, highlightFragments);

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneIndexToolProvider, luceneSearchByDescriptionElement, luceneSearchByTaxonBase);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);

    }


    /**
     * @param clazz
     * @param queryString
     * @param classification
     * @param features
     * @param languages
     * @param highlightFragments
     * @param directorySelectClass
     * @return
     */
    protected LuceneSearch prepareByDescriptionElementFullTextSearch(Class<? extends CdmBase> clazz,
            String queryString, Classification classification, List<Feature> features,
            List<Language> languages, boolean highlightFragments) {

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, DescriptionElementBase.class);
        QueryFactory descriptionElementQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(DescriptionElementBase.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("inDescription.taxon.titleCache__sort", SortField.STRING, false)};

        BooleanQuery finalQuery = createByDescriptionElementFullTextQuery(queryString, classification, features,
                languages, descriptionElementQueryFactory);

        luceneSearch.setSortFields(sortFields);
        luceneSearch.setCdmTypRestriction(clazz);
        luceneSearch.setQuery(finalQuery);
        if(highlightFragments){
            luceneSearch.setHighlightFields(descriptionElementQueryFactory.getTextFieldNamesAsArray());
        }

        return luceneSearch;
    }

    /**
     * @param queryString
     * @param classification
     * @param features
     * @param languages
     * @param descriptionElementQueryFactory
     * @return
     */
    private BooleanQuery createByDescriptionElementFullTextQuery(String queryString, Classification classification,
            List<Feature> features, List<Language> languages, QueryFactory descriptionElementQueryFactory) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();
        textQuery.add(descriptionElementQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);

        // common name
        Query nameQuery;
        if(languages == null || languages.size() == 0){
            nameQuery = descriptionElementQueryFactory.newTermQuery("name", queryString);
        } else {
            nameQuery = new BooleanQuery();
            BooleanQuery languageSubQuery = new BooleanQuery();
            for(Language lang : languages){
                languageSubQuery.add(descriptionElementQueryFactory.newTermQuery("language.uuid",  lang.getUuid().toString(), false), Occur.SHOULD);
            }
            ((BooleanQuery) nameQuery).add(descriptionElementQueryFactory.newTermQuery("name", queryString), Occur.MUST);
            ((BooleanQuery) nameQuery).add(languageSubQuery, Occur.MUST);
        }
        textQuery.add(nameQuery, Occur.SHOULD);


        // text field from TextData
        textQuery.add(descriptionElementQueryFactory.newMultilanguageTextQuery("text", queryString, languages), Occur.SHOULD);

        // --- TermBase fields - by representation ----
        // state field from CategoricalData
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.state", queryString, languages), Occur.SHOULD);

        // state field from CategoricalData
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.modifyingText", queryString, languages), Occur.SHOULD);

        // area field from Distribution
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("area", queryString, languages), Occur.SHOULD);

        // status field from Distribution
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("status", queryString, languages), Occur.SHOULD);

        finalQuery.add(textQuery, Occur.MUST);
        // --- classification ----

        if(classification != null){
            finalQuery.add(descriptionElementQueryFactory.newEntityIdQuery("inDescription.taxon.taxonNodes.classification.id", classification), Occur.MUST);
        }

        // --- IdentifieableEntity fields - by uuid
        if(features != null && features.size() > 0 ){
            finalQuery.add(descriptionElementQueryFactory.newEntityUuidsQuery("feature.uuid", features), Occur.MUST);
        }

        // the description must be associated with a taxon
        finalQuery.add(descriptionElementQueryFactory.newIsNotNullQuery("inDescription.taxon.id"), Occur.MUST);

        logger.info("prepareByDescriptionElementFullTextSearch() query: " + finalQuery.toString());
        return finalQuery;
    }

    /**
     * DefinedTerm representations and MultilanguageString maps are stored in the Lucene index by the {@link DefinedTermBaseClassBridge}
     * and {@link MultilanguageTextFieldBridge } in a consistent way. One field per language and also in one additional field for all languages.
     * This method is a convenient means to retrieve a Lucene query string for such the fields.
     *
     * @param name name of the term field as in the Lucene index. Must be field created by {@link DefinedTermBaseClassBridge}
     * or {@link MultilanguageTextFieldBridge }
     * @param languages the languages to search for exclusively. Can be <code>null</code> to search in all languages
     * @param stringBuilder a StringBuilder to be reused, if <code>null</code> a new StringBuilder will be instantiated and is returned
     * @return the StringBuilder given a parameter or a new one if the stringBuilder parameter was null.
     *
     * TODO move to utiliy class !!!!!!!!
     */
    private StringBuilder appendLocalizedFieldQuery(String name, List<Language> languages, StringBuilder stringBuilder) {

        if(stringBuilder == null){
            stringBuilder = new StringBuilder();
        }
        if(languages == null || languages.size() == 0){
            stringBuilder.append(name + ".ALL:(%1$s) ");
        } else {
            for(Language lang : languages){
                stringBuilder.append(name + "." + lang.getUuid().toString() + ":(%1$s) ");
            }
        }
        return stringBuilder;
    }

    @Override
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification, SynonymRelationshipType type, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<Synonym>();

        HashMap <UUID, ZoologicalName> zooHashMap = new HashMap<UUID, ZoologicalName>();


        UUID nameUuid= taxon.getName().getUuid();
        ZoologicalName taxonName = getZoologicalName(nameUuid, zooHashMap);
        String epithetOfTaxon = null;
        String infragenericEpithetOfTaxon = null;
        String infraspecificEpithetOfTaxon = null;
        if (taxonName.isSpecies()){
             epithetOfTaxon= taxonName.getSpecificEpithet();
        } else if (taxonName.isInfraGeneric()){
            infragenericEpithetOfTaxon = taxonName.getInfraGenericEpithet();
        } else if (taxonName.isInfraSpecific()){
            infraspecificEpithetOfTaxon = taxonName.getInfraSpecificEpithet();
        }
        String genusOfTaxon = taxonName.getGenusOrUninomial();
        Set<TaxonNode> nodes = taxon.getTaxonNodes();
         List<String> taxonNames = new ArrayList<String>();

        for (TaxonNode node: nodes){
           // HashMap<String, String> synonymsGenus = new HashMap<String, String>(); // Changed this to be able to store the idInSource to a genusName
           // List<String> synonymsEpithet = new ArrayList<String>();

            if (node.getClassification().equals(classification)){
                if (!node.isTopmostNode()){
                    TaxonNode parent = node.getParent();
                    parent = (TaxonNode)HibernateProxyHelper.deproxy(parent);
                    TaxonNameBase<?,?> parentName =  parent.getTaxon().getName();
                    ZoologicalName zooParentName = HibernateProxyHelper.deproxy(parentName, ZoologicalName.class);
                    Taxon parentTaxon = (Taxon)HibernateProxyHelper.deproxy(parent.getTaxon());
                    Rank rankOfTaxon = taxonName.getRank();


                    //create inferred synonyms for species, subspecies
                    if ((parentName.isGenus() || parentName.isSpecies() || parentName.getRank().equals(Rank.SUBGENUS())) ){

                        Synonym inferredEpithet = null;
                        Synonym inferredGenus = null;
                        Synonym potentialCombination = null;

                        List<String> propertyPaths = new ArrayList<String>();
                        propertyPaths.add("synonym");
                        propertyPaths.add("synonym.name");
                        List<OrderHint> orderHints = new ArrayList<OrderHint>();
                        orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

                        List<SynonymRelationship> synonymRelationshipsOfParent = dao.getSynonyms(parentTaxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);
                        List<SynonymRelationship> synonymRelationshipsOfTaxon= dao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);

                        List<TaxonRelationship> taxonRelListParent = null;
                        List<TaxonRelationship> taxonRelListTaxon = null;
                        if (doWithMisappliedNames){
                            taxonRelListParent = dao.getTaxonRelationships(parentTaxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, orderHints, propertyPaths, Direction.relatedTo);
                            taxonRelListTaxon = dao.getTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, orderHints, propertyPaths, Direction.relatedTo);
                        }


                        if (type.equals(SynonymRelationshipType.INFERRED_EPITHET_OF())){


                            for (SynonymRelationship synonymRelationOfParent:synonymRelationshipsOfParent){
                                Synonym syn = synonymRelationOfParent.getSynonym();

                                inferredEpithet = createInferredEpithets(taxon,
                                        zooHashMap, taxonName, epithetOfTaxon,
                                        infragenericEpithetOfTaxon,
                                        infraspecificEpithetOfTaxon,
                                        taxonNames, parentName,
                                        syn);


                                inferredSynonyms.add(inferredEpithet);
                                zooHashMap.put(inferredEpithet.getName().getUuid(), (ZoologicalName)inferredEpithet.getName());
                                taxonNames.add(((ZoologicalName)inferredEpithet.getName()).getNameCache());
                            }

                            if (doWithMisappliedNames){

                                for (TaxonRelationship taxonRelationship: taxonRelListParent){
                                     Taxon misappliedName = taxonRelationship.getFromTaxon();

                                     inferredEpithet = createInferredEpithets(taxon,
                                             zooHashMap, taxonName, epithetOfTaxon,
                                             infragenericEpithetOfTaxon,
                                             infraspecificEpithetOfTaxon,
                                             taxonNames, parentName,
                                             misappliedName);

                                    inferredSynonyms.add(inferredEpithet);
                                    zooHashMap.put(inferredEpithet.getName().getUuid(), (ZoologicalName)inferredEpithet.getName());
                                     taxonNames.add(((ZoologicalName)inferredEpithet.getName()).getNameCache());
                                }
                            }

                            if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();

                                for (Synonym syn :inferredSynonyms){
                                    name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                }

                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                        }

                    }else if (type.equals(SynonymRelationshipType.INFERRED_GENUS_OF())){


                        for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){
                            TaxonNameBase synName;
                            ZoologicalName inferredSynName;

                            Synonym syn = synonymRelationOfTaxon.getSynonym();
                            inferredGenus = createInferredGenus(taxon,
                                    zooHashMap, taxonName, epithetOfTaxon,
                                    genusOfTaxon, taxonNames, zooParentName, syn);

                            inferredSynonyms.add(inferredGenus);
                            zooHashMap.put(inferredGenus.getName().getUuid(), (ZoologicalName)inferredGenus.getName());
                            taxonNames.add(( (ZoologicalName)inferredGenus.getName()).getNameCache());


                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                                Taxon misappliedName = taxonRelationship.getFromTaxon();
                                inferredGenus = createInferredGenus(taxon, zooHashMap, taxonName, infraspecificEpithetOfTaxon, genusOfTaxon, taxonNames, zooParentName,  misappliedName);

                                inferredSynonyms.add(inferredGenus);
                                zooHashMap.put(inferredGenus.getName().getUuid(), (ZoologicalName)inferredGenus.getName());
                                 taxonNames.add(( (ZoologicalName)inferredGenus.getName()).getNameCache());
                            }
                        }


                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();

                                for (Synonym syn :inferredSynonyms){
                                    name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                }

                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                        }

                    }else if (type.equals(SynonymRelationshipType.POTENTIAL_COMBINATION_OF())){

                        Reference sourceReference = null; // TODO: Determination of sourceReference is redundant
                        ZoologicalName inferredSynName;
                        //for all synonyms of the parent...
                        for (SynonymRelationship synonymRelationOfParent:synonymRelationshipsOfParent){
                            TaxonNameBase synName;
                            Synonym synParent = synonymRelationOfParent.getSynonym();
                            synName = synParent.getName();

                            HibernateProxyHelper.deproxy(synParent);

                            // Set the sourceReference
                            sourceReference = synParent.getSec();

                            // Determine the idInSource
                            String idInSourceParent = getIdInSource(synParent);

                            ZoologicalName parentSynZooName = getZoologicalName(synName.getUuid(), zooHashMap);
                            String synParentGenus = parentSynZooName.getGenusOrUninomial();
                            String synParentInfragenericName = null;
                            String synParentSpecificEpithet = null;

                            if (parentSynZooName.isInfraGeneric()){
                                synParentInfragenericName = parentSynZooName.getInfraGenericEpithet();
                            }
                            if (parentSynZooName.isSpecies()){
                                synParentSpecificEpithet = parentSynZooName.getSpecificEpithet();
                            }

                           /* if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
                                synonymsGenus.put(synGenusName, idInSource);
                            }*/

                            //for all synonyms of the taxon

                            for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){

                                Synonym syn = synonymRelationOfTaxon.getSynonym();
                                ZoologicalName zooSynName = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                potentialCombination = createPotentialCombination(idInSourceParent, parentSynZooName, zooSynName,
                                        synParentGenus,
                                        synParentInfragenericName,
                                        synParentSpecificEpithet, syn, zooHashMap);

                                taxon.addSynonym(potentialCombination, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
                                inferredSynonyms.add(potentialCombination);
                                zooHashMap.put(potentialCombination.getName().getUuid(), (ZoologicalName)potentialCombination.getName());
                                 taxonNames.add(( (ZoologicalName)potentialCombination.getName()).getNameCache());

                            }


                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship parentRelationship: taxonRelListParent){

                                TaxonNameBase misappliedParentName;

                                Taxon misappliedParent = parentRelationship.getFromTaxon();
                                misappliedParentName = misappliedParent.getName();

                                HibernateProxyHelper.deproxy(misappliedParent);

                                // Set the sourceReference
                                sourceReference = misappliedParent.getSec();

                                // Determine the idInSource
                                String idInSourceParent = getIdInSource(misappliedParent);

                                ZoologicalName parentSynZooName = getZoologicalName(misappliedParentName.getUuid(), zooHashMap);
                                String synParentGenus = parentSynZooName.getGenusOrUninomial();
                                String synParentInfragenericName = null;
                                String synParentSpecificEpithet = null;

                                if (parentSynZooName.isInfraGeneric()){
                                    synParentInfragenericName = parentSynZooName.getInfraGenericEpithet();
                                }
                                if (parentSynZooName.isSpecies()){
                                    synParentSpecificEpithet = parentSynZooName.getSpecificEpithet();
                                }


                                for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                                    Taxon misappliedName = taxonRelationship.getFromTaxon();
                                    ZoologicalName zooMisappliedName = getZoologicalName(misappliedName.getName().getUuid(), zooHashMap);
                                    potentialCombination = createPotentialCombination(
                                            idInSourceParent, parentSynZooName, zooMisappliedName,
                                            synParentGenus,
                                            synParentInfragenericName,
                                            synParentSpecificEpithet, misappliedName, zooHashMap);


                                    taxon.addSynonym(potentialCombination, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
                                    inferredSynonyms.add(potentialCombination);
                                    zooHashMap.put(potentialCombination.getName().getUuid(), (ZoologicalName)potentialCombination.getName());
                                     taxonNames.add(( (ZoologicalName)potentialCombination.getName()).getNameCache());
                                }
                            }
                        }

                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();
                                for (Synonym syn :inferredSynonyms){
                                    try{
                                        name = (ZoologicalName) syn.getName();
                                    }catch (ClassCastException e){
                                        name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    }
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                 }
                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                         }
                        }
                    }else {
                        logger.info("The synonymrelationship type is not defined.");
                        return inferredSynonyms;
                    }
                }
            }

        }

        return inferredSynonyms;
    }

    private Synonym createPotentialCombination(String idInSourceParent,
            ZoologicalName parentSynZooName, 	ZoologicalName zooSynName, String synParentGenus,
            String synParentInfragenericName, String synParentSpecificEpithet,
            TaxonBase syn, HashMap<UUID, ZoologicalName> zooHashMap) {
        Synonym potentialCombination;
        Reference sourceReference;
        ZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Set sourceReference
        sourceReference = syn.getSec();
        if (sourceReference == null){
            logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon");
            //TODO:Remove
            if (!parentSynZooName.getTaxa().isEmpty()){
                TaxonBase taxon = parentSynZooName.getTaxa().iterator().next();

                sourceReference = taxon.getSec();
            }
        }
        String synTaxonSpecificEpithet = zooSynName.getSpecificEpithet();

        String synTaxonInfraSpecificName= null;

        if (parentSynZooName.isSpecies()){
            synTaxonInfraSpecificName = zooSynName.getInfraSpecificEpithet();
        }

        /*if (epithetName != null && !synonymsEpithet.contains(epithetName)){
            synonymsEpithet.add(epithetName);
        }*/

        //create potential combinations...
        inferredSynName = ZoologicalName.NewInstance(syn.getName().getRank());

        inferredSynName.setGenusOrUninomial(synParentGenus);
        if (zooSynName.isSpecies()){
              inferredSynName.setSpecificEpithet(synTaxonSpecificEpithet);
              if (parentSynZooName.isInfraGeneric()){
                  inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
              }
        }
        if (zooSynName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synParentSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(synTaxonInfraSpecificName);
        }
        if (parentSynZooName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
        }


        potentialCombination = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        potentialCombination.setSec(sourceReference);


        // Determine the idInSource
        String idInSourceSyn= getIdInSource(syn);

        if (idInSourceParent != null && idInSourceSyn != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            potentialCombination.addSource(originalSource);
        }

        inferredSynName.generateTitle();

        return potentialCombination;
    }

    private Synonym createInferredGenus(Taxon taxon,
            HashMap<UUID, ZoologicalName> zooHashMap, ZoologicalName taxonName,
            String epithetOfTaxon, String genusOfTaxon,
            List<String> taxonNames, ZoologicalName zooParentName,
            TaxonBase syn) {

        Synonym inferredGenus;
        TaxonNameBase synName;
        ZoologicalName inferredSynName;
        synName =syn.getName();
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon = getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = syn.getSec();

        //logger.warn(sourceReference.getTitleCache());

        synName = syn.getName();
        ZoologicalName synZooName = getZoologicalName(synName.getUuid(), zooHashMap);
        String synSpeciesEpithetName = synZooName.getSpecificEpithet();
                     /* if (synonymsEpithet != null && !synonymsEpithet.contains(synSpeciesEpithetName)){
            synonymsEpithet.add(synSpeciesEpithetName);
        }*/

        inferredSynName = ZoologicalName.NewInstance(taxon.getName().getRank());
        //TODO:differ between parent is genus and taxon is species, parent is subgenus and taxon is species, parent is species and taxon is subspecies and parent is genus and taxon is subgenus...


        inferredSynName.setGenusOrUninomial(genusOfTaxon);
        if (zooParentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(zooParentName.getInfraGenericEpithet());
        }

        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(synSpeciesEpithetName);
        }
        if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(epithetOfTaxon);
            inferredSynName.setInfraSpecificEpithet(synZooName.getInfraGenericEpithet());
        }


        inferredGenus = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredGenus.setSec(sourceReference);

        // Add the original source
        if (idInSourceSyn != null && idInSourceTaxon != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;

        }else{
            logger.error("There is an idInSource missing: " + idInSourceSyn + " of Synonym or " + idInSourceTaxon + " of Taxon");
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;
        }

        taxon.addSynonym(inferredGenus, SynonymRelationshipType.INFERRED_GENUS_OF());

        inferredSynName.generateTitle();


        return inferredGenus;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            HashMap<UUID, ZoologicalName> zooHashMap, ZoologicalName taxonName,
            String epithetOfTaxon, String infragenericEpithetOfTaxon,
            String infraspecificEpithetOfTaxon, List<String> taxonNames,
            TaxonNameBase parentName, TaxonBase syn) {

        Synonym inferredEpithet;
        TaxonNameBase<?,?> synName;
        ZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference<?> sourceReference = syn.getSec();

        if (sourceReference == null){
        	 logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
             sourceReference = taxon.getSec();
        }

        synName = syn.getName();
        ZoologicalName zooSynName = getZoologicalName(synName.getUuid(), zooHashMap);
        String synGenusName = zooSynName.getGenusOrUninomial();
        String synInfraGenericEpithet = null;
        String synSpecificEpithet = null;

        if (zooSynName.getInfraGenericEpithet() != null){
            synInfraGenericEpithet = zooSynName.getInfraGenericEpithet();
        }

        if (zooSynName.isInfraSpecific()){
            synSpecificEpithet = zooSynName.getSpecificEpithet();
        }

                     /* if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
            synonymsGenus.put(synGenusName, idInSource);
        }*/

        inferredSynName = ZoologicalName.NewInstance(taxon.getName().getRank());

        // DEBUG TODO: for subgenus or subspecies the infrageneric or infraspecific epithet should be used!!!
        if (epithetOfTaxon == null && infragenericEpithetOfTaxon == null && infraspecificEpithetOfTaxon == null) {
            logger.error("This specificEpithet is NULL" + taxon.getTitleCache());
        }
        inferredSynName.setGenusOrUninomial(synGenusName);

        if (parentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synInfraGenericEpithet);
        }
        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(epithetOfTaxon);
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(infraspecificEpithetOfTaxon);
        }

        inferredEpithet = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredEpithet.setSec(sourceReference);

        /* Add the original source
        if (idInSource != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "InferredEpithetOf", syn.getSec(), null);

            // Add the citation
            Reference citation = getCitation(syn);
            if (citation != null) {
                originalSource.setCitation(citation);
                inferredEpithet.addSource(originalSource);
            }
        }*/
        String taxonId = idInSourceTaxon+ "; " + idInSourceSyn;


        IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredEpithet.addSource(originalSource);

        originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredSynName.addSource(originalSource);



        taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_EPITHET_OF());

        inferredSynName.generateTitle();
        return inferredEpithet;
    }

    /**
     * Returns an existing ZoologicalName or extends an internal hashmap if it does not exist.
     * Very likely only useful for createInferredSynonyms().
     * @param uuid
     * @param zooHashMap
     * @return
     */
    private ZoologicalName getZoologicalName(UUID uuid, HashMap <UUID, ZoologicalName> zooHashMap) {
        ZoologicalName taxonName =nameDao.findZoologicalNameByUUID(uuid);
        if (taxonName == null) {
            taxonName = zooHashMap.get(uuid);
        }
        return taxonName;
    }

    /**
     * Returns the idInSource for a given Synonym.
     * @param syn
     */
    private String getIdInSource(TaxonBase taxonBase) {
        String idInSource = null;
        Set<IdentifiableSource> sources = taxonBase.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                idInSource  = source.getIdInSource();
            }
        } else if (sources.size() > 1) {
            int count = 1;
            idInSource = "";
            for (IdentifiableSource source : sources) {
                idInSource += source.getIdInSource();
                if (count < sources.size()) {
                    idInSource += "; ";
                }
                count++;
            }
        } else if (sources.size() == 0){
            logger.warn("No idInSource for TaxonBase " + taxonBase.getUuid() + " - " + taxonBase.getTitleCache());
        }


        return idInSource;
    }


    /**
     * Returns the citation for a given Synonym.
     * @param syn
     */
    private Reference getCitation(Synonym syn) {
        Reference citation = null;
        Set<IdentifiableSource> sources = syn.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                citation = source.getCitation();
            }
        } else if (sources.size() > 1) {
            logger.warn("This Synonym has more than one source: " + syn.getUuid() + " (" + syn.getTitleCache() +")");
        }

        return citation;
    }

    @Override
    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();

        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_EPITHET_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_GENUS_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.POTENTIAL_COMBINATION_OF(), doWithMisappliedNames));

        return inferredSynonyms;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listClassifications(eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    @Override
    public List<Classification> listClassifications(TaxonBase taxonBase, Integer limit, Integer start, List<String> propertyPaths) {

        // TODO quickly implemented, create according dao !!!!
        Set<TaxonNode> nodes = new HashSet<TaxonNode>();
        Set<Classification> classifications = new HashSet<Classification>();
        List<Classification> list = new ArrayList<Classification>();

        if (taxonBase == null) {
            return list;
        }

        taxonBase = load(taxonBase.getUuid());

        if (taxonBase instanceof Taxon) {
            nodes.addAll(((Taxon)taxonBase).getTaxonNodes());
        } else {
            for (Taxon taxon : ((Synonym)taxonBase).getAcceptedTaxa() ) {
                nodes.addAll(taxon.getTaxonNodes());
            }
        }
        for (TaxonNode node : nodes) {
            classifications.add(node.getClassification());
        }
        list.addAll(classifications);
        return list;
    }






}
