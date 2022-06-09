/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * A mapper base class for all mappers mapping source attributes to destination attributes.
 *
 * @author a.mueller
 * @since 05.08.2008
 */
public abstract class CdmAttributeMapperBase extends CdmMapperBase{
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CdmAttributeMapperBase.class);

	public abstract Set<String> getSourceAttributes();

	public abstract Set<String> getDestinationAttributes();

	public abstract List<String> getSourceAttributeList();

	public abstract List<String> getDestinationAttributeList();

	@Override
    public String toString(){
		String sourceAtt = CdmUtils.concat(",", getSourceAttributeList().toArray(new String[1]));
		String destAtt = CdmUtils.concat(",", getDestinationAttributeList().toArray(new String[1]));
		return this.getClass().getSimpleName() +"[" + sourceAtt + "->" + destAtt + "]";
	}

}
