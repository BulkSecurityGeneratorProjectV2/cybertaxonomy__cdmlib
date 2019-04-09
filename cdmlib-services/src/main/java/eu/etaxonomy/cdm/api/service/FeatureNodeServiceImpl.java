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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.FeatureNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;

/**
 * @author n.hoffmann
 * @since Aug 5, 2010
 * @version 1.0
 */
@Service
@Transactional(readOnly = false)
public class FeatureNodeServiceImpl extends VersionableServiceBase<FeatureNode, IFeatureNodeDao> implements IFeatureNodeService {
	private static final Logger logger = Logger.getLogger(FeatureNodeServiceImpl.class);

	@Override
    @Autowired
	protected void setDao(IFeatureNodeDao dao) {
		this.dao = dao;
	}

	@Autowired
    private ITermService termService;

	@Autowired
	private IVocabularyService vocabularyService;

	 @Override
	 @Transactional(readOnly = false)
	 public DeleteResult deleteFeatureNode(UUID nodeUuid, FeatureNodeDeletionConfigurator config) {
	     DeleteResult result = new DeleteResult();
	     FeatureNode node = HibernateProxyHelper.deproxy(dao.load(nodeUuid), FeatureNode.class);
	     result = isDeletable(node, config);
	     if (result.isOk()){
	         FeatureNode parent = node.getParent();
             parent = HibernateProxyHelper.deproxy(parent, FeatureNode.class);
	         List<FeatureNode> children = new ArrayList(node.getChildNodes());

	         if (config.getChildHandling().equals(ChildHandling.DELETE)){

	             for (FeatureNode child: children){
	                 deleteFeatureNode(child.getUuid(), config);
	                // node.removeChild(child);
	             }
	             if (parent != null){
	                 parent.removeChild(node);
	             }

	         } else{

	             if (parent != null){
	                 parent.removeChild(node);
	                 for (FeatureNode child: children){
	                     node.removeChild(child);
	                     parent.addChild(child);
	                 }
	             }else{
	                 result.setAbort();
	                 result.addException(new ReferencedObjectUndeletableException("The root node can not be deleted without its child nodes"));
	                 return result;
	             }
	         }

	         dao.delete(node);
	         result.addDeletedObject(node);
	         if(parent!=null){
	             result.addUpdatedObject(parent);
	         }
	         if (config.isDeleteElement()){
	             DefinedTermBase term = node.getTerm();
                 termService.delete(term.getUuid());
                 result.addDeletedObject(term);
             }
	     }
	     return result;
	 }

	 @Override
	 public UpdateResult createChildFeatureNode(UUID parentNodeUuid, UUID termUuid, UUID vocabularyUuid){
	     TermVocabulary vocabulary = vocabularyService.load(vocabularyUuid);
	     DefinedTermBase term = HibernateProxyHelper.deproxy(termService.load(termUuid), DefinedTermBase.class);

	     vocabulary.addTerm(term);
	     vocabularyService.save(vocabulary);
	     return addChildFeatureNode(parentNodeUuid, termUuid);
	 }

     @Override
     public UpdateResult addChildFeatureNode(UUID nodeUUID, UUID termChildUuid){
         return addChildFeatureNode(nodeUUID, termChildUuid, 0);
     }

	 @Override
	 public UpdateResult addChildFeatureNode(UUID nodeUUID, UUID termChildUuid, int position){
	     FeatureNode node = load(nodeUUID);
	     DefinedTermBase child = HibernateProxyHelper.deproxy(termService.load(termChildUuid), DefinedTermBase.class);

	     FeatureNode childNode = FeatureNode.NewInstance(node.getTermType());
         childNode.setTerm(child);
         UpdateResult result = new UpdateResult();
         if(position<0) {
             node.addChild(childNode);
         }
         else{
             node.addChild(childNode, position);
         }
         save(childNode);
         result.addUpdatedObject(node);
         result.setCdmEntity(childNode);
         return result;
     }

	 @Override
	 public DeleteResult isDeletable(FeatureNode node, FeatureNodeDeletionConfigurator config){
	     DeleteResult result = new DeleteResult();
	     Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(node);
	     for (CdmBase ref:references){
	         if (ref instanceof FeatureNode){
	             break;
	         }
	         if (ref instanceof FeatureTree){
	             FeatureTree refTree = HibernateProxyHelper.deproxy(ref, FeatureTree.class);
	             if (node.getFeatureTree().equals((refTree))){
	                 break;
	             }
	         }
	         result.setAbort();
	         result.addException(new ReferencedObjectUndeletableException("The featureNode is referenced by " + ref.getUserFriendlyDescription() +" with id " +ref.getId()));

	     }
	     return result;
	 }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid, int position) {
        UpdateResult result = new UpdateResult();
        FeatureNode movedNode = HibernateProxyHelper.deproxy(load(movedNodeUuid), FeatureNode.class);
        FeatureNode targetNode = HibernateProxyHelper.deproxy(load(targetNodeUuid), FeatureNode.class);
        FeatureNode parent = HibernateProxyHelper.deproxy(movedNode.getParent(), FeatureNode.class);
        if(position<0){
            targetNode.addChild(movedNode);
        }
        else{
            targetNode.addChild(movedNode, position);
        }
        result.addUpdatedObject(targetNode);
        if(parent!=null){
            result.addUpdatedObject(parent);
        }
        result.setCdmEntity(movedNode);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid) {
        return moveFeatureNode(movedNodeUuid, targetNodeUuid, -1);
    }

}
