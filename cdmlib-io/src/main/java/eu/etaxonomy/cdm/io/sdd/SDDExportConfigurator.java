/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.SDDCdmExporter;

/**
 * @author h.fradin (from a.babadshanjan JaxbExportConfigurator)
 * @created 09.12.2008
 * @version 1.0
 */
public class SDDExportConfigurator extends ExportConfiguratorBase implements IExportConfigurator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDExportConfigurator.class);

	private int maxRows = 0;

//	private boolean doAgents = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
//	private boolean doOccurrences = true;
//	private boolean doReferences = true;
	private boolean doReferencedEntities = true;
//	private boolean doRelationships = true;
	private boolean doSynonyms = true;
//	private boolean doTaxonNames = true;
//	private boolean doTaxa = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;
	
	
	public static SDDExportConfigurator NewInstance(ICdmDataSource source, String url, String destinationFolder) {
		return new SDDExportConfigurator(source, url);
	}
	
	
	public int getMaxRows() {
		return maxRows;
	}
	
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
	

	public boolean isDoAgentData() {
		return doAgentData;
	}
	
	public void setDoAgentData(boolean doAgentData) {
		this.doAgentData = doAgentData;
	}

	public boolean isDoLanguageData() {
		return doLanguageData;
	}
	
	public void setDoLanguageData(boolean doLanguageData) {
		this.doLanguageData = doLanguageData;
	}

	public boolean isDoFeatureData() {
		return doFeatureData;
	}
	
	public void setDoFeatureData(boolean doFeatureData) {
		this.doFeatureData = doFeatureData;
	}

	public boolean isDoDescriptions() {
		return doDescriptions;
	}
	
	public void setDoDescriptions(boolean doDescriptions) {
		this.doDescriptions = doDescriptions;
	}

	public boolean isDoMedia() {
		return doMedia;
	}
	
	public void setDoMedia(boolean doMedia) {
		this.doMedia = doMedia;
	}

	public boolean isDoReferencedEntities() {
		return doReferencedEntities;
	}
	
	public void setDoReferencedEntities(boolean doReferencedEntities) {
		this.doReferencedEntities = doReferencedEntities;
	}


	public boolean isDoSynonyms() {
		return doSynonyms;
	}
	
	public void setDoSynonyms(boolean doSynonyms) {
		this.doSynonyms = doSynonyms;
	}


	public boolean isDoTerms() {
		return doTerms;
	}
	
	public void setDoTerms(boolean doTerms) {
		this.doTerms = doTerms;
	}

	public boolean isDoTermVocabularies() {
		return doTermVocabularies;
	}
	
	public void setDoTermVocabularies(boolean doTermVocabularies) {
		this.doTermVocabularies = doTermVocabularies;
	}

	public boolean isDoHomotypicalGroups() {
		return doHomotypicalGroups;
	}
	
	public void setDoHomotypicalGroups(boolean doHomotypicalGroups) {
		this.doHomotypicalGroups = doHomotypicalGroups;
	}

	
//	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				SDDCdmExporter.class,
		};
	};


	
	
	/**
	 * @param url
	 * @param destination
	 */
	private SDDExportConfigurator(ICdmDataSource source, String url) {
		super();
		setDestination(url);
		setSource(source);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public File getDestination() {
		return (File)super.getDestination();
	}

	
	/**
	 * @param file
	 */
	public void setDestination(String fileName) {
		super.setDestination(fileName);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getDestinationNameString()
	 */
	public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().toString();
		}
	}
	
		
}
