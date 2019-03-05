/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO.InfoPart;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.api.utility.DistributionOrder;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;

/**
 * @author a.kohlbecker
 * @since 18.06.2009
 *
 */
@Service
@Transactional(readOnly = true)
public class EditGeoService implements IEditGeoService {
    public static final Logger logger = Logger.getLogger(EditGeoService.class);

    @Autowired
    private IDescriptionDao dao;

    @Autowired
    private IGeoServiceAreaMapping areaMapping;

    @Autowired
    private ITermVocabularyDao vocabDao;

    @Autowired
    private IDefinedTermDao termDao;

    @Autowired
    private IOccurrenceDao occurrenceDao;

    private Set<Feature> getDistributionFeatures() {
        Set<Feature> distributionFeature = new HashSet<>();
        Feature feature = (Feature) termDao.findByUuid(Feature.DISTRIBUTION().getUuid());
        distributionFeature.add(feature);
        return distributionFeature;
    }

    /**
     * @param taxonDescriptions
     * @return
     */
    private Set<Distribution> getDistributionsOf(List<TaxonDescription> taxonDescriptions) {
        Set<Distribution> result = new HashSet<>();

        Set<Feature> features = getDistributionFeatures();
        for (TaxonDescription taxonDescription : taxonDescriptions) {
            List<Distribution> distributions;
            if (taxonDescription.getId() > 0){
                distributions = dao.getDescriptionElements(
                        taxonDescription,
                        null,
                        null /*features*/,
                        Distribution.class,
                        null,
                        null,
                        null);
            }else{
                distributions = new ArrayList<Distribution>();
                for (DescriptionElementBase deb : taxonDescription.getElements()){
                    if (deb.isInstanceOf(Distribution.class)){
                        if (features == null || features.isEmpty()
                                || features.contains(deb.getFeature())) {
                            distributions.add(CdmBase.deproxy(deb, Distribution.class));
                        }
                    }
                }
            }
            result.addAll(distributions);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDistributionServiceRequestParameterString(List<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs) {

        Set<Distribution> distributions = getDistributionsOf(taxonDescriptions);

        String uriParams = getDistributionServiceRequestParameterString(distributions,
                subAreaPreference,
                statusOrderPreference,
                hideMarkedAreas,
                presenceAbsenceTermColors,
                langs);

        return uriParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDistributionServiceRequestParameterString(
            Set<Distribution> distributions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs) {

        Collection<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(distributions,
                hideMarkedAreas, true, statusOrderPreference, subAreaPreference);


        String uriParams = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(
                filteredDistributions,
                areaMapping,
                presenceAbsenceTermColors,
                null, langs);
        return uriParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public String getDistributionServiceRequestParameterString(TaxonDescription taxonDescription,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs) {

        List<TaxonDescription> taxonDescriptions = new ArrayList<>();
        taxonDescriptions.add(taxonDescription);

        return getDistributionServiceRequestParameterString(taxonDescriptions,
                subAreaPreference,
                statusOrderPreference,
                hideMarkedAreas,
                presenceAbsenceTermColors,
                langs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OccurrenceServiceRequestParameterDto getOccurrenceServiceRequestParameterString(
            List<SpecimenOrObservationBase> specimensOrObersvations,
            Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors) {

        List<Point> fieldUnitPoints = new ArrayList<>();
        List<Point> derivedUnitPoints = new ArrayList<>();

        for (SpecimenOrObservationBase<?> specimenOrObservationBase : specimensOrObersvations) {
            SpecimenOrObservationBase<?> specimenOrObservation = occurrenceDao
                    .load(specimenOrObservationBase.getUuid());

            if (specimenOrObservation instanceof FieldUnit) {
                GatheringEvent gatherEvent = ((FieldUnit) specimenOrObservation).getGatheringEvent();
                if (gatherEvent != null && gatherEvent.getExactLocation() != null){
                    fieldUnitPoints.add(gatherEvent.getExactLocation());
                }
            }
            if (specimenOrObservation instanceof DerivedUnit) {
                registerDerivedUnitLocations((DerivedUnit) specimenOrObservation, derivedUnitPoints);
            }
        }

        return EditGeoServiceUtilities.getOccurrenceServiceRequestParameterString(fieldUnitPoints,
                derivedUnitPoints, specimenOrObservationTypeColors);

    }

    public CondensedDistribution getCondensedDistribution(List<TaxonDescription> taxonDescriptions,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            MarkerType fallbackAreaMarkerType,
            CondensedDistributionRecipe recipe,
            List<Language> langs) {
        Set<Distribution> distributions = getDistributionsOf(taxonDescriptions);
        return getCondensedDistribution(distributions, statusOrderPreference,
                hideMarkedAreas, fallbackAreaMarkerType, recipe, langs);
    }

    @Override
    public CondensedDistribution getCondensedDistribution(Set<Distribution> distributions,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            MarkerType fallbackAreaMarkerType,
            CondensedDistributionRecipe recipe,
            List<Language> langs) {

        Collection<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(
                distributions, hideMarkedAreas, true, statusOrderPreference, false);



        CondensedDistribution condensedDistribution = EditGeoServiceUtilities.getCondensedDistribution(
                filteredDistributions,
                recipe,
                langs);

        return condensedDistribution;
    }

    /**
     * @param derivedUnit
     * @param derivedUnitPoints
     */
    private void registerDerivedUnitLocations(DerivedUnit derivedUnit, List<Point> derivedUnitPoints) {

        Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
        if (originals != null) {
            for (SpecimenOrObservationBase<?> original : originals) {
                if (original instanceof FieldUnit) {
                    if (((FieldUnit) original).getGatheringEvent() != null) {
                        Point point = ((FieldUnit) original).getGatheringEvent().getExactLocation();
                        if (point != null) {
                            // points with no longitude or latitude should not exist
                            // see  #4173 ([Rule] Longitude and Latitude in Point must not be null)
                            if (point.getLatitude() == null || point.getLongitude() == null){
                                continue;
                            }
                            // FIXME: remove next statement after
                            // DerivedUnitFacade or ABCD import is fixed
                            //
                            if(point.getLatitude() == 0.0 || point.getLongitude() == 0.0) {
                                continue;
                            }
                            derivedUnitPoints.add(point);
                        }
                    }
                } else {
                    registerDerivedUnitLocations((DerivedUnit) original, derivedUnitPoints);
                }
            }
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setMapping(NamedArea area, GeoServiceArea geoServiceArea) {
        areaMapping.set(area, geoServiceArea);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly=false)
    public Map<NamedArea, String> mapShapeFileToNamedAreas(Reader csvReader,
            List<String> idSearchFields, String wmsLayerName, UUID areaVocabularyUuid,
            Set<UUID> namedAreaUuids) throws IOException {

        Set<NamedArea> areas = new HashSet<>();

        if(areaVocabularyUuid != null){
            TermVocabulary<NamedArea> areaVocabulary = vocabDao.load(areaVocabularyUuid);
            if(areaVocabulary == null){
                throw new EntityNotFoundException("No Vocabulary found for uuid " + areaVocabularyUuid);
            }
            areas.addAll(areaVocabulary.getTerms());
        }
        if(namedAreaUuids != null && !namedAreaUuids.isEmpty()){
            for(DefinedTermBase<?> dtb : termDao.list(namedAreaUuids, null, null, null, null)){
                areas.add((NamedArea)CdmBase.deproxy(dtb));
            }
        }

        ShpAttributesToNamedAreaMapper mapper = new ShpAttributesToNamedAreaMapper(areas, areaMapping);
        Map<NamedArea, String> resultMap = mapper.readCsv(csvReader, idSearchFields, wmsLayerName);
        termDao.saveOrUpdateAll((Collection)areas);
        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistributionInfoDTO composeDistributionInfoFor(EnumSet<DistributionInfoDTO.InfoPart> parts, UUID taxonUUID,
            boolean subAreaPreference, boolean statusOrderPreference, Set<MarkerType> hiddenAreaMarkerTypes,
            Set<NamedAreaLevel> omitLevels, Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> languages,  List<String> propertyPaths, CondensedDistributionRecipe recipe,
            DistributionOrder distributionOrder){

        DistributionInfoDTO dto = new DistributionInfoDTO();

        if (propertyPaths == null){
            propertyPaths = Arrays.asList(new String []{});
        }
        // Adding default initStrategies to improve the performance of this method
        // adding 'status' and 'area' has a good positive effect:
        // filterDistributions() only takes 21% of the total method time (before it was 46%)
        // at the same time the cost of the getDescriptionElementForTaxon is not increased at all!
        //
        // adding 'markers.markerType' is not improving the performance since it only
        // moved the load from the filter method to the getDescriptionElementForTaxon()
        // method.
        // overall improvement by this means is by 42% (from 77,711 ms to 44,868 ms)
        ArrayList<String> initStrategy = new ArrayList<>(propertyPaths);
        if(!initStrategy.contains("status")) {
            initStrategy.add("status");
        }
        if(!initStrategy.contains("area")) {
            initStrategy.add("area");
        }
        if(!initStrategy.contains("markers.markerType")) {
            initStrategy.add("markers.markerType");
        }
        if(omitLevels == null) {
            omitLevels = new HashSet<>(0);
        }

        List<Distribution> distributions = dao.getDescriptionElementForTaxon(taxonUUID, null, Distribution.class, null, null, initStrategy);

        // Apply the rules statusOrderPreference and hideMarkedAreas for textual distribution info
        Set<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(distributions, hiddenAreaMarkerTypes,
                true, statusOrderPreference, false);

        if(parts.contains(InfoPart.elements)) {
            dto.setElements(filteredDistributions);
        }

        if(parts.contains(InfoPart.tree)) {
            DistributionTree tree = DescriptionUtility.orderDistributions(termDao, omitLevels, filteredDistributions, hiddenAreaMarkerTypes,distributionOrder);
            dto.setTree(tree);
        }

        if(parts.contains(InfoPart.condensedDistribution)) {
            dto.setCondensedDistribution(EditGeoServiceUtilities.getCondensedDistribution(filteredDistributions, recipe, languages));
        }

        if (parts.contains(InfoPart.mapUriParams)) {
            // only apply the subAreaPreference rule for the maps
            Set<Distribution> filteredMapDistributions = DescriptionUtility.filterDistributions(filteredDistributions, null, false, false, subAreaPreference);

            dto.setMapUriParams(EditGeoServiceUtilities.getDistributionServiceRequestParameterString(filteredMapDistributions,
                    areaMapping,
                    presenceAbsenceTermColors,
                    null, languages));
        }

        return dto;
    }
}
