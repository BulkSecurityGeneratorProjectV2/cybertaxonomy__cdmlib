/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 */
public class UuidGenerator {
	private static final Logger logger = LogManager.getLogger(UuidGenerator.class);

	private static int n = 100;

	public static void main(String[] args) {
		if (logger.isDebugEnabled()){logger.debug("create UUIDs");}
		for (int i = 0; i < n; i++){
			System.out.println(UUID.randomUUID());
		}
	}
}
