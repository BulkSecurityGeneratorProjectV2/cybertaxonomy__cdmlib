/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
@Repository
public class TermVocabularyDaoImpl extends IdentifiableDaoBase<TermVocabulary> implements
		ITermVocabularyDao {
	/**
	 * @param type
	 */
	public TermVocabularyDaoImpl() {
		super(TermVocabulary.class);
	}

	public int countTerms(TermVocabulary termVocabulary) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.vocabulary = :vocabulary");
		    query.setParameter("vocabulary", termVocabulary);
		
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.addProjection(AuditEntity.id().count("id"));
			query.add(AuditEntity.relatedId("vocabulary").eq(termVocabulary.getId()));
			return (Integer)query.getSingleResult();
		}
	}

	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> vocabulary,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(DefinedTermBase.class);
			criteria.createCriteria("vocabulary").add(Restrictions.idEq(vocabulary.getId()));
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        }
		    }
		    
		    this.addOrder(criteria, orderHints);
		    List<T> result = (List<T>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("vocabulary").eq(vocabulary.getId()));
			
			if(pageSize != null) {
			    query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    query.setFirstResult(pageNumber * pageSize);
		        }
			}
			
			List<T> result = (List<T>)query.getResultList();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
			return (List<T>)query.getResultList();
		}
	}

	public <T extends DefinedTermBase> TermVocabulary<T> findByUri(String termSourceUri, Class<T> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select vocabulary from TermVocabulary vocabulary where vocabulary.termSourceUri= :termSourceUri");
	    	query.setParameter("termSourceUri", termSourceUri);
		 
		    return (TermVocabulary<T>)query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("termSourceUri").eq(termSourceUri));
			
			return (TermVocabulary<T>)query.getSingleResult();
		}
	}

	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize,	Integer pageNumber) {
		return getTerms(termVocabulary, pageSize, pageNumber, null, null);
	}
}
