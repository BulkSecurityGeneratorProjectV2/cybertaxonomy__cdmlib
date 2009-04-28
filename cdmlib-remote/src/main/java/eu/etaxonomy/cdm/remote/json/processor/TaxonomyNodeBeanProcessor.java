// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.TaggedText;

import net.sf.json.CycleSetAcess;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * WARNING! The idea i started implementing here will not work at all!!
 * @author a.kohlbecker
 *
 */
public class TaxonomyNodeBeanProcessor extends CycleSetAcess implements JsonBeanProcessor {
	
	
	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonBeanProcessor#processBean(java.lang.Object, net.sf.json.JsonConfig)
	 */
	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
	
		Taxon taxon = (Taxon)bean;
		JSONObject json = new JSONObject();
		json.element("titleCache", taxon.getName().getTitleCache(), jsonConfig);
		List<TaggedText> taggedTitle = TaxonNameBaseBeanProcessor.getTaggedName(taxon.getName());
		json.element("taggedTitle", taggedTitle, jsonConfig);
		json.element("uuid", taxon.getUuid(), jsonConfig);
		json.element("secUuid", taxon.getSec().getUuid(), jsonConfig);
		json.element("taxonomicChildrenCount", taxon.getTaxonomicChildrenCount(), jsonConfig);
		
		return json;
	}

}
