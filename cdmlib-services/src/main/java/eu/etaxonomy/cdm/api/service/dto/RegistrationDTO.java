/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationDTO;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetFormatter;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public class RegistrationDTO {

    private static final Logger logger = LogManager.getLogger();

    private String summary = "";

    private RegistrationType registrationType;

    private Reference citation = null;

    private String citationDetail = null;

    private String submitterUserName = null;

    private EntityReference name = null;

    private TypeDesignationSetContainer typeDesignationSetContainer;

    private Registration reg;

    private List<String> validationProblems = new ArrayList<>();

    private Set<TypedEntityReference<Registration>> blockedBy;

    private List<TaggedText> summaryTaggedText = new ArrayList<>();

    private String nomenclaturalCitationString;

    private String bibliographicCitationString;

    private String bibliographicInRefCitationString;

    /**
     * @param reg
     * @param typifiedName should be provided for registrations for TypeDesignations
     * @throws TypeDesignationSetException
     */
    public RegistrationDTO(Registration reg) {

         this.reg = reg;

         registrationType = RegistrationType.from(reg);

         if(reg.getSubmitter() != null ){
             submitterUserName = reg.getSubmitter().getUsername();
         }

         if(hasName(reg)){
             name = new EntityReference(reg.getName().getUuid(), reg.getName().getTitleCache());
         }
        NamedSourceBase publishedUnit = findPublishedUnit(reg);
        if(publishedUnit != null) {
            citation = publishedUnit.getCitation();
            citationDetail = publishedUnit.getCitationMicroReference();
        }

        switch (registrationType) {
        case EMPTY:
            summary = "BLANK REGISTRATION";
            summaryTaggedText.addAll(Arrays.asList(new TaggedText(TagEnum.label, summary)));
            break;
        case NAME:
            summary = reg.getName().getTitleCache();
            summaryTaggedText.addAll(reg.getName().getTaggedName());
            break;
        case NAME_AND_TYPIFICATION:
        case TYPIFICATION:
        default:
            try {
                typeDesignationSetContainer = TypeDesignationSetContainer.NewDefaultInstance(reg.getTypeDesignations());
                summaryTaggedText.addAll(new TypeDesignationSetFormatter(false, true, true)
                        .toTaggedText(typeDesignationSetContainer));
                summary = TaggedCacheHelper.createString(summaryTaggedText);
            } catch (TypeDesignationSetException e) {
                validationProblems.add("Validation errors: " + e.getMessage());
            }
            break;
        }

        makeBibliographicCitationStrings();
        makeNomenclaturalCitationString();
    }

    /**
     * To create an initially empty DTO for which only the <code>typifiedName</code> and the <code>publication</code> are defined.
     * All TypeDesignations added to the <code>Registration</code> need to refer to the same <code>typifiedName</code> and must be
     * published in the same <code>publication</code>.
     *
     * @param reg
     * @param typifiedName
     */
    public RegistrationDTO(Registration reg, TaxonName typifiedName, Reference publication) {
        this.reg = reg;
        citation = publication;
        // create a TypeDesignationSetContainer with only a reference to the typifiedName for validation
        typeDesignationSetContainer = new TypeDesignationSetContainer(typifiedName);
        makeBibliographicCitationStrings();
        makeNomenclaturalCitationString();
    }

    public static NamedSourceBase findPublishedUnit(Registration reg) {
        NamedSourceBase publishedUnit = null;
        if(hasName(reg)){
            publishedUnit = reg.getName().getNomenclaturalSource();
        } else if(hasTypifications(reg)){
            if(!reg.getTypeDesignations().isEmpty()){
                for(TypeDesignationBase<?> td : reg.getTypeDesignations()){
                    if(td.getDesignationSource() != null) {
                        publishedUnit = td.getDesignationSource();
                    }
                }
            }
        }
        return publishedUnit;
    }

    private static boolean hasTypifications(Registration reg) {
        return reg.getTypeDesignations() != null && reg.getTypeDesignations().size() > 0;
    }

    private static boolean hasName(Registration reg) {
        return reg.getName() != null;
    }

    /**
     * Provides access to the Registration entity this DTO has been build from.
     * This method is purposely not a getter to hide the original Registration
     * from generic processes which are exposing, binding bean properties.
     *IReference
     * @return
     */
    public Registration registration() {
        return reg;
    }

    public String getSummary() {
        return summary;
    }

    public List<TaggedText> getSummaryTaggedText() {
        return summaryTaggedText;
    }

    public String getSubmitterUserName(){
        return submitterUserName;
    }

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public RegistrationStatus getStatus() {
        return reg.getStatus();
    }

    public String getIdentifier() {
        return reg.getIdentifier();
    }

    public UUID getUuid() {
        return reg.getUuid();
    }

    public String getSpecificIdentifier() {
        return reg.getSpecificIdentifier();
    }

    public DateTime getRegistrationDate() {
        return reg.getRegistrationDate();
    }

    public String getInstitutionTitleCache(){
        return reg.getInstitution() != null ? reg.getInstitution().getName() : null;
    }

    public VerbatimTimePeriod getDatePublished() {
        return citation == null ? null : citation.getDatePublished();
    }

    public DateTime getCreated() {
        return reg.getCreated();
    }

    public Reference getCitation() {
        return citation;
    }

    public void setCitation(Reference citation) throws Exception {
        if(this.citation == null){
            this.citation = citation;
        } else {
            throw new Exception("Can not set the citation on a non emtpy RegistrationDTO");
        }
        makeBibliographicCitationStrings();
        makeNomenclaturalCitationString();
    }

    public UUID getCitationUuid() {
        return citation == null ? null : citation.getUuid();
    }

    public EntityReference getTypifiedNameRef() {
        return typeDesignationSetContainer != null ? typeDesignationSetContainer.getTypifiedNameAsEntityRef() : null;
    }

    public TaxonName typifiedName() {
        return typeDesignationSetContainer != null ? typeDesignationSetContainer.getTypifiedName() : null;
    }

    public EntityReference getNameRef() {
        return name;
    }

    public Map<TypedEntityReference<? extends VersionableEntity>,TypeDesignationSet> getOrderedTypeDesignationSets() {
        return typeDesignationSetContainer != null ?
                typeDesignationSetKeyToTypedEntity(typeDesignationSetContainer.getOrderedTypeDesignationSets()) : null;
    }

    private Map<TypedEntityReference<? extends VersionableEntity>,TypeDesignationSet> typeDesignationSetKeyToTypedEntity(
            Map<VersionableEntity,TypeDesignationSet> orderedTypeDesignationSets) {
        Map<TypedEntityReference<? extends VersionableEntity>,TypeDesignationSet> result = new LinkedHashMap<>(orderedTypeDesignationSets.size());

        orderedTypeDesignationSets.entrySet().forEach(e->
            result.put(TypeDesignationSet.makeEntityReference(e.getKey()), e.getValue()));
        return result;
    }

    public TypeDesignationSet getTypeDesignationSet(VersionableEntity baseEntity) {
        return typeDesignationSetContainer != null ? typeDesignationSetContainer.getOrderedTypeDesignationSets().get(baseEntity) : null;
    }

    public Set<TypeDesignationBase> getTypeDesignationsInWorkingSet(VersionableEntity baseEntity) {
        Set<TypeDesignationBase> typeDesignations = new HashSet<>();
        TypeDesignationSet workingSet = getTypeDesignationSet(baseEntity);
        for(TypeDesignationDTO<?> ref :  workingSet.getTypeDesignations()){
            typeDesignations.add(findTypeDesignation(ref));
        }
        return typeDesignations;
    }

    public NameTypeDesignation getNameTypeDesignation(VersionableEntity baseEntity) {
        Set<TypeDesignationBase> typeDesignations = getTypeDesignationsInWorkingSet(baseEntity);
        if(typeDesignations.size() == 1){
            TypeDesignationBase<?> item = typeDesignations.iterator().next();
            return (NameTypeDesignation)item ;
        }
        if(typeDesignations.size() == 0){
            return null;
        }
        if(typeDesignations.size() > 1){
            throw new RuntimeException("Workingsets of NameTypeDesignations must contain exactly one item.");
        }
        return null;
    }

    private TypeDesignationBase<?> findTypeDesignation(TypeDesignationDTO ref) {
        return typeDesignationSetContainer != null ? typeDesignationSetContainer.findTypeDesignation(ref.getUuid()) : null;
    }

    public Collection<TypeDesignationBase<?>> typeDesignations() {
        return typeDesignationSetContainer != null ? typeDesignationSetContainer.getTypeDesignations() : null;
    }

    private void makeNomenclaturalCitationString() {
        if(citation == null){
            nomenclaturalCitationString = null;
        } else {
            if(INomenclaturalReference.class.isAssignableFrom(citation.getClass())){
                nomenclaturalCitationString = NomenclaturalSourceFormatter.INSTANCE().format(citation, citationDetail);
            } else {
                logger.error("The citation is not a NomenclaturalReference");
                nomenclaturalCitationString = citation.generateTitle();
            }
        }
    }

    private void makeBibliographicCitationStrings() {
        if(citation == null){
            bibliographicCitationString = null;
        } else {
            Reference bibliographicCitation;
            String bibliographicCitationDetail = citationDetail;
            if((citation.getType() == ReferenceType.Section || citation.getType() == ReferenceType.BookSection) && citation.getInReference() != null){
                bibliographicCitation = citation.getInReference();
                bibliographicCitationDetail = null; // can possibly be known once https://dev.e-taxonomy.eu/redmine/issues/6623 is solved
            } else {
                bibliographicCitation = citation;
            }
            if(StringUtils.isNotEmpty(bibliographicCitationDetail)){
                // TODO see https://dev.e-taxonomy.eu/redmine/issues/6623
                bibliographicInRefCitationString = bibliographicCitation.generateTitle().replaceAll("\\.$", "") + (StringUtils.isNotEmpty(bibliographicCitationDetail) ? ": " + bibliographicCitationDetail : "");
            } else {
                bibliographicInRefCitationString = bibliographicCitation.generateTitle();
            }
            if(StringUtils.isNotEmpty(citationDetail)){
                // TODO see https://dev.e-taxonomy.eu/redmine/issues/6623
                bibliographicCitationString = citation.generateTitle().replaceAll("\\.$", "") + (StringUtils.isNotEmpty(citationDetail) ? ": " + citationDetail : "");
            } else {
                bibliographicCitationString = citation.generateTitle();
            }
        }
    }

    /**
     * The nomenclatural citation is always the nomenclaturalCitation of the reference which is directly
     * associated with the registration.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicCitationString()}
     *
     * @return the nomenclaturalCitationString
     */
    public String getNomenclaturalCitationString() {
        return nomenclaturalCitationString;
    }

    /**
     * The bibliographic in-reference citation is either taken from the reference which is directly
     * associated with the registration. In case this reference is a {@link eu.etaxonomy.cdm.model.reference.ReferenceType#Section} or
     * {@link eu.etaxonomy.cdm.model.reference.ReferenceType#BookSection} the inReference will be taken instead.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicCitationString()}
     *
     * @return the bibliographicInRefCitationString
     */
    public String getBibliographicInRefCitationString() {
       return bibliographicInRefCitationString;
    }

    /**
     * The bibliographic citation is either reference which is directly
     * associated with the registration.
     * <p>
     * <b>Note:</b>Compare with {@link #getBibliographicInRefCitationString()}
     *
     * @return the bibliographicCitationString
     */
    public String getBibliographicCitationString() {
        return bibliographicCitationString;
    }

    public boolean isBlocked() {
        return reg.getBlockedBy() != null && !reg.getBlockedBy().isEmpty();
    }

    /**
     * @return the blockedBy
     */
    public Set<TypedEntityReference<Registration>> getBlockedBy() {

        if(blockedBy == null){
            blockedBy = new HashSet<>();
            if(reg.getBlockedBy() != null){
                for(Registration blockReg : reg.getBlockedBy()){
                    blockedBy.add(new TypedEntityReference<Registration>(Registration.class, blockReg.getUuid(), blockReg.getIdentifier()));
                }
            }
        }
        return blockedBy;
    }

    public List<String> getValidationProblems() {
        return validationProblems;
    }

    public boolean isPersisted() {
        return reg.isPersited();
    }
}