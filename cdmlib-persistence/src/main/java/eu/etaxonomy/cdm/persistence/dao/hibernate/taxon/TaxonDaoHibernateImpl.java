/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;


/**
 * @author a.mueller
 * @created 24.11.2008
 * @version 1.0
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {	
	private AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser;
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
		indexedClasses = new Class[2];
		indexedClasses[0] = Taxon.class;
		indexedClasses[1] = Synonym.class;
		super.defaultField = "name.titleCache";
	}
	
	@Autowired
	private ITaxonNameDao taxonNameDao;
	
	@Autowired(required = false)   //TODO switched of because it caused problems when starting CdmApplicationController
	public void setAlternativeSpellingSuggestionParser(AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser) {
		this.alternativeSpellingSuggestionParser = alternativeSpellingSuggestionParser; 
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true, false);
	}
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.ReferenceBase, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
	 */
	public List<Taxon> getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications, List<String> propertyPaths) {
		checkNotInPriorView("TaxonDaoHibernateImpl.getRootTaxa(Rank rank, ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications)");
		if (onlyWithChildren == null){
			onlyWithChildren = true;
		}
		if (withMisapplications == null){
			withMisapplications = true;
		}
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}

		Criteria crit = getSession().createCriteria(Taxon.class);
		
		crit.setFetchMode("name", FetchMode.JOIN);
		crit.createAlias("name", "name");
		
		if (rank != null) {
			crit.add(Restrictions.eq("name.rank", rank));
		}else{
			crit.add(Restrictions.isNull("taxonomicParentCache"));
		}

		if (sec != null){
			crit.add(Restrictions.eq("sec", sec) );
		}

		if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
			logger.info("Not fetching child taxa");
			//TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
			crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
		}

		List<Taxon> results = new ArrayList<Taxon>();
		List<Taxon> taxa = crit.list();
		for(Taxon taxon : taxa){
			
			
			//childTaxa
			//TODO create restriction instead
			// (a) not using cache fields
			/*Hibernate.initialize(taxon.getRelationsFromThisTaxon());
			if (onlyWithChildren == false || taxon.getRelationsFromThisTaxon().size() > 0){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
					defaultBeanInitializer.initialize(taxon, propertyPaths);
					results.add(taxon);
				}
			}*/
			// (b) using cache fields
			if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
				if (withMisapplications == true || ! taxon.isMisappliedName()){
					defaultBeanInitializer.initialize(taxon, propertyPaths);
					results.add(taxon);
				}
			}
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications) {
		return getRootTaxa(null, sec, cdmFetch, onlyWithChildren, withMisapplications, null);
	}
	

	public List<TaxonBase> getTaxaByName(String queryString, ReferenceBase sec) {
		
		return getTaxaByName(queryString, true, sec);
	}

	public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, ReferenceBase sec) {
		checkNotInPriorView("TaxonDaoHibernateImpl.getTaxaByName(String name, ReferenceBase sec)");
		
        Criteria criteria = null;
		if (accepted == true) {
			criteria = getSession().createCriteria(Taxon.class);
		} else {
			criteria = getSession().createCriteria(Synonym.class);
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if (sec != null && sec.getId() != 0) {
			criteria.add(Restrictions.eq("sec", sec ) );
		}

		if (queryString != null) {
			criteria.add(Restrictions.ilike("name.nameCache", queryString));
		}

		return (List<TaxonBase>)criteria.list();
	}

	public List<TaxonBase> getTaxaByName(Class<? extends TaxonBase> clazz, String queryString, MatchMode matchMode,
			Integer pageSize, Integer pageNumber) {
		
		return getTaxaByName(clazz, queryString, null, matchMode, null, pageSize, pageNumber, null);
	}
	
	public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode, 
			Boolean accepted, Integer pageSize, Integer pageNumber) {
		
		if (accepted == true) {
			return getTaxaByName(Taxon.class, queryString, matchMode, pageSize, pageNumber);
		} else {
			return getTaxaByName(Synonym.class, queryString, matchMode, pageSize, pageNumber);
		}
	}
	
	
	public List<TaxonBase> getTaxaByName(Class<? extends TaxonBase> clazz, String queryString, TaxonomicTree taxonomicTree,
			MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, 
			Integer pageNumber, List<String> propertyPaths) {
				
		boolean doCount = false;
		Query query = prepareTaxaByName(clazz, queryString, taxonomicTree, matchMode, namedAreas, pageSize, pageNumber, doCount);
		if (query != null){
			List<TaxonBase> results = query.list();
			results.addAll (prepareTaxaByCommonName(queryString, taxonomicTree, matchMode, namedAreas, pageSize, pageNumber, doCount).list());
			defaultBeanInitializer.initializeAll(results, propertyPaths);
			return results;
		}
		return new ArrayList<TaxonBase>();
		
	}

	public List<TaxonBase> getTaxaByCommonName(String queryString, TaxonomicTree taxonomicTree,
			MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, 
			Integer pageNumber, List<String> propertyPaths) {
			boolean doCount = false;	
			Query query = prepareTaxaByCommonName(queryString, taxonomicTree, matchMode, namedAreas, pageSize, pageNumber, doCount);
			if (query != null){
				List<TaxonBase> results = query.list();
				defaultBeanInitializer.initializeAll(results, propertyPaths);
				return results;
			}
			return new ArrayList<TaxonBase>();
		
	}
	

	/**
	 * @param clazz
	 * @param queryString
	 * @param taxonomicTree TODO
	 * @param matchMode
	 * @param namedAreas
	 * @param pageSize
	 * @param pageNumber
	 * @param doCount
	 * @return
	 * 
	 * FIXME implement taxontree restriction & implement test: see {@link TaxonDaoHibernateImplTest#testCountTaxaByName()}
	 */
	private Query prepareTaxaByName(Class<? extends TaxonBase> clazz, String queryString, TaxonomicTree taxonomicTree,
			MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, boolean doCount) {

		//TODO ? checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec)");

		String hqlQueryString = matchMode.queryStringFrom(queryString);
		
		String matchOperator;
		if (matchMode == MatchMode.EXACT) {
			matchOperator = "=";
		} else {
			matchOperator = "like";
		}
		
		String selectWhat = (doCount ? "count(t)": "t");
		
		String hql = "";
		Set<NamedArea> areasExpanded = new HashSet<NamedArea>();
		if(namedAreas != null && namedAreas.size() > 0){
			// expand areas and restrict by distribution area
			List<NamedArea> childAreas;
			Query areaQuery = getSession().createQuery("select childArea from NamedArea as childArea left join childArea.partOf as parentArea where parentArea = :area");
			expandNamedAreas(namedAreas, areasExpanded, areaQuery);
		}
		boolean doAreaRestriction = areasExpanded.size() > 0;
		
		Set<UUID> namedAreasUuids = new HashSet<UUID>();
		for (NamedArea area:areasExpanded){
			namedAreasUuids.add(area.getUuid());
		}
		
		String taxonSubselect = null;
		String synonymSubselect = null;
		
		if(taxonomicTree != null){
			
			if(doAreaRestriction){
				
				taxonSubselect = "select t from" +
					" Distribution e" +
					" join e.inDescription d" +
					" join d.taxon t" +
					" join t.name n " +
					" join t.taxonNodes as tn "+
					" where" +
					" e.area.uuid in (:namedAreasUuids) AND" +
					" tn.taxonomicTree = :taxonomicTree" +
					" AND n.nameCache " + matchOperator + " :queryString";
				
				
				synonymSubselect = "select s from" +
					" Distribution e" +
					" join e.inDescription d" +
					" join d.taxon t" + // the taxa
					" join t.taxonNodes as tn "+
					" join t.synonymRelations sr" +
					" join sr.relatedFrom s" + // the synonyms
					" join s.name sn"+ 
					" where" +
					" e.area.uuid in (:namedAreasUuids) AND" +
					" tn.taxonomicTree = :taxonomicTree" +
					" AND sn.nameCache " + matchOperator + " :queryString";
				
			} else {
				
				taxonSubselect = "select t from" +
					" Taxon t" +
					" join t.name n " +
					" join t.taxonNodes as tn "+
					" where" +
					" tn.taxonomicTree = :taxonomicTree" +
					" AND n.nameCache " + matchOperator + " :queryString";
				
				synonymSubselect = "select s from" +
					" Taxon t" + // the taxa
					" join t.taxonNodes as tn "+
					" join t.synonymRelations sr" +
					" join sr.relatedFrom s" + // the synonyms
					" join s.name sn"+ 
					" where" +
					" tn.taxonomicTree = :taxonomicTree" +
					" AND sn.nameCache " + matchOperator + " :queryString";
			}	
		} else {
			
			if(doAreaRestriction){
				
				taxonSubselect = "select t from " +
					" Distribution e" +
					" join e.inDescription d" +
					" join d.taxon t" +
					" join t.name n "+
					" where" +
					(doAreaRestriction ? " e.area.uuid in (:namedAreasUuids) AND" : "") +
					" n.nameCache " + matchOperator + " :queryString";
				
				synonymSubselect = "select s from" +
					" Distribution e" +
					" join e.inDescription d" +
					" join d.taxon t" + // the taxa
					" join t.synonymRelations sr" +
					" join sr.relatedFrom s" + // the synonyms
					" join s.name sn"+ 
					" where" +
					(doAreaRestriction ? " e.area.uuid in (:namedAreasUuids) AND" : "") +
					" sn.nameCache " + matchOperator + " :queryString";
				
			} else {
				
				taxonSubselect = "select t from " +
					" Taxon t" +
					" join t.name n "+
					" where" +
					" n.nameCache " + matchOperator + " :queryString";

				synonymSubselect = "select s from" +
					" Taxon t" + // the taxa
					" join t.synonymRelations sr" +
					" join sr.relatedFrom s" + // the synonyms
					" join s.name sn"+ 
					" where" +
					" sn.nameCache " + matchOperator + " :queryString";
			}
			
		
		}
		
		
		
		// TODO  mysql needs  optimization:  see http://www.xaprb.com/blog/2006/04/30/how-to-optimize-subqueries-and-joins-in-mysql/#commen
		Query subTaxon = null;
		Query subSynonym = null;
		if(clazz.equals(Taxon.class)){
			// find Taxa
			subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);
			//subTaxon = getSession().createQuery(taxonSubselect);
			
			if(doAreaRestriction){
				subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
			}	
			if(taxonomicTree != null){
				subTaxon.setParameter("taxonomicTree", taxonomicTree);
			}
		} else if(clazz.equals(Synonym.class)){
			// find synonyms
			subSynonym = getSession().createQuery(synonymSubselect).setParameter("queryString", hqlQueryString);
			
			if(doAreaRestriction){
				subSynonym.setParameterList("namedAreasUuids", namedAreasUuids);
			}		
			if(taxonomicTree != null){
				subSynonym.setParameter("taxonomicTree", taxonomicTree);
			}
		} else {
			// find taxa and synonyms
			subSynonym = getSession().createQuery(synonymSubselect).setParameter("queryString", hqlQueryString);
			subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);
			if(doAreaRestriction){
				subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
				subSynonym.setParameterList("namedAreasUuids", namedAreasUuids);
			}
			if(taxonomicTree != null){
				subTaxon.setParameter("taxonomicTree", taxonomicTree);
				subSynonym.setParameter("taxonomicTree", taxonomicTree);
			}
		}
		
		List<TaxonBase> taxa = new ArrayList<TaxonBase>();
		List<TaxonBase> synonyms = new ArrayList<TaxonBase>();
		if(clazz.equals(Taxon.class)){
			taxa = subTaxon.list();
			
		}else if (clazz.equals(Synonym.class)){
			synonyms = subSynonym.list();
		}else {
			taxa = subTaxon.list();
			synonyms = subSynonym.list();
		}
		if(clazz.equals(Taxon.class)){
			if  (taxa.size()>0){
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t in (:taxa)";
			}else{
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
			}
		} else if(clazz.equals(Synonym.class) ){
			if (synonyms.size()>0){
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t in (:synonyms)";		
			}else{
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
			}
		} else {
			if(synonyms.size()>0 && taxa.size()>0){
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t in (:taxa) OR t in (:synonyms)";
			}else if (synonyms.size()>0 ){
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" 
				+ " where t in (:synonyms)";	
			} else if (taxa.size()>0 ){
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t in (:taxa) ";
			} else{
				hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
			}
		}
		
		if (hql == "") return null;
		if(!doCount){
			hql += " order by t.titleCache"; //" order by t.name.nameCache";
		}
	
		Query query = getSession().createQuery(hql);
		
		if(clazz.equals(Taxon.class) && taxa.size()>0){
			//find taxa
			query.setParameterList("taxa", taxa );
		} else if(clazz.equals(Synonym.class) && synonyms.size()>0){
			// find synonyms
			query.setParameterList("synonyms", synonyms);
			
		
		} else {
			// find taxa and synonyms
			if (taxa.size()>0){
				query.setParameterList("taxa", taxa);
			}
			if (synonyms.size()>0){
				query.setParameterList("synonyms",synonyms);
			}
			if (taxa.size()== 0 && synonyms.size() == 0){
				return null;
			}
		}
		
		if(pageSize != null &&  !doCount) {
			query.setMaxResults(pageSize);
			if(pageNumber != null) {
				query.setFirstResult(pageNumber * pageSize);
			}
		}
		
		return query;
	}
	
	private Query prepareTaxaByCommonName(String queryString, TaxonomicTree taxonomicTree,
			MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, boolean doCount){
		
		String matchOperator;
		if (matchMode == MatchMode.EXACT) {
			matchOperator = "=";
		} else {
			matchOperator = "like";
		}
		
		String hql= "from Taxon t " +
		"join t.descriptions d "+
		"join d.descriptionElements e " +
		"join e.feature f " +
		"where f.supportsCommonTaxonName = true and e.name "+matchOperator+" :queryString";//and ls.text like 'common%'";
		
		Query query = getSession().createQuery(hql);
		
		query.setParameter("queryString", queryString);
		
		if(pageSize != null &&  !doCount) {
			query.setMaxResults(pageSize);
			if(pageNumber != null) {
				query.setFirstResult(pageNumber * pageSize);
			}
		}
		return query;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.util.Set)
	 */
	public long countTaxaByName(Class<? extends TaxonBase> clazz, String queryString, TaxonomicTree taxonomicTree,
		MatchMode matchMode, Set<NamedArea> namedAreas) {
		
		boolean doCount = true;
		Query query = prepareTaxaByName(clazz, queryString, taxonomicTree, matchMode, namedAreas, null, null, doCount);
		if (query != null) {
			return (Long)query.uniqueResult();
		}
		return 0;
		
	}

	/**
	 * @param namedAreas
	 * @param areasExpanded
	 * @param areaQuery
	 */
	private void expandNamedAreas(Collection<NamedArea> namedAreas, Set<NamedArea> areasExpanded, Query areaQuery) {
		List<NamedArea> childAreas;
		for(NamedArea a : namedAreas){
			areasExpanded.add(a);
			areaQuery.setParameter("area", a);
			childAreas = areaQuery.list();
			if(childAreas.size() > 0){
				areasExpanded.addAll(childAreas);
				expandNamedAreas(childAreas, areasExpanded, areaQuery);
			}
		}
	}
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode)
//	 */
//	public Integer countTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode) {		
//		return countTaxaByName(queryString, matchMode, selectMode, null);
//	}

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode, eu.etaxonomy.cdm.model.reference.ReferenceBase)
//	 */
//	public Integer countTaxaByName(String queryString, 
//			MatchMode matchMode, SelectMode selectMode, ReferenceBase sec) {
//
//		Long count = countTaxaByName(queryString, matchMode, selectMode, sec, null);
//		return count.intValue();
//
//	}
	
//	public Integer countTaxaByName(String queryString, MatchMode matchMode, Boolean accepted) {
//		
//		SelectMode selectMode = (accepted ? SelectMode.TAXA : SelectMode.SYNONYMS);
//		Long count = countTaxaByName(queryString, matchMode, selectMode, null, null);
//		return count.intValue();
//	}
	

	public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page) {
		return super.list(pagesize, page);
	}

	public List<Synonym> getAllSynonyms(Integer limit, Integer start) {
		Criteria criteria = getSession().createCriteria(Synonym.class);
		
		if(limit != null) {
			criteria.setFirstResult(start);
			criteria.setMaxResults(limit);
		}
		
		return criteria.list();
	}

	public List<Taxon> getAllTaxa(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Taxon.class);
		
		if(limit != null) {
			criteria.setFirstResult(start);
			criteria.setMaxResults(limit);
		}
		
		return criteria.list();
	}
	
	

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getSession().createCriteria(RelationshipBase.class);
		    return (List<RelationshipBase>)criteria.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(RelationshipBase.class,auditEvent.getRevisionNumber());
			return (List<RelationshipBase>)query.getResultList();
		}
	}
	
	/** Sets the taxonomic parent to null. Does not handle taxonomic relationships. */
//	private boolean nullifyTaxonomicParent(Taxon taxon) {
//
//		try {
//			Method nullifyTaxonomicParent = taxon.getClass().getMethod("nullifyTaxonomicParent");
//			nullifyTaxonomicParent.invoke(taxon);
//		} catch (NoSuchMethodException ex) {
//			logger.error("NoSuchMethod: " + ex.getMessage());
//			return false;
//		} catch (IllegalArgumentException ex) {
//			logger.error("IllegalArgumentException: " + ex.getMessage());
//			return false;
//		} catch (IllegalAccessException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		} catch (InvocationTargetException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		}
//		return true;
//	}
	
	@Override
	public UUID delete(TaxonBase taxonBase) throws DataAccessException{
		if (taxonBase == null){
			logger.warn("TaxonBase was 'null'");
			return null;
		}
		
		// Merge the object in if it is detached
		//
		// I think this is preferable to catching lazy initialization errors 
		// as that solution only swallows and hides the exception, but doesn't 
		// actually solve it.
		getSession().merge(taxonBase);
		
		if (taxonBase instanceof Taxon){ //	is Taxon
			for (Iterator<TaxonRelationship> iterator = ((Taxon)taxonBase).getRelationsFromThisTaxon().iterator(); iterator.hasNext();){
				TaxonRelationship relationFromThisTaxon = iterator.next();
				
				// decrease children count of taxonomic parent by one
				if (relationFromThisTaxon.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
					Taxon toTaxon = relationFromThisTaxon.getToTaxon(); // parent
					if (toTaxon != null) {
						toTaxon.setTaxonomicChildrenCount(toTaxon.getTaxonomicChildrenCount() - 1);	
					}
				}
			}
		}
		
		return super.delete(taxonBase);
	}


	// TODO add generic return type !!
	public List findByName(String queryString, MatchMode matchMode, int page, int pagesize, boolean onlyAcccepted) {
		ArrayList<Criterion> criteria = new ArrayList<Criterion>();
		//TODO ... Restrictions.eq(propertyName, value)
		return super.findByTitle(queryString, matchMode, page, pagesize, criteria);

	}

	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		crit.setProjection(Projections.rowCount());
		int result = ((Integer)crit.list().get(0)).intValue();
		return result;
	}


	public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.setProjection(Projections.rowCount());
		int result = ((Integer)crit.list().get(0)).intValue();
		return result;
	}

	public int countTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Direction direction) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = null;
		
		    if(type == null) {
			    query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon");
		    } else {
			    query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon and taxonRelationship.type = :type");
			    query.setParameter("type",type);
		    }
		    query.setParameter("relatedTaxon", taxon);
		
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId(direction.toString()).eq(taxon.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countSynonyms(Taxon taxon, SynonymRelationshipType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

			criteria.add(Restrictions.eq("relatedTo", taxon));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		    criteria.setProjection(Projections.rowCount());
			return (Integer)criteria.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			return ((Long)query.getSingleResult()).intValue();
		}
	}
	
	public int countSynonyms(Synonym synonym, SynonymRelationshipType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

			criteria.add(Restrictions.eq("relatedFrom", synonym));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		    
		    criteria.setProjection(Projections.rowCount());
			return (Integer)criteria.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedFrom").eq(synonym.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank) {
		checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(Boolean accepted, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank)");
        Criteria criteria = null;
		
		if(clazz == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			criteria = getSession().createCriteria(clazz);		
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if(genusOrUninomial != null) {
			criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
		}
		
		if(infraGenericEpithet != null) {
			criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
		}
		
		if(specificEpithet != null) {
			criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));
		}
		
		if(infraSpecificEpithet != null) {
			criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
		}
		
		if(rank != null) {
			criteria.add(Restrictions.eq("name.rank", rank));
		}
		
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
	
		return (Integer)criteria.uniqueResult();
	}

	public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("TaxonDaoHibernateImpl.findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber)");
		Criteria criteria = null;
		
		if(clazz == null) {
			criteria = getSession().createCriteria(TaxonBase.class);
		} else {
			criteria = getSession().createCriteria(clazz);
		}
		
		criteria.setFetchMode( "name", FetchMode.JOIN );
		criteria.createAlias("name", "name");
		
		if(genusOrUninomial == null) {
			criteria.add(Restrictions.isNull("name.genusOrUninomial"));
		} else if(!genusOrUninomial.equals("*")) {
			criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
		}
		
		if(infraGenericEpithet == null) {
			criteria.add(Restrictions.isNull("name.infraGenericEpithet"));
		} else if(!infraGenericEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
		} 
		
		if(specificEpithet == null) {
			criteria.add(Restrictions.isNull("name.specificEpithet"));
		} else if(!specificEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));
			
		}
		
		if(infraSpecificEpithet == null) {
			criteria.add(Restrictions.isNull("name.infraSpecificEpithet"));
		} else if(!infraSpecificEpithet.equals("*")) {
			criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
		}
		
		if(rank != null) {
			criteria.add(Restrictions.eq("name.rank", rank));
		}
		
		if(pageSize != null) {
	    	criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    } else {
		    	criteria.setFirstResult(0);
		    }
		}
	
		return (List<TaxonBase>)criteria.list();
	}

	public List<TaxonRelationship> getTaxonRelationships(Taxon taxon,	TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths, Direction direction) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonRelationship.class);
            
			criteria.add(Restrictions.eq("relatedTo", taxon));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		
            addOrder(criteria,orderHints);
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        } else {
		        	criteria.setFirstResult(0);
		        }
		    }
		
		    List<TaxonRelationship> result = (List<TaxonRelationship>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			if(pageSize != null) {
		        query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		            query.setFirstResult(pageNumber * pageSize);
		        } else {
		    	    query.setFirstResult(0);
		        }
		    }
			
			List<TaxonRelationship> result = (List<TaxonRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			// Ugly, but for now, there is no way to sort on a related entity property in Envers,
			// and we can't live without this functionality in CATE as it screws up the whole 
			// taxon tree thing
			if(orderHints != null && !orderHints.isEmpty()) {
			    SortedSet<TaxonRelationship> sortedList = new TreeSet<TaxonRelationship>(new TaxonRelationshipFromTaxonComparator());
			    sortedList.addAll(result);
			    return new ArrayList<TaxonRelationship>(sortedList);
			}
			
			return result;
		}
	}
	
	class TaxonRelationshipFromTaxonComparator implements Comparator<TaxonRelationship> {

		public int compare(TaxonRelationship o1, TaxonRelationship o2) {
			return o1.getFromTaxon().getTitleCache().compareTo(o2.getFromTaxon().getTitleCache());
		}
		
	}

	public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);
            
			criteria.add(Restrictions.eq("relatedTo", taxon));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		
            addOrder(criteria,orderHints);
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        } else {
		        	criteria.setFirstResult(0);
		        }
		    }
		
		    List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			if(pageSize != null) {
		        query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		            query.setFirstResult(pageNumber * pageSize);
		        } else {
		    	    query.setFirstResult(0);
		        }
		    }
			
			List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			return result;
		}
	}
	
	public List<SynonymRelationship> getSynonyms(Synonym synonym, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);
            
			criteria.add(Restrictions.eq("relatedFrom", synonym));
		    if(type != null) {
		    	criteria.add(Restrictions.eq("type", type));
		    } 
		
            addOrder(criteria,orderHints);
		
		    if(pageSize != null) {
		    	criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		        	criteria.setFirstResult(pageNumber * pageSize);
		        } else {
		        	criteria.setFirstResult(0);
		        }
		    }
		
		    List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    
		    return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("relatedFrom").eq(synonym.getId()));
			
			if(type != null) {
				query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
			
			if(pageSize != null) {
		        query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		            query.setFirstResult(pageNumber * pageSize);
		        } else {
		    	    query.setFirstResult(0);
		        }
		    }
			
			List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			return result;
		}
	}
	
	@Override
	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		
		for(TaxonBase taxonBase : list(null,null)) { // re-index all taxon base
			Hibernate.initialize(taxonBase.getName());
			fullTextSession.index(taxonBase);
		}
		fullTextSession.flushToIndexes();
	}
	
	@Override
	public String suggestQuery(String queryString) {
		checkNotInPriorView("TaxonDaoHibernateImpl.suggestQuery(String queryString)");
		String alternativeQueryString = null;
		if (alternativeSpellingSuggestionParser != null) {
			try {

				alternativeSpellingSuggestionParser.parse(queryString);
				org.apache.lucene.search.Query alternativeQuery = alternativeSpellingSuggestionParser.suggest(queryString);
				if (alternativeQuery != null) {
					alternativeQueryString = alternativeQuery
							.toString("name.titleCache");
				}

			} catch (ParseException e) {
				throw new QueryParseException(e, queryString);
			}
		}
		return alternativeQueryString;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheOfAcceptedTaxa(eu.etaxonomy.cdm.model.taxon.TaxonomicTree)
	 */
	public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByTaxonomicTree(TaxonomicTree taxonomicTree) {

		int taxonomicTreeId = taxonomicTree.getId();
		
		String queryString = "SELECT nodes.uuid, taxa.titleCache FROM TaxonNode AS nodes LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id WHERE taxa.DTYPE = 'Taxon' AND nodes.taxonomictree_id = " + taxonomicTreeId;
		
		List<Object[]> result = getSession().createSQLQuery(queryString).list();
				
		if(result.size() == 0){
			return null;
		}else{
			List<UuidAndTitleCache<TaxonNode>> list = new ArrayList<UuidAndTitleCache<TaxonNode>>(result.size()); 
			
			for (Object object : result){
				
				Object[] objectArray = (Object[]) object;
				
				UUID uuid = UUID.fromString((String) objectArray[0]);
				String titleCache = (String) objectArray[1];
				
				list.add(new UuidAndTitleCache(TaxonNode.class, uuid, titleCache));
			}
			
			return list;	
		}
	}
	
	
	public class UuidAndTitleCacheOfAcceptedTaxon{
		UUID uuid;
		
		String titleCache;

		public UuidAndTitleCacheOfAcceptedTaxon(UUID uuid, String titleCache){
			this.uuid = uuid;
			this.titleCache = titleCache;
		}
		
		public UUID getUuid() {
			return uuid;
		}

		public void setUuid(UUID uuid) {
			this.uuid = uuid;
		}

		public String getTitleCache() {
			return titleCache;
		}

		public void setTitleCache(String titleCache) {
			this.titleCache = titleCache;
		}
	}
	
	@Override
	public TaxonBase find(LSID lsid) {
		TaxonBase taxonBase = super.find(lsid);
		if(taxonBase != null) {
			List<String> propertyPaths = new ArrayList<String>();
			propertyPaths.add("createdBy");
			propertyPaths.add("updatedBy");
			propertyPaths.add("name");
			propertyPaths.add("sec");
			propertyPaths.add("relationsToThisTaxon");
			propertyPaths.add("relationsToThisTaxon.fromTaxon");
			propertyPaths.add("relationsToThisTaxon.toTaxon");
			propertyPaths.add("relationsFromThisTaxon");
			propertyPaths.add("relationsFromThisTaxon.toTaxon");
			propertyPaths.add("relationsToThisTaxon.type");
			propertyPaths.add("synonymRelations");
			propertyPaths.add("synonymRelations.synonym");
			propertyPaths.add("synonymRelations.type");
			propertyPaths.add("descriptions");
			
			defaultBeanInitializer.initialize(taxonBase, propertyPaths);
		}
		return taxonBase;
	}

	public List<TaxonBase> getTaxaByCommonName(String queryString,
			TaxonomicTree taxonomicTree, MatchMode matchMode,
			Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean createInferredSynonymy(TaxonomicTree tree) {
		boolean result = true;
		//1. get a list of all accepted taxa of rank species
		List<Taxon> acceptedTaxa = this.getRootTaxa(Rank.SPECIES(), null, null, false, true, null);
		List<ZoologicalName> zooNames = this.taxonNameDao.getAllZoologicalNames(null, 0);
		//create HashMap
		HashMap <UUID, ZoologicalName> zooHashMap = new HashMap<UUID, ZoologicalName>();
		UUID uuid;
		
		for (ZoologicalName zooName: zooNames){
			uuid = zooName.getUuid();
			zooHashMap.put(uuid, zooName);
		}
		HashMap<UUID,TaxonBase> taxaToSave = new HashMap<UUID,TaxonBase>();
		//2. get the synonyms of the genus of all these taxa
		for (Taxon taxon: acceptedTaxa){
			ZoologicalName zooName;
			TaxonNameBase name = taxon.getName();
			
			zooName = zooHashMap.get(name.getUuid());
					
			String epithetOfTaxon = zooName.getSpecificEpithet();
			String genusOfTaxon = zooName.getGenusOrUninomial();
		 	Set<TaxonNode> nodes = taxon.getTaxonNodes();
		 	
			for (TaxonNode node: nodes){
				List<String> synonymsGenus = new ArrayList<String>();
			 	List<String> synonymsEpithet = new ArrayList<String>();
				if (node.getTaxonomicTree().equals(tree)){
					if (!node.isTopmostNode()){
					TaxonNode parent = (TaxonNode) node.getParent();
					parent = (TaxonNode)HibernateProxyHelper.deproxy(parent);
					TaxonNameBase parentName = parent.getTaxon().getName();
					parentName = (TaxonNameBase)HibernateProxyHelper.deproxy(parentName);
					if (parentName.isGenus()){
						Set<Synonym> synonymsOfGenus = parentName.getSynonyms();
						TaxonNameBase synName;
						NonViralName inferredSynName;
						Synonym inferredEpithet;
						
						List<String> propertyPaths = new ArrayList<String>();
						propertyPaths.add("synonym");
						propertyPaths.add("synonym.name");
						
						List<OrderHint> orderHints = new ArrayList<OrderHint>();
						orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));
						
						List<SynonymRelationship> synonymRelationshipsOfGenus = getSynonyms(parent.getTaxon(), null, null, null,orderHints,propertyPaths);
						
						
						//3. combine these synonyms with the corresponding accepted epithet
						for (SynonymRelationship synonymRelationOfGenus:synonymRelationshipsOfGenus){
							
							Synonym syn = synonymRelationOfGenus.getSynonym();
							synName =syn.getName();
							zooName = zooHashMap.get(synName.getUuid());
							//String epithetName = zooName.getSpecificEpithet();
							String genusName = zooName.getGenusOrUninomial();
							if (!synonymsGenus.contains(genusName)){
								synonymsGenus.add(genusName);
							}
							inferredSynName = NonViralName.NewInstance(Rank.SPECIES());
							inferredSynName.setSpecificEpithet(epithetOfTaxon);
							inferredSynName.setGenusOrUninomial(genusName);

							List<TaxonBase> synInCDM = this.getTaxaByName(inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet(), MatchMode.BEGINNING, false,null, null);
							if (synInCDM.size() != 0){
								logger.info("Synonym already in DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet() );
								System.err.println("Synonym already in DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet());
							}else{
								logger.info("new Synonym to DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet() );
								
								System.err.println("new inferred epithet");
								inferredEpithet = Synonym.NewInstance(inferredSynName, null);
								taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_EPITHET_OF());
								
							}
						}
						//4. get the synonyms of the species and extract the epithets
						TaxonNameBase taxonName = taxon.getName();
						taxonName = (TaxonNameBase)HibernateProxyHelper.deproxy(taxonName);
												
						List<SynonymRelationship> synonymsOfSpecies = getSynonyms(taxon, null, null, null,orderHints,propertyPaths);
											
						//5. combine the syn. epithets with the accepted genus
						for (SynonymRelationship synonymRelationOfSpecies:synonymsOfSpecies){
							Synonym syn = synonymRelationOfSpecies.getSynonym();
							synName =syn.getName();
							zooName = zooHashMap.get(synName.getUuid());
							String epithetName = zooName.getSpecificEpithet();
							if (!synonymsEpithet.contains(epithetName)){
								synonymsEpithet.add(epithetName);
							}
							inferredSynName = NonViralName.NewInstance(Rank.SPECIES());
							inferredSynName.setSpecificEpithet(epithetName);
							inferredSynName.setGenusOrUninomial(genusOfTaxon);
							inferredEpithet = Synonym.NewInstance(inferredSynName, null);
							
							List<TaxonBase> synInCDM = this.getTaxaByName(inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet(), MatchMode.BEGINNING, false,null, null);
							if (synInCDM.size() != 0){
								logger.info("Synonym already in DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet() );
								System.err.println("Synonym already in DB"+ inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet());
							}else{
								logger.info("new Synonym to DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet());
								System.err.println("new inferred genus"+ inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet());
								inferredEpithet = Synonym.NewInstance(inferredSynName, null);
								taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_GENUS_OF());
								
							}
						}
						
						for (String epithetName:synonymsEpithet){
							for (String genusName: synonymsGenus){
								inferredSynName = NonViralName.NewInstance(Rank.SPECIES());
								inferredSynName.setSpecificEpithet(epithetName);
								inferredSynName.setGenusOrUninomial(genusName);
								inferredEpithet = Synonym.NewInstance(inferredSynName, null);
								List<TaxonBase> synInCDM = this.getTaxaByName(inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet(), MatchMode.BEGINNING, false,null, null);
								
								if (synInCDM.size() != 0){
									logger.info("Synonym already in DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet() );
									System.err.println("potential combination already in DB");
								}else{
									logger.info("new Synonym to DB" + inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet() );
									System.err.println("new potential combination"+ inferredSynName.getGenusOrUninomial() + " "+inferredSynName.getSpecificEpithet());
									inferredEpithet = Synonym.NewInstance(inferredSynName, null);
									taxon.addSynonym(inferredEpithet, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
									
								}
							}
						}
						
					}else{
						//TODO: what to do with SubGenus or SubSpecies??
					}
				}
						
				}
			}
			if (!taxaToSave.containsKey(taxon.getUuid())){
				taxaToSave.put(taxon.getUuid(), taxon);
			}
			}
		//}
		
		this.saveAll(taxaToSave.values());
		
		
		
		return result;
		
		
	}


	public int countAllRelationships() {
		List<RelationshipBase> relationships = this.getAllRelationships(null, 0);
		return relationships.size();
	}
	
	

	
	
}
