// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/*/featuretree/", "/*/featuretree/*"})
public class FeatureTreeListController extends BaseController<DescriptionBase, IDescriptionService>
{

	public FeatureTreeListController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/featuretree/([^/?#&\\.]+).*");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IDescriptionService service) {
		this.service = service;
	}
	
	@RequestMapping()
	@Deprecated
	public DescriptionBase doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return null;
	}
	
	//	public List<FeatureTree> getFeatureTreesAll();
	//  public List<FeatureNode> getFeatureNodesAll();
	
	@RequestMapping(method = RequestMethod.GET)
	public List<FeatureTree> doGetFeatureTrees(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<FeatureTree> obj = service.getFeatureTreesAll();
		return obj;
	}

}
