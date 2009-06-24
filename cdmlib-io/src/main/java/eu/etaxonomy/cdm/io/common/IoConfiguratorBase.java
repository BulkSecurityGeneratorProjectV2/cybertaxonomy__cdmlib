/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class IoConfiguratorBase implements IIoConfigurator{
	private static final Logger logger = Logger.getLogger(IoConfiguratorBase.class);

	//im-/export uses TaxonomicTree for is_taxonomically_included_in relationships
	private boolean useTaxonomicTree = true;
	
//	protected Class<ICdmIO>[] ioClassList;
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	
	private CdmApplicationController cdmApp = null;
	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
	private boolean doNameStatus = true;
	private boolean doTypes = true;
	private boolean doNameFacts = true;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	private boolean doFacts = true;

	//occurrence
	private boolean doOccurrence = true;
	
	//etc
	private boolean doMarker = true;
	private boolean doUser = true;

	
	public DbSchemaValidation getDbSchemaValidation() {
		return dbSchemaValidation;
	}

	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation) {
		this.dbSchemaValidation = dbSchemaValidation;
	}
	
	public CdmApplicationController getCdmAppController(){
		return this.cdmApp;
	}
	
	/**
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmAppController(CdmApplicationController cdmApp) {
		this.cdmApp = cdmApp;
	}
	
	public boolean isDoAuthors() {
		return doAuthors;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoAuthors(boolean)
	 */
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getDoReferences()
	 */
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoReferences(eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator.DO_REFERENCES)
	 */
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTaxonNames(boolean)
	 */
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoRelNames()
	 */
	public boolean isDoRelNames() {
		return doRelNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoRelNames(boolean)
	 */
	public void setDoRelNames(boolean doRelNames) {
		this.doRelNames = doRelNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoNameStatus()
	 */
	public boolean isDoNameStatus() {
		return doNameStatus;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoNameStatus(boolean)
	 */
	public void setDoNameStatus(boolean doNameStatus) {
		this.doNameStatus = doNameStatus;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoNameFacts()
	 */
	public boolean isDoNameFacts() {
		return doNameFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoNameFacts(boolean)
	 */
	public void setDoNameFacts(boolean doNameFacts) {
		this.doNameFacts = doNameFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoTypes()
	 */
	public boolean isDoTypes() {
		return doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTypes(boolean)
	 */
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoTaxa()
	 */
	public boolean isDoTaxa() {
		return doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTaxa(boolean)
	 */
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoRelTaxa()
	 */
	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoRelTaxa(boolean)
	 */
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoFacts()
	 */
	public boolean isDoFacts() {
		return doFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoFacts(boolean)
	 */
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoOccurrence()
	 */
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoOccurrence(boolean)
	 */
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}

	public boolean isDoMarker() {
		return doMarker;
	}

	public void setDoMarker(boolean doMarker) {
		this.doMarker = doMarker;
	}
	

	public boolean isDoUser() {
		return doUser;
	}

	public void setDoUser(boolean doUser) {
		this.doUser = doUser;
	}
	

	/**
	 * @return the useTaxonomicTree
	 */
	public boolean isUseTaxonomicTree() {
		return useTaxonomicTree;
	}

	/**
	 * @param useTaxonomicTree the useTaxonomicTree to set
	 */
	public void setUseTaxonomicTree(boolean useTaxonomicTree) {
		this.useTaxonomicTree = useTaxonomicTree;
	}
}
