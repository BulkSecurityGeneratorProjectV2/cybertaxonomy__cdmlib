/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoBaseImpl;
import eu.etaxonomy.cdm.persistence.dao.molecular.IAmplificationDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @since 11.03.2014
 *
 */
@Repository
public class AmplificationDaoHibernateImpl extends AnnotatableDaoBaseImpl<Amplification> implements IAmplificationDao{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(AmplificationDaoHibernateImpl.class);

    /**
     * @param type
     */
    public AmplificationDaoHibernateImpl() {
        super(Amplification.class);
    }

    @Override
    public List<UuidAndTitleCache<Amplification>> getAmplificationUuidAndLabelCache(Integer limit, String pattern) {
        List<UuidAndTitleCache<Amplification>> list = new ArrayList<UuidAndTitleCache<Amplification>>();
        Session session = getSession();
        Query<Object[]> query;
        if (pattern != null){
            query = session.createQuery("select uuid, id, labelCache from Amplification where labelCache like :pattern", Object[].class);
            query.setParameter("pattern", pattern);
        }else{
            query = session.createQuery("select uuid, id, labelCache from Amplification", Object[].class);
        }
        if (limit != null){
            query.setMaxResults(limit);
         }
        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<Amplification>(Amplification.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public long countByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria) {
        return countByParam(Amplification.class, "labelCache", queryString, matchmode, criteria);
    }

    @Override
    public List<Amplification> findByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParam(Amplification.class, "labelCache", queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
    }
}