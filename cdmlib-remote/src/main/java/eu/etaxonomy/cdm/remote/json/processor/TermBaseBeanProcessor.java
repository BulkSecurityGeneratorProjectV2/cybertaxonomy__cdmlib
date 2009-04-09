// $Id: TaxonBeanProcessor.java 5561 2009-04-07 12:25:33Z a.kohlbecker $
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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 *
 */
public class TermBaseBeanProcessor extends AbstractCdmBeanProcessor<TermBase> {

	public static final Logger logger = Logger.getLogger(TermBaseBeanProcessor.class);

	private static final List<String> IGNORE_LIST = Arrays.asList(new String[]{"representations"});

	private boolean replaceRepresentations = false;
	
	public boolean isReplaceRepresentations() {
		return replaceRepresentations;
	}

	public void setReplaceRepresentations(boolean replace) {
		this.replaceRepresentations = replace;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return IGNORE_LIST;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TermBase bean, JSONObject json,	JsonConfig jsonConfig) {
		
		TermBase term = (TermBase)bean;
		Representation representation;
		List<Language> languages = LocaleContext.getLanguages();
		if(Hibernate.isInitialized(term.getRepresentations())){
			
			representation = term.getPreferredRepresentation(languages);
			if(representation != null){
				json.element("representation_L10n", representation.getText());
			}
			if(!replaceRepresentations){
				json.element("representations", term.getRepresentations(), jsonConfig);
			}
		} else {
			logger.info("representations of term not initialized  " + term.getUuid().toString());
		}
		return json;
	}
	
}
