/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since 29.05.2008
 * @version 1.0
 */
@Repository
public class DefinedTermDaoImpl extends IdentifiableDaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDaoImpl.class);

	public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
		indexedClasses = new Class[25];
		indexedClasses[0] = Rank.class;
		indexedClasses[1] = AnnotationType.class;
		indexedClasses[2] = ExtensionType.class;
		indexedClasses[3] = Language.class;
		indexedClasses[4] = MarkerType.class;
		indexedClasses[5] = MeasurementUnit.class;
		indexedClasses[6] = DefinedTerm.class;
		indexedClasses[7] = PresenceAbsenceTerm.class;
		indexedClasses[8] = State.class;
		indexedClasses[9] = StatisticalMeasure.class;
		indexedClasses[10] = TextFormat.class;
		indexedClasses[11] = DerivationEventType.class;
		indexedClasses[12] = NamedArea.class;
		indexedClasses[13] = NamedAreaLevel.class;
		indexedClasses[14] = NamedAreaType.class;
		indexedClasses[15] = ReferenceSystem.class;
		indexedClasses[16] = Country.class;
		indexedClasses[17] = RightsType.class;
		indexedClasses[18] = HybridRelationshipType.class;
		indexedClasses[19] = NameRelationshipType.class;
		indexedClasses[20] = NameTypeDesignationStatus.class;
		indexedClasses[21] = NomenclaturalStatusType.class;
		indexedClasses[22] = SpecimenTypeDesignationStatus.class;
		indexedClasses[23] = SynonymType.class;
		indexedClasses[24] = TaxonRelationshipType.class;
	}

	/**
	 * Searches by Label
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String)
	 */
	@Override
    public List<DefinedTermBase> findByTitle(String queryString) {
		return findByTitle(queryString, null);
	}


	/**
	 * Searches by Label
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
    public List<DefinedTermBase> findByTitle(String queryString, CdmBase sessionObject) {
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, CdmBase sessionObject)");
		Session session = getSession();
		if ( sessionObject != null ) {//attache the object to the session, TODO needed?
			session.update(sessionObject);
		}
		Query query = session.createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
		query.setParameter("label", queryString);
		return query.list();

	}

	@Override
    public List<DefinedTermBase> findByTitleAndClass(String queryString, Class<DefinedTermBase> clazz) {
		checkNotInPriorView("DefinedTermDaoImpl.findByTitleAndClass(String queryString, Class<DefinedTermBase> clazz)");
		Session session = getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<DefinedTermBase> results = crit.list();
		return results;
	}

	@Override
    public List<DefinedTermBase> findByTitle(String queryString, MatchMode matchMode, int page, int pagesize, List<Criterion> criteria) {
		//FIXME is query parametrised?
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		crit.setMaxResults(pagesize);
		int firstItem = (page - 1) * pagesize + 1;
		crit.setFirstResult(firstItem);
		List<DefinedTermBase> results = crit.list();
		return results;
	}


	@Override
    public Country getCountryByIso(String iso3166) {
		// If iso639 = "" query returns non-unique result. We prevent this here:
		if (StringUtils.isBlank(iso3166) || iso3166.length()<2 || iso3166.length()>3) { return null; }
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("from Country where iso3166_A2 = :isoCode OR idInVocabulary = :isoCode");
		    query.setParameter("isoCode", iso3166);
		    return (Country) query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Country.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("iso3166_A2").eq(iso3166));
			query.add(AuditEntity.property("idInVocabulary").eq(iso3166));
			return (Country) query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationText(String text, Class<T> clazz ) {
		return getDefinedTermByRepresentationText(text,clazz,null,null);
	}

	@Override
    public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationText(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByRepresentationText(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.text", text));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<T> result = criteria.list();
		return result;
	}

	@Override
    public long countDefinedTermByRepresentationText(String text, Class<? extends DefinedTermBase> clazz) {
	    checkNotInPriorView("DefinedTermDaoImpl.countDefinedTermByRepresentationText(String text, Class<? extends DefinedTermBase> clazz)");
		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.text", text));

		criteria.setProjection(Projections.rowCount());

		return (Long)criteria.uniqueResult();
	}

	@Override
	public <T extends DefinedTermBase> List<T> getDefinedTermByIdInVocabulary(String label, UUID vocUuid, Class<T> clazz, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByIdInVocabulary(String label, UUID vocUuid, Class<T> clazz, Integer pageSize, Integer pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("vocabulary", "voc")
		    .add(Restrictions.like("voc.uuid", vocUuid))
			.add(Restrictions.like("idInVocabulary", label, org.hibernate.criterion.MatchMode.EXACT));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<T> result = criteria.list();
		return result;
	}

    @Override
	public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationAbbrev(String text, Class<T> clazz, Integer pageSize,Integer  pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getDefinedTermByRepresentationAbbrev(String abbrev, Class<T> clazz, Integer pageSize,Integer  pageNumber)");

		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.abbreviatedLabel", text));

		addPageSizeAndNumber(criteria, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		return result;
	}

	@Override
	public long countDefinedTermByRepresentationAbbrev(String text, Class<? extends DefinedTermBase> clazz) {
	    checkNotInPriorView("DefinedTermDaoImpl.countDefinedTermByRepresentationAbbrev(String abbrev, Class<? extends DefinedTermBase> clazz)");
		Criteria criteria = getCriteria(clazz);

		criteria.createAlias("representations", "r").add(Restrictions.like("r.abbreviatedLabel", text));
		criteria.setProjection(Projections.rowCount());

        return (Long)criteria.uniqueResult();
	}

	@Override
    public Language getLanguageByIso(String iso639) {
		if (iso639.length() < 2 || iso639.length() > 3) {
			logger.warn("Invalid length " + iso639.length() + " of ISO code. Length must be 2 or 3.");
			return null;
		}
		boolean isIso639_1 = iso639.length() == 2;

		String queryStr;
		if (isIso639_1){
			queryStr = "from Language where iso639_1 = :isoCode";
		}else{
			queryStr = "from Language where idInVocabulary = :isoCode and vocabulary.uuid = :vocUuid";
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery(queryStr);
		    query.setParameter("isoCode", iso639);
		    if (! isIso639_1){
		    	query.setParameter("vocUuid", Language.uuidLanguageVocabulary);
			}
		    return (Language) query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Language.class,auditEvent.getRevisionNumber());
			if (isIso639_1){
				query.add(AuditEntity.property("iso639_1").eq(iso639));
			}else{
				query.add(AuditEntity.property("iso639_2").eq(iso639));
				query.add(AuditEntity.property("vocabulary.uuid").eq(Language.uuidLanguageVocabulary));
			}

			return (Language)query.getSingleResult();
		}
	}

	/**
	 *  FIXME this will result in a query per language - could you, given that iso codes
	 *  are unique, use from Language where iso639_1 in (:isoCode) or iso639_2 in (:isoCode)
	 */
	@Override
    public List<Language> getLanguagesByIso(List<String> iso639List) {
		List<Language> languages = new ArrayList<Language>(iso639List.size());
		for (String iso639 : iso639List) {
			languages.add(getLanguageByIso(iso639));
		}
		return languages;
	}

	@Override
    public List<Language> getLanguagesByLocale(Enumeration<Locale> locales) {
		List<Language> languages = new ArrayList<Language>();
		while(locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			languages.add(getLanguageByIso(locale.getLanguage()));
		}
		return languages;
	}

	@Override
    public long count(NamedAreaLevel level, NamedAreaType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getCriteria(NamedArea.class);

		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }

		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }

		    criteria.setProjection(Projections.rowCount());

	        return (Long)criteria.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(NamedArea.class, auditEvent);

			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }

		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
		    query.addProjection(AuditEntity.id().count());
		    return (Long)query.getSingleResult();
		}
	}

	@Override
    public long countMedia(DefinedTermBase definedTerm) {
		checkNotInPriorView("DefinedTermDaoImpl.countMedia(DefinedTermBase definedTerm)");
		Query query = getSession().createQuery("select count(media) from DefinedTermBase definedTerm join definedTerm.media media where definedTerm = :definedTerm");
	    query.setParameter("definedTerm", definedTerm);

		return (Long)query.uniqueResult();
	}

	@Override
    public List<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber)");
		Query query = getSession().createQuery("select media from DefinedTermBase definedTerm join definedTerm.media media where definedTerm = :definedTerm");
		query.setParameter("definedTerm", definedTerm);

		addPageSizeAndNumber(query, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<Media> result = query.list();
		return result;
	}

	@Override
    public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(NamedArea.class);

		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }

		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }

		    addPageSizeAndNumber(criteria, pageSize, pageNumber);

	        @SuppressWarnings("unchecked")
	        List<NamedArea> result = criteria.list();
	        return result;
		} else {
            AuditQuery query = makeAuditQuery(NamedArea.class, auditEvent);

			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }

		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }

		    @SuppressWarnings("unchecked")
            List<NamedArea> result = query.getResultList();
		    return result;
		}
	}

	@Override
    public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {

	    List<NamedArea> result;

		AuditEvent auditEvent = getAuditEventFromContext();
		if (auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getCriteria(NamedArea.class);

			if (level != null) {
				criteria.add(Restrictions.eq("level", level));
			}
			if (type != null) {
				criteria.add(Restrictions.eq("type", type));
			}
			addOrder(criteria,orderHints);
			addPageSizeAndNumber(criteria, pageSize, pageNumber);

			result = criteria.list();

		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NamedArea.class,
				auditEvent.getRevisionNumber());
			if (level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
			}
			if (type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
			}
			result = query.getResultList();
		}

		defaultBeanInitializer.initializeAll(result, propertyPaths);

		return result;
	}


	@Override
    public <T extends DefinedTermBase> long countGeneralizationOf(T kindOf) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.kindOf = :kindOf");
		    query.setParameter("kindOf", kindOf);
		    return (Long)query.uniqueResult();
		} else {
            AuditQuery query = makeAuditQuery(DefinedTermBase.class,auditEvent);
			query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));
		    query.addProjection(AuditEntity.id().count());
		    return (Long)query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> long countIncludes(Collection<T> partOf) {
		if (partOf == null || partOf.isEmpty()){
			return 0;
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.partOf in (:partOf)");
	    	query.setParameterList("partOf", partOf);
		    return (Long)query.uniqueResult();
		} else {
			long count = 0;
			for(T t : partOf) {
				AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
			    query.addProjection(AuditEntity.id().count());
			    count += (Long)query.getSingleResult();
			}
			return count;
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getGeneralizationOf(T kindOf, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select term from DefinedTermBase term where term.kindOf = :kindOf");
		    query.setParameter("kindOf", kindOf);

		    addPageSizeAndNumber(query, pageSize, pageNumber);

		    @SuppressWarnings("unchecked")
            List<T> result = query.list();
		    return result;
		} else {
			 AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
			 query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));

			 addPageSizeAndNumber(query, pageSize, pageNumber);

             @SuppressWarnings("unchecked")
             List<T> result = query.getResultList();
             return result;
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getIncludes(Collection<T> partOf,	Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		if (partOf == null || partOf.isEmpty()){
			return new ArrayList<>();
		}
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select term from DefinedTermBase term where term.partOf in (:partOf)");
    		query.setParameterList("partOf", partOf);

    		addPageSizeAndNumber(query, pageSize, pageNumber);

		    @SuppressWarnings("unchecked")
            List<T> results = query.list();
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		    return results;
		} else {
			List<T> result = new ArrayList<T>();
			for(T t : partOf) {
				AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
				addPageSizeAndNumber(query, pageSize, pageNumber);

			    result.addAll(query.getResultList());
			}
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			return result;
		}
	}

	@Override
    public <T extends DefinedTermBase> long countPartOf(Set<T> definedTerms) {
		checkNotInPriorView("DefinedTermDaoImpl.countPartOf(Set<T> definedTerms)");
		Query query = getSession().createQuery("select count(distinct definedTerm) from DefinedTermBase definedTerm join definedTerm.includes included where included in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		return (Long)query.uniqueResult();
	}

	@Override
    public <T extends DefinedTermBase> List<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("DefinedTermDaoImpl.getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select distinct definedTerm from DefinedTermBase definedTerm join definedTerm.includes included where included in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);

		addPageSizeAndNumber(query, pageSize, pageNumber);

		@SuppressWarnings("unchecked")
        List<T> r = query.list();
		/**
		 * For some weird reason, hibernate returns proxies (extending the superclass), not the actual class on this,
		 * despite querying the damn database and returning the discriminator along with the rest of the object properties!
		 *
		 * Probably a bug in hibernate, but we'll manually deproxy for now since the objects are initialized anyway, the
		 * performance implications are small (we're swapping one array of references for another, not hitting the db or
		 * cache).
		 */
		List<T> results = new ArrayList<>();
		if(!definedTerms.isEmpty()) {
		    for(T t : r) {
		        T deproxied = CdmBase.deproxy(t);
                results.add(deproxied);
		    }
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		}
		return results;
	}

	@Override
    public DefinedTermBase findByUri(URI uri) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select term from DefinedTermBase term where term.uri = :uri");
		    query.setParameter("uri", uri);
		    return (DefinedTermBase<?>)query.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(DefinedTermBase.class, auditEvent);
			query.add(AuditEntity.property("uri").eq(uri));
		    return (DefinedTermBase<?>)query.getSingleResult();
		}
	}

	@Override
	public <T extends DefinedTermBase> List<T> listByTermType(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths) {
	    Query query = getSession().createQuery("SELECT term FROM DefinedTermBase term WHERE term.termType = :termType");
	    query.setParameter("termType", termType);

	    @SuppressWarnings("unchecked")
        List<T> result = query.list();

	    defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
	}

	@Override
    public <TERM extends DefinedTermBase> List<TERM> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
//		checkNotInPriorView("DefinedTermDaoImpl.listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths)");

		Query query = getSession().createQuery("FROM " + clazz.getSimpleName());

	    @SuppressWarnings("unchecked")
        List<TERM> result = query.list();

	    defaultBeanInitializer.initializeAll(result, propertyPaths);

	    return result;
	}

    @Override
    public <S extends DefinedTermBase> List<S> list(Class<S> type, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPath) {

        return deduplicateResult(super.list(type, limit, start, orderHints, propertyPath));
    }

    /**
     * Workaround for http://dev.e-taxonomy.eu/trac/ticket/5871 and #5945
     * Terms with multiple representations return identical duplicates
     * due to eager representation loading. We expect these duplicates to appear
     * in line wo we only compare one term with its predecessor. If it already
     * exists we remove it from the result.
     * @param orginals
     * @return
     */
    private <S extends DefinedTermBase<?>> List<S> deduplicateResult(List<S> orginals) {
        List<S> result = new ArrayList<>();
        Iterator<S> it = orginals.iterator();
        S last = null;
        while (it.hasNext()){
            S a = it.next();
            if (a != last){
                if (!result.contains(a)){
                    result.add(a);
                }
            }
            last = a;
        }
        return result;
    }


    @Override
    public List<NamedArea> getUuidAndTitleCache(List<TermVocabulary> vocs, Integer limit, String pattern){
        Session session = getSession();
        Query query = null;
        if (pattern != null){
            if (vocs != null && !vocs.isEmpty()){
                query = session.createQuery("from NamedArea where titleCache like :pattern and vocabulary in :vocs");
                query.setParameterList("vocs", vocs);
            }else{
                query = session.createQuery("from NamedArea where titleCache like :pattern ");
            }
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);

        } else {
            query = session.createQuery("FROM NamedArea WHERE vocabulary IN :vocs");
            query.setParameterList("vocs", vocs);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        @SuppressWarnings("unchecked")
        List<NamedArea> result = query.list();
        return result;
    }

    @Override
    public UuidAndTitleCache<DefinedTermBase> getParentUuidAndTitleCache(UuidAndTitleCache<DefinedTermBase> childTerm) {
        String parentQueryString = ""
                + "select distinct t1.uuid, t1.id, t1.titleCache "
                + "from DefinedTermBase t1, DefinedTermBase t2 "
                + "where t1.id = t2.partOf.id "
                + "and t2.id = :childTerm";
        Query query =  getSession().createQuery(parentQueryString);
        query.setParameter("childTerm", childTerm.getId());
        List<UuidAndTitleCache<DefinedTermBase>> list = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        if(result.size()==1){
            Object[] object = result.get(0);
            UUID uuid = (UUID) object[0];
            Integer id = (Integer) object[1];
            String titleCache = (String) object[2];
            return new UuidAndTitleCache<>(uuid,id, titleCache);
        }
        return null;
    }

    @Override
    public List<UuidAndTitleCache<DefinedTermBase>> getIncludesAsUuidAndTitleCache(
            UuidAndTitleCache<DefinedTermBase> parentTerm) {
        String queryString = ""
                + "select distinct t2.uuid, t2.id, t2.titleCache "
                + "from DefinedTermBase t1, DefinedTermBase t2 "
                + "where t2.partOf.id = :parentId";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("parentId", parentTerm.getId());

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<UuidAndTitleCache<DefinedTermBase>> list = generateUuidAndTitleCache(result);
        return list;
    }

    @Override
    public List<UuidAndTitleCache<DefinedTermBase>> getKindOfsAsUuidAndTitleCache(
            UuidAndTitleCache<DefinedTermBase> parentTerm) {
        String queryString = ""
                + "select distinct t2.uuid, t2.id, t2.titleCache "
                + "from DefinedTermBase t1, DefinedTermBase t2 "
                + "where t2.kindOf.id = :parentId";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("parentId", parentTerm.getId());

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<UuidAndTitleCache<DefinedTermBase>> list = generateUuidAndTitleCache(result);
        return list;
    }

    private List<UuidAndTitleCache<DefinedTermBase>> generateUuidAndTitleCache(List<Object[]> result){
        List<UuidAndTitleCache<DefinedTermBase>> list = new ArrayList<>();
        for(Object[] object : result){
            UUID uuid = (UUID) object[0];
            Integer id = (Integer) object[1];
            String titleCache = (String) object[2];
            if(titleCache!=null){
                list.add(new UuidAndTitleCache<>(uuid,id, titleCache));
            }
        }
        return list;
    }


}
