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


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 *
 */
/**
 * @author a.kohlbecker
 * @date 23.03.2009
 *
 * @param <T>
 */
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public interface IService<T extends CdmBase>{

	// FIXME what does this method do?
//	@Transactional(readOnly=false)
	public void clear();

	
	/**
	 * Returns a count of all entities of type <T>  optionally restricted
	 * to objects belonging to a class that that extends <T>
	 * 
	 * @param clazz the class of entities to be counted (can be null to count all entities of type <T>)
	 * @return a count of entities
	 */
	public int count(Class<? extends T> clazz);
	
	/**
	 * Delete an existing persistent object
	 * 
	 * @param persistentObject the object to be deleted
	 * @return the unique identifier of the deleted entity
	 */
	@Transactional(readOnly=false)
	public UUID delete(T persistentObject);
	
	/**
	 * Returns true if an entity of type <T> with a unique identifier matching the 
	 * identifier supplied exists in the database, or false if no such entity can be 
	 * found. 
	 * @param uuid the unique identifier of the entity required
	 * @return an entity of type <T> matching the uuid, or null if that entity does not exist
	 */
	public boolean exists(UUID uuid);
	
	/**
	 * Return a list of persisted entities that match the unique identifier
	 * set supplied as an argument
	 * 
	 * @param uuidSet the set of unique identifiers of the entities required
	 * @return a list of entities of type <T>
	 */
	public List<T> find(Set<UUID> uuidSet);
	
	/**
	 * Return a persisted entity that matches the unique identifier
	 * supplied as an argument, or null if the entity does not exist
	 * 
	 * @param uuid the unique identifier of the entity required
	 * @return an entity of type <T>, or null if the entity does not exist
	 */
	public T find(UUID uuid);
	
	// FIXME should we expose this method?
	public Session getSession();
	
	/**
     * Returns a sublist of objects matching the grouping projections supplied using the groups parameter
     * 
     * It would be nice to be able to return a pager, but for the moment hibernate doesn't
     * seem to support this (HHH-3238 - impossible to get the rowcount for a criteria that has projections)
     * 
     * @param clazz Restrict the query to objects of a certain class, or null for all objects of type T or subclasses
     * @param limit the maximum number of entities returned (can be null to return
	 *            all entities)
     * @param start The (0-based) offset from the start of the recordset (can be null, equivalent of starting at the beginning of the recordset)
     * @param groups The grouping objects representing a projection, plus an optional ordering on that projected property
     * @param propertyPaths paths initialized on the returned objects - only applied to the objects returned from the first grouping
     * @return a list of arrays of objects, each matching the grouping objects supplied in the parameters.
     */
	public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths);
	
	/**
	 * Returns a list of entities of type <T> optionally restricted
	 * to objects belonging to a class that that extends <T>
	 * 
	 * @param type  The type of entities to return (can be null to count all entities of type <T>)
	 * @param limit The maximum number of objects returned (can be null for all matching objects)
	 * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return
	 */
	public List<T> list(Class<? extends T> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Finds the cdm entity specified by the <code>uuid</code> parameter and
	 * initializes all its *ToOne relations.
	 * 
	 * @param uuid
	 * @return
	 */
	public T load(UUID uuid);
	
	/**
	 * Finds the cdm entity specified by the <code>uuid</code> parameter and
	 * recursively initializes all bean properties given in the
	 * <code>propertyPaths</code> parameter.
	 * <p>
	 * For detailed description and examples <b>please refer to:</b> 
	 * {@link BeanInitializer#initialize(Object, List)}
	 * 
	 * @param uuid
	 * @return
	 */
	public T load(UUID uuid, List<String> propertyPaths);
	
	/**
	 * Copy the state of the given object onto the persistent object with the same identifier.
	 * 
	 * @param transientObject the entity to be merged
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID merge(T transientObject);
	
	/**
	 * Returns a paged list of entities of type <T> optionally restricted
	 * to objects belonging to a class that that extends <T>
	 * 
	 * @param type  The type of entities to return (can be null to count all entities of type <T>)
	 * @param pageSize The maximum number of objects returned (can be null for all matching objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based, 
	 *                   can be null, equivalent of starting at the beginning of the recordset)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a pager of objects of type <T>
	 */
	public Pager<T> page(Class<? extends T> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Re-read the state of the given instance from the underlying database.
	 * 
	 * Hibernate claims that it is inadvisable to use refresh in long-running-sessions. 
	 * I don't really see where we would get into a situation where problems as discussed
	 * this forum thread would apply for our scenario 
	 * 
	 * http://forum.hibernate.org/viewtopic.php?t=974544 
	 * 
	 * @param persistentObject the object to be refreshed
	 * @return the unique identifier
	 */
	@Transactional(readOnly=false)
	public UUID refresh(T persistentObject);
	
	public List<T> rows(String tableName, int limit, int start);

	/**
	 * Save a collection containing new entities (persists the entities)
	 * @param newInstances the new entities to be persisted
	 * @return A Map containing the new entities, keyed using the generated UUID's
	 *         of those entities
	 */
	@Transactional(readOnly=false)
	public Map<UUID,T> save(Collection<T> newInstances);	
	
	/**
	 * Save a new entity (persists the entity)
	 * @param newInstance the new entity to be persisted
	 * @return A generated UUID for the new persistent entity
	 */
	@Transactional(readOnly=false)
	public UUID save(T newInstance);
	
	/**
	 * Save a new entity or update the persistent state of an existing 
	 * transient entity that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID saveOrUpdate(T transientObject);
	
    /**
	 * Update the persistent state of an existing transient entity 
	 * that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID update(T transientObject);
}