/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.OneToMany;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.AssociationType;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ListType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadataFactory;

@Component
public class ReferringObjectMetadataFactoryImpl implements	ReferringObjectMetadataFactory {

	@Autowired
	private SessionFactory sessionFactory;

	private Map<Class<? extends CdmBase>, Set<ReferringObjectMetadata>> referringObjectMap = new HashMap<>();

	@Override
    public Set<ReferringObjectMetadata> get(Class<? extends CdmBase> toClass) {
		if(!referringObjectMap.containsKey(toClass)) {
			ClassMetadata toClassMetadata = sessionFactory.getClassMetadata(toClass);
			Map<Class<?>,Set<String>> bidirectionalRelationships = new HashMap<>();
			for(String propertyName : toClassMetadata.getPropertyNames()) {
				Type propertyType = toClassMetadata.getPropertyType(propertyName);
				if(propertyType.isAssociationType() && !propertyType.isAnyType()) {
					AssociationType associationType = (AssociationType)propertyType;

					Field field = null;
					try {
					    field = toClass.getDeclaredField(propertyName);
					} catch(NoSuchFieldException nsfe) {
						Class<?> superClass = toClass.getSuperclass();
						while(!superClass.equals(CdmBase.class)) {
							try{
								field = superClass.getDeclaredField(propertyName);
								break;
							} catch(NoSuchFieldException nsfe1) { }
							superClass = superClass.getSuperclass();
						}
						if(field == null) {
//							throw nsfe;
						}
					}
					if(field != null) {
					    field.setAccessible(true);
					    if(field.isAnnotationPresent(OneToMany.class)){
					        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
					        if(oneToMany.mappedBy() != null && oneToMany.mappedBy().length() > 0) {
					        	String associatedEntityName = associationType.getAssociatedEntityName((SessionFactoryImpl) sessionFactory.getCurrentSession().getSessionFactory());
					        	Class<?> associatedEntity;
								try {
									associatedEntity = Class.forName(associatedEntityName);
									if(!bidirectionalRelationships.containsKey(associatedEntity)) {
										bidirectionalRelationships.put(associatedEntity, new HashSet<>());
									}
									bidirectionalRelationships.get(associatedEntity).add(oneToMany.mappedBy());
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
//						        System.out.println(oneToMany.mappedBy() + " " + associatedEntityName);
					        }
					    }
					}
				}
			}

			Set<ReferringObjectMetadata> referringObjectMetadata = new HashSet<>();

			EntityManagerFactory entityManagerFactory = sessionFactory;
			Metamodel metamodel = entityManagerFactory.getMetamodel();
            for(EntityType<?> entityType : metamodel.getEntities()) {
                String entityName = entityType.getName();
            	if(!entityName.endsWith("_AUD")) {
            		try {
//            			System.out.println(entityName);
						Class<?> entityClass = entityType.getJavaType();
//						ClassMetadata classMetadata = allClassMetadata.get(entityName);

                        for(Attribute<?,?> attribute : entityType.getAttributes()) {
                            String propertyName = attribute.getName();
                            if(bidirectionalRelationships.containsKey(entityClass) && bidirectionalRelationships.get(entityClass).contains(propertyName)) {
//                              System.out.println("Excluding " + entityClass.getName() + " " + propertyName);
							} else {
							    PersistentAttributeType type = attribute.getPersistentAttributeType();

//							    Type propertyType = classMetadata.getPropertyType(propertyName);

							    Type propertyType = getPropertyType();
//							    if (propertyType.isAssociationType()){
	                            if (attribute.isAssociation()){
//									System.out.println(entityName+"."+propertyName);
									AssociationType associationType = (AssociationType)propertyType;

									if(!propertyType.isAnyType()) {
										try {

											String associatedEntityName = associationType.getAssociatedEntityName((SessionFactoryImpl) sessionFactory.getCurrentSession().getSessionFactory());
											Class<?> associatedClass = Class.forName(associatedEntityName);
											if (associatedClass.isAssignableFrom(toClass)){

												try {
													if(associationType.isEntityType()) {

														referringObjectMetadata.add(new ToOneReferringObjectMetadata(entityClass,propertyName, toClass));
//														System.out.println(propertyName + " " + fromClass + " " + toClass);
													} else if(associationType.isCollectionType()) {
														CollectionType collectionType = (CollectionType)propertyType;

														if(propertyType instanceof BagType || propertyType instanceof SetType) {
															referringObjectMetadata.add(new SetReferringObjectMetadata(entityClass,propertyName, toClass));
//															System.out.println(propertyName + " " + fromClass + " " + toClass);
														} else if(propertyType instanceof ListType) {
															referringObjectMetadata.add(new ListReferringObjectMetadata(entityClass,propertyName, toClass));
//															System.out.println(propertyName + " " + fromClass + " " + toClass);
														}
													}
												} catch(NoSuchFieldException nsfe) { }
											}
										} catch(MappingException me) { }
									}
								}
							}
                        }

					} catch (ClassNotFoundException e) {

					}

            	}
            }
            referringObjectMap.put(toClass, referringObjectMetadata);
        }

        return referringObjectMap.get(toClass);
	}

    private boolean isAssociationType(PersistentAttributeType type) {
        // TODO Auto-generated method stub
        return type == PersistentAttributeType.MANY_TO_MANY;
    }

    private Type getPropertyType() {
        // TODO Auto-generated method stub
        return null;
    }
}
