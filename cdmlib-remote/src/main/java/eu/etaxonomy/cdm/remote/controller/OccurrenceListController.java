/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.search.spatial.impl.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.RectanglePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("occurrence")
@RequestMapping(value = {"/occurrence"})
public class OccurrenceListController extends AbstractIdentifiableListController<SpecimenOrObservationBase, IOccurrenceService> {


    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ITermService termService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(Rectangle.class, new RectanglePropertyEditor());
    }

    /**
     * @param taxonUuid
     * @param relationshipUuids a comma separated list of uuids e.g. CongruentTo;  "60974c98-64ab-4574-bb5c-c110f6db634d"
     * @param relationshipInversUuids a comma separated list of uuids
     * @param maxDepth null for unlimited
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"byAssociatedTaxon"},
            method = RequestMethod.GET)
    public Pager<SpecimenOrObservationBase> doListByAssociatedTaxon(
                @RequestParam(value = "taxonUuid", required = true) UUID taxonUuid,
                @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
                @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
                @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {

        logger.info("doListByAssociatedTaxon()" + requestPathAndQuery(request));

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);

        Taxon associatedTaxon = (Taxon) taxonService.find(taxonUuid);
        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        List<OrderHint> orderHints = null;

        return service.pageByAssociatedTaxon(null, includeRelationships, associatedTaxon,
                maxDepth, pagerParams.getPageSize(), pagerParams.getPageIndex(),
                orderHints, getInitializationStrategy());

    }

    @RequestMapping(value = "specimensOrObservationsByAssociatedTaxon", method = RequestMethod.GET)
    public ModelAndView doListOccurrencesByAssociatedTaxon(
            @RequestParam(value = "uuid", required = true) UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doListSpecimensOrObservations() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();
        List<FieldUnitDTO> fieldUnitDtos = service.findFieldUnitDTOByAssociatedTaxon(null, uuid);
           // List<SpecimenOrObservationBase<?>> specimensOrObservations = occurrenceService.listByAssociatedTaxon(null, null, (Taxon)tb, null, null, null, orderHints, null);
        mv.addObject(fieldUnitDtos);
        return mv;
    }

    /**
     *
     * @param clazz
     * @param queryString
     * @param boundingBox
     *            as
     *            minx(minlongitute),miny(minlatitute),maxx(maxlongitute),max(
     *            maxlatitute), e.g. 13.112,52.374,13.681,52.641 for the Berlin
     *            area
     * @param languages
     * @param highlighting
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByFullText"})
    public Pager<SearchResult<SpecimenOrObservationBase>> dofindByFullText(
            @RequestParam(value = "clazz", required = false) Class<? extends SpecimenOrObservationBase<?>> clazz,
            @RequestParam(value = "query", required = false) String queryString,
            @RequestParam(value = "bbox", required = false) Rectangle boundingBox,
            @RequestParam(value = "languages", required = false) List<Language> languages,
            @RequestParam(value = "hl", required = false) Boolean highlighting,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException, LuceneParseException {

         logger.info("dofindByFullText() " + requestPathAndQuery(request) );

         PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
         pagerParams.normalizeAndValidate(response);

         if(highlighting == null){
             highlighting = false;
         }

         if(queryString == null && boundingBox == null) {
             HttpStatusMessage.create("Either query or bbox must be given", 400).send(response);
             return null;
         }

        Pager<SearchResult<SpecimenOrObservationBase>> pager = service.findByFullText(clazz, queryString, boundingBox, languages,
                highlighting, pagerParams.getPageSize(), pagerParams.getPageIndex(), ((List<OrderHint>)null),
                initializationStrategy);
        return pager;
    }

    /**
    *
    * @param queryString
    * @param request
    * @param response
    * @return
    * @throws IOException
    * @throws ParseException
    */
//   @RequestMapping(method = RequestMethod.GET, value={"byGeneticAccessionNumber"})
//   public Pager<DerivedUnit> dofindByGeneticAccessionNumber(
//           @RequestParam(value = "query", required = false) String queryString,
//           @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
//           @RequestParam(value = "pageSize", required = false) Integer pageSize,
//           HttpServletRequest request,
//           HttpServletResponse response
//           )
//            throws IOException {
//
//        logger.info("dofindByIdentifier() " + requestPathAndQuery(request) );
//
//        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
//        pagerParams.normalizeAndValidate(response);
//
//
//        if(queryString == null ) {
//            HttpStatusMessage.create("Query must be given", 400).send(response);
//            return null;
//        }
//
//       Pager<DerivedUnit> pager = service.findByAccessionNumber(queryString,pageSize, pageNumber, null,this.initializationStrategy);
//
//       return pager;
//   }

    @RequestMapping(method = RequestMethod.GET, value = "byGeneticAccessionNumber" )
    public FieldUnitDTO doFindByGeneticAccessionNumber(
            @RequestParam("accessionNumber") String accessionNumber,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doListSpecimensOrObservations() - " + request.getRequestURI());

        if(accessionNumber == null ) {
            response.setHeader("Failure", "Query must be given");
            HttpStatusMessage.create("Query must be given", 400).send(response);
            return null;
        }

       FieldUnitDTO fieldUnitDto = service.findByAccessionNumber(accessionNumber, null,this.initializationStrategy);
       if(fieldUnitDto == null ) {
           response.setHeader("Failure", "No DNA available for accession number ");
           HttpStatusMessage.create("No DNA available for accession number " + accessionNumber, 400).send(response);
           return null;
       }
       return fieldUnitDto;
    }


}
