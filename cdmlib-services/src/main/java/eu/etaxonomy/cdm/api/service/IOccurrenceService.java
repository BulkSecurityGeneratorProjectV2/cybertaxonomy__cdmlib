// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.spatial.impl.Rectangle;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.dto.DerivateHierarchyDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {

    public Country getCountryByIso(String iso639);

    public List<Country> getCountryByName(String name);

    /**
     * Returns a paged list of occurrences that have been determined to belong
     * to the taxon concept determinedAs, optionally restricted to objects
     * belonging to a class that that extends SpecimenOrObservationBase.
     * <p>
     * In contrast to {@link #listByAnyAssociation(Class, Taxon, List)} this
     * method only takes SpecimenOrObservationBase instances into account which
     * are actually determined as the taxon specified by
     * <code>determinedAs</code>.
     *
     * @param type
     *            The type of entities to return (can be null to count all
     *            entities of type <T>)
     * @param determinedAs
     *            the taxon concept that the occurrences have been determined to
     *            belong to
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based, can be null, equivalent of starting at the
     *            beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to be initialized
     * @return
     */
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a List of Media that are associated with a given occurence
     *
     * @param occurence the occurence associated with these media
     * @param pageSize The maximum number of media returned (can be null for all related media)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
    public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of determinations that have been made for a given occurence and for a given taxon concept
     *
     * @param occurence the occurence associated with these determinations (can be null for all occurrences)
     * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @return a count of determination events
     */
    public int countDeterminations(SpecimenOrObservationBase occurence,TaxonBase taxonbase);

    /**
     * Returns a List of determinations that have been made for a given occurence
     *
     * @param occurence the occurence associated with these determinations (can be null for all occurrences)
     * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of determination instances
     */
    public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonBase, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a list of derivation events that have involved creating new DerivedUnits from this occurence
     *
     * @param occurence the occurence that was a source of these derivation events
     * @param pageSize The maximum number of derivation events returned (can be null for all related derivation events)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of derivation events
     */
    public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a Paged List of SpecimenOrObservationBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
     *
     * @param clazz filter the results by class (or pass null to return all SpecimenOrObservationBase instances)
     * @param queryString
     * @param pageSize The maximum number of occurrences returned (can be null for all matching occurrences)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a Pager SpecimenOrObservationBase instances
     * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
     */
    @Override
    public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths);

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link FieldUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache();

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link DerivedUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache();

    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException;

    public List<DerivedUnitFacade> listDerivedUnitFacades(DescriptionBase description, List<String> propertyPaths);

    /**
     * Lists all instances of {@link SpecimenOrObservationBase} which are
     * associated with the <code>taxon</code> specified as parameter.
     * SpecimenOrObservationBase instances can be associated to taxa in multiple
     * ways, all these possible relations are taken into account:
     * <ul>
     * <li>The {@link IndividualsAssociation} elements in a
     * {@link TaxonDescription} contain {@link DerivedUnit}s</li>
     * <li>{@link SpecimenTypeDesignation}s may be associated with any
     * {@link HomotypicalGroup} related to the specific {@link Taxon}.</li>
     * <li>A {@link Taxon} may be referenced by the {@link DeterminationEvent}
     * of the {@link SpecimenOrObservationBase}</li>
     * </ul>
     * Further more there also can be taxa which are associated with the taxon
     * in question (parameter associatedTaxon) by {@link TaxonRelationship}s. If
     * the parameter <code>includeRelationships</code> is containing elements,
     * these according {@TaxonRelationshipType}s and
     * directional information will be used to collect further
     * {@link SpecimenOrObservationBase} instances found this way.
     *
     * @param <T>
     * @param type
     * @param associatedTaxon
     * @param Set<TaxonRelationshipVector> includeRelationships. TaxonRelationships will not be taken into account if this is <code>NULL</code>.
     * @param maxDepth TODO
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Lists all instances of {@link FieldUnit} which are
     * associated <b>directly or indirectly</b>with the <code>taxon</code> specified
     * as parameter. "Indirectly" means that a sub derivate of the FieldUnit is
     * directly associated with the given taxon.
     * SpecimenOrObservationBase instances can be associated to taxa in multiple
     * ways, all these possible relations are taken into account:
     * <ul>
     * <li>The {@link IndividualsAssociation} elements in a
     * {@link TaxonDescription} contain {@link DerivedUnit}s</li>
     * <li>{@link SpecimenTypeDesignation}s may be associated with any
     * {@link HomotypicalGroup} related to the specific {@link Taxon}.</li>
     * <li>A {@link Taxon} may be referenced by the {@link DeterminationEvent}
     * of the {@link SpecimenOrObservationBase}</li>
     * </ul>
     * Further more there also can be taxa which are associated with the taxon
     * in question (parameter associatedTaxon) by {@link TaxonRelationship}s. If
     * the parameter <code>includeRelationships</code> is containing elements,
     * these according {@TaxonRelationshipType}s and
     * directional information will be used to collect further
     * {@link SpecimenOrObservationBase} instances found this way.
     *
     * @param <T>
     * @param type
     * @param associatedTaxon
     * @param Set<TaxonRelationshipVector> includeRelationships. TaxonRelationships will not be taken into account if this is <code>NULL</code>.
     * @param maxDepth TODO
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public Collection<FieldUnit> listFieldUnitsByAssociatedTaxon(Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * See {@link #listByAssociatedTaxon(Class, Set, Taxon, Integer, Integer, Integer, List, List)}
     *
     * @param type
     * @param includeRelationships
     * @param associatedTaxon
     * @param maxDepth
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return a Pager
     */
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link FieldUnit}s for the {@link SpecimenOrObservationBase} with the given {@link UUID}.<br>
     * @param specimenUuid the UUID of the specimen
     * @return either a collection of FieldUnits this specimen was derived from, the FieldUnit itself
     * if this was a FieldUnit or an empty collection if no FieldUnits were found
     */
    public Collection<FieldUnit> getFieldUnits(UUID specimenUuid);

    /**
     * @param clazz
     * @param queryString
     * @param languages
     * @param highlightFragments
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(Class<? extends SpecimenOrObservationBase> clazz,
            String queryString, Rectangle boundingBox, List<Language> languages, boolean highlightFragments, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException,
            IOException, ParseException;
    /**
     * See {@link #listByAssociatedTaxon(Class, Set, String, Integer, Integer, Integer, List, List)}
     *
     * @param type
     * @param includeRelationships
     * @param associatedTaxon
     * @param maxDepth
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return a Pager
     */
    public <T extends SpecimenOrObservationBase> Pager<T>  pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Moves the given {@link Sequence} from one {@link DnaSample} to another
     * @param from the DnaSample from which the sequence will be removed
     * @param to the DnaSample which to which the sequence will be added
     * @param sequence the Sequence to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public boolean moveSequence(DnaSample from, DnaSample to, Sequence sequence);

    /**
     * Moves the given {@link DerivedUnit} from one {@link SpecimenOrObservationBase} to another.
     * @param from the SpecimenOrObservationBase from which the DerivedUnit will be removed
     * @param to the SpecimenOrObservationBase to which the DerivedUnit will be added
     * @param derivate the DerivedUnit to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to, DerivedUnit derivate);

    /**
     * Assembles a {@link DerivateHierarchyDTO} for the given field unit uuid which is associated to the {@link Taxon}.<br>
     * <br>
     * For the meaning of "associated" see also {@link #listFieldUnitsByAssociatedTaxon(Set, Taxon, Integer, Integer, Integer, List, List)}
     * @param fieldUnit
     * @param associatedTaxonUuid
     * @return
     */
    public DerivateHierarchyDTO assembleDerivateHierarchyDTO(FieldUnit fieldUnit, UUID associatedTaxonUuid);

}
