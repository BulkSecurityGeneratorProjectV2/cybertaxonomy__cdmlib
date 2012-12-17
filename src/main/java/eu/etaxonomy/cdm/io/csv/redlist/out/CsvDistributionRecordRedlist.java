// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.PrintWriter;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class CsvDistributionRecordRedlist extends CsvRecordBaseRedlist implements ICsvAreaRecord{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CsvDistributionRecordRedlist.class);
	
	private CsvId locationId;
	private String locationIdString;
	private String locality;
	private String countryCode;
	private Stage lifeStage;
	private PresenceAbsenceTermBase<?> occurrenceStatus;
	private String threadStatus;
	
	private PresenceAbsenceTermBase establishmentMeans;
	private String appendixCITES;
	private TimePeriod eventDate;
	
	private TimePeriod seasonalDate; //startDayOfYear and endDayOfYear
	private String source;
	private String occurrenceRemarks;
	
	
	public CsvDistributionRecordRedlist(CsvMetaDataRecordRedlist metaDataRecord, CsvTaxExportConfiguratorRedlist config){
		super(metaDataRecord, config);
		locationId = new CsvId(config);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
	 */
	protected void registerKnownFields(){
		try {
			addKnownField("locationID", "http://rs.tdwg.org/dwc/terms/locationID");
			addKnownField("locality", "http://rs.tdwg.org/dwc/terms/locality");
			addKnownField("countryCode", "http://rs.tdwg.org/dwc/terms/countryCode");
			addKnownField("lifeStage", "http://rs.tdwg.org/dwc/terms/lifeStage");
			addKnownField("threatStatus", "http://iucn.org/terms/threatStatus");
			addKnownField("occurrenceStatus", "http://rs.tdwg.org/dwc/terms/occurrenceStatus");
			addKnownField("establishmentMeans", "http://rs.tdwg.org/dwc/terms/establishmentMeans");
			addKnownField("appendixCITES", "http://rs.gbif.org/terms/1.0/appendixCITES");
			addKnownField("eventDate", "http://rs.tdwg.org/dwc/terms/eventDate");
			addKnownField("startDayOfYear", "http://rs.tdwg.org/dwc/terms/startDayOfYear");
			addKnownField("endDayOfYear", "http://rs.tdwg.org/dwc/terms/endDayOfYear");
			addKnownField("source", "http://purl.org/dc/terms/source");
			addKnownField("occurrenceRemarks", "http://rs.tdwg.org/dwc/terms/occurrenceRemarks");

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{"coreid", 
//				"locationId",
//				"locality",
//				"countryCode", 
//				"lifeStage", 
//				"occurrenceStatus", 
//				"threadStatus", 
//				"establishmentMeans", 
//				"appendixCITES", 
//				"eventDate", 
//				"startDayOfYear", 
//				"endDayOfYear", 
//				"source", 
//				"occurrenceRemarks"};
//		return Arrays.asList(result);
//	}
	
	public void write(PrintWriter writer) {
		printId(getUuid(), writer, IS_FIRST, "coreid");
		if (StringUtils.isNotBlank(locationIdString)){
			print(locationIdString, writer, IS_NOT_FIRST, TermUri.DWC_LOCATION_ID);
		}else{
			print(locationId, writer, IS_NOT_FIRST, TermUri.DWC_LOCATION_ID);
		}
		print(locality, writer, IS_NOT_FIRST, TermUri.DWC_LOCALITY);
		print(countryCode, writer, IS_NOT_FIRST, TermUri.DWC_COUNTRY_CODE);
		print(getLifeStage(lifeStage), writer, IS_NOT_FIRST, TermUri.DWC_LIFESTAGE);
		print(getOccurrenceStatus(occurrenceStatus), writer, IS_NOT_FIRST, TermUri.DWC_OCCURRENCE_STATUS);
		print(threadStatus, writer, IS_NOT_FIRST, TermUri.IUCN_THREAD_STATUS);
		print(getEstablishmentMeans(establishmentMeans), writer, IS_NOT_FIRST, TermUri.DWC_ESTABLISHMENT_MEANS);
		print(appendixCITES, writer, IS_NOT_FIRST, TermUri.GBIF_APPENDIX_CITES);
		print(getTimePeriod(eventDate),writer, IS_NOT_FIRST, TermUri.DWC_EVENT_DATE);
		print(getTimePeriodPart(seasonalDate, false),writer, IS_NOT_FIRST, TermUri.DWC_START_DAY_OF_YEAR);
		//TODO
		print(getTimePeriodPart(seasonalDate, true),writer, IS_NOT_FIRST, TermUri.DWC_END_DAY_OF_YEAR);
		print(source, writer, IS_NOT_FIRST, TermUri.DC_SOURCE);
		print(occurrenceRemarks, writer, IS_NOT_FIRST, TermUri.DWC_OCCURRENCE_REMARKS);
		writer.println();
	}


	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getLocality() {
		return locality;
	}


	public void setLocality(String locality) {
		this.locality = locality;
	}


	public String getCountryCode() {
		return countryCode;
	}


	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}


	public Stage getLifeStage() {
		return lifeStage;
	}


	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}


	public String getLocationId() {
		return locationId.getId();
	}

	public void setLocationId(NamedArea area) {
		if (area.isInstanceOf(TdwgArea.class)){
			String locationId = "TDWG:" + area.getRepresentation(Language.ENGLISH()).getAbbreviatedLabel();
			this.locationIdString = locationId;
		}else if (area.isInstanceOf(WaterbodyOrCountry.class)){
			WaterbodyOrCountry country = CdmBase.deproxy(area, WaterbodyOrCountry.class);
			String locationId = "ISO3166:" + country.getIso3166_A2();
			this.locationIdString = locationId;
		}else{
			this.locationId.setId(area);
		}
	}

	public PresenceAbsenceTermBase<?> getOccurrenceStatus() {
		return occurrenceStatus;
	}


	public void setOccurrenceStatus(PresenceAbsenceTermBase<?> occurrenceStatus) {
		this.occurrenceStatus = occurrenceStatus;
	}


	public String getThreadStatus() {
		return threadStatus;
	}


	public void setThreadStatus(String threadStatus) {
		this.threadStatus = threadStatus;
	}


	public PresenceAbsenceTermBase getEstablishmentMeans() {
		return establishmentMeans;
	}


	public void setEstablishmentMeans(PresenceAbsenceTermBase establishmentMeans) {
		this.establishmentMeans = establishmentMeans;
	}


	public String getAppendixCITES() {
		return appendixCITES;
	}


	public void setAppendixCITES(String appendixCITES) {
		this.appendixCITES = appendixCITES;
	}


	public TimePeriod getEventDate() {
		return eventDate;
	}


	public void setEventDate(TimePeriod eventDate) {
		this.eventDate = eventDate;
	}


	public TimePeriod getSeasonalDate() {
		return seasonalDate;
	}


	public void setSeasonalDate(TimePeriod seasonalDate) {
		this.seasonalDate = seasonalDate;
	}


	public String getOccurrenceRemarks() {
		return occurrenceRemarks;
	}


	public void setOccurrenceRemarks(String occurrenceRemarks) {
		this.occurrenceRemarks = occurrenceRemarks;
	}

	
}
