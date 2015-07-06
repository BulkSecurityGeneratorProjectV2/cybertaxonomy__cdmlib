// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;


@SuppressWarnings("serial")
public class SaveOrUpdateEntityListener implements SaveOrUpdateEventListener, MergeEventListener {

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        //System.err.println("SaveOrUpdateListener" + event.getEntity().getClass());
        Object entity = event.getObject();
        saveOrUpdateOrMerge(entity, event.getSession());
    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent)
     */
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        Object entity = event.getOriginal();
        saveOrUpdateOrMerge(entity,event.getSession());
    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent, java.util.Map)
     */
    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        // do nothing
    }

    private void saveOrUpdateOrMerge(Object entity, Session session) {

        if(entity != null && CdmBase.class.isAssignableFrom(entity.getClass())){

            //CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();

            if (VersionableEntity.class.isAssignableFrom(entity.getClass())) {
                VersionableEntity versionableEntity = (VersionableEntity)entity;

                versionableEntity.setUpdated(new DateTime());

                if(versionableEntity.getCreatedBy() != null) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
                        User user = (User)authentication.getPrincipal();
                        versionableEntity.setUpdatedBy(user);
                    }
                }
            }

            if (entity instanceof ITreeNode) {
                ITreeNode<?> node = (ITreeNode<?>)entity;
                reindex(node);

            }

            if (entity instanceof DeterminationEvent) {
                DeterminationEvent detEv = (DeterminationEvent)entity;
                if (detEv.getTaxon() != null && detEv.getTaxonName() == null && detEv.getTaxon().getName() != null){
                    detEv.setTaxonName(detEv.getTaxon().getName());
                }
            }
        }
    }

    static String sep = ITreeNode.separator;
    static String pref = ITreeNode.treePrefix;

    /**
     * @param event
     * @param node
     */
    private <T extends ITreeNode> void reindex(T node) {
        String oldChildIndex = node.treeIndex();
        ITreeNode<?> parent = node.getParent();
        String parentIndex = (parent == null) ? (sep + pref + node.treeId() + sep)  : parent.treeIndex();  //TODO
        if (node.getId() > 0){   //TODO
            String newChildIndex = parentIndex + node.getId() + sep;
            if (oldChildIndex == null || ! oldChildIndex.equals(newChildIndex)){
                node.setTreeIndex(newChildIndex);

                //TODO this is a greedy implementation, better use update by replace string
                //either using and improving the below code or by using native SQL
                //The current approach may run out of memory for large descendant sets.
                List<T> childNodes = node.getChildNodes();
                for (T child : childNodes){
                    if (child != null && ! child.equals(node)){  //node should not be it's own child, however just in case
                        reindex(child);
                    }
                }

                //			String className = event.getEntityName();
                //					String updateQuery = " UPDATE %s tn " +
                //							" SET tn.treeIndex = Replace(tn.treeIndex, '%s', '%s') " +
                //							" WHERE tn.id <> "+ node.getId()+" ";
                //					updateQuery = String.format(updateQuery, className, oldChildIndex, parentIndex);  //dummy
                //					System.out.println(updateQuery);
                //					EventSource session = event.getSession();
                //					Query query = session.createQuery(updateQuery);
                //					query.executeUpdate();
            }
        }
    }



}
