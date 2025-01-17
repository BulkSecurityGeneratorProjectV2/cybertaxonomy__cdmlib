/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import io.swagger.annotations.Api;

/**
 * The ClassificationController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * @author a.kohlbecker
 * @since 20.03.2009
 *
 * TODO this controller should be a portal controller!!
 */
@Controller
@Api("portal_classification")
@RequestMapping(value = {"/portal/classification/{uuid}"})
public class ClassificationPortalController extends ClassificationController {

    public static final Logger logger = LogManager.getLogger(ClassificationPortalController.class);

    private static final List<String> CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[]{
            "source.citation.authorship",
            "childNodes"
    });

    @Override
    @Autowired
    public void setService(IClassificationService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
    }

    public ClassificationPortalController() {
        super();
        setInitializationStrategy(CLASSIFICATION_INIT_STRATEGY);
    }
}