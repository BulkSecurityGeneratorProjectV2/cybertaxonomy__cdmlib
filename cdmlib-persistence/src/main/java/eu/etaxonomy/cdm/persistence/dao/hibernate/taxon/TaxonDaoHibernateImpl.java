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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.FetchType;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;

/**
 * @author a.mueller
 *
 */
@Repository
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {
	static Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren) {
		if (onlyWithChildren == null){
			onlyWithChildren = true;
		}
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}
		
		
//		String query = "from Taxon root ";
//		query += " where root.taxonomicParentCache is NULL ";
//		if (sec != null){
//			query += " AND root.sec.id = :sec "; 
//		}		
//		Query q = getSession().createQuery(query);
//		if (sec != null){
//			q.setInteger("sec", sec.getId());
//		}
		
		
		Criteria crit = getSession().createCriteria(Taxon.class);
		crit.add(Restrictions.isNull("taxonomicParentCache"));
		if (sec != null){
			crit.add(Restrictions.eq("sec", sec) );
		}
//		if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
//			logger.warn("no child taxa fetch qq");
			//TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
//			crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
//		}
		
		List<Taxon> results = new ArrayList<Taxon>();
		for(Taxon taxon : (List<Taxon>) crit.list()){
			if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
				results.add(taxon);
			}
		}
		return results;
	}

	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec) {
		Criteria crit = getSession().createCriteria(Taxon.class);
		if (sec != null){
			crit.add(Restrictions.eq("sec", sec ) );
		}
		crit.createCriteria("name").add(Restrictions.eq("titleCache", name));
		List<TaxonBase> results = crit.list();
		return results;
	}

	public List<TaxonBase> getAllTaxa(Integer pagesize, Integer page) {
		Criteria crit = getSession().createCriteria(TaxonBase.class);
		List<TaxonBase> results = crit.list();
		// TODO add page & pagesize criteria
		return results;
	}
	
	@Override
	public UUID delete(TaxonBase taxonBase) throws DataAccessException{
		
		Set<Annotation> annotations = taxonBase.getAnnotations();
		for (Annotation annotation: annotations){
			taxonBase.removeAnnotation(annotation);
		}
		Set<Marker> markers = taxonBase.getMarkers();
		for (Marker marker: markers){
			taxonBase.removeMarker(marker);
		}
		Set<OriginalSource> origSources = taxonBase.getSources();
		for (OriginalSource source: origSources){
			taxonBase.removeSource(source);
		}
		taxonBase.getName().removeTaxonBase(taxonBase);
		if (taxonBase instanceof Taxon){
			Taxon taxon = (Taxon)taxonBase;
			Set<TaxonRelationship> taxRels = taxon.getTaxonRelations();
			for (TaxonRelationship taxRel: taxRels){
				taxon.removeTaxonRelation(taxRel);
			} ;
			Set<SynonymRelationship> synRels = taxon.getSynonymRelations();
			for (SynonymRelationship synRel: synRels){
				taxon.removeSynonymRelation(synRel);
			} ;
		}else if (taxonBase instanceof Synonym){
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> synRels = synonym.getSynonymRelations();
			for (SynonymRelationship synRel: synRels){
				synonym.removeSynonymRelation(synRel);
			} ;
		}
		return super.delete(taxonBase);
	}
	
}