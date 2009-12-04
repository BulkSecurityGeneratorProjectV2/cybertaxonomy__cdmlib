// $Id: TaxonController.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.editor.NamedAreaPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * The TaxonPortalController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}</b>
 * 
 * Get the {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
 * The returned Taxon is initialized by
 * the following strategy {@link #TAXON_INIT_STRATEGY}
 * </blockquote>
 * 
 * @author a.kohlbecker
 * @date 20.07.2009
 *
 */
@Controller
@RequestMapping(value = {"/*/portal/taxon/*", "/*/portal/taxon/*/*", "/*/portal/name/*/*", "/*/portal/taxon/*/media/*/*"})
public class TaxonPortalController extends BaseController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonPortalController.class);
	
	@Autowired
	private INameService nameService;
	@Autowired
	private IDescriptionService descriptionService;
	@Autowired
	private IReferenceService referenceService;
	
	@Autowired
	private ITaxonTreeService taxonTreeService;
	
	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name.taggedName",
			// the name
			"name.$",
			"name.taggedName",
			"name.rank.representations",
			"name.status.type.representations",
			
			// taxon descriptions
			"descriptions.elements.$",
			"descriptions.elements.area",
			"descriptions.elements.area.$",
			"descriptions.elements.multilanguageText",
			"descriptions.elements.media.representations.parts",
			
//			// typeDesignations
//			"name.typeDesignations.$",
//			"name.typeDesignations.citation.authorTeam",
//			"name.typeDesignations.typeName.$",
//			"name.typeDesignations.typeStatus.representations",
//			"name.typeDesignations.typeSpecimen.media.representations.parts"
			
			});
	
	private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name.taggedName",
			// the name
			"name.$",
			"name.taggedName",
			"name.rank.representations",
			"name.status.type.representations"
			});
	
	private static final List<String> SYNONYMY_INIT_STRATEGY = Arrays.asList(new String []{
			// initialize homotypical and heterotypical groups; needs synonyms
			"synonymRelations.$",
			"synonymRelations.synonym.$",
			"synonymRelations.synonym.name.taggedName",
			"synonymRelations.synonym.name.nomenclaturalReference.inBook.authorTeam",
			"synonymRelations.synonym.name.nomenclaturalReference.inJournal",
			"synonymRelations.synonym.name.nomenclaturalReference.inProceedings",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.name.taggedName",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName",
			
			"name.homotypicalGroup.$",
			"name.homotypicalGroup.typifiedNames.$",
			"name.homotypicalGroup.typifiedNames.name.taggedName",
			
			"name.homotypicalGroup.typifiedNames.taxonBases.$",
			"name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName"
			
	});
	
	private static final List<String> TAXONRELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"type.inverseRepresentations",
			"fromTaxon.sec.authorTeam",
			"fromTaxon.name.taggedName"
	});
	
	private static final List<String> NAMERELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"type.inverseRepresentations",
			"fromName.taggedName",
	});
	
	
	protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"elements.$",
			"elements.sources.citation.",
			"elements.sources.citation.authorTeam.$",
			//"elements.sources.citation.authorTeam.teamMembers.",
			"elements.multilanguageText",
			"elements.media.representations.parts",
	});
	
	private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
			"uuid",
			"feature",
			"elements.$",
			"elements.multilanguageText",
			"elements.media.representations.parts",
	});
	
	private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
			//"$",
			"typeSpecimen.$",
			"typeStatus.representations",
			"citation.authorTeam",
			"typeName.taggedName"
	});
	
	
	
	private static final String featureTreeUuidPattern = "^/(?:[^/]+)/taxon(?:(?:/)([^/?#&\\.]+))+.*";
	
	public TaxonPortalController(){
		super();
		setInitializationStrategy(TAXON_INIT_STRATEGY);
		setUuidParameterPattern("^/(?:[^/]+)/portal/(?:[^/]+)/([^/?#&\\.]+).*");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
		binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseController#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public TaxonBase doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGet()");
		TaxonBase tb = getCdmBase(request, response, TAXON_INIT_STRATEGY, TaxonBase.class);
		return tb;
	}
	 */
	/**
	 * Find Taxa, Synonyms, Common Names by name, either globally or in a specific geographic area. 
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;find</b>
	 * 
	 * @param query
	 *            the string to query for. Since the wildcard character '*'
	 *            internally always is appended to the query string, a search
	 *            always compares the query string with the beginning of a name.
	 *            - <i>required parameter</i>
	 * @param treeUuid
	 *            the {@link UUID} of a {@link TaxonomicTree} to which the
	 *            search is to be restricted. - <i>optional parameter</i>
	 * @param areas
	 *            restrict the search to a set of geographic {@link NamedArea}s.
	 *            The parameter currently takes a list of TDWG area labels.
	 *            - <i>optional parameter</i>
	 * @param page
	 *            the number of the page to be returned, the first page has the
	 *            pageNumber = 1 - <i>optional parameter</i>
	 * @param pageSize
	 *            the maximum number of entities returned per page (can be null
	 *            to return all entities in a single page) - <i>optional parameter</i>
	 * @param doTaxa
	 *            weather to search for instances of {@link Taxon} - <i>optional parameter</i>
	 * @param doSynonyms
	 *            weather to search for instances of {@link Synonym} - <i>optional parameter</i>
	 * @param doTaxaByCommonNames
	 *            for instances of {@link Taxon} by a common name used - <i>optional parameter</i>
	 * @return a Pager on a list of {@link IdentifiableEntity}s initialized by
	 *         the following strategy {@link #SIMPLE_TAXON_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET,
			value = {"/*/portal/taxon/find"}) //TODO map to path /*/portal/taxon/
	public Pager<IdentifiableEntity> doFind(
			@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "tree", required = false) UUID treeUuid,
			@RequestParam(value = "area", required = false) Set<NamedArea> areas,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
			@RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
			@RequestParam(value = "doTaxaByCommonNames", required = false) Boolean doTaxaByCommonNames)
			 throws IOException {
		
		logger.info("doFind( " +
			"query=\"" + ObjectUtils.toString(query) + "\", treeUuid=" + ObjectUtils.toString(treeUuid) + 
			", area=" + ObjectUtils.toString(areas) +
			", pageSize=" + ObjectUtils.toString(pageSize) +  ", page=" + ObjectUtils.toString(page) + 
			", doTaxa=" + ObjectUtils.toString(doTaxa) + ", doSynonyms=" + ObjectUtils.toString(doSynonyms) +")" );
		
		if(page == null){ page = BaseListController.DEFAULT_PAGE_NUMBER;}
		if(pageSize == null){ pageSize = BaseListController.DEFAULT_PAGESIZE;}
			
		ITaxonServiceConfigurator config = new TaxonServiceConfiguratorImpl();
		config.setPageNumber(page);
		config.setPageSize(pageSize);
		config.setSearchString(query);
		config.setDoTaxa(doTaxa!= null ? doTaxa : Boolean.FALSE );
		config.setDoSynonyms(doSynonyms != null ? doSynonyms : Boolean.FALSE );
		config.setDoTaxaByCommonNames(doTaxaByCommonNames != null ? doTaxaByCommonNames : Boolean.FALSE );
		config.setMatchMode(MatchMode.BEGINNING);
		config.setTaxonPropertyPath(SIMPLE_TAXON_INIT_STRATEGY);
		config.setNamedAreas(areas);
		if(treeUuid != null){
			TaxonomicTree taxonomicTree = taxonTreeService.find(treeUuid);
			config.setTaxonomicTree(taxonomicTree);
		}
			
		return (Pager<IdentifiableEntity>) service.findTaxaAndNames(config);
	}

	/**
	 * Get the synonymy for a taxon identified by the <code>{taxon-uuid}</code>. 
	 * The synonymy consists
	 * of two parts: The group of homotypic synonyms of the taxon and the
	 * heterotypic synonymy groups of the taxon. The synonymy is ordered
	 * historically by the type designations and by the publication date of the
	 * nomenclatural reference
	 * <p>
	 * URI:
	 * <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;synonymy</b>
	 * 
	 * 
	 * @param request
	 * @param response
	 * @return a Map with to entries which are mapped by the following keys:
	 *         "homotypicSynonymsByHomotypicGroup", "heterotypicSynonymyGroups", 
	 *         containing lists of {@link Synonym}s which are initialized using the 
	 *         following initialization strategy: {@link #SYNONYMY_INIT_STRATEGY}
	 * 
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/taxon/*/synonymy"},
			method = RequestMethod.GET)
	public ModelAndView doGetSynonymy(HttpServletRequest request, HttpServletResponse response)throws IOException {
		
		logger.info("doGetSynonymy() " + request.getServletPath());
		ModelAndView mv = new ModelAndView();
		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		Taxon taxon = (Taxon)tb;
		Map<String, List<?>> synonymy = new Hashtable<String, List<?>>();
		synonymy.put("homotypicSynonymsByHomotypicGroup", service.getHomotypicSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY));
		synonymy.put("heterotypicSynonymyGroups", service.getHeterotypicSynonymyGroups(taxon, SYNONYMY_INIT_STRATEGY));
		mv.addObject(synonymy);
		return mv;
	}

	/**
	 * Get the set of accepted {@link Taxon} entities for a given
	 * {@link TaxonBase} entity identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;accepted</b>
	 * 
	 * @param request
	 * @param response
	 * @return a Set of {@link Taxon} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #SYNONYMY_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = "/*/portal/taxon/*/accepted", method = RequestMethod.GET)
	public Set<TaxonBase> getAccepted(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.info("getAccepted() " + request.getServletPath());
		
		UUID uuid = readValueUuid(request, null);
		TaxonBase tb = service.load(uuid, SYNONYMY_INIT_STRATEGY);
		if(tb == null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "A taxon with the uuid " + uuid + " does not exist");
			return null;
		}
		HashSet<TaxonBase> resultset = new HashSet<TaxonBase>();
		if(tb instanceof Taxon){
			//the taxon already is accepted
			//FIXME take the current view into account once views are implemented!!!
			resultset.add((Taxon)tb);
		} else {
			Synonym syn = (Synonym)tb;
			for(TaxonBase accepted : syn.getAcceptedTaxa()){
				accepted = service.load(accepted.getUuid(), SIMPLE_TAXON_INIT_STRATEGY);
				resultset.add(accepted);
			}
		}
		return resultset;
	}
	
	/**
     * Get the list of {@link TaxonRelationship}s for the given 
	 * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;taxonRelationships</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TaxonRelationship} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #TAXONRELATIONSHIP_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/taxon/*/taxonRelationships"},
			method = RequestMethod.GET)
	public List<TaxonRelationship> doGetTaxonRelations(HttpServletRequest request, HttpServletResponse response)throws IOException {

		logger.info("doGetTaxonRelations()" + request.getServletPath());
		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		Taxon taxon = (Taxon)tb;
		List<TaxonRelationship> relations = new ArrayList<TaxonRelationship>();
		List<TaxonRelationship> results = service.listToTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);
		relations.addAll(results);
		results = service.listToTaxonRelationships(taxon, TaxonRelationshipType.INVALID_DESIGNATION_FOR(), null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);
		relations.addAll(results);

		return relations;
	}
	
	/**
     * Get the list of {@link NameRelationship}s of the Name associated with the 
	 * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameRelationships</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link NameRelationship} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #NAMERELATIONSHIP_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/taxon/*/nameRelationships"},
			method = RequestMethod.GET)
	public List<NameRelationship> doGetNameRelations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetNameRelations()" + request.getServletPath());
		TaxonBase tb = getCdmBase(request, response, SIMPLE_TAXON_INIT_STRATEGY, Taxon.class);
		List<NameRelationship> list = nameService.listToNameRelationships(tb.getName(), null, null, null, null, NAMERELATIONSHIP_INIT_STRATEGY);
		return list;
	}
	
	/**
     * Get the list of {@link TaxonNameDescription}s of the Name associated with the 
	 * {@link TaxonNameBase} instance identified by the <code>{name-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}&#x002F;descriptions</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TaxonNameDescription} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #NAMEDESCRIPTION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/name/*/descriptions"},
			method = RequestMethod.GET)
	public List<TaxonNameDescription> doGetNameDescriptions(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetNameDescriptions()" + request.getServletPath());
		UUID nameUuuid = readValueUuid(request, null);
		TaxonNameBase tnb = nameService.load(nameUuuid, null);
		Pager<TaxonNameDescription> p = descriptionService.getTaxonNameDescriptions(tnb, null, null, NAMEDESCRIPTION_INIT_STRATEGY);
		return p.getRecords();
	}
	
	/**
     * Get the list of {@link TypeDesignationBase}s of the 
	 * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameTypeDesignations</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TypeDesignationBase} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #TYPEDESIGNATION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/taxon/*/nameTypeDesignations"},
			method = RequestMethod.GET)
	public List<TypeDesignationBase> doGetNameTypeDesignations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetNameTypeDesignations()" + request.getServletPath());
		TaxonBase tb = getCdmBase(request, response, SIMPLE_TAXON_INIT_STRATEGY, Taxon.class);
		Pager<TypeDesignationBase> p = nameService.getTypeDesignations(tb.getName(), null, null, null, TYPEDESIGNATION_INIT_STRATEGY);
		return p.getRecords();
	}
	
	/**
     * Get the list of {@link TaxonDescription}s of the 
	 * {@link Taxon} instance identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;descriptions</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TaxonDescription} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/*/portal/taxon/*/descriptions"},
			method = RequestMethod.GET)
	public List<TaxonDescription> doGetDescriptions(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetDescriptions()" + request.getServletPath());
		Taxon t = getCdmBase(request, response, null, Taxon.class);
		Pager<TaxonDescription> p = descriptionService.getTaxonDescriptions(t, null, null, null, null, 
			TAXONDESCRIPTION_INIT_STRATEGY);
		return p.getRecords();
	}

	/**
	 * Get the {@link Media} attached to the {@link Taxon} instance 
	 * identified by the <code>{taxon-uuid}</code>.
	 * 
	 * Usage &#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-
	 * uuid}&#x002F;media&#x002F;{mime type
	 * list}&#x002F;{size}[,[widthOrDuration}][,{height}]&#x002F;
	 * 
	 * Whereas
	 * <ul>
	 * <li><b>{mime type list}</b>: a comma separated list of mime types, in the
	 * order of preference. The forward slashes contained in the mime types must
	 * be replaced by a colon. Regular expressions can be used. Each media
	 * associated with this given taxon is being searched whereas the first
	 * matching mime type matching a representation always rules.</li>
	 * <li><b>{size},{widthOrDuration},{height}</b>: <i>not jet implemented</i>
	 * valid values are an integer or the asterisk '*' as a wildcard</li>
	 * </ul>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link Media} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
		value = {"/*/portal/taxon/*/media/*/*"},
		method = RequestMethod.GET)
	public List<Media> doGetMedia(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetMedia()" + request.getServletPath());
		Taxon t = getCdmBase(request, response, null, Taxon.class);
		Pager<TaxonDescription> p = 
			descriptionService.getTaxonDescriptions(t, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
		
		// pars the media and quality parameters
		
		
		// collect all media of the given taxon
		boolean limitToGalleries = false;
		List<Media> taxonMedia = new ArrayList<Media>();
		for(TaxonDescription desc : p.getRecords()){
			if(!limitToGalleries || desc.isImageGallery()){
				for(DescriptionElementBase element : desc.getElements()){
					for(Media media : element.getMedia()){
						taxonMedia.add(media);
					}
				}
			}
		}
		
		// move into media ...
		
		// find best matching representations of each media
		String path = request.getServletPath();
		String[] pathTokens = path.split("/");
		String[] mimeTypes = pathTokens[6].split(",");
		String[] sizeTokens = pathTokens[7].split(",");
		Integer widthOrDuration = null;
		Integer height = null;
		Integer size = null;
		
		for(int i=0; i<mimeTypes.length; i++){
			mimeTypes[i] = mimeTypes[i].replace(':', '/');
		}
		
		if(sizeTokens.length > 0){
			try {
				size = Integer.valueOf(sizeTokens[0]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		if(sizeTokens.length > 1){
			try {
				widthOrDuration = Integer.valueOf(sizeTokens[1]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		if(sizeTokens.length > 2){
			try {
				height = Integer.valueOf(sizeTokens[2]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		
		List<Media> returnMedia = new ArrayList<Media>(taxonMedia.size());
		for(Media media : taxonMedia){
			SortedMap<String, MediaRepresentation> prefRepresentations 
				= orderMediaRepresentations(media, mimeTypes, size, widthOrDuration, height);
			try {
				// take first one and remove all other representations
				MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
				for (MediaRepresentation representation : media.getRepresentations()) {
					if (representation != prefOne) {
						media.removeRepresentation(representation);
					}
				}
				returnMedia.add(media);
			} catch (NoSuchElementException nse) {
				/* IGNORE */
			}
		}
		
		return returnMedia;
	}

	/**
	 * @param media
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * TODO move into a media utils class
	 * TODO implement the quality filter  
	 */
	private SortedMap<String, MediaRepresentation> orderMediaRepresentations(Media media, String[] mimeTypeRegexes,
			Integer size, Integer widthOrDuration, Integer height) {
		SortedMap<String, MediaRepresentation> prefRepr = new TreeMap<String, MediaRepresentation>();
		for (String mimeTypeRegex : mimeTypeRegexes) {
			// getRepresentationByMimeType
			Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
			int representationCnt = 0;
			for (MediaRepresentation representation : media.getRepresentations()) {
				Matcher mather = mimeTypePattern.matcher(representation.getMimeType());
				if (mather.matches()) {
					int dwa = 0;

					/* TODO the quality filter part is being skipped 
					 * // look for representation with the best matching parts
					for (MediaRepresentationPart part : representation.getParts()) {
						if (part instanceof ImageFile) {
							ImageFile image = (ImageFile) part;
							int dw = image.getWidth() * image.getHeight() - height * widthOrDuration;
							if (dw < 0) {
								dw *= -1;
							}
							dwa += dw;
						}
						dwa = (representation.getParts().size() > 0 ? dwa / representation.getParts().size() : 0);
					}*/
					prefRepr.put((dwa + representationCnt++) + '_' + representation.getMimeType(), representation);
										
					// preferred mime type found => end loop
					break;
				}
			}
		}
		return prefRepr;
	}
	
// ---------------------- code snippet preserved for possible later use --------------------
//	@RequestMapping(
//			value = {"/*/portal/taxon/*/descriptions"},
//			method = RequestMethod.GET)
//	public List<TaxonDescription> doGetDescriptionsbyFeatureTree(HttpServletRequest request, HttpServletResponse response)throws IOException {
//		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
//		if(tb instanceof Taxon){
//			//T O D O this is a quick and dirty implementation -> generalize
//			UUID featureTreeUuid = readValueUuid(request, featureTreeUuidPattern);
//			
//			FeatureTree featureTree = descriptionService.getFeatureTreeByUuid(featureTreeUuid);
//			Pager<TaxonDescription> p = descriptionService.getTaxonDescriptions((Taxon)tb, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
//			List<TaxonDescription> descriptions = p.getRecords();
//			
//			if(!featureTree.isDescriptionSeparated()){
//				
//				TaxonDescription superDescription = TaxonDescription.NewInstance();
//				//put all descriptionElements in superDescription and make it invisible
//				for(TaxonDescription description: descriptions){
//					for(DescriptionElementBase element: description.getElements()){
//						superDescription.addElement(element);
//					}
//				}
//				List<TaxonDescription> separatedDescriptions = new ArrayList<TaxonDescription>(descriptions.size());
//				separatedDescriptions.add(superDescription);
//				return separatedDescriptions;
//			}else{
//				return descriptions;
//			}
//		} else {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, "invalid type; Taxon expected but " + tb.getClass().getSimpleName() + " found.");
//			return null;
//		}
//	}

}
