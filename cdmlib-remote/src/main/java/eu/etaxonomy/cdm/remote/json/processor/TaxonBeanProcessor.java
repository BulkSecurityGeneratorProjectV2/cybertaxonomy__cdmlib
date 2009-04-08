// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonBeanProcessor extends AbstractCdmBeanProcessor<Taxon> implements JsonBeanProcessor {

	public static final Logger logger = Logger.getLogger(TaxonBeanProcessor.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				"taxonomicParent"
		});
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStage(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(Taxon bean, JSONObject json, JsonConfig jsonConfig) {
		if(logger.isDebugEnabled()){
			logger.debug("processing second step" + bean);
		}
		if(Hibernate.isInitialized(bean.getName().getHomotypicalGroup())){
			json.element("homotypicGroup", bean.getHomotypicGroup(), jsonConfig);
		}
		return json;
	}
	
}
