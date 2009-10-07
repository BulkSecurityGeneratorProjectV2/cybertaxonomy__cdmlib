package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;

@Service
@Transactional(readOnly = true)
public class FeatureTreeServiceImpl extends IdentifiableServiceBase<FeatureTree, IFeatureTreeDao> implements IFeatureTreeService {

	private IFeatureNodeDao featureNodeDao;
	
	@Autowired
	protected void setDao(IFeatureTreeDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setFeatureNodeDao(IFeatureNodeDao featureNodeDao) {
		this.featureNodeDao = featureNodeDao;
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
	}

	public List<FeatureNode> getFeatureNodesAll() {
		return featureNodeDao.list();
	}

	public FeatureTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {
		nodePaths.add("children");
		
		List<String> rootPaths = new ArrayList<String>();
		rootPaths.add("root");
		for(String path : nodePaths) {
			rootPaths.add("root." + path);
		}
		
		if(propertyPaths != null) { 
		    rootPaths.addAll(propertyPaths);
		}
		
		FeatureTree featureTree = load(uuid, rootPaths);
		loadNodes(featureTree.getRoot(),nodePaths);
		return featureTree;
	}
	
	private void loadNodes(FeatureNode node, List<String> nodePaths) {
		for(FeatureNode child : node.getChildren()) {
			FeatureNode featureNode = featureNodeDao.load(child.getUuid(),nodePaths);
			loadNodes(featureNode,nodePaths);
		}
	}
}
