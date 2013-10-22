// $Id$
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author a.kohlbecker
 * @date 18.06.2009
 * @author n.hoffmann
 *
 */
public interface IEditGeoService {

    /**
     * Retrieve a parameter string to pass to an EditGeoService endpoint
     * The endpoint will deliver a  a map generated by the Edit Geo Service for the given
     * <code>TaxonDescription</code>
     *
     * @param taxonDescriptions
     * 			A List of <code>TaxonDescription</code> holding the distribution data
     * @param presenceAbsenceTermColors
     * 			A map that classifies which <code>PresenceAbsenceTermBase</code> should
     * 			be assigned which <code>Color</code>
     * @param width
     * 			The width of the map image
     * @param height
     * 			The height of the map image
     * @param bbox
     *
     * @param backLayer
     * @return
     *
     */
    public String getDistributionServiceRequestParameterString(
            List<TaxonDescription> taxonDescriptions,
            Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors,
            int width, int height, String bbox, String backLayer,
            List<Language> langs);


    /**
     *
     * @param distributions
     * @param presenceAbsenceTermColors
     * @param width
     * @param height
     * @param bbox
     * @param backLayer
     * @param langs
     * @return
     */
    public String getDistributionServiceRequestParameterString(Set<Distribution> distributions, Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors, int width,
            int height, String bbox, String backLayer, List<Language> langs);


    /**
     * Retrieve a parameter string to pass to an EditGeoService endpoint
     * The endpoint will deliver a  a map generated by the Edit Geo Service for the given
     * <code>TaxonDescription</code>
     *
     * @param description
     * 			The <code>TaxonDescription</code> holding the distribution data
     * @param presenceAbsenceTermColors
     * 			A map that classifies which <code>PresenceAbsenceTermBase</code> should
     * 			be assigned which <code>Color</code>
     * @param width
     * 			The width of the map image
     * @param height
     * 			The height of the map image
     * @param bbox
     *
     * @param backLayer
     * @return
     *
     * @deprecated use {@link #getDistributionServiceRequestParameterString(List, Map, int, int, String, String, List)} instead
     */
    @Deprecated
    public String getDistributionServiceRequestParameterString(TaxonDescription description,
            Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors,
            int width,
            int height,
            String bbox,
            String backLayer,
            List<Language> langs);


    public String getOccurrenceServiceRequestParameterString(
            List<SpecimenOrObservationBase> specimensOrObersvations,
            Map<SpecimenOrObservationType,Color> specimenOrObservationTypeColors,
            Boolean doReturnImage,
            Integer width,
            Integer height,
            String bbox,
            String backLayer);

    /**
     * Adds an area mapping (CDM area -> geo service area). It is recommended to set the mapping
     * in a persistent way, so it is available after restarting the application.
     * @param area
     * @param geoServiceArea
     * @throws XMLStreamException
     */
    public void setMapping(NamedArea area, GeoServiceArea geoServiceArea);

    /**
    *
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
    * @param areaVocabularyUuidy
    * @return
    * @throws IOException
    */
    @Transactional(readOnly=false)
    public abstract Map<NamedArea, String> mapShapeFileToNamedAreas(Reader csvReader, List<String> idSearchFields, String wmsLayerName, UUID areaVocabularyUuidy)
            throws IOException;

}
