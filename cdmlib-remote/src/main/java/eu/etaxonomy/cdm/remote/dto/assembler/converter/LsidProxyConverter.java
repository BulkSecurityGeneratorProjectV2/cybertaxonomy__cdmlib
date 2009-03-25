// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;
import eu.etaxonomy.cdm.model.common.LSID;

public class LsidProxyConverter implements CustomConverter {

	private String lsidProxyServiceUrl;
	
	public void setLsidProxyServiceUrl(String lsidProxyServiceUrl) {
		this.lsidProxyServiceUrl = lsidProxyServiceUrl;
	}

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}
		String dest = null;
		if (source instanceof LSID) {		      
			dest = this.lsidProxyServiceUrl + ((LSID)source).getLsid();
			return dest;
		} else {
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
