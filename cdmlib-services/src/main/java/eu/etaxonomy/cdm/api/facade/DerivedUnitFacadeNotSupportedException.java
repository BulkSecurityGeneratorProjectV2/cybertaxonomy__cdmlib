/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

/**
 * This exception should be thrown if a derived unit facade can not be created or 
 * initialized because the underlying data complexity is not supported by the facade.
 * 
 * @see MethodNotSupportedByDerivedUnitTypeException
 * 
 * @author a.mueller
 * @since 17.05.2010
 *
 */
public class DerivedUnitFacadeNotSupportedException extends Exception {
	private static final long serialVersionUID = -2593445506656913492L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DerivedUnitFacadeNotSupportedException.class);


	/**
	 * @param arg0
	 */
	public DerivedUnitFacadeNotSupportedException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public DerivedUnitFacadeNotSupportedException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DerivedUnitFacadeNotSupportedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}


}
