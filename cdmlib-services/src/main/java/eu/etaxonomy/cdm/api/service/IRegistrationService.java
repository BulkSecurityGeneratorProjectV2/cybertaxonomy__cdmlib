/**
* Copyright (C) 2017 EDIT
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
public interface IRegistrationService extends IAnnotatableService<Registration> {

    /**
     * Returns a sublist of Registration instances stored in the database. A
     * maximum of 'limit' objects are returned, starting at object with index
     * 'start'. The bean properties specified by the parameter
     * <code>propertyPaths</code> and recursively initialized for each of the
     * entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based, can be null, equivalent of starting at the
     *            beginning of the recordset)
     * @param reference
     *            filters the Registration by the reference of the nomenclatural
     *            act for which the Registration as been created. The name and
     *            all type designations associated with the Registration are
     *            sharing the same citation. If the Optional itself is
     *            <code>null</code> the parameter is neglected. If Optional
     *            contains the value <code>null</code> all registrations with a
     *            name or type designation that has no reference are returned.
     *            Also those registrations having no name and type designation
     *            at all.
     * @param includedStatus
     *            filters the Registration by the RegistrationStatus. Only
     *            Registration having one of the supplied status will included.
     *            // * @param orderHints // * Supports path like
     *            <code>orderHints.propertyNames</code> which // * include
     *            *-to-one properties like createdBy.username or // *
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public Pager<Registration> page(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths);

    /**
     * Returns a sublist of Registration instances stored in the database. A
     * maximum of 'limit' objects are returned, starting at object with index
     * 'start'. The bean properties specified by the parameter
     * <code>propertyPaths</code> and recursively initialized for each of the
     * entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param submitter
     *            Limits the result set to Registrations having the given
     *            submitter. This filter is ignored if set to <code>null</code>.
     * @param includedStatus
     *            filters the Registration by the RegistrationStatus. Only
     *            Registration having one of the supplied status will included.
     * @param identifierFilterPattern
     *            filters the Registration by this pattern, The asterisk can be used
     *            * as wildcard in any position of the pattern string
     * @param taxonNameFilterPattern
     *            filters the registered taxon name by this pattern, The asterisk can be used
     *            * as wildcard in any position of the pattern string
     * @param typeDesignationStatus
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
     * @return
     * @throws DataAccessException
     */
    public Pager<Registration> page(User submitter, Collection<RegistrationStatus> includedStatus,
            String identifierFilterPattern, String taxonNameFilterPattern, Set<TypeDesignationStatusBase> typeDesignationStatus, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

    Pager<Registration> pageByIdentifier(String identifier, Integer pageIndex, Integer pageSize, List<String> propertyPaths) throws IOException;

    /**
     * @param submitterUuid
     * @param includedStatusUuids
     * @param identifierFilterPattern
     * @param taxonNameFilterPattern
     * @param typeDesignationStatusUuids
     * @param pageSize
     * @param pageIndex
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    Pager<Registration> page(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, Collection<UUID> typeDesignationStatusUuids, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

    // ============= functionality to be moved into a "RegistrationManagerBean" ==================

    public Registration newRegistration();

    Registration assureIsPersisted(Registration reg);

    Registration createRegistrationForName(UUID taxonNameUuid);

    boolean checkRegistrationExistsFor(TaxonName name);

    public void addTypeDesignation(Registration reg, UUID typeDesignationUuid);

    // ============================================================================================

}
