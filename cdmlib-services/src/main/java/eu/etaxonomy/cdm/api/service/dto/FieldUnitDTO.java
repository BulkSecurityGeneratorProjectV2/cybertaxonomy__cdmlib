/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO {

    private static final long serialVersionUID = 3981843956067273220L;

    private static final String SEPARATOR_STRING = ", ";

	private String country;
	private String collectingString;
	private Partial date;
	private String collectionsStatistics;

	private GatheringEventDTO gatheringEvent;

	public static FieldUnitDTO fromEntity(FieldUnit entity){
        return FieldUnitDTO.fromEntity(entity, null);
	}

	/**
     * Factory method for the construction of a FieldUnitDTO.
     * <p>
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
     *
     *
     * @param fieldUnit
     *     The FieldUnit entity to create a DTO for. Is null save.
     * @param specimenOrObservationTypeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     */
	public static FieldUnitDTO fromEntity(FieldUnit entity, EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter){
        if(entity == null) {
            return null;
        }
        return new FieldUnitDTO(entity, specimenOrObservationTypeFilter);
    }

	/**
     * The direct derivatives are added to the field {@link #getDerivatives() derivates}.
	 *
	 * @param fieldUnit
	 *     The FieldUnit entity to create a DTO for
	 * @param specimenOrObservationTypeFilter
	 *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
	 */
    private FieldUnitDTO(FieldUnit fieldUnit, EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter ) {
        super(fieldUnit);

        if(specimenOrObservationTypeFilter == null) {
            specimenOrObservationTypeFilter = EnumSet.allOf(SpecimenOrObservationType.class);
        }
        if (fieldUnit.getGatheringEvent() != null){
            gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
        }
        setRecordBase(fieldUnit.getRecordBasis());

        // --------------------------------------

        if (fieldUnit.getGatheringEvent() != null) {
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            // Country
            NamedArea country = gatheringEvent.getCountry();
            setCountry(country != null ? country.getLabel() : null);
            // Collection
            AgentBase<?> collector = gatheringEvent.getCollector();
            String fieldNumber = fieldUnit.getFieldNumber();
            String collectionString = "";
            if (collector != null || fieldNumber != null) {
                collectionString += collector != null ? collector : "";
                if (!collectionString.isEmpty()) {
                    collectionString += " ";
                }
                collectionString += (fieldNumber != null ? fieldNumber : "");
                collectionString.trim();
            }
            setCollectingString(collectionString);
            setDate(gatheringEvent.getGatheringDate());
        }

        Map<eu.etaxonomy.cdm.model.occurrence.Collection, List<String> > unitIdenfierLabelsByCollections = new HashMap<>();
        assembleDerivatives(fieldUnit, specimenOrObservationTypeFilter, unitIdenfierLabelsByCollections);

        // assemble derivate data DTO
        DerivationTreeSummaryDTO derivateDataDTO = DerivationTreeSummaryDTO.fromEntity(fieldUnit, null);
        setDerivationTreeSummary(derivateDataDTO);

        // assemble citation
        String summaryLabel = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(summaryLabel) || summaryLabel.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            summaryLabel = fieldUnit.getTitleCache();
        }

        List<String> derivativesAccessionNumbers = new ArrayList<>();
        for(List<String> labels : unitIdenfierLabelsByCollections.values()) {
            derivativesAccessionNumbers.addAll(labels);
        }
        if (!derivativesAccessionNumbers.isEmpty()) {
            summaryLabel += " (";
            for (String accessionNumber : derivativesAccessionNumbers) {
                if (accessionNumber != null && !accessionNumber.isEmpty()) {
                    summaryLabel += accessionNumber + SEPARATOR_STRING;
                }
            }
            summaryLabel = removeTail(summaryLabel, SEPARATOR_STRING);
            summaryLabel += ")";
        }
        setSummaryLabel(summaryLabel);

        // assemble herbaria string
        String herbariaString = "";
        for (eu.etaxonomy.cdm.model.occurrence.Collection collection : unitIdenfierLabelsByCollections.keySet()) {
            int unitCount = unitIdenfierLabelsByCollections.get(collection).size();
            if (collection.getCode() != null) {
                herbariaString += collection.getCode();
            }
            if (unitCount > 1) {
                herbariaString += "(" + unitCount + ")";
            }
            herbariaString += SEPARATOR_STRING;
        }
        herbariaString = removeTail(herbariaString, SEPARATOR_STRING);
        setCollectionStatistics(herbariaString);
    }

    private String removeTail(String string, final String tail) {
        if (string.endsWith(tail)) {
            string = string.substring(0, string.length() - tail.length());
        }
        return string;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getCollectionStatistics() {
        return collectionsStatistics;
    }

    public void setCollectionStatistics(String collection) {
        this.collectionsStatistics = collection;
    }

    public String getCollectingString() {
        return collectingString;
    }
    public void setCollectingString(String collectingString) {
        this.collectingString = collectingString;
    }

    public Partial getDate() {
        return date;
    }
    public void setDate(Partial date) {
        this.date = date;
    }

    public boolean isHasType() {
        boolean hasType = collectDerivatives()
                .stream()
                .anyMatch(derivedUnitDTO -> derivedUnitDTO.getSpecimenTypeDesignations() != null && !derivedUnitDTO.getSpecimenTypeDesignations().isEmpty());
        return hasType;
    }

    public GatheringEventDTO getGatheringEvent() {
        return gatheringEvent;
    }
    public void setGatheringEvent(GatheringEventDTO gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }
}