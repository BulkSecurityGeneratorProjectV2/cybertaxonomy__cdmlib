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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.SortField;
import org.hibernate.TransientObjectException;
import org.hibernate.search.spatial.impl.Rectangle;
import org.joda.time.Partial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeConfigurator;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.dto.DerivateDTO;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO.ContigFile;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO.MolecularData;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase,IOccurrenceDao> implements IOccurrenceService {

    static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);

    @Autowired
    private IDefinedTermDao definedTermDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ISequenceService sequenceService;

    @Autowired
    private AbstractBeanInitializer beanInitializer;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    public OccurrenceServiceImpl() {
        logger.debug("Load OccurrenceService Bean");
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = SpecimenOrObservationBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    /**
     * FIXME Candidate for harmonization
     * move to termService
     */
    @Override
    public Country getCountryByIso(String iso639) {
        return this.definedTermDao.getCountryByIso(iso639);

    }

    /**
     * FIXME Candidate for harmonization
     * move to termService
     */
    @Override
    public List<Country> getCountryByName(String name) {
        List<? extends DefinedTermBase> terms = this.definedTermDao.findByTitle(Country.class, name, null, null, null, null, null, null) ;
        List<Country> countries = new ArrayList<Country>();
        for (int i=0;i<terms.size();i++){
            countries.add((Country)terms.get(i));
        }
        return countries;
    }

    @Override
    @Autowired
    protected void setDao(IOccurrenceDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDerivationEvents(occurence);

        List<DerivationEvent> results = new ArrayList<DerivationEvent>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDerivationEvents(occurence, pageSize, pageNumber,propertyPaths);
        }

        return new DefaultPagerImpl<DerivationEvent>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#countDeterminations(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)
     */
    @Override
    public int countDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonbase) {
        return dao.countDeterminations(occurence, taxonbase);
    }

    @Override
    public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDeterminations(occurrence, taxonBase);

        List<DeterminationEvent> results = new ArrayList<DeterminationEvent>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDeterminations(occurrence,taxonBase, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<DeterminationEvent>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<Media> getMedia(SpecimenOrObservationBase occurence,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMedia(occurence);

        List<Media> results = new ArrayList<Media>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getMedia(occurence, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#list(java.lang.Class, eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(type,determinedAs);
        List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
        pageNumber = pageNumber == null ? 0 : pageNumber;
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            Integer start = pageSize == null ? 0 : pageSize * pageNumber;
            results = dao.list(type,determinedAs, pageSize, start, orderHints,propertyPaths);
        }
        return new DefaultPagerImpl<SpecimenOrObservationBase>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache() {
        return dao.getDerivedUnitUuidAndTitleCache();
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        return dao.getFieldUnitUuidAndTitleCache();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getDerivedUnitFacade(eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException {
        derivedUnit = (DerivedUnit)dao.load(derivedUnit.getUuid(), null);
        DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
        config.setThrowExceptionForNonSpecimenPreservationMethodRequest(false);
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit, config);
        beanInitializer.initialize(derivedUnitFacade, propertyPaths);
        return derivedUnitFacade;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listDerivedUnitFacades(eu.etaxonomy.cdm.model.description.DescriptionBase, java.util.List)
     */
    @Override
    public List<DerivedUnitFacade> listDerivedUnitFacades(
            DescriptionBase description, List<String> propertyPaths) {

        List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<DerivedUnitFacade>();
        IndividualsAssociation tempIndividualsAssociation;
        SpecimenOrObservationBase tempSpecimenOrObservationBase;
        List<DescriptionElementBase> elements = descriptionService.listDescriptionElements(description, null, IndividualsAssociation.class, null, 0, Arrays.asList(new String []{"associatedSpecimenOrObservation"}));
        for(DescriptionElementBase element : elements){
            if(element.isInstanceOf(IndividualsAssociation.class)){
                tempIndividualsAssociation = HibernateProxyHelper.deproxy(element, IndividualsAssociation.class);
                if(tempIndividualsAssociation.getAssociatedSpecimenOrObservation() != null){
                    tempSpecimenOrObservationBase = HibernateProxyHelper.deproxy(tempIndividualsAssociation.getAssociatedSpecimenOrObservation(), SpecimenOrObservationBase.class);
                    if(tempSpecimenOrObservationBase.isInstanceOf(DerivedUnit.class)){
                        try {
                            derivedUnitFacadeList.add(DerivedUnitFacade.NewInstance(HibernateProxyHelper.deproxy(tempSpecimenOrObservationBase, DerivedUnit.class)));
                        } catch (DerivedUnitFacadeNotSupportedException e) {
                            logger.warn(tempIndividualsAssociation.getAssociatedSpecimenOrObservation().getTitleCache() + " : " +e.getMessage());
                        }
                    }
                }

            }
        }

        beanInitializer.initializeAll(derivedUnitFacadeList, propertyPaths);

        return derivedUnitFacadeList;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listByAnyAssociation(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        return pageByAssociatedTaxon(type, includeRelationships, associatedTaxon, maxDepth, pageSize, pageNumber, orderHints, propertyPaths).getRecords();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listByAnyAssociation(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Collection<SpecimenOrObservationBase> listFieldUnitsByAssociatedTaxon(Taxon associatedTaxon, List<OrderHint> orderHints, List<String> propertyPaths) {
        return pageFieldUnitsByAssociatedTaxon(null, associatedTaxon, null, null, null, null, propertyPaths).getRecords();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#pageFieldUnitsByAssociatedTaxon(java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SpecimenOrObservationBase> pageFieldUnitsByAssociatedTaxon(Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        if(!getSession().contains(associatedTaxon)){
            associatedTaxon = (Taxon) taxonService.load(associatedTaxon.getUuid());
        }

        //gather the IDs of all relevant field units
        Set<Integer> fieldUnitIds = new HashSet<Integer>();
        List<SpecimenOrObservationBase> records = listByAssociatedTaxon(null, includeRelationships, associatedTaxon, maxDepth, null, null, orderHints, propertyPaths);
        for(SpecimenOrObservationBase<?> specimen:records){
            for (FieldUnit fieldUnit : getFieldUnits(specimen.getUuid())) {
                fieldUnitIds.add(fieldUnit.getId());
            }
        }
        //dao.listByIds() does the paging of the field units. Passing the field units directly to the Pager would not work
        List<SpecimenOrObservationBase> fieldUnits = dao.listByIds(fieldUnitIds, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<SpecimenOrObservationBase>(pageNumber, fieldUnitIds.size(), pageSize, fieldUnits);
    }

    @Override
    public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit, UUID associatedTaxonUuid){

        if(!getSession().contains(fieldUnit)){
            fieldUnit = (FieldUnit) load(fieldUnit.getUuid());
        }
        TaxonBase associatedTaxon = taxonService.load(associatedTaxonUuid);

        FieldUnitDTO fieldUnitDTO = new FieldUnitDTO();

        if(fieldUnit.getGatheringEvent()!=null){
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            //Country
            final NamedArea country = gatheringEvent.getCountry();
            fieldUnitDTO.setCountry(country!=null?country.getDescription():"");
            //Collection
            final AgentBase collector = gatheringEvent.getCollector();
            final String fieldNumber = fieldUnit.getFieldNumber();
            fieldUnitDTO.setCollection(((collector!=null?collector:"") + " " + (fieldNumber!=null?fieldNumber:"")).trim());
            //Date
            final Partial gatheringDate = gatheringEvent.getGatheringDate();
            fieldUnitDTO.setDate(gatheringDate!=null?gatheringDate.toString():"");
        }

        //Taxon Name
        fieldUnitDTO.setTaxonName(associatedTaxon.getName().getFullTitleCache());


        //Herbaria map
        Map<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> collectionToCountMap = new HashMap<eu.etaxonomy.cdm.model.occurrence.Collection, Integer>();
        //List of accession numbers for citation
        List<String> preservedSpecimenAccessionNumbers = new ArrayList<String>();

        //assemble preserved specimen DTOs
        Set<DerivationEvent> derivationEvents = fieldUnit.getDerivationEvents();
        for (DerivationEvent derivationEvent : derivationEvents) {
            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
            for (DerivedUnit derivedUnit : derivatives) {
                if(derivedUnit.getRecordBasis().equals(SpecimenOrObservationType.PreservedSpecimen)){
                    PreservedSpecimenDTO preservedSpecimenDTO = assemblePreservedSpecimenDTO(derivedUnit, fieldUnitDTO);
                    fieldUnitDTO.addPreservedSpecimenDTO(preservedSpecimenDTO);
                    fieldUnitDTO.setHasCharacterData(preservedSpecimenDTO.isHasCharacterData());
                    fieldUnitDTO.setHasDetailImage(preservedSpecimenDTO.isHasDetailImage());
                    fieldUnitDTO.setHasDna(preservedSpecimenDTO.isHasDna());
                    fieldUnitDTO.setHasSpecimenScan(preservedSpecimenDTO.isHasSpecimenScan());
                }
            }
        }
        //assemble derivate data DTO
        assembleDerivateDataDTO(fieldUnitDTO, fieldUnit);

        final String separator = ", ";
        //assemble citation
        String citation = "";
        citation += !fieldUnitDTO.getCountry().isEmpty()?fieldUnitDTO.getCountry()+separator:"";
        if(fieldUnit.getGatheringEvent()!=null){
            if(fieldUnit.getGatheringEvent().getLocality()!=null){
                citation += fieldUnit.getGatheringEvent().getLocality().getText();
                citation += separator;
            }
            if(fieldUnit.getGatheringEvent().getExactLocation()!=null
                    && fieldUnit.getGatheringEvent().getExactLocation().getLatitude()!=null
                    && fieldUnit.getGatheringEvent().getExactLocation().getLongitude()!=null){
                citation += fieldUnit.getGatheringEvent().getExactLocation().getLatitude().toString();
                citation += separator;
                citation += fieldUnit.getGatheringEvent().getExactLocation().getLongitude().toString();
                citation += separator;
            }
        }
        citation += !fieldUnitDTO.getCollection().isEmpty()?fieldUnitDTO.getCollection():"";
        if(!preservedSpecimenAccessionNumbers.isEmpty()){
            citation += " (";
            for(String accessionNumber:preservedSpecimenAccessionNumbers){
                if(!accessionNumber.isEmpty()){
                    citation += accessionNumber+separator;
                }
            }
            citation = removeTail(citation, separator);
            citation += ")";
        }
        citation = removeTail(citation, separator);
        fieldUnitDTO.setCitation(citation);

        //assemble herbaria string
        String herbariaString = "";
        for(Entry<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> e:collectionToCountMap.entrySet()){
            eu.etaxonomy.cdm.model.occurrence.Collection collection = e.getKey();
            if(collection.getCode()!=null){
                herbariaString += collection.getCode();
            }
            if(e.getValue()>1){
                herbariaString += "("+e.getValue()+")";
            }
            herbariaString += separator;
        }
        herbariaString = removeTail(herbariaString, separator);
        fieldUnitDTO.setHerbarium(herbariaString);

        return fieldUnitDTO;
    }


    @Override
    public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit){
        return assemblePreservedSpecimenDTO(derivedUnit, null);
    }

    public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit, FieldUnitDTO hierarchyDTO){
        PreservedSpecimenDTO preservedSpecimenDTO = new PreservedSpecimenDTO();
        preservedSpecimenDTO.setAccessionNumber(derivedUnit.getAccessionNumber());

        //character state data
        Collection<DescriptionElementBase> characterDataForSpecimen = getCharacterDataForSpecimen(derivedUnit);
        if(!characterDataForSpecimen.isEmpty()){
            if(hierarchyDTO!=null){
                hierarchyDTO.setHasCharacterData(true);
            }
        }
        for (DescriptionElementBase descriptionElementBase : characterDataForSpecimen) {
            String character = descriptionElementBase.getFeature().getLabel();
            ArrayList<Language> languages = new ArrayList<Language>(Collections.singleton(Language.DEFAULT()));
            if(descriptionElementBase instanceof QuantitativeData){
                QuantitativeData quantitativeData = (QuantitativeData)descriptionElementBase;
                DefaultQuantitativeDescriptionBuilder builder = new DefaultQuantitativeDescriptionBuilder();
                String state = builder.build(quantitativeData, languages).getText(Language.DEFAULT());
                preservedSpecimenDTO.addCharacterData(character, state);
            }
            else if(descriptionElementBase instanceof CategoricalData){
                CategoricalData categoricalData = (CategoricalData)descriptionElementBase;
                DefaultCategoricalDescriptionBuilder builder = new DefaultCategoricalDescriptionBuilder();
                String state = builder.build(categoricalData, languages).getText(Language.DEFAULT());
                preservedSpecimenDTO.addCharacterData(character, state);
            }
        }
        //check if derived unit is a type
        Collection<SpecimenTypeDesignation> specimenTypeDesignations = listTypeDesignations(derivedUnit, null, null, null, null);
        for (SpecimenTypeDesignation specimenTypeDesignation : specimenTypeDesignations) {
            if(hierarchyDTO!=null){
                hierarchyDTO.setHasType(true);
            }
            preservedSpecimenDTO.addType(specimenTypeDesignation.getTypeStatus().getLabel());
        }
        //assemble sub derivates
        preservedSpecimenDTO.setDerivateDataDTO(assembleDerivateDataDTO(preservedSpecimenDTO, derivedUnit));
        return preservedSpecimenDTO;
    }

    /**
     * @param derivedUnit
     * @param derivateDataDTO
     * @return
     */
    private DerivateDataDTO assembleDerivateDataDTO(DerivateDTO derivateDTO, SpecimenOrObservationBase<?> specimenOrObservation) {
        DerivateDataDTO derivateDataDTO = new DerivateDataDTO();
        Collection<DerivedUnit> childDerivates = getDerivedUnitsFor(specimenOrObservation);
        for (DerivedUnit childDerivate : childDerivates) {
            //current accession number
            String currentAccessionNumber = childDerivate.getAccessionNumber()!=null?childDerivate.getAccessionNumber():"";
            //current herbarium
            String currentHerbarium = "";
            eu.etaxonomy.cdm.model.occurrence.Collection collection = childDerivate.getCollection();
            if(collection!=null){
                currentHerbarium = collection.getCode()!=null?collection.getCode():"";
            }
            //assemble molecular data
            //pattern: DNAMarker [contig1, primer1_1, primer1_2, ...][contig2, primer2_1, ...]...
            if(childDerivate.isInstanceOf(DnaSample.class)){
                if(childDerivate.getRecordBasis()==SpecimenOrObservationType.TissueSample){
                    //TODO implement TissueSample assembly for web service
                }
                if(childDerivate.getRecordBasis()==SpecimenOrObservationType.DnaSample){

                    DnaSample dna = HibernateProxyHelper.deproxy(childDerivate, DnaSample.class);
                    if(!dna.getSequences().isEmpty()){
                        derivateDTO.setHasDna(true);
                    }
                    for(Sequence sequence:dna.getSequences()){
                        URI boldUri = null;
                        try {
                            boldUri = sequence.getBoldUri();
                        } catch (URISyntaxException e1) {
                            logger.error("Could not create BOLD URI", e1);
                        }
                        final DefinedTerm dnaMarker = sequence.getDnaMarker();
                        MolecularData molecularData = derivateDataDTO.addProviderLink(boldUri!=null?boldUri:null,dnaMarker!=null?dnaMarker.getLabel():"[no marker]");

                        //contig file FIXME show primer although contig not present?
                        if(sequence.getContigFile()!=null){
                            MediaRepresentationPart contigMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile());
                            if(contigMediaRepresentationPart!=null){
                                ContigFile contigFile = molecularData.addContigFile(contigMediaRepresentationPart.getUri(), "contig");
                                //primer files
                                if(sequence.getSingleReads()!=null){
                                    for (SingleRead singleRead : sequence.getSingleReads()) {
                                        MediaRepresentationPart pherogramMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(singleRead.getPherogram());
                                        if(pherogramMediaRepresentationPart!=null){
                                            contigFile.addPrimerLink(pherogramMediaRepresentationPart.getUri(), "primer");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //assemble media data
            else if(childDerivate.isInstanceOf(MediaSpecimen.class)){

                MediaSpecimen media = HibernateProxyHelper.deproxy(childDerivate, MediaSpecimen.class);
                String mediaUriString = getMediaUriString(media);
                if(media.getKindOfUnit()!=null){
                    //specimen scan
                    if(media.getKindOfUnit().getUuid().equals(UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03"))){
                        derivateDTO.setHasSpecimenScan(true);
                        final String imageLinkText = currentHerbarium+" "+currentAccessionNumber;
                        derivateDataDTO.addSpecimenScan(mediaUriString==null?"":mediaUriString, !imageLinkText.equals(" ")?imageLinkText:"[no accession]");
                    }
                    //detail image
                    else if(media.getKindOfUnit().getUuid().equals(UUID.fromString("31eb8d02-bf5d-437c-bcc6-87a626445f34"))){
                        derivateDTO.setHasDetailImage(true);
                        String motif = "";
                        if(media.getMediaSpecimen()!=null && media.getMediaSpecimen().getTitle()!=null){
                            motif = media.getMediaSpecimen().getTitle().getText();
                        }
                        derivateDataDTO.addDetailImage(mediaUriString==null?"":mediaUriString, motif!=null?motif:"[no motif]");
                    }
                }
            }
        }
        return derivateDataDTO;
    }


    /**
     * @param string
     * @param tail
     * @return
     */
    private String removeTail(String string, final String tail) {
        if(string.endsWith(tail)){
            string = string.substring(0, string.length()-tail.length());
        }
        return string;
    }

    private String getMediaUriString(MediaSpecimen mediaSpecimen){
        String mediaUri = null;
        Collection<MediaRepresentation> mediaRepresentations = mediaSpecimen.getMediaSpecimen().getRepresentations();
        if(mediaRepresentations!=null && !mediaRepresentations.isEmpty()){
            Collection<MediaRepresentationPart> mediaRepresentationParts = mediaRepresentations.iterator().next().getParts();
            if(mediaRepresentationParts!=null && !mediaRepresentationParts.isEmpty()){
                MediaRepresentationPart part = mediaRepresentationParts.iterator().next();
                if(part.getUri()!=null){
                    mediaUri = part.getUri().toASCIIString();
                }
            }
        }
        return mediaUri;
    }

    private Collection<DerivedUnit> getDerivedUnitsFor(SpecimenOrObservationBase<?> specimen){
        Collection<DerivedUnit> derivedUnits = new ArrayList<DerivedUnit>();
        for(DerivationEvent derivationEvent:specimen.getDerivationEvents()){
            for(DerivedUnit derivative:derivationEvent.getDerivatives()){
                derivedUnits.add(derivative);
                derivedUnits.addAll(getDerivedUnitsFor(derivative));
            }
        }
        return derivedUnits;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#pageByAssociatedTaxon(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Set<Taxon> taxa = new HashSet<Taxon>();
        Set<Integer> occurrenceIds = new HashSet<Integer>();
        List<T> occurrences = new ArrayList<T>();

//        Integer limit = PagerUtils.limitFor(pageSize);
//        Integer start = PagerUtils.startFor(pageSize, pageNumber);

        if(!getSession().contains(associatedTaxon)){
            associatedTaxon = (Taxon) taxonService.load(associatedTaxon.getUuid());
        }

        if(includeRelationships != null) {
            taxa = taxonService.listRelatedTaxa(associatedTaxon, includeRelationships, maxDepth, null, null, propertyPaths);
        }

        taxa.add(associatedTaxon);

        for (Taxon taxon : taxa) {
            List<T> perTaxonOccurrences = dao.listByAssociatedTaxon(type, taxon, null, null, orderHints, propertyPaths);
            for (SpecimenOrObservationBase o : perTaxonOccurrences) {
                occurrenceIds.add(o.getId());
            }
        }
        occurrences = (List<T>) dao.listByIds(occurrenceIds, pageSize, pageNumber, orderHints, propertyPaths);

        return new DefaultPagerImpl<T>(pageNumber, occurrenceIds.size(), pageSize, occurrences);

    }


    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        UUID uuid = UUID.fromString(taxonUUID);
        Taxon tax = (Taxon) taxonService.load(uuid);
       //TODO REMOVE NULL STATEMENT
        type=null;
        return pageByAssociatedTaxon( type,includeRelationships,tax, maxDepth, pageSize, pageNumber, orderHints, propertyPaths );

    }


    @Override
    public Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(
            Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle boundingBox, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {

        LuceneSearch luceneSearch = prepareByFullTextSearch(clazz, queryString, boundingBox, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.SPECIMEN_OR_OBSERVATIONBASE, "id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<SpecimenOrObservationBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;

        return new DefaultPagerImpl<SearchResult<SpecimenOrObservationBase>>(pageNumber, totalHits, pageSize,
                searchResults);

    }


    /**
     * @param clazz
     * @param queryString
     * @param languages
     * @param highlightFragments
     * @return
     */
    private LuceneSearch prepareByFullTextSearch(Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle bbox,
            List<Language> languages, boolean highlightFragments) {

        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, FieldUnit.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(FieldUnit.class);

        // --- criteria
        luceneSearch.setCdmTypRestriction(clazz);
        if(queryString != null){
            textQuery.add(queryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
            finalQuery.add(textQuery, Occur.MUST);
        }

        // --- spacial query
        if(bbox != null){
            finalQuery.add(QueryFactory.buildSpatialQueryByRange(bbox, "gatheringEvent.exactLocation.point"), Occur.MUST);
        }

        luceneSearch.setQuery(finalQuery);

        // --- sorting
        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING, false)};
        luceneSearch.setSortFields(sortFields);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getFieldUnits(eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public Collection<FieldUnit> getFieldUnits(UUID derivedUnitUuid) {
        //It will search recursively over all {@link DerivationEvent}s and get the "originals" ({@link SpecimenOrObservationBase})
        //from which this DerivedUnit was derived until all FieldUnits are found.

        //FIXME: use HQL queries to increase performance
        SpecimenOrObservationBase<?> specimen = load(derivedUnitUuid);
//        specimen = HibernateProxyHelper.deproxy(specimen, SpecimenOrObservationBase.class);
        Collection<FieldUnit> fieldUnits = new ArrayList<FieldUnit>();

        if(specimen.isInstanceOf(FieldUnit.class)){
            fieldUnits.add(HibernateProxyHelper.deproxy(specimen, FieldUnit.class));
        }
        else if(specimen.isInstanceOf(DerivedUnit.class)){
            fieldUnits.addAll(getFieldUnits(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class)));
        }
        return fieldUnits;
    }


    /**
     * @param original
     * @param fieldUnits
     */
    private Collection<FieldUnit> getFieldUnits(DerivedUnit derivedUnit) {
        Collection<FieldUnit> fieldUnits = new HashSet<FieldUnit>();
        Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
        if(originals!=null && !originals.isEmpty()){
            for(SpecimenOrObservationBase<?> original:originals){
                if(original.isInstanceOf(FieldUnit.class)){
                    fieldUnits.add(HibernateProxyHelper.deproxy(original, FieldUnit.class));
                }
                else if(original.isInstanceOf(DerivedUnit.class)){
                    fieldUnits.addAll(getFieldUnits(HibernateProxyHelper.deproxy(original, DerivedUnit.class)));
                }
            }
        }
        return fieldUnits;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#moveSequence(eu.etaxonomy.cdm.model.molecular.DnaSample, eu.etaxonomy.cdm.model.molecular.DnaSample, eu.etaxonomy.cdm.model.molecular.Sequence)
     */
    @Override
    public boolean moveSequence(DnaSample from, DnaSample to, Sequence sequence) {
        //reload specimens to avoid session conflicts
        from = (DnaSample) load(from.getUuid());
        to = (DnaSample) load(to.getUuid());
        sequence = sequenceService.load(sequence.getUuid());

        if(from==null || to==null || sequence==null){
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
                    "Operation was move "+sequence+ " from "+from+" to "+to);
        }
        from.removeSequence(sequence);
        saveOrUpdate(from);
        to.addSequence(sequence);
        saveOrUpdate(to);
        return true;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#moveDerivate(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to, DerivedUnit derivate) {
        //reload specimens to avoid session conflicts
        from = load(from.getUuid());
        to = load(to.getUuid());
        derivate = (DerivedUnit) load(derivate.getUuid());

        if(from==null || to==null || derivate==null){
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
            		"Operation was move "+derivate+ " from "+from+" to "+to);
        }

        SpecimenOrObservationType derivateType = derivate.getRecordBasis();
        SpecimenOrObservationType toType = to.getRecordBasis();
        //check if type is a sub derivate type
        if(toType==SpecimenOrObservationType.FieldUnit //moving to FieldUnit always works
                || derivateType==SpecimenOrObservationType.Media //moving media always works
                || (derivateType.isKindOf(toType) && toType!=derivateType)){ //moving only to parent derivate type
            //remove derivation event from parent specimen of dragged object
            DerivationEvent eventToRemove = null;
            for(DerivationEvent event:from.getDerivationEvents()){
                if(event.getDerivatives().contains(derivate)){
                    eventToRemove = event;
                    break;
                }
            }
            from.removeDerivationEvent(eventToRemove);
            saveOrUpdate(from);
            //add new derivation event to target
            DerivationEvent derivedFromNewOriginalEvent = DerivationEvent.NewSimpleInstance(to, derivate, eventToRemove==null?null:eventToRemove.getType());
            to.addDerivationEvent(derivedFromNewOriginalEvent);
            derivate.setDerivedFrom(derivedFromNewOriginalEvent);
            saveOrUpdate(to);
            return true;
        }
        return false;
    }

    @Override
    public Collection<ICdmBase> getNonCascadedAssociatedElements(SpecimenOrObservationBase<?> specimen){
        //potential fields that are not persisted cascadingly
        /*
         * SOOB
        -DescriptionBase
        -determinations
        --modifier TERM
        -kindOfUnit TERM
        -lifeStage TERM
        -sex TERM

        FieldUnit
        -GatheringEvent
        --Country TERM
        --CollectingAreas TERM

        DerivedUnit
        -collection
        --institute
        ---types TERM
        -preservationMethod
        --medium TERM
        -storedUnder CDM TaxonNameBase
        */

        Collection<ICdmBase> nonCascadedCdmEntities = new HashSet<ICdmBase>();

        //Choose the correct entry point to traverse the graph (FieldUnit or DerivedUnit)

        //FieldUnit
        if(specimen.isInstanceOf(FieldUnit.class)){
            nonCascadedCdmEntities.addAll(getFieldUnitNonCascadedAssociatedElements(HibernateProxyHelper.deproxy(specimen, FieldUnit.class)));
        }
        //DerivedUnit
        else if(specimen.isInstanceOf(DerivedUnit.class)){
            DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(specimen, DerivedUnit.class);
            if(derivedUnit.getDerivedFrom()!=null){
                Collection<FieldUnit> fieldUnits = getFieldUnits(derivedUnit);
                for(FieldUnit fieldUnit:fieldUnits){
                    nonCascadedCdmEntities.addAll(getFieldUnitNonCascadedAssociatedElements(fieldUnit));
                }
            }
        }
        return nonCascadedCdmEntities;
    }

    private Collection<ICdmBase> getFieldUnitNonCascadedAssociatedElements(FieldUnit fieldUnit){
        //get non cascaded element on SpecimenOrObservationBase level
        Collection<ICdmBase> nonCascadedCdmEntities = getSpecimenOrObservationNonCascadedAssociatedElements(fieldUnit);

        //get FieldUnit specific elements
        GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
        if(gatheringEvent!=null){
            //country
            if(gatheringEvent.getCountry()!=null){
                nonCascadedCdmEntities.add(gatheringEvent.getCountry());
            }
            //collecting areas
            for (NamedArea namedArea : gatheringEvent.getCollectingAreas()) {
                nonCascadedCdmEntities.add(namedArea);
            }
        }
        for (DerivationEvent derivationEvent : fieldUnit.getDerivationEvents()) {
            for (DerivedUnit derivedUnit : derivationEvent.getDerivatives()) {
                nonCascadedCdmEntities.addAll(getDerivedUnitNonCascadedAssociatedElements(derivedUnit));
            }
        }
        return nonCascadedCdmEntities;
    }

    private Collection<ICdmBase> getDerivedUnitNonCascadedAssociatedElements(DerivedUnit derivedUnit){
        //get non cascaded element on SpecimenOrObservationBase level
        Collection<ICdmBase> nonCascadedCdmEntities = getSpecimenOrObservationNonCascadedAssociatedElements(derivedUnit);

        //get DerivedUnit specific elements
        if(derivedUnit.getCollection()!=null && derivedUnit.getCollection().getInstitute()!=null){
            for (DefinedTerm type : derivedUnit.getCollection().getInstitute().getTypes()) {
                nonCascadedCdmEntities.add(type);
            }
        }
        if(derivedUnit.getPreservation()!=null && derivedUnit.getPreservation().getMedium()!=null){
            nonCascadedCdmEntities.add(derivedUnit.getPreservation().getMedium());
        }
        if(derivedUnit.getStoredUnder()!=null){
            nonCascadedCdmEntities.add(derivedUnit.getStoredUnder());
        }
        return nonCascadedCdmEntities;
    }

    /**
     * @param specimen
     * @return
     */
    private Collection<ICdmBase> getSpecimenOrObservationNonCascadedAssociatedElements(
            SpecimenOrObservationBase<?> specimen) {
        Collection<ICdmBase> nonCascadedCdmEntities = new HashSet<ICdmBase>();
        //scan SpecimenOrObservationBase
        for(DeterminationEvent determinationEvent:specimen.getDeterminations()){
            //modifier
            if(determinationEvent.getModifier()!=null){
                nonCascadedCdmEntities.add(determinationEvent.getModifier());
            }
        }
        //kindOfUnit
        if(specimen.getKindOfUnit()!=null){
            nonCascadedCdmEntities.add(specimen.getKindOfUnit());
        }
        //lifeStage
        if(specimen.getLifeStage()!=null){
            nonCascadedCdmEntities.add(specimen.getLifeStage());
        }
        //sex
        if(specimen.getSex()!=null){
            nonCascadedCdmEntities.add(specimen.getSex());
        }
        return nonCascadedCdmEntities;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.VersionableServiceBase#isDeletable(eu.etaxonomy.cdm.model.common.VersionableEntity, eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase)
     */
    @Override
    public DeleteResult isDeletable(SpecimenOrObservationBase specimen, DeleteConfiguratorBase config) {
        DeleteResult deleteResult = new DeleteResult();
        SpecimenDeleteConfigurator specimenDeleteConfigurator = (SpecimenDeleteConfigurator)config;

        //check elements found by super method
        Set<CdmBase> relatedObjects = super.isDeletable(specimen, config).getRelatedObjects();
        for (CdmBase cdmBase : relatedObjects) {
            //check for type designation
            if(cdmBase.isInstanceOf(SpecimenTypeDesignation.class) && !specimenDeleteConfigurator.isDeleteFromTypeDesignation()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen is a type specimen."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            //check for IndividualsAssociations
            else if(cdmBase.isInstanceOf(IndividualsAssociation.class) && !specimenDeleteConfigurator.isDeleteFromIndividualsAssociation()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen is still associated via IndividualsAssociations"));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            //check for specimen/taxon description
            else if((cdmBase.isInstanceOf(SpecimenDescription.class) || cdmBase.isInstanceOf(TaxonDescription.class))
                    && !specimenDeleteConfigurator.isDeleteFromDescription()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen is still used in a Description."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            //check for children and parents (derivation events)
            else if(cdmBase.isInstanceOf(DerivationEvent.class)){
                DerivationEvent derivationEvent = HibernateProxyHelper.deproxy(cdmBase, DerivationEvent.class);
                //check if derivation event is empty
                if(!derivationEvent.getDerivatives().isEmpty()){
                    if(derivationEvent.getDerivatives().size()==1 && derivationEvent.getDerivatives().contains(specimen)){
                        //if it is the parent event with only one derivate then the specimen is still deletable
                        continue;
                    }
                    else if(!specimenDeleteConfigurator.isDeleteChildren()){
                        //if not and children should not be deleted then it is undeletable
                        deleteResult.setAbort();
                        deleteResult.addException(new ReferencedObjectUndeletableException("Derivate still has child derivates."));
                        deleteResult.addRelatedObject(cdmBase);
                        break;
                    }
                    else{
                        //check all children if they can be deleted
                        Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
                        DeleteResult childResult = new DeleteResult();
                        for (DerivedUnit derivedUnit : derivatives) {
                            childResult.includeResult(isDeletable(derivedUnit, specimenDeleteConfigurator));
                        }
                        if(!childResult.isOk()){
                            deleteResult.setAbort();
                            deleteResult.includeResult(childResult);
                            deleteResult.addRelatedObject(cdmBase);
                            break;
                        }
                    }
                }
            }
            //check for amplification
            else if(cdmBase.isInstanceOf(AmplificationResult.class) && !specimenDeleteConfigurator.isDeleteMolecularData()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("DnaSample is used in amplification results."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            //check for sequence
            else if(cdmBase.isInstanceOf(Sequence.class) && !specimenDeleteConfigurator.isDeleteMolecularData()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("DnaSample is used in sequences."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
        }
        if(deleteResult.isOk()){
            //add all related object if deletion is OK so they can be handled by the delete() method
            deleteResult.addRelatedObjects(relatedObjects);
        }
        return deleteResult;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#delete(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator)
     */
    @Override
    public DeleteResult delete(SpecimenOrObservationBase<?> specimen, SpecimenDeleteConfigurator config) {
        specimen = HibernateProxyHelper.deproxy(specimen, SpecimenOrObservationBase.class);

        if(config.isDeleteChildren()){
            Set<DerivationEvent> derivationEvents = specimen.getDerivationEvents();
            for (DerivationEvent derivationEvent : derivationEvents) {
                Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
                for (DerivedUnit derivedUnit : derivatives) {
                    delete(derivedUnit, config);
                }
            }
        }

        DeleteResult deleteResult = isDeletable(specimen, config);
        if(!deleteResult.isOk()){
            return deleteResult;
        }

        //check related objects
        Set<CdmBase> relatedObjects = deleteResult.getRelatedObjects();

        for (CdmBase relatedObject : relatedObjects) {
            //check for TypeDesignations
            if(relatedObject.isInstanceOf(SpecimenTypeDesignation.class)){
                SpecimenTypeDesignation designation = HibernateProxyHelper.deproxy(relatedObject, SpecimenTypeDesignation.class);
                designation.setTypeSpecimen(null);
                Set<TaxonNameBase> typifiedNames = designation.getTypifiedNames();
                for (TaxonNameBase taxonNameBase : typifiedNames) {
                    taxonNameBase.removeTypeDesignation(designation);
                }
            }
            //delete IndividualsAssociation
            if(relatedObject.isInstanceOf(IndividualsAssociation.class)){
                IndividualsAssociation assciation = HibernateProxyHelper.deproxy(relatedObject, IndividualsAssociation.class);
                assciation.setAssociatedSpecimenOrObservation(null);
                assciation.getInDescription().removeElement(assciation);
            }
            //check for taxon description
            if(relatedObject.isInstanceOf(TaxonDescription.class)){
                TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(relatedObject, TaxonDescription.class);
                taxonDescription.setDescribedSpecimenOrObservation(null);
            }
            //check for specimen description
            if(relatedObject.isInstanceOf(SpecimenDescription.class)){
                SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(relatedObject, SpecimenDescription.class);
                //check if specimen is "described" specimen
                if(specimenDescription.getDescribedSpecimenOrObservation().equals(specimen)){
                    specimenDescription.setDescribedSpecimenOrObservation(null);
                }
                //check if description is a description of the given specimen
                if(specimen.getDescriptions().contains(specimenDescription)){
                    specimen.removeDescription(specimenDescription);
                }
            }
            //check for amplification
            if(relatedObject.isInstanceOf(AmplificationResult.class)){
                AmplificationResult amplificationResult = HibernateProxyHelper.deproxy(relatedObject, AmplificationResult.class);
                amplificationResult.getDnaSample().removeAmplificationResult(amplificationResult);
            }
            //check for sequence
            if(relatedObject.isInstanceOf(Sequence.class)){
                Sequence sequence = HibernateProxyHelper.deproxy(relatedObject, Sequence.class);
                sequence.getDnaSample().removeSequence(sequence);
            }
            //check for children and parents (derivation events)
            if(relatedObject.isInstanceOf(DerivationEvent.class)){
                DerivationEvent derivationEvent = HibernateProxyHelper.deproxy(relatedObject, DerivationEvent.class);
                //parent derivation event (derivedFrom)
                if(derivationEvent.getDerivatives().contains(specimen) && specimen.isInstanceOf(DerivedUnit.class)){
                    derivationEvent.removeDerivative(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class));
                    if(derivationEvent.getDerivatives().isEmpty()){
                        Set<SpecimenOrObservationBase> originals = derivationEvent.getOriginals();
                        for (SpecimenOrObservationBase specimenOrObservationBase : originals) {
                            specimenOrObservationBase.removeDerivationEvent(derivationEvent);
                        }
                    }
                }
                else{
                    //child derivation events should not occur since we delete the hierarchy from bottom to top
                }
            }
        }

        deleteResult.includeResult(delete(specimen));
        return deleteResult;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#deleteDerivateHierarchy(eu.etaxonomy.cdm.model.common.ICdmBase)
     */
    @Override
    public DeleteResult deleteDerivateHierarchy(CdmBase from, SpecimenDeleteConfigurator config) {
        DeleteResult deleteResult = new DeleteResult();
        if(from.isInstanceOf(Sequence.class)){
            if(!config.isDeleteMolecularData()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("deleting molecur data is not allowed in config"));
                return deleteResult;
            }
            Sequence sequence = HibernateProxyHelper.deproxy(from, Sequence.class);
            sequence.getDnaSample().removeSequence(sequence);
            deleteResult = sequenceService.delete(sequence);
        }
        else if(from.isInstanceOf(SingleRead.class))  {
            if(!config.isDeleteMolecularData()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("deleting molecur data is not allowed in config"));
                return deleteResult;
            }
            SingleRead singleRead = HibernateProxyHelper.deproxy(from, SingleRead.class);
            singleRead.getAmplificationResult().removeSingleRead(singleRead);
            deleteResult.setStatus(Status.OK);
        }
        else if(from.isInstanceOf(SpecimenOrObservationBase.class))  {
            deleteResult = delete(HibernateProxyHelper.deproxy(from, SpecimenOrObservationBase.class), config);
        }
        return deleteResult;
    }

//    private DeleteResult deepDelete(SpecimenOrObservationBase<?> entity, SpecimenDeleteConfigurator config){
//        Set<DerivationEvent> derivationEvents = entity.getDerivationEvents();
//        for (DerivationEvent derivationEvent : derivationEvents) {
//            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
//            for (DerivedUnit derivedUnit : derivatives) {
//                DeleteResult deleteResult = deepDelete(derivedUnit, config);
//                if(!deleteResult.isOk()){
//                    return deleteResult;
//                }
//            }
//        }
//        return delete(entity, config);
//    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listAssociatedTaxa(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase)
     */
    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listIndividualsAssociations(specimen, null, null, null, null);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listTypeDesignations(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listTypeDesignations(specimen, limit, start, orderHints, propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listDescriptionsWithDescriptionSpecimen(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(
            SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return dao.listDescriptionsWithDescriptionSpecimen(specimen, limit, start, orderHints, propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getStatesForSpecimen(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase)
     */
    @Override
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(SpecimenOrObservationBase<?> specimen) {
        Collection<DescriptionElementBase> states = new ArrayList<DescriptionElementBase>();
        if(specimen!=null){
            Set<DescriptionBase> descriptions = specimen.getDescriptions();
            for (DescriptionBase<?> descriptionBase : descriptions) {
                if(descriptionBase.isInstanceOf(SpecimenDescription.class)){
                    SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(descriptionBase, SpecimenDescription.class);
                    Set<DescriptionElementBase> elements = specimenDescription.getElements();
                    for (DescriptionElementBase descriptionElementBase : elements) {
                        if(descriptionElementBase.getFeature().isSupportsCategoricalData()
                                ||descriptionElementBase.getFeature().isSupportsQuantitativeData()){
                            states.add(descriptionElementBase);
                        }
                    }
                }
            }
        }
        return states;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getStatesForSpecimen(java.util.UUID)
     */
    @Override
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(UUID specimenUuid) {
        SpecimenOrObservationBase<?> specimen = load(specimenUuid);
        if(specimen!=null){
            return getCharacterDataForSpecimen(specimen);
        }
        else{
            throw new DataRetrievalFailureException("Specimen with the given uuid not found in the data base");
        }
    }

}
