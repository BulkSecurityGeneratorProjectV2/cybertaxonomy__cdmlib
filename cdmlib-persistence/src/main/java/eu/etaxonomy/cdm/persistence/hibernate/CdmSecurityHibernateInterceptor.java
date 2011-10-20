
package eu.etaxonomy.cdm.persistence.hibernate;



import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
@Component
public class CdmSecurityHibernateInterceptor extends EmptyInterceptor {
	private static final Logger logger = Logger
			.getLogger(CdmSecurityHibernateInterceptor.class);
	
	
	public boolean onSave(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] type) {
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		if (SecurityContextHolder.getContext().getAuthentication() != null && entity instanceof CdmBase){
			if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.CREATE)){
				throw new EvaluationFailedException("Permission evaluation failed for test test" + entity);
			}else return true;
		}
		else return true;
		
	}
	public boolean onFlushDirty(Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		String permission = null;;
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			boolean equals = true;
			for (int i = 0; i<currentState.length; i++){
				if (currentState[i]== null ) {
					if ( previousState[i]!= null) {
						equals = false;
						break;
					}
				} 
				if (currentState[i]!= null ){
					if (previousState == null){
						equals = false;
						break;
					}
				}
				if (currentState[i]!= null && previousState[i] != null){
					Object a = currentState[i];
					Object b = previousState[i];
					if (!currentState[i].equals(previousState[i])) {
						if (propertyNames[i].equals("password")){
							permission = "changePassword";
						}
						equals = false;
						break;
					}
				}
			}
			
		
			if (!equals){
				if (permission != null){
					if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, permission)){
						throw new EvaluationFailedException("Permission evaluation failed for " + entity);
					}else {
						return true;
					}
				}
				if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.UPDATE)){
					throw new EvaluationFailedException("Permission evaluation failed for " + entity);
				}else {
					return true;
				}
			}else {
				return true;
			}
		}
		else{
			return true;
		}
		
	}
	
	
	
}
