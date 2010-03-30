/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.search.FullTextQuery;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadataFactory;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 * FIXME CdmEntityDaoBase is abstract, can it be annotated with @Repository?
 */
@Repository
public abstract class CdmEntityDaoBase<T extends CdmBase> extends DaoBase implements ICdmEntityDao<T> {
	private static final Logger logger = Logger.getLogger(CdmEntityDaoBase.class);

	int flushAfterNo = 1000; //large numbers may cause synchronisation errors when commiting the session !!
	protected Class<T> type;
	
	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected BeanInitializer defaultBeanInitializer;
	
	public void setDefaultBeanInitializer(BeanInitializer defaultBeanInitializer) {
		this.defaultBeanInitializer = defaultBeanInitializer;
	}

	@Autowired
	private ReferringObjectMetadataFactory referringObjectMetadataFactory;
	
	
	public CdmEntityDaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	public void lock(T t, LockMode lockMode) {
		getSession().lock(t, lockMode);
	}
	
	public void refresh(T t, LockMode lockMode, List<String> propertyPaths) {
		getSession().refresh(t, lockMode);
		defaultBeanInitializer.initialize(t, propertyPaths);
	}
	
	//TODO this method should be moved to a concrete class (not typed)
	public UUID saveCdmObj(CdmBase cdmObj) throws DataAccessException  {
		getSession().saveOrUpdate(cdmObj);
		return cdmObj.getUuid();
	}
	
    //TODO: Replace saveCdmObj() by saveCdmObject_
	private UUID saveCdmObject_(T cdmObj){
		getSession().saveOrUpdate(cdmObj);
		return cdmObj.getUuid();
	}
	
    //TODO: Use everywhere CdmEntityDaoBase.saveAll() instead of ServiceBase.saveCdmObjectAll()?
	public Map<UUID, T> saveAll(Collection<T> cdmObjCollection){
		int types = cdmObjCollection.getClass().getTypeParameters().length;
		if (types > 0){
			if (logger.isDebugEnabled()){logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);}
		}

		Map<UUID, T> resultMap = new HashMap<UUID, T>();
		Iterator<T> iterator = cdmObjCollection.iterator();
		int i = 0;
		while(iterator.hasNext()){
			if ( ( (i % 2000) == 0) && (i > 0)   ){logger.debug("Saved " + i + " objects" );}
			T cdmObj = iterator.next();
			UUID uuid = saveCdmObject_(cdmObj);
			if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
			resultMap.put(uuid, cdmObj);
			i++;
			if ( (i % flushAfterNo) == 0){
				try{
					//TODO: fixme!!
					logger.debug("flush");
					flush();
				}catch(Exception e){
					logger.error("UUUIIIII");
					e.printStackTrace();
				}
			}
		}

		if ( logger.isInfoEnabled() ){logger.info("Saved " + i + " objects" );}
		return resultMap;
	}
	
	public T replace(T x, T y) {
		if(x.equals(y)) {
			return y;
		}
		
		Class commonClass = x.getClass();
		if(y != null) {
            while(!commonClass.isAssignableFrom(y.getClass())) {
        	    if(commonClass.equals(type)) {
        		    throw new RuntimeException();
        	    }
        	    commonClass = commonClass.getSuperclass();
            }
		}

		getSession().merge(x);
		
        Set<ReferringObjectMetadata> referringObjectMetas = referringObjectMetadataFactory.get(x.getClass());
        
        for(ReferringObjectMetadata referringObjectMetadata : referringObjectMetas) {
          
          List<CdmBase> referringObjects = referringObjectMetadata.getReferringObjects(x,getSession());
         
          for(CdmBase referringObject : referringObjects) {
            try {
				referringObjectMetadata.replace(referringObject,x,y);
				getSession().update(referringObject);
              
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(),e);
			}
          }
        }
        return y;
    }

	public Session getSession() throws DataAccessException {
		return super.getSession();
	}

	public void clear() throws DataAccessException {
		Session session = getSession();
		session.clear();
		if (logger.isDebugEnabled()){logger.debug("dao clear end");}
	}

	public UUID merge(T transientObject) throws DataAccessException {
		Session session = getSession();
		session.merge(transientObject);
		if (logger.isDebugEnabled()){logger.debug("dao merge end");}
		return transientObject.getUuid();
	}
	
	public UUID saveOrUpdate(T transientObject) throws DataAccessException  {
		try {
			if (logger.isDebugEnabled()){logger.debug("dao saveOrUpdate start...");}
			if (logger.isDebugEnabled()){logger.debug("transientObject(" + transientObject.getClass().getSimpleName() + ") ID:" + transientObject.getId() + ", UUID: " + transientObject.getUuid()) ;}
			Session session = getSession();
			if(transientObject.getId() != 0 && VersionableEntity.class.isAssignableFrom(transientObject.getClass())) {
				VersionableEntity versionableEntity = (VersionableEntity)transientObject;
				versionableEntity.setUpdated(new DateTime());
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
				  User user = (User)authentication.getPrincipal();
				  versionableEntity.setUpdatedBy(user);
				} 
			}
			session.saveOrUpdate(transientObject);
			if (logger.isDebugEnabled()){logger.debug("dao saveOrUpdate end");}
			return transientObject.getUuid();
		} catch (NonUniqueObjectException e) {
			logger.error("Error in CdmEntityDaoBase.saveOrUpdate(obj)");
			logger.error(e.getIdentifier());
			logger.error(e.getEntityName());
			logger.error(e.getMessage());
			
			e.printStackTrace();
			throw e;
		} catch (HibernateException e) {
			
			e.printStackTrace();
			throw e;
		}
	}

	public UUID save(T newInstance) throws DataAccessException {
		getSession().save(newInstance);
		return newInstance.getUuid();
	}
	
	public UUID update(T transientObject) throws DataAccessException {
		getSession().update(transientObject);
		return transientObject.getUuid();
	}
	
	public UUID refresh(T persistentObject) throws DataAccessException {
		getSession().refresh(persistentObject);
		return persistentObject.getUuid();
	}
	
	public UUID delete(T persistentObject) throws DataAccessException {
		if (persistentObject == null){
			logger.warn(type.getName() + " was 'null'");
			return null;
		}
		
		// Merge the object in if it is detached
		//
		// I think this is preferable to catching lazy initialization errors 
		// as that solution only swallows and hides the exception, but doesn't 
		// actually solve it.
		getSession().merge(persistentObject);
		getSession().delete(persistentObject);
		return persistentObject.getUuid();
	}

	public T findById(int id) throws DataAccessException {
		return (T) getSession().get(type, id);
	}
	public List<T> findById(Set<Integer> idSet) throws DataAccessException {
		Session session = getSession();
		String hql = "from " + type.getSimpleName() + " type where type.id in ( :idSet )" ;
		Query query = session.createQuery(hql);
		query.setParameterList("idSet", idSet);
		List<T> results = query.list();
		return results;			
	}
	public T findByUuid(UUID uuid) throws DataAccessException{
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("uuid", uuid));
		crit.addOrder(Order.desc("created"));
		List<T> results = crit.list();
		if (results.isEmpty()){
			return null;
		}else{
			if(results.size() > 1){
				logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
			}
			return results.get(0);			
		}
	}
	
	public List<T> findByUuid(Set<UUID> uuidSet) throws DataAccessException {
		Session session = getSession();
		String hql = "from " + type.getSimpleName() + " type where type.uuid in ( :uuidSet )" ;
		Query query = session.createQuery(hql);
		query.setParameterList("uuidSet", uuidSet);
		List<T> results = query.list();
		return results;			
	}
	
	public T load(UUID uuid) {
		T bean = findByUuid(uuid);
		if(bean == null) 
			return null;
		defaultBeanInitializer.load(bean);
		
		return bean;
	}
	

	public T load(UUID uuid, List<String> propertyPaths){
		T bean = findByUuid(uuid);
		if(bean == null) 
			return bean;
		
		defaultBeanInitializer.initialize(bean, propertyPaths);
		
		return bean;
	}
	
	public List<T> load(Set<UUID> uuidSet, List<String> propertyPaths) throws DataAccessException{
		List<T> list = findByUuid(uuidSet);
		defaultBeanInitializer.initializeAll(list, propertyPaths);
		return list;
	}
	
	public Boolean exists(UUID uuid) {
		if (findByUuid(uuid)==null){
			return false;
		}
		return true;
	}
	
	public int count() {
		return count(type);
	}
	
	public int count(Class<? extends T> clazz) {
		Session session = getSession();
		Criteria criteria = null;
		if(clazz == null) {
			criteria = session.createCriteria(type);
		} else {
		    criteria = session.createCriteria(clazz);
		}
		criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
		return (Integer) criteria.uniqueResult();
	}

	public List<T> list(Integer limit, Integer start) {
		return list(limit, start, null); 
	}
	
	public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths) {
		
		Criteria criteria = null;
		if(clazz == null){
			criteria = getSession().createCriteria(type);
		} else {
			criteria = getSession().createCriteria(clazz);
		}
		
		addGroups(criteria,groups);
		
		if(limit != null) {
			criteria.setFirstResult(start);
			criteria.setMaxResults(limit);
		}
		
		List<Object[]> result = (List<Object[]>)criteria.list();
		
		if(propertyPaths != null && !propertyPaths.isEmpty()) {
		  for(Object[] objects : result) {
			defaultBeanInitializer.initialize(objects[0], propertyPaths);
		  }
		}
		
		return result;		
	}
	
	protected void countGroups(DetachedCriteria criteria,List<Grouping> groups) {
		if(groups != null){


			Map<String,String> aliases = new HashMap<String,String>();

			for(Grouping grouping : groups) {
				if(grouping.getAssociatedObj() != null) {
					String alias = null;
					if((alias = aliases.get(grouping.getAssociatedObj())) == null) {
						alias = grouping.getAssociatedObjectAlias();
						aliases.put(grouping.getAssociatedObj(), alias);
						criteria.createAlias(grouping.getAssociatedObj(),alias);
					}
				}
			}

			ProjectionList projectionList = Projections.projectionList();

			for(Grouping grouping : groups) {
				grouping.addProjection(projectionList);
			}
			criteria.setProjection(projectionList);
		}
	}  
	
	protected void addGroups(Criteria criteria,List<Grouping> groups) {
		if(groups != null){


			Map<String,String> aliases = new HashMap<String,String>();

			for(Grouping grouping : groups) {
				if(grouping.getAssociatedObj() != null) {
					String alias = null;
					if((alias = aliases.get(grouping.getAssociatedObj())) == null) {
						alias = grouping.getAssociatedObjectAlias();
						aliases.put(grouping.getAssociatedObj(), alias);
						criteria.createAlias(grouping.getAssociatedObj(),alias);
					}
				}
			}

			ProjectionList projectionList = Projections.projectionList();

			for(Grouping grouping : groups) {
				grouping.addProjection(projectionList);
			}
			criteria.setProjection(projectionList);
			
			for(Grouping grouping : groups) {
				grouping.addOrder(criteria);

			}
		}
	}  
	
	protected void addCriteria(Criteria criteria, List<Criterion> criterion) {
		if(criterion != null) {
			for(Criterion c : criterion) {
				criteria.add(c);
			}
		}
			
	}
	
	protected void addOrder(FullTextQuery fullTextQuery, List<OrderHint> orderHints) {
		if(orderHints != null && !orderHints.isEmpty()) {
		    org.apache.lucene.search.Sort sort = new Sort();
		    SortField[] sortFields = new SortField[orderHints.size()];
		    for(int i = 0; i < orderHints.size(); i++) {
		    	OrderHint orderHint = orderHints.get(i);
		    	switch(orderHint.getSortOrder()) {
		    	case ASCENDING:
		            sortFields[i] = new SortField(orderHint.getPropertyName(), true);
		            break;
		    	case DESCENDING:
		        default:
		    		sortFields[i] = new SortField(orderHint.getPropertyName(),false);
		    		
		    	}
		    }
		    sort.setSort(sortFields);
		    fullTextQuery.setSort(sort);
		    
		}
	}
	
	public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints) {
		return list(limit,start,orderHints,null);
	}
	
	public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		Criteria criteria = getSession().createCriteria(type); 
		if(limit != null) {
			criteria.setFirstResult(start);
			criteria.setMaxResults(limit);
		}
		
		addOrder(criteria,orderHints);
		List<T> results = (List<T>)criteria.list();
		
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	public List<T> list(Class<? extends T> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		Criteria criteria = null;
		if(clazz == null) {
			criteria = getSession().createCriteria(type); 
		} else {
		    criteria = getSession().createCriteria(clazz);	
		} 
		
		if(limit != null) {
			if(start != null) {
			    criteria.setFirstResult(start);
			} else {
				criteria.setFirstResult(0);
			}
			criteria.setMaxResults(limit);
		}
		
		addOrder(criteria,orderHints);
		
		List<T> results = (List<T>)criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results; 
	}
	
	public List<T> list(Class<? extends T> type, Integer limit, Integer start, List<OrderHint> orderHints) {
		return list(type,limit,start,orderHints,null);
	}
	
	public List<T> list(Class<? extends T> type, Integer limit, Integer start) {
		return list(type,limit,start,null,null);
	}
	
	public List<T> rows(String tableName, int limit, int start) {
		Query query = getSession().createQuery("from " + tableName + " order by uuid");
		query.setFirstResult(start);
		query.setMaxResults(limit);
		List<T> result = query.list();
		return result;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	protected void setPagingParameter(Query query, Integer pageSize, Integer pageNumber){
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
	}
	
	protected void setPagingParameter(AuditQuery query, Integer pageSize, Integer pageNumber){
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
	}
	
	public int count(T example, Set<String> includeProperties) {
		Criteria criteria = getSession().createCriteria(example.getClass());
		addExample(criteria,example,includeProperties);
		
		criteria.setProjection(Projections.rowCount());
		return (Integer)criteria.uniqueResult();
	}
	
	protected void addExample(Criteria criteria, T example, Set<String> includeProperties) {
		if(includeProperties != null && !includeProperties.isEmpty()) {
		    criteria.add(Example.create(example).setPropertySelector(new PropertySelectorImpl(includeProperties)));
		    ClassMetadata classMetadata = getSession().getSessionFactory().getClassMetadata(example.getClass());
		    for(String property : includeProperties) {  
		        Type type  = classMetadata.getPropertyType(property);
		        if(type.isEntityType()) {
				    try {
					    Field field = ReflectionUtils.findField(example.getClass(), property);
					    field.setAccessible(true);
					    Object value =  field.get(example);
					    if(value != null) {
			    	        criteria.add(Restrictions.eq(property,value));
					    } else {
					    	criteria.add(Restrictions.isNull(property));
					    }
				    } catch (SecurityException se) {
					    throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property, se);
				    } catch (HibernateException he) {
					    throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property, he);
				    } catch (IllegalArgumentException iae) {
					    throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property, iae);
				    } catch (IllegalAccessException ie) {
					    throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property, ie);
				    }
		    	
		        }
		    }
		} else {
			criteria.add(Example.create(example));
		}
	}
	
	public List<T> list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		Criteria criteria = getSession().createCriteria(example.getClass());
		addExample(criteria,example,includeProperties);
		
		if(limit != null) {
			if(start != null) {
			    criteria.setFirstResult(start);
			} else {
				criteria.setFirstResult(0);
			}
			criteria.setMaxResults(limit);
		}
		
		addOrder(criteria,orderHints);
		
		List<T> results = (List<T>)criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results; 
	}
	
	private class PropertySelectorImpl implements PropertySelector {

		private Set<String> includeProperties;
		/**
		 * 
		 */
		private static final long serialVersionUID = -3175311800911570546L;

		public PropertySelectorImpl(Set<String> includeProperties) {
			this.includeProperties = includeProperties;
		}

		public boolean include(Object propertyValue, String propertyName,	Type type) {
			if(includeProperties.contains(propertyName)) {
				return true;
			} else {
				return false;
			}
		}
		
	}
}
