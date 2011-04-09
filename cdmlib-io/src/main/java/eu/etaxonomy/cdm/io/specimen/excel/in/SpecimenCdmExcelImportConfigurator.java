/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public class SpecimenCdmExcelImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImportConfigurator.class);
	private static IInputTransformer defaultTransformer = new SpecimenCdmExcelTransformer();
	
	//old
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = false;
	private String taxonReference = null;
	
	//new
	private boolean doSpecimen = true;
	private boolean doAreaLevels = true;
	private boolean useCountry;  //if isocountry and country is available, use country instead of isocountry 
	private PersonParserFormatEnum personParserFormat = PersonParserFormatEnum.POSTFIX;  //
	private boolean useHexagesimalCoordinates;
	
	
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			SpecimenCdmExcelImport.class,
		};
	}
	
	public static SpecimenCdmExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new SpecimenCdmExcelImportConfigurator(uri, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private SpecimenCdmExcelImportConfigurator(URI uri, ICdmDataSource destination) {
		super(uri, destination, defaultTransformer);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public SpecimenCdmExcelImportState getNewState() {
		return new SpecimenCdmExcelImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("Specimen import", true);
		}
		return sourceReference;
	}
	



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}
	
// **************************** OLD ******************************************	
	
	public void setTaxonReference(String taxonReference) {
		this.taxonReference = taxonReference;
	}
	
	public Reference getTaxonReference() {
		//TODO
		if (this.taxonReference == null){
			logger.info("getTaxonReference not yet fully implemented");
		}
		return sourceReference;
	}
	
	public void setDoAutomaticParsing(boolean doParsing){
		this.doParsing=doParsing;
	}
	
	public boolean getDoAutomaticParsing(){
		return this.doParsing;
	}
	
	public void setReUseExistingMetadata(boolean reuseMetadata){
		this.reuseMetadata = reuseMetadata;
	}
	
	public boolean getReUseExistingMetadata(){
		return this.reuseMetadata;
	}
	
	public void setReUseTaxon(boolean reuseTaxon){
		this.reuseTaxon = reuseTaxon;
	}
	
	public boolean getDoReUseTaxon(){
		return this.reuseTaxon;
	}

	public void setDoSpecimen(boolean doSpecimen) {
		this.doSpecimen = doSpecimen;
	}

	public boolean isDoSpecimen() {
		return doSpecimen;
	}

	public void setDoAreaLevels(boolean doAreaLevels) {
		this.doAreaLevels = doAreaLevels;
	}

	public boolean isDoAreaLevels() {
		return doAreaLevels;
	}
	
	
	
}
