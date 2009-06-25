// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.kohlbecker
 * @date 23.06.2009
 *
 */
@Controller
@RequestMapping(value = {"/*/term/", "/*/term/?*"})
public class TermListConroller extends BaseListController<DefinedTermBase, ITermService> {
	
	private static final List<String> VOCABULARY_LIST_INIT_STRATEGY = Arrays.asList(new String []{
			"representations"
	});
	
	private static final List<String> VOCABULARY_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"representations",
			"terms.representations"
	});

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.AbstractListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITermService service) {
		this.service = service;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET,
		value = "/*/term/")
	public Pager<TermVocabulary<DefinedTermBase>> doGetVocabularies(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		
		if(page == null){ page = DEFAULT_PAGE;}
		if(pageSize == null){ pageSize = DEFAULT_PAGESIZE;}
		
		return (Pager<TermVocabulary<DefinedTermBase>>) service.pageTermVocabularies(pageSize, page, null, VOCABULARY_LIST_INIT_STRATEGY);
	}
	
	@RequestMapping(method = RequestMethod.GET,
		value = "/*/term/?*")
	public TermVocabulary<DefinedTermBase> doGetTerms(HttpServletRequest request, HttpServletResponse response) throws IOException {
		UUID uuid = readValueUuid(request, "^/(?:[^/]+)/term/([^/?#&\\.]+).*");
		TermVocabulary<DefinedTermBase> vocab = service.loadVocabulary(uuid, VOCABULARY_INIT_STRATEGY);
		return vocab;
	}

	
	

}
