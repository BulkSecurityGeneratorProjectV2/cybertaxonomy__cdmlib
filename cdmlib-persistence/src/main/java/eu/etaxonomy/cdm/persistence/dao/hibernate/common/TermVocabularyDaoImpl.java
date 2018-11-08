/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
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
		indexedClasses = new Class[2];
		indexedClasses[0] = TermVocabulary.class;
		indexedClasses[1] = OrderedTermVocabulary.class;
	}

	@Override
    public long countTerms(TermVocabulary termVocabulary) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.vocabulary = :vocabulary");
		    query.setParameter("vocabulary", termVocabulary);

		    return (Long)query.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(null, auditEvent);
			query.addProjection(AuditEntity.id().count());
			query.add(AuditEntity.relatedId("vocabulary").eq(termVocabulary.getId()));
			return (Long)query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> vocabulary,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getCriteria(DefinedTermBase.class);
			criteria.createCriteria("vocabulary").add(Restrictions.idEq(vocabulary.getId()));

			addPageSizeAndNumber(criteria, pageSize, pageNumber);
		    this.addOrder(criteria, orderHints);

		    @SuppressWarnings("unchecked")
            List<T> result = criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;
		} else {
			AuditQuery query = makeAuditQuery(null, auditEvent);
			query.add(AuditEntity.relatedId("vocabulary").eq(vocabulary.getId()));

			addPageSizeAndNumber(query, pageSize, pageNumber);

			@SuppressWarnings("unchecked")
            List<T> result = query.getResultList();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
			return result;
		}
	}

    @Override
    public <T extends DefinedTermBase> TermVocabulary<T> findByUri(String termSourceUri, Class<T> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    //TODO use clazz
    		Query query = getSession().createQuery("select vocabulary from TermVocabulary vocabulary where vocabulary.termSourceUri= :termSourceUri");
	    	query.setParameter("termSourceUri", termSourceUri);

	    	@SuppressWarnings("unchecked")
	    	TermVocabulary<T> result = (TermVocabulary<T>)query.uniqueResult();
	    	return result;
		} else {
			@SuppressWarnings("unchecked")
            AuditQuery query = makeAuditQuery(clazz, auditEvent);
			query.add(AuditEntity.property("termSourceUri").eq(termSourceUri));

			@SuppressWarnings("unchecked")
            TermVocabulary<T> result = (TermVocabulary<T>)query.getSingleResult();
			return result;
		}
	}


	@Override
    public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize,	Integer pageNumber) {
		return getTerms(termVocabulary, pageSize, pageNumber, null, null);
	}


    @Override
    public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType, List<String> propertyPaths) {

        Criteria criteria = getSession().createCriteria(type);
        criteria.add(Restrictions.eq("termType", termType));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        //this.addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<TermVocabulary<T>> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

	@Override
    public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubTypes, Integer limit, Integer start,List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("TermVocabularyDao.listByTermType(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths)");

        Set<TermType> allTermTypes = new HashSet<TermType>();
        allTermTypes.add(termType);
        if (includeSubTypes){
            allTermTypes.addAll(termType.getGeneralizationOf(true));
        }

        Criteria criteria = getSession().createCriteria(type);
        criteria.add(Restrictions.in("termType", allTermTypes));

        if(limit != null) {
            criteria.setMaxResults(limit);
            if(start != null) {
                criteria.setFirstResult(start);
            }
        }

        this.addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<TermVocabulary> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

	@Override
	public void missingTermUuids(
			Map<UUID, Set<UUID>> uuidsRequested,
			Map<UUID, Set<UUID>> uuidMissingTermsRepsonse,
			Map<UUID, TermVocabulary<?>> vocabularyResponse){

		Set<UUID> missingTermCandidateUuids = new HashSet<>();

		for (Set<UUID> uuidsPerVocSet : uuidsRequested.values()){
			missingTermCandidateUuids.addAll(uuidsPerVocSet);
		}

 		//search persisted subset of required (usually all)
		String hql = " SELECT terms.uuid " +
				" FROM TermVocabulary voc join voc.terms terms  " +
				" WHERE terms.uuid IN (:uuids) " +
				" ORDER BY voc.uuid ";
		Query query = getSession().createQuery(hql);

		int splitSize = 2000;
		List<Collection<UUID>> missingTermCandidates = splitCollection(missingTermCandidateUuids, splitSize);
		List<UUID> persistedUuids = new ArrayList<>();

		for (Collection<UUID> uuids : missingTermCandidates){
		    query.setParameterList("uuids", uuids);
		    @SuppressWarnings("unchecked")
            List<UUID> list = query.list();
		    persistedUuids.addAll(list);
		}


 		//fully load and initialize vocabularies if required
		if (vocabularyResponse != null){
			String hql2 = " SELECT DISTINCT voc " +
					" FROM TermVocabulary voc " +
						" LEFT JOIN FETCH voc.terms terms " +
						" LEFT JOIN FETCH terms.representations representations " +
						" LEFT JOIN FETCH voc.representations vocReps " +
					" WHERE terms.uuid IN (:termUuids) OR  (  voc.uuid IN (:vocUuids)  ) " +  //was: AND voc.terms is empty, but did not load originally empty vocabularies with user defined terms added
//					" WHERE  voc.uuid IN (:vocUuids) AND voc.terms is empty  " +
					" ORDER BY voc.uuid ";
			query = getSession().createQuery(hql2);
			query.setParameterList("termUuids", missingTermCandidateUuids);
			query.setParameterList("vocUuids", uuidsRequested.keySet());

			for (Collection<UUID> uuids : missingTermCandidates){
			    query.setParameterList("termUuids", uuids);
			    @SuppressWarnings("unchecked")
	            List<TermVocabulary<?>> o = query.list();
	            for (TermVocabulary<?> voc : o){
	                vocabularyResponse.put(voc.getUuid(), voc);
	            }
	        }
		}

		//compute missing terms
		if (missingTermCandidateUuids.size() == persistedUuids.size()){
			missingTermCandidateUuids.clear();
		}else{
			missingTermCandidateUuids.removeAll(persistedUuids);
			//add missing terms to response
			for (UUID vocUUID : uuidsRequested.keySet()){
				for (UUID termUuid : uuidsRequested.get(vocUUID)){
					if (missingTermCandidateUuids.contains(termUuid)){
						Set<UUID> r = uuidMissingTermsRepsonse.get(vocUUID);
						if (r == null){
							r = new HashSet<>();
							uuidMissingTermsRepsonse.put(vocUUID, r);
						}
						r.add(termUuid);
					}
				}
			}
		}

		return;
	}

    @Override
    public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid) {
        String queryString = TermDto.getTermDtoSelect()
                + "where v.uuid = :vocabularyUuid "
                + "and a.partOf is null "
                + "and a.kindOf is null";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("vocabularyUuid", vocabularyUuid);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType) {
        String queryString = ""
                + "select v.uuid, r "
                + "from TermVocabulary as v LEFT JOIN v.representations AS r "
                + "where v.termType = :termType "
                ;
        Query query =  getSession().createQuery(queryString);
        query.setParameter("termType", termType);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        Map<UUID, TermVocabularyDto> dtoMap = new HashMap<>(result.size());
        for (Object[] elements : result) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
            } else {
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                } else {
                    representations = (Set<Representation>)elements[1];
                }
                dtoMap.put(uuid, new TermVocabularyDto(uuid, representations));
            }
        }
        return new ArrayList<>(dtoMap.values());
    }

}
