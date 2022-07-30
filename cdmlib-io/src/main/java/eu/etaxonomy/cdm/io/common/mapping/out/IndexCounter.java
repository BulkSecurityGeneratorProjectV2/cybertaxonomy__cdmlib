/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class IndexCounter {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	int index = 0;

	public IndexCounter(int startValue){
		index = startValue;
	}

	/**
	 * Returns the index and increases it by 1
	 * @return
	 */
	public int getIncreasing(){
		return index++;
	}

	@Override
    public String toString(){
		return String.valueOf(index);
	}

}
