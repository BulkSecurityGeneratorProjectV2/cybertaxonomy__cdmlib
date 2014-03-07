/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;


public abstract class StrategyBase implements IStrategy, Serializable {
	private static final long serialVersionUID = -274791080847215663L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StrategyBase.class);
	
	final static UUID uuid = UUID.fromString("2ff2b1d6-17a6-4807-a55f-f6b45bf429b7");

	abstract protected UUID getUuid();
	
	protected StrategyBase(){
	}

	


	/**
	 * @param fieldType
	 * @return
	 */
	protected static boolean isCollection(Class<?> fieldType) {
		if (Collection.class.isAssignableFrom(fieldType) ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isPrimitive(Class<?> fieldType) {
		if (fieldType.isPrimitive()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isSingleCdmBaseObject(Class<?> fieldType) {
		if (CdmBase.class.isAssignableFrom(fieldType)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isUserType(Class<?> fieldType) {
		if (	fieldType == TimePeriod.class ||
				fieldType == DateTime.class ||
				fieldType == LSID.class ||
				fieldType == Contact.class ||
				fieldType == URI.class ||
				fieldType == DOI.class
			){
				return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Null safe string. Returns the given string if it is not <code>null</code>.
	 * Empty string otherwise. 
	 * @see CdmUtils#Nz(String)
	 * @return the null-safe string
	 */
	protected String Nz(String str){
		return CdmUtils.Nz(str);
	}
	
	/**
	 * Checks if a string is not blank.
	 * @see StringUtils#isNotBlank(String)
	 */
	protected boolean isNotBlank(String str){
		return StringUtils.isNotBlank(str);
	}
	
	/**
	 * Checks if a string is blank.
	 * @see StringUtils#isNotBlank(String)
	 */
	protected boolean isBlank(String str){
		return StringUtils.isBlank(str);
	}
	
	
	
}
