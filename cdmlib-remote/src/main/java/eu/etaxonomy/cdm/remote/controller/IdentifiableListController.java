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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.FindByIdentifierDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;

/**
 * @author l.morris
 * @date 27 Mar 2012
 *
 */
public abstract class IdentifiableListController <T extends IdentifiableEntity, SERVICE extends IIdentifiableEntityService<T>> extends BaseListController<T,SERVICE>  {

	
    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
    }
	
	@Autowired
	private ITermService termservice;

    /**
     * Find IdentifiableEntity objects by name
     * <p>
     *
     * @param query
     *            the string to query for. Since the wildcard character '*'
     *            internally always is appended to the query string, a search
     *            always compares the query string with the beginning of a name.
     *            - <i>required parameter</i>
     * @param pageNumber
     *            the number of the page to be returned, the first page has the
     *            pageNumber = 1 - <i>optional parameter</i>
     * @param pageSize
     *            the maximum number of entities returned per page (can be -1
     *            to return all entities in a single page) - <i>optional parameter</i>
     * @param matchMode
     *           valid values are "EXACT", "BEGINNING", "ANYWHERE", "END" (case sensitive !!!)
     * @return a Pager on a list of {@link IdentifiableEntity}s
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByTitle"})
    public Pager<T> doFindByTitle(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {



        logger.info("doFind : " + request.getRequestURI() + "?" + request.getQueryString() );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.BEGINNING;

        return service.findByTitle(null, query, matchMode, null, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, initializationStrategy);

    }

    /**
     * list IdentifiableEntity objects by identifiers
     * 
     * @param type
     * @param identifierType
     * @param identifier
     * @param pageNumber
     * @param pageSize
     * @param matchMode
     * @param request
     * @param response
     * @param includeEntity
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByIdentifier"})
    public  Pager<FindByIdentifierDTO<T>> doFindByIdentifier(
    		@RequestParam(value = "class", required = false) Class type,
    		@RequestParam(value = "identifierType", required = false) String identifierType,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "includeEntity", required = false) Boolean includeEntity,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

    	DefinedTerm definedTerm = null;
    	if(StringUtils.isNotBlank(identifierType)){
    		identifierType = StringUtils.trim(identifierType);
    		UUID identifierTypeUUID = UUID.fromString(identifierType);
    		definedTerm = CdmBase.deproxy(termservice.find(identifierTypeUUID), DefinedTerm.class);
    	}
    	
        logger.info("doFind : " + request.getRequestURI() + "?" + request.getQueryString() );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber).normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.EXACT;
        boolean includeCdmEntity = includeEntity == null ||  includeEntity == true ? true : false;
        return service.findByIdentifier(type, identifier, definedTerm , matchMode, includeCdmEntity, pagerParams.getPageSize(), pagerParams.getPageIndex(), initializationStrategy);
    }
    
    
}
