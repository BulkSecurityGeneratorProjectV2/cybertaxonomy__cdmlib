// $Id$
/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {
	
	public WaterbodyOrCountry getCountryByIso(String iso639);
	
	public List<WaterbodyOrCountry> getWaterbodyOrCountryByName(String name);
	
	/**
     * Returns a List of Media that are associated with a given occurence
     * 
	 * @param occurence the occurence associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
	public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
     * Returns a List of determinations that have been made for a given occurence
     * 
	 * @param occurence the occurence associated with these determinations
	 * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of determination instances
     */
	public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
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
	public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths);
}
