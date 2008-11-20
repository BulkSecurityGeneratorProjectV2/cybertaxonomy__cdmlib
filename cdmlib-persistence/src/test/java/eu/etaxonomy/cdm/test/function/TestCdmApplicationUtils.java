/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;

/**
 * @author a.mueller
 * @created 20.11.2008
 * @version 1.0
 */
public class TestCdmApplicationUtils {
	private static final Logger logger = Logger.getLogger(TestCdmApplicationUtils.class);

	private boolean testWritableResourceDirectory(){
		CdmApplicationUtils.getWritableResourceDir();
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestCdmApplicationUtils me = new TestCdmApplicationUtils();
		me.testWritableResourceDirectory();

	}
}
