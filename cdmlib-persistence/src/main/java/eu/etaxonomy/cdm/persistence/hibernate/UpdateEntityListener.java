/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

public class UpdateEntityListener implements SaveOrUpdateEventListener {
	private static final long serialVersionUID = -3295612929556041686L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(UpdateEntityListener.class);

	public void onSaveOrUpdate(SaveOrUpdateEvent event)	throws HibernateException {
		Object entity = event.getObject();
		if(entity != null && VersionableEntity.class.isAssignableFrom(entity.getClass())) {
			VersionableEntity versionableEntity = (VersionableEntity)entity;
			versionableEntity.setUpdated(new DateTime());
			SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
			  User user = (User)authentication.getPrincipal();
			  versionableEntity.setUpdatedBy(user);
			} 
		}
	}
}
