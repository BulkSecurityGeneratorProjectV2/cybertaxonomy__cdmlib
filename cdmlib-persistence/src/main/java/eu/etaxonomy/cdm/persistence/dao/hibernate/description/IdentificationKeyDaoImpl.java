package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;

@Repository
public class IdentificationKeyDaoImpl extends DaoBase implements IIdentificationKeyDao {
	
	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected BeanInitializer defaultBeanInitializer;

	public int count() {
		Query query = getSession().createQuery("select count(key) from eu.etaxonomy.cdm.model.description.IIdentificationKey key");
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<IIdentificationKey> list(Integer limit,Integer start, List<String> propertyPaths) {
		Query query = getSession().createQuery("select key from eu.etaxonomy.cdm.model.description.IIdentificationKey key order by created desc");
		
		if(limit != null) {
			if(start != null) {
				query.setFirstResult(start);
			} else {
				query.setFirstResult(0);
			}
			query.setMaxResults(limit);
		}
		
		List<IIdentificationKey> results = (List<IIdentificationKey>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results; 
	}

}
