// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author a.mueller
 * @created 17.11.2008
 * @version 1.0
 */
public class EditGeoServiceUtilities {
	private static final Logger logger = Logger.getLogger(EditGeoServiceUtilities.class);

	private static PresenceAbsenceTermBase<?> defaultStatus = PresenceTerm.PRESENT();
	
	private static HashMap<Class<? extends SpecimenOrObservationBase>, Color> defaultSpecimenOrObservationTypeColors;
	private static HashMap<PresenceAbsenceTermBase<?>, Color> defaultPresenceAbsenceTermBaseColors;

	private static final String SUBENTRY_DELIMITER = ",";
	private static final String ENTRY_DELIMITER = ";";
	static final String ID_FROM_VALUES_SEPARATOR = ":";
	static final String VALUE_LIST_ENTRY_SEPARATOR = "|";
	static final String VALUE_SUPER_LIST_ENTRY_SEPARATOR = "||";
	
	static {
		defaultSpecimenOrObservationTypeColors = new HashMap<Class<? extends SpecimenOrObservationBase>, Color>();
		defaultSpecimenOrObservationTypeColors.put(FieldObservation.class, Color.ORANGE);
		defaultSpecimenOrObservationTypeColors.put(DerivedUnit.class, Color.RED);
		defaultSpecimenOrObservationTypeColors.put(LivingBeing.class, Color.GREEN);
		defaultSpecimenOrObservationTypeColors.put(Observation.class, Color.ORANGE);
		defaultSpecimenOrObservationTypeColors.put(Specimen.class, Color.GRAY);
	}
	
	static {
		defaultPresenceAbsenceTermBaseColors = new HashMap<PresenceAbsenceTermBase<?>, Color>();
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.PRESENT(), Color.decode("0x4daf4a"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.NATIVE(), Color.decode("0x4daf4a"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_ADVENTITIOUS(), Color.decode("0xffff33"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
		defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_NATURALIZED(), Color.decode("0xf781bf"));	
	}
	


	//preliminary implementation for TDWG areas
	/**
	 * Returns the parameter String for the EDIT geo webservice to create a
	 * dsitribution map.
	 * 
	 * @param distributions
	 *            A set of distributions that should be shown on the map
	 * @param presenceAbsenceTermColors
	 *            A map that defines the colors of PresenceAbsenceTerms. The
	 *            PresenceAbsenceTerms are defined by their uuid. If a
	 *            PresenceAbsenceTerm is not included in this map, it's default
	 *            color is taken instead. If the map == null all terms are
	 *            colored by their default color.
	 * @param width
	 *            The maps width
	 * @param height
	 *            The maps height
	 * @param bbox
	 *            The maps bounding box (e.g. "-180,-90,180,90" for the whole
	 *            world)
	 * @param layer
	 *            The layer that is responsible for background borders and
	 *            colors. Use the name for the layer. If null 'earth' is taken
	 *            as default.
	 * @return the parameter string or an empty string if the
	 *         <code>distributions</code> set was null or empty.
	 */
	@Transient
	public static String getDistributionServiceRequestParameterString(
			Set<Distribution> distributions, 
			Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors, 
			int width, 
			int height, 
			String bbox, 
			String backLayer,
			List<Language> languages){
		

		/**
		 * generateMultipleAreaDataParameters switches between the two possible styles:
		 * 1. ad=layername1:area-data||layername2:area-data
		 * 2. ad=layername1:area-data&ad=layername2:area-data
		 */
		boolean generateMultipleAreaDataParameters = false;

		List<String>  perLayerAreaData = new ArrayList<String>();
		List<String> areaStyles = new ArrayList<String>();
		List<String> legendLabels = new ArrayList<String>();

		
		String borderWidth = "0.1";
		String borderColorRgb = "";
		String borderDashingPattern = "";
		
		
		if(distributions == null || distributions.size() == 0){
			return "";
		}
		Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<String, Map<Integer, Set<Distribution>>>(); 
		List<PresenceAbsenceTermBase<?>> statusList = new ArrayList<PresenceAbsenceTermBase<?>>();
		groupStylesAndLayers(distributions, layerMap, statusList);
				
		presenceAbsenceTermColors = mergeMaps(defaultPresenceAbsenceTermBaseColors, presenceAbsenceTermColors);

		Map<String, String> parameters = new HashMap<String, String>();
		
		//bbox
		if (bbox != null){
			parameters.put("bbox", bbox);
		}
		// map size
		String ms = compileMapSizeParameterValue(width, height);
		if(ms != null){
			parameters.put("ms", ms);
		}
		//layer
		if (StringUtils.isBlank(backLayer)){
			backLayer = "earth"; 
		}
		parameters.put("l", backLayer);
		
		//style
		int i = 0;
		for (PresenceAbsenceTermBase<?> status: statusList){
			
			char styleId = getStyleAbbrev(i);
			
			//getting the area title
			if (languages == null){
				languages = new ArrayList<Language>();
			}
			if (languages.size() == 0){
				languages.add(Language.DEFAULT());
			}
			Representation representation = status.getPreferredRepresentation(languages);
			String statusLabel = representation.getLabel();
			//statusLabel.replace('introduced: ', '');
			statusLabel = statusLabel.replace("introduced: ", "introduced, ");
			statusLabel = statusLabel.replace("native: ", "native,  ");
			
			//getting the area color
			Color statusColor = presenceAbsenceTermColors.get(status);
			String fillColorRgb;
			if (statusColor != null){
				fillColorRgb = Integer.toHexString(statusColor.getRGB()).substring(2);
			}else{
				if(status != null){
					fillColorRgb = status.getDefaultColor(); //TODO
				} else {
					fillColorRgb = defaultStatus.getDefaultColor();
				}
			}
			String styleValues = StringUtils.join(new String[]{fillColorRgb, borderColorRgb, borderWidth, borderDashingPattern}, ',');			

			areaStyles.add(styleId + ID_FROM_VALUES_SEPARATOR + styleValues);
			legendLabels.add(styleId + ID_FROM_VALUES_SEPARATOR + encode(statusLabel));
			i++;			
		}
		
		if(areaStyles.size() > 0){
			parameters.put("as", StringUtils.join(areaStyles.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
		}
		if(legendLabels.size() > 0){
			parameters.put("title", StringUtils.join(legendLabels.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
		}
		
		// area data
		List<String> stylesPerLayer;
		List<String> areasPerStyle;
		for (String layerString : layerMap.keySet()){
			// each layer
			stylesPerLayer = new ArrayList<String>();
			Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
			for (int style: styleMap.keySet()){
				// stylesPerLayer
				char styleChar = getStyleAbbrev(style);
				Set<Distribution> distributionSet = styleMap.get(style);
				areasPerStyle = new ArrayList<String>();
				for (Distribution distribution: distributionSet){
					// areasPerStyle
					areasPerStyle.add(getAreaAbbrev(distribution));
				}
				stylesPerLayer.add(styleChar + ID_FROM_VALUES_SEPARATOR + StringUtils.join(areasPerStyle.iterator(), SUBENTRY_DELIMITER));
			}
			perLayerAreaData.add(encode(layerString) + ID_FROM_VALUES_SEPARATOR + StringUtils.join(stylesPerLayer.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
		}
		
		if(generateMultipleAreaDataParameters){
			// not generically possible since parameters can not contain duplicate keys with value "ad"
		} else {
			parameters.put("ad", StringUtils.join(perLayerAreaData.iterator(), VALUE_SUPER_LIST_ENTRY_SEPARATOR));
		}

		String queryString = makeQueryString(parameters);
		logger.debug("getDistributionServiceRequestParameterString(): " + queryString);
		
		return queryString;
	}

	private static void groupStylesAndLayers(Set<Distribution> distributions,
			Map<String, Map<Integer, Set<Distribution>>> layerMap,
			List<PresenceAbsenceTermBase<?>> statusList) {
		//iterate through distributions and group styles and layers
		//and collect necessary information
		for (Distribution distribution:distributions){
			//collect status
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
			if(status == null){
				status = defaultStatus;
			}
			if (! statusList.contains(status)){
				statusList.add(status);
			}
			//group by layers and styles
			NamedArea area = distribution.getArea();
			if (area != null){
				String geoLayerString = getWMSLayerName(area);
				if(geoLayerString != null){
					Map<Integer, Set<Distribution>> styleMap = layerMap.get(geoLayerString);
					if (styleMap == null){
						styleMap = new HashMap<Integer, Set<Distribution>>();
						layerMap.put(geoLayerString, styleMap);
					}
					addDistributionToMap(distribution, styleMap, statusList);
				}
			}
		}
	}

	private static String compileMapSizeParameterValue(int width, int height) {

		String widthStr = "";
		String heightStr = "";

		if (width > 0) {
			widthStr = "" + width;
		}
		if (height > 0) {
			heightStr = SUBENTRY_DELIMITER + height;
		}
		String ms = widthStr + heightStr;
		if(ms.length() == 0){
			ms = null;
		}
		return ms;
	}

	/**
	 * URI encode the given String
	 * @param string
	 * @return
	 */
	private static String encode(String string) {
		String encoded = string;
		try {
			encoded = URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return encoded;
	}
	
	/**
	 * combine parameter into a URI query string fragment. The values will be
	 * escaped correctly.
	 * 
	 * @param parameters
	 * @return a URI query string fragment
	 */
	private static String makeQueryString(Map<String, String> parameters){
		StringBuilder queryString = new StringBuilder();
		for (String key : parameters.keySet()) {
			if(queryString.length() > 0){
				queryString.append('&');
			}
			if(key.equals("od") || key.equals("os") || key.equals("ms") || key.equals("ad") || key.equals("as") || key.equals("title") || key.equals("bbox")){
				queryString.append(key).append('=').append(parameters.get(key));				
			} else {
				queryString.append(key).append('=').append(encode(parameters.get(key)));
			}
		}
		return queryString.toString();
	}
	
	private static String getAreaAbbrev(Distribution distribution){
		NamedArea area = distribution.getArea();
		Representation representation = area.getRepresentation(Language.DEFAULT());
		String areaAbbrev = representation.getAbbreviatedLabel();
		if (area.getLevel() != null && area.getLevel().equals(NamedAreaLevel.TDWG_LEVEL4())){
			areaAbbrev = areaAbbrev.replace("-", "");
		}
		return CdmUtils.Nz(areaAbbrev, "-");
	}
	

	//Preliminary as long as user defined areas are not fully implemented  
	public static final UUID uuidCyprusDivisionsVocabulary = UUID.fromString("2119f610-1f93-4d87-af28-40aeefaca100");
	
	private static String getWMSLayerName(NamedArea area){
		TermVocabulary<NamedArea> voc = area.getVocabulary();
		//TDWG areas
		if (voc.getUuid().equals(TdwgArea.uuidTdwgAreaVocabulary)){
			NamedAreaLevel level = area.getLevel();
			if (level != null) {
				//TODO integrate into CDM 
				if (level.equals(NamedAreaLevel.TDWG_LEVEL1())) {
					return "tdwg1";
				} else if (level.equals(NamedAreaLevel.TDWG_LEVEL2())) {
					return "tdwg2";
				}else if (level.equals(NamedAreaLevel.TDWG_LEVEL3())) {
					return "tdwg3";
				}else if (level.equals(NamedAreaLevel.TDWG_LEVEL4())) {
					return "tdwg4";
				}
			}
			//unrecognized tdwg area
			return null;
		
		}
		//hardcoded for cyprus (as long as user defined areas are not fully implemented). Remove afterwards.
		if (voc.getUuid().equals(uuidCyprusDivisionsVocabulary)){
			return "cyprusdivs:bdcode";
		}
		return null;
	}
	
	
	private static void addDistributionToMap(Distribution distribution, Map<Integer, Set<Distribution>> styleMap,
			List<PresenceAbsenceTermBase<?>> statusList) {
		PresenceAbsenceTermBase<?> status = distribution.getStatus();
		if (status == null) {
			status = defaultStatus;
		}
		int style = statusList.indexOf(status);
		Set<Distribution> distributionSet = styleMap.get(style);
		if (distributionSet == null) {
			distributionSet = new HashSet<Distribution>();
			styleMap.put(style, distributionSet);
		}
		distributionSet.add(distribution);
	}
	
	/**
	 * @param fieldObservationPoints
	 * @param derivedUnitPoints
	 * @param specimenOrObservationTypeColors
	 * @param doReturnImage TODO
	 * @param width
	 * @param height
	 * @param bbox
	 * @param backLayer
	 * @return
	 * e.g.:
	 * 	l=v%3Aatbi%2Ce_w_0
	 *  &legend=0
	 *  &image=false
	 *  &recalculate=false
	 *  &ms=400%2C350

	 *  &od=1%3A44.29481%2C6.82161|44.29252%2C6.822873|44.29247%2C6.82346|44.29279%2C6.823678|44.29269%2C6.82394|44.28482%2C6.887252|44.11469%2C7.287144|44.11468%2C7.289168
	 *  &os=1%3Ac%2FFFD700%2F10%2FAporrectodea caliginosa
	 */
	public static String getOccurrenceServiceRequestParameterString(
			List<Point> fieldObservationPoints,
			List<Point> derivedUnitPoints,
			Map<Class<? extends SpecimenOrObservationBase>, Color> specimenOrObservationTypeColors,
			Boolean doReturnImage, Integer width, Integer height, String bbox, String backLayer) {
		
			specimenOrObservationTypeColors = mergeMaps(defaultSpecimenOrObservationTypeColors, specimenOrObservationTypeColors);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("legend", "0");
			parameters.put("image", doReturnImage != null && doReturnImage ? "true" : "false");
			parameters.put("recalculate", "false"); // TODO add parameter to method
			if(bbox != null){
				parameters.put("bbox", bbox);
			}
			if(width != null || height != null){
				parameters.put("ms", compileMapSizeParameterValue(width, height));
			}
			
			Map<String, String> styleAndData = new HashMap<String, String>();
			
			addToStyleAndData(fieldObservationPoints, FieldObservation.class, specimenOrObservationTypeColors, styleAndData);
			addToStyleAndData(derivedUnitPoints, DerivedUnit.class, specimenOrObservationTypeColors, styleAndData);
			
			parameters.put("os", StringUtils.join(styleAndData.keySet().iterator(), "||"));
			parameters.put("od", StringUtils.join(styleAndData.values().iterator(), "||"));
			
			String queryString = makeQueryString(parameters);
			
			logger.info(queryString);
			
		return queryString;
	}

	/**
	 * @param <T>
	 * @param <S>
	 * @param defaultMap
	 * @param overrideMap
	 * @return
	 */
	private static <T, S> Map<T, S> mergeMaps(Map<T, S> defaultMap, Map<T, S> overrideMap) {
		Map<T, S> tmpMap = new HashMap<T, S>();
		tmpMap.putAll(defaultMap);
		if(overrideMap != null){				
			tmpMap.putAll(overrideMap);
		}
		return tmpMap;
	}

	private static void addToStyleAndData(
			List<Point> points,
			Class<? extends SpecimenOrObservationBase> specimenOrObservationType,
			Map<Class<? extends SpecimenOrObservationBase>, Color> specimenOrObservationTypeColors, Map<String, String> styleAndData) {

		//TODO add markerShape and size and Label to specimenOrObservationTypeColors -> Map<Class<SpecimenOrObservationBase<?>>, MapStyle>
		
		if(points != null && points.size()>0){
			String style =  "c/" + Integer.toHexString(specimenOrObservationTypeColors.get(specimenOrObservationType).getRGB()).substring(2) + "/10/noLabel";
			StringBuilder data = new StringBuilder();
			for(Point point : points){
				if(data.length() > 0){
					data.append('|');
				}
				data.append(point.getLatitude() + "," + point.getLongitude());
			}
			int index = styleAndData.size() + 1;
			styleAndData.put(index + ":" +style, index + ":" +data.toString());
		}
	}
	
	
	/**
	 * transform an integer (style counter) into a valid character representing a style.
	 * 0-25 => a-z<br>
	 * 26-51 => A-Z<br>
	 * i not in {0,...,51} is undefined
	 * @param i
	 * @return
	 */
	private static char getStyleAbbrev(int i){
		i++;
		int ascii = 96 + i;
		if (i >26){
			ascii = 64 + i;
		}
		return (char)ascii;
	}
}
