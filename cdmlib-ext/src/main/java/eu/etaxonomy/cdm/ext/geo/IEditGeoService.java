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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.springframework.transaction.annotation.Transactional;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO;
import eu.etaxonomy.cdm.api.util.DescriptionUtility;
import eu.etaxonomy.cdm.api.util.DistributionOrder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author a.kohlbecker
 * @author n.hoffmann
 * @since 18.06.2009
 */
public interface IEditGeoService {

    /**
     * Retrieve a parameter string to pass to an EditGeoService endpoint
     * The endpoint will deliver a  a map generated by the Edit Geo Service for the given
     * <code>TaxonDescription</code>
     *
     * @param taxonDescriptions
     * 			A List of <code>TaxonDescription</code> holding the distribution data
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}     * @param hideMarkedAreas
     *            distributions where the area has a {@link Marker} with one of
     *            the specified {@link MarkerType}s will be skipped, see
     *            {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param presenceAbsenceTermColors
     * 			A map that classifies which <code>PresenceAbsenceTermBase</code> should
     * 			be assigned which <code>Color</code>
     * @param langs
     * @return
     *
     */
    public String getDistributionServiceRequestParameterString(
            List<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs);


    /**
     *
     * @param distributions
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param presenceAbsenceTermColors
     * @param langs
     * @return
     */
    public String getDistributionServiceRequestParameterString(
            Set<Distribution> distributions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs);

    public OccurrenceServiceRequestParameterDto getOccurrenceServiceRequestParameters(
            List<SpecimenOrObservationBase> specimensOrObersvations,
            Map<SpecimenOrObservationType,Color> specimenOrObservationTypeColors
            );

    /**
     * Adds an area mapping (CDM area -> geo service area). It is recommended to set the mapping
     * in a persistent way, so it is available after restarting the application.
     * @param area
     * @param geoServiceArea
     * @throws XMLStreamException
     */
    public void setMapping(NamedArea area, GeoServiceArea geoServiceArea);

    /**
     * Reads csv data containing the attributes from a shape file and adds the
     * shapefile data to each area in the given set of {@link NamedAreas}. The
     * way this data it attached to the areas is specific to the
     * {@link IGeoServiceAreaMapping} implementation. It is recommended to
     * create csv file directly from the original shape file by making use of
     * the {@code org2ogr} command which is contained in the <a
     * href="http://www.gdal.org/ogr2ogr.html">gdal</a> tools:
     *
     * <pre>
     * ogr2ogr -f csv out.csv input_shape_file.shp
     * </pre>
     *
     * @param csvReader
     * @param idSearchFields
     *            An ordered list column names in the the csv file to be
     *            imported. These columns will be used to search for the
     *            {@link NamedArea#getIdInVocabulary() IdInVocabulary} of each
     *            area
     * @param wmsLayerName
     * @return the resulting table of the import, also together with diagnostic
     *         messages per NamedArea (id not found, ambiguous mapping)
     * @param areaVocabularyUuid
     *            , can be <code>NULL</code>. The NamedAreas contained in this
     *            vocabulary will be combined with areas defined in the
     *            <code>namedAreaUuids</code>
     * @param namedAreaUuids
     *            a set of UUIDS for {@link NamedArea}. Can be <code>NULL</code>.
     *            Will be combined with the vocabulary if the
     *            <code>areaVocabularyUuid</code> is also given.
     *
     * @return
     * @throws IOException
     */
    @Transactional(readOnly=false)
    public abstract Map<NamedArea, String> mapShapeFileToNamedAreas(Reader csvReader,
            List<String> idSearchFields, String wmsLayerName, UUID areaVocabularyUuid,
            Set<UUID> namedAreaUuids)
            throws IOException;

   /**
    * @param parts
    * @param taxonUUID
    * @param subAreaPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
    * @param statusOrderPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
    * @param hiddenAreaMarkerTypes see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
    * @param omitLevels see {@link DescriptionUtility#orderDistributions(Set, Collection)}
    * @param presenceAbsenceTermColors
    * @param languages
    * @param propertyPaths
    * @param ignoreDistributionStatusUndefined workaround until #9500 is implemented to ignore status "undefined"
    * @return
    */
    public DistributionInfoDTO composeDistributionInfoFor(EnumSet<DistributionInfoDTO.InfoPart> parts, UUID taxonUUID,
            boolean subAreaPreference, boolean statusOrderPreference, Set<MarkerType> hiddenAreaMarkerTypes,
            boolean neverUseFallbackAreaAsParent, Set<NamedAreaLevel> omitLevels,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> languages, List<String> propertyPaths, CondensedDistributionConfiguration config,
            DistributionOrder distributionOrder, boolean ignoreDistributionStatusUndefined);

    /**
    * @param distributions
    * @param statusOrderPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
    * @param hiddenAreaMarkerTypes marker types to make areas hidden, this includes fallback-areas which are defined to have visible sub-areas
    * @param config the {@link CondensedDistributionConfiguration condensed distribution configuration}
    * @param languages
    * @return
    */
    public CondensedDistribution getCondensedDistribution(Set<Distribution> distributions,
            boolean statusOrderPreference,
            Set<MarkerType> hiddenAreaMarkerTypes,
            CondensedDistributionConfiguration config,
            List<Language> langs);

	public Kml occurrencesToKML(List<SpecimenOrObservationBase> specimensOrObersvations,
			Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors);

}
