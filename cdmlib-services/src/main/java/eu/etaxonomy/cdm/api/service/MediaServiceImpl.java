// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationPartDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional
public class MediaServiceImpl extends AnnotatableServiceBase<Media,IMediaDao> implements IMediaService {
	
	@Autowired
	private IMediaRepresentationDao mediaRepresentationDao;
	
	@Autowired
	private IMediaRepresentationPartDao mediaRepresentationPartDao;	
	
	@Transactional(readOnly = false)
	public Map<UUID, Media> saveMediaAll(Collection<Media> mediaCollection){
		return saveCdmObjectAll(mediaCollection);
	    //TODO: Fix saveAll() types
//		return mediaDao.saveAll(mediaCollection);
	}

	public List<Media> getAllMedia(int limit, int start){
		return dao.list(limit, start);
	}

	public List<MediaRepresentation> getAllMediaRepresentations(int limit, int start){
		return mediaRepresentationDao.list(limit, start);
	}

	public List<MediaRepresentationPart> getAllMediaRepresentationParts(int limit, int start){
		return mediaRepresentationPartDao.list(limit, start);
	}

	@Autowired
	protected void setDao(IMediaDao dao) {
		this.dao = dao;
	}

	public Pager<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countIdentificationKeys(taxonomicScope, geoScopes);
		
		List<IdentificationKey> results = new ArrayList<IdentificationKey>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getIdentificationKeys(taxonomicScope, geoScopes, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<IdentificationKey>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public Pager<Media> search(Class<? extends Media> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}
}
