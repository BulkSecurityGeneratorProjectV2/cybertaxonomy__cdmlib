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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.idminter.IdentifierMinter.Identifier;
import eu.etaxonomy.cdm.api.service.idminter.RegistrationIdentifierMinter;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.taxonGraph.ITaxonGraphService;
import eu.etaxonomy.cdm.api.utility.UserHelper;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
@Service
@Transactional(readOnly = true)
public class RegistrationServiceImpl extends AnnotatableServiceBase<Registration, IRegistrationDao>
    implements IRegistrationService {

    /**
     * {@inheritDoc}
     */
    @Autowired
    @Override
    protected void setDao(IRegistrationDao dao) {
        this.dao = dao;
    }

    @Autowired(required=false)
    private RegistrationIdentifierMinter minter;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonGraphService taxonGraphService;




    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> page(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths) {

        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            includedStatus = Arrays.asList(RegistrationStatus.PUBLISHED);
        }

        long numberOfResults = dao.count(reference, includedStatus);

        List<Registration> results = new ArrayList<>();
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(reference, includedStatus, limitStart[0], limitStart[1], propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> page(User submitter, Collection<RegistrationStatus> includedStatus,
            String identifierFilterPattern, String taxonNameFilterPattern, Set<TypeDesignationStatusBase> typeDesignationStatus,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<Restriction<? extends Object>> restrictions = new ArrayList<>();

        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            includedStatus = Arrays.asList(RegistrationStatus.PUBLISHED);
        }

        if(submitter != null){
            restrictions.add(new Restriction<>("submitter", MatchMode.EXACT, submitter));
        }
        if(includedStatus != null && !includedStatus.isEmpty()){
            restrictions.add(new Restriction<>("status", MatchMode.EXACT, includedStatus.toArray(new RegistrationStatus[includedStatus.size()])));
        }
        if(identifierFilterPattern != null){
            restrictions.add(new Restriction<>("identifier", MatchMode.LIKE, identifierFilterPattern));
        }
        if(taxonNameFilterPattern != null){
            restrictions.add(new Restriction<>("name.titleCache", MatchMode.LIKE, taxonNameFilterPattern));
        }
        if(typeDesignationStatus != null){
            restrictions.add(new Restriction<>("typeDesignations.typeStatus", null, typeDesignationStatus.toArray(new TypeDesignationStatusBase[typeDesignationStatus.size()])));
        }

        long numberOfResults = dao.count(Registration.class, restrictions);

        List<Registration> results = new ArrayList<>();
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(Registration.class, restrictions, limitStart[0], limitStart[1], orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }

    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> page(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String identifierFilterPattern, String taxonNameFilterPattern, Collection<UUID> typeDesignationStatusUuids,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<Restriction<? extends Object>> restrictions = new ArrayList<>();

        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            includedStatus = Arrays.asList(RegistrationStatus.PUBLISHED);
        }

        if(submitterUuid != null){
            restrictions.add(new Restriction<>("submitter.uuid", null, submitterUuid));
        }
        if(includedStatus != null && !includedStatus.isEmpty()){
            restrictions.add(new Restriction<>("status", null, includedStatus.toArray(new RegistrationStatus[includedStatus.size()])));
        }
        if(identifierFilterPattern != null){
            restrictions.add(new Restriction<>("identifier", MatchMode.LIKE, identifierFilterPattern));
        }
        if(taxonNameFilterPattern != null){
            restrictions.add(new Restriction<>("name.titleCache", MatchMode.LIKE, taxonNameFilterPattern));
        }
        if(typeDesignationStatusUuids != null){
            restrictions.add(new Restriction<>("typeDesignations.typeStatus.uuid", null, typeDesignationStatusUuids.toArray(new UUID[typeDesignationStatusUuids.size()])));
        }

        long numberOfResults = dao.count(Registration.class, restrictions);

        List<Registration> results = new ArrayList<>();
        if(pageIndex == null){
            pageIndex = 0;
        }
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(Registration.class, restrictions, limitStart[0], limitStart[1], orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }

    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> pageTaxomicInclusion(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String taxonNameFilterPattern, MatchMode matchMode,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<TaxonName> includedNames = taxonGraphService.listIncludedNames(taxonNameFilterPattern, matchMode);
        Set<UUID> includedNamesUuids = includedNames.stream().map(TaxonName::getUuid).collect(Collectors.toSet());

        if(includedNames.size() > 0){
            return page(submitterUuid, includedStatus, includedNamesUuids, pageSize, pageIndex, orderHints, propertyPaths);
        } else {
            return new DefaultPagerImpl<>(pageIndex, 0l, pageSize, new ArrayList<Registration>());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> page(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            Collection<UUID> taxonNameUUIDs,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<Restriction<? extends Object>> restrictions = new ArrayList<>();

        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            includedStatus = Arrays.asList(RegistrationStatus.PUBLISHED);
        }

        if(submitterUuid != null){
            restrictions.add(new Restriction<>("submitter.uuid", null, submitterUuid));
        }
        if(includedStatus != null && !includedStatus.isEmpty()){
            restrictions.add(new Restriction<>("status", null, includedStatus.toArray(new RegistrationStatus[includedStatus.size()])));
        }

        if(taxonNameUUIDs != null){
            restrictions.add(new Restriction<>("name.uuid", null , taxonNameUUIDs.toArray(new UUID[taxonNameUUIDs.size()])));
        }

        long numberOfResults = dao.count(Registration.class, restrictions);

        List<Registration> results = new ArrayList<>();
        if(pageIndex == null){
            pageIndex = 0;
        }
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(Registration.class, restrictions, limitStart[0], limitStart[1], orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }

    /**
     * @param identifier
     * @param validateUniqueness
     * @param response
     * @return
     * @throws IOException
     */
    @Override
    @Transactional(readOnly = true)
    public Pager<Registration> pageByIdentifier(String identifier, Integer pageIndex,  Integer pageSize, List<String> propertyPaths) throws IOException {

        List<Restriction<?>> restrictions = new ArrayList<>();
        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            restrictions.add(new Restriction<>("status", null, RegistrationStatus.PUBLISHED));
        }

        Pager<Registration> regPager = pageByRestrictions(Registration.class, "identifier", identifier, MatchMode.EXACT,
                restrictions, pageSize, pageIndex, null, propertyPaths);


        return regPager;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, RegistrationStatus> statusByIdentifier(String identifier) throws IOException {

        Pager<Registration> regPager = pageByRestrictions(Registration.class, "identifier", identifier, MatchMode.EXACT,
                null, null, null, null, Arrays.asList("status"));

        Map<UUID, RegistrationStatus> map = new HashMap<>();
        for(Registration reg : regPager.getRecords()){
            map.put(reg.getUuid(), reg.getStatus());
        }

        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registration save(Registration newInstance) {
        return assureIsPersisted(newInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID saveOrUpdate(Registration transientObject) {
        transientObject = assureIsPersisted(transientObject);
        return super.saveOrUpdate(transientObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Registration> save(Collection<Registration> newInstances) {
        Map<UUID, Registration> regs = new HashMap<>();
        for(Registration newInstance : newInstances) {
            Registration reg = save(newInstance);
            regs.put(reg.getUuid(), reg);
        }
        return regs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Registration> saveOrUpdate(Collection<Registration> transientInstances) {
        Map<UUID, Registration> regs = new HashMap<>();
        for(Registration transientInstance : transientInstances) {
            UUID uuid = saveOrUpdate(transientInstance);
            regs.put(uuid, transientInstance);
        }
        return regs;
    }

    // ============= functionality to be moved into a "RegistrationManagerBean" ==================


    /**
     * Factory Method
     * TODO move into RegistrationFactory
     *
     * @return a new Registration instance with submitter set to the current authentications principal
     */
    @Override
    public Registration newRegistration() {

        Registration reg = Registration.NewInstance(
                null,
                null,
                null,
                null);
        Authentication authentication = userHelper.getAuthentication();
        reg.setSubmitter((User)authentication.getPrincipal());
        return reg;
    }

    /**
     * @param taxonNameId
     * @return
     */
    @Override
    @Transactional(readOnly=false)
    public Registration createRegistrationForName(UUID taxonNameUuid) {

        Registration reg = Registration.NewInstance(
                null,
                null,
                taxonNameUuid != null ? nameService.load(taxonNameUuid, Arrays.asList("nomenclaturalReference.inReference")) : null,
                        null);

        reg = assureIsPersisted(reg);

        return load(reg.getUuid(), Arrays.asList(new String []{"blockedBy"}));
    }

    /**
     * @param typeDesignationTarget
     */
    @Override
    @Transactional(readOnly=false)
    public Registration assureIsPersisted(Registration reg) {

        if(reg.isPersited()){
            return reg;
        }

        prepareForSave(reg);
        reg = super.save(reg);
        userHelper.createAuthorityForCurrentUser(reg, Operation.UPDATE, RegistrationStatus.PREPARATION.name());

        return reg;
    }

    @Override
    @Transactional(readOnly=false)
    public void addTypeDesignation(UUID registrationUUID, UUID typeDesignationUuid){

        // load the typeDesignations with the registration so that typified names can not be twice in detached sessions
        // otherwise multiple representation problems might occur
        Registration registration = load(registrationUUID, Arrays.asList("typeDesignations"));
        if(registration == null){
            registration = newRegistration();
            registration = assureIsPersisted(registration);
        }
        TypeDesignationBase<?> nameTypeDesignation = nameService.loadTypeDesignation(typeDesignationUuid, Arrays.asList(""));
        registration.getTypeDesignations().add(nameTypeDesignation);
    }

    @Override
    @Transactional(readOnly=false)
    public void addTypeDesignation(Registration registration, UUID typeDesignationUuid){

        if(registration == null){
            registration = newRegistration();
            registration = assureIsPersisted(registration);
        } else {
            if(registration.isPersited()){
                // make sure the the typeDesignations are loaded with the registration so that typified names can not be twice in detached sessions
                // otherwise multiple representation problems might occur
                registration.getTypeDesignations();
            }
        }
        TypeDesignationBase<?> nameTypeDesignation = nameService.loadTypeDesignation(typeDesignationUuid, Arrays.asList(""));
        registration.getTypeDesignations().add(nameTypeDesignation);
    }

    /**
     * Sets the registration identifier and submitter in case the registration is not yet persisted.
     *
     * @param reg
     *   The Registration to prepare for saving.
     */
    private void prepareForSave(Registration reg) {

        if(!reg.isPersited()){
            if(minter != null){
                Identifier<String> identifiers = minter.mint();
                if(identifiers.getIdentifier() == null){
                    throw new RuntimeException("RegistrationIdentifierMinter configuration incomplete.");
                }
                reg.setIdentifier(identifiers.getIdentifier());
                reg.setSpecificIdentifier(identifiers.getLocalId());
            }
            Authentication authentication = userHelper.getAuthentication();
            reg.setSubmitter((User)authentication.getPrincipal());
        }
    }

    /**
     * @param name
     */
    @Override
    public boolean checkRegistrationExistsFor(TaxonName name) {

        for(Registration reg : name.getRegistrations()){
            if(minter != null){
                if(minter.isFromOwnRegistration(reg.getIdentifier())){
                    return true;
                }
            } else {
                return true; // first registrations wins as we can't distinguish them without a minter.
            }
        }
        return false;
    }



    // =============================================================================================


}
