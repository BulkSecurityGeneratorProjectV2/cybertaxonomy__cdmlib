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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author a.kohlbecker
 * @date 09.04.2009
 *
 */
@Controller
@RequestMapping(value = {"/*/portal/featuretree/", "/*/portal/featuretree/*"})
public class FeatureTreeListPortalController extends FeatureTreeListController {

}
