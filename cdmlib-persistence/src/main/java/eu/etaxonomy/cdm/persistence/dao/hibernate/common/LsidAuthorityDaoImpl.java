package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.persistence.dao.common.ILsidAuthorityDao;

@Repository
public class LsidAuthorityDaoImpl extends CdmEntityDaoBase<LSIDAuthority> implements
		ILsidAuthorityDao {

	public LsidAuthorityDaoImpl() {
		super(LSIDAuthority.class);
	}

	public Class<? extends IIdentifiableEntity> getClassForNamespace(LSID lsid) {
		Query query = getSession().createQuery("select clazz from LSIDAuthority authority join authority.namespaces clazz where authority.authority = :authority and index(clazz) = :namespace");
		query.setParameter("authority",lsid.getAuthority());
		query.setParameter("namespace", lsid.getNamespace());
		return (Class<? extends IIdentifiableEntity>)query.uniqueResult();
	}
}
