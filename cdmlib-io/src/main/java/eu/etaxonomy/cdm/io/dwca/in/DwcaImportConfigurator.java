/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.MappingType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public class DwcaImportConfigurator extends ImportConfiguratorBase<DwcaImportState, URI> implements IImportConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportConfigurator.class);
	private static IInputTransformer defaultTransformer = new DwcaImportTransformer();
	
	//partitions
	private boolean usePartitions = true;
	private int defaultPartitionSize = 2000;
	
	//taxon
	private boolean deduplicateNamePublishedIn = true;
	private boolean scientificNameIdAsOriginalSourceId = false;
	private DatasetUse datasetUse = DatasetUse.CLASSIFICATION;
	
	//distribution
	private boolean excludeLocality = false;   //if set to true the dwc locality is not considered during distribution import
	
	//validation
	private boolean validateRankConsistency = true;
	
	private IImportMapping.MappingType mappingType = MappingType.InMemoryMapping;
	
	public enum DatasetUse{
		CLASSIFICATION,
		SECUNDUM,
		ORIGINAL_SOURCE
		
	}
	
		


	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			DwcaImport.class
		};
	}
	
	public static DwcaImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new DwcaImportConfigurator(uri, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private DwcaImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		this.setSource(uri);
		this.setDestination(destination);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public DwcaImportState getNewState() {
		return new DwcaImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		if (this.sourceReference == null){
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache("DwC-A import", true);
		}
		if (getSourceRefUuid() != null){
			sourceReference.setUuid(getSourceRefUuid());
		}else{
			setSourceRefUuid(sourceReference.getUuid());
		}
		return sourceReference;
	}

	
	
	public boolean isUsePartitions() {
		return usePartitions;
	}

	public void setUsePartitions(boolean usePartitions) {
		this.usePartitions = usePartitions;
	}

	public void setDefaultPartitionSize(int defaultPartitionSize) {
		this.defaultPartitionSize = defaultPartitionSize;
	}

	public int getDefaultPartitionSize() {
		return defaultPartitionSize;
	}

	public IImportMapping.MappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(IImportMapping.MappingType mappingType) {
		this.mappingType = mappingType;
	}

	public boolean isDeduplicateNamePublishedIn() {
		return deduplicateNamePublishedIn;
	}

	public void setDeduplicateNamePublishedIn(boolean deduplicateNamePublishedIn) {
		this.deduplicateNamePublishedIn = deduplicateNamePublishedIn;
	}

	public void setScientificNameIdAsOriginalSourceId(
			boolean scientificNameIdAsOriginalSourceId) {
		this.scientificNameIdAsOriginalSourceId = scientificNameIdAsOriginalSourceId;
	}

	public boolean isScientificNameIdAsOriginalSourceId() {
		return scientificNameIdAsOriginalSourceId;
	}

	public boolean isDatasetsAsClassifications() {
		return this.datasetUse.equals(DatasetUse.CLASSIFICATION);
	}

	public boolean isExcludeLocality() {
		return excludeLocality;
	}

	public void setExcludeLocality(boolean excludeLocality) {
		this.excludeLocality = excludeLocality;
	}

	public boolean isValidateRankConsistency() {
		return validateRankConsistency;
	}

	public void setValidateRankConsistency(boolean validateRankConsistency) {
		this.validateRankConsistency = validateRankConsistency;
	}

	public boolean isDatasetsAsSecundumReference() {
		return this.datasetUse.equals(DatasetUse.SECUNDUM);
	}

	public boolean isDatasetsAsOriginalSource() {
		return this.datasetUse.equals(DatasetUse.ORIGINAL_SOURCE);
	}
	
	
}
