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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

public class FieldUnitDTO extends SpecimenOrObservationBaseDTO {

    private static final long serialVersionUID = 3981843956067273220L;

    private static final String SEPARATOR_STRING = ", ";

	private String country;
	private String collectingString;
	private String date;
	private String collectionsStatistics;

	private boolean hasType;

	private GatheringEventDTO gatheringEvent;

	public static FieldUnitDTO fromEntity(FieldUnit entity){
        if(entity == null) {
            return null;
        }
        return new FieldUnitDTO(entity);
	}

    private FieldUnitDTO(FieldUnit fieldUnit) {
        super(fieldUnit);
        if (fieldUnit.getGatheringEvent() != null){
            gatheringEvent = GatheringEventDTO.newInstance(fieldUnit.getGatheringEvent());
        }
        setRecordBase(fieldUnit.getRecordBasis().getMessage());
        setListLabel(fieldUnit.getTitleCache());

        // --------------------------------------

        if (fieldUnit.getGatheringEvent() != null) {
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            // Country
            NamedArea country = gatheringEvent.getCountry();
            setCountry(country != null ? country.getLabel() : null);
            // Collection
            AgentBase collector = gatheringEvent.getCollector();
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
            // Date
            Partial gatheringDate = gatheringEvent.getGatheringDate();
            String dateString = null;
            if (gatheringDate != null) {
                dateString = gatheringDate.toString();
            }
            else if(gatheringEvent.getTimeperiod()!=null && gatheringEvent.getTimeperiod().getFreeText()!=null){
                dateString = gatheringEvent.getTimeperiod().getFreeText();
            }
            setDate(dateString);
        }

        // Herbaria map
        Map<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> collectionToCountMap = new HashMap<>();
        // List of accession numbers for citation
        List<String> preservedSpecimenAccessionNumbers = new ArrayList<>();

        // assemble preserved specimen DTOs
        Set<DerivationEvent> derivationEvents = fieldUnit.getDerivationEvents();
        for (DerivationEvent derivationEvent : derivationEvents) {
            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
            for (DerivedUnit derivedUnit : derivatives) {
                if(!derivedUnit.isPublish()){
                    continue;
                }
                // collect accession numbers for citation
                String identifier = derivedUnit.getMostSignificantIdentifier();
                // collect collections for herbaria column
                eu.etaxonomy.cdm.model.occurrence.Collection collection = derivedUnit.getCollection();
                if (collection != null) {
                    //combine collection with identifier
                    if (identifier != null) {
                        if(collection.getCode()!=null){
                            identifier = (collection.getCode()!=null?collection.getCode():"[no collection]")+" "+identifier;
                        }
                        preservedSpecimenAccessionNumbers.add(identifier);
                    }

                    Integer herbariumCount = collectionToCountMap.get(collection);
                    if (herbariumCount == null) {
                        herbariumCount = 0;
                    }
                    collectionToCountMap.put(collection, herbariumCount + 1);
                }
                if (derivedUnit.getRecordBasis().equals(SpecimenOrObservationType.PreservedSpecimen)) {
                    DerivedUnitDTO preservedSpecimenDTO = DerivedUnitDTO.fromEntity(derivedUnit, null);
                    addDerivate(preservedSpecimenDTO);
                    setHasCharacterData(isHasCharacterData() || preservedSpecimenDTO.isHasCharacterData());
                    setHasDetailImage(isHasDetailImage() || preservedSpecimenDTO.isHasDetailImage());
                    setHasDna(isHasDna() || preservedSpecimenDTO.isHasDna());
                    setHasSpecimenScan(isHasSpecimenScan() || preservedSpecimenDTO.isHasSpecimenScan());
                }
            }
        }
        // assemble derivate data DTO
        DerivateDataDTO derivateDataDTO = DerivateDataDTO.fromEntity(fieldUnit, null);
        setDerivateDataDTO(derivateDataDTO);

        // assemble citation
        String citation = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(citation) || citation.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            citation = fieldUnit.getTitleCache();
        }
        if (!preservedSpecimenAccessionNumbers.isEmpty()) {
            citation += " (";
            for (String accessionNumber : preservedSpecimenAccessionNumbers) {
                if (!accessionNumber.isEmpty()) {
                    citation += accessionNumber + SEPARATOR_STRING;
                }
            }
            citation = removeTail(citation, SEPARATOR_STRING);
            citation += ")";
        }
        setCitation(citation);

        // assemble herbaria string
        String herbariaString = "";
        for (Entry<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> e : collectionToCountMap.entrySet()) {
            eu.etaxonomy.cdm.model.occurrence.Collection collection = e.getKey();
            if (collection.getCode() != null) {
                herbariaString += collection.getCode();
            }
            if (e.getValue() > 1) {
                herbariaString += "(" + e.getValue() + ")";
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

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public boolean isHasType() {
        return hasType;
    }
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }

    public GatheringEventDTO getGatheringEvent() {
        return gatheringEvent;
    }
    public void setGatheringEvent(GatheringEventDTO gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }
}