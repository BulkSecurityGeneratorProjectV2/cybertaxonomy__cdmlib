/**
* Copyright (C) 2009 EDIT
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class PolytomousKeyServiceImpl extends IdentifiableServiceBase<PolytomousKey, IPolytomousKeyDao> implements IPolytomousKeyService {

	IPolytomousKeyNodeDao polytomousKeyNodeDao;
	
	@Autowired
	protected void setDao(IPolytomousKeyDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setPolytomousKeyNodeDao(IPolytomousKeyNodeDao polytomousKeyNodeDao) {
		this.polytomousKeyNodeDao = polytomousKeyNodeDao;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache()
	 */
	@Override
	public void updateTitleCache() {
		Class<PolytomousKey> clazz = PolytomousKey.class;
		super.updateTitleCache(clazz, null, null);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IPolytomousKeyService#getPolytomousKeyNodesAll()
	 */
	public List<PolytomousKeyNode> getPolytomousKeyNodesAll() {
		return polytomousKeyNodeDao.list();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IPolytomousKeyService#savePolytomousKeyNodesAll(java.util.Collection)
	 */
	public Map<UUID, PolytomousKeyNode> savePolytomousKeyNodesAll(Collection<PolytomousKeyNode> polytomousKeyNodeCollection) {
		return polytomousKeyNodeDao.saveAll(polytomousKeyNodeCollection);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IPolytomousKeyService#saveOrUpdatePolytomousKeyNodesAll(java.util.Collection)
	 */
	public Map<UUID, PolytomousKeyNode> saveOrUpdatePolytomousKeyNodesAll(Collection<PolytomousKeyNode> polytomousKeyNodeCollection) {
		return polytomousKeyNodeDao.saveOrUpdateAll(polytomousKeyNodeCollection);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IFeatureTreeService#loadWithNodes(java.util.UUID, java.util.List, java.util.List)
	 */
	public PolytomousKey loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {
		nodePaths.add("children");
		
		List<String> rootPaths = new ArrayList<String>();
		rootPaths.add("root");
		for(String path : nodePaths) {
			rootPaths.add("root." + path);
		}
		
		if(propertyPaths != null) { 
		    rootPaths.addAll(propertyPaths);
		}
		
		PolytomousKey polytomousKey = load(uuid, rootPaths);
		dao.loadNodes(polytomousKey.getRoot(),nodePaths);
		return polytomousKey;
	}
	
	/**
	 * Returns the polytomous key specified by the given <code>uuid</code>.
	 * The specified polytomous key either can be one of those stored in the CDM database or can be the 
	 * DefaultFeatureTree (contains all Features in use). 
	 * The uuid of the DefaultFeatureTree is defined in {@link IFeatureTreeService#DefaultFeatureTreeUuid}.
	 * The DefaultFeatureTree is also returned if no feature tree at all is stored in the cdm database.
	 *  
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
	 */
	@Override
	public PolytomousKey load(UUID uuid, List<String> propertyPaths) {
		return super.load(uuid, propertyPaths);
	}
	
}
