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
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

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
	 */
	public String getDistributionServiceRequestParameterString(TaxonDescription description, 
			Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors, 
			int width, 
			int height, 
			String bbox, 
			String backLayer,
			List<Language> langs);
	
	/**
	 * 
	 * 
	 * @param taxon
	 * @param presenceAbsenceTermColors
	 * @param width
	 * @param height
	 * @param bbox
	 * @param backLayer
	 * @return
	 * @deprecated 
	 * 			this method throws all distribution data from all taxon descriptions in one big pot 
	 * 			and returns a map based on this data. However this might be useful for certain 
	 * 			use cases it does not really make sense to mix data from different taxon descriptions.
	 * 			Why have they been separated in the first place?
	 * 
	 */
	@Deprecated
	public String getDistributionServiceRequestParameterString(Taxon taxon, 
			Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors, 
			int width, 
			int height, 
			String bbox, 
			String backLayer,
			List<Language> langs);
	
	
	public String getOccurrenceServiceRequestParameterString(
			List<SpecimenOrObservationBase> specimensOrObersvations, 
			Map<Class<? extends SpecimenOrObservationBase>,Color> specimenOrObservationTypeColors,
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
	
}
