/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.test.unit;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * Superclass for all (hibernate)entities to test if certain (hibernate) needs are fulfilled.
 * E.g. testing if all persistent getter have an according setter.
 * @author a.mueller
 *
 */
public abstract class EntityTestBase {
	private static Logger logger = Logger.getLogger(EntityTestBase.class);

	/**
	 * Constructor
	 */
	public EntityTestBase() {
		super();
	}
	

	protected Class<CdmBase> clazzToTest = clazzToTest();
	protected Class<CdmBase> clazzToTest(){
		String testClassName = this.getClass().getName();
		if (testClassName.endsWith("Test")){
			String className = testClassName.substring(0, testClassName.length() - "Test".length());
			try {
				return (Class<CdmBase>)Class.forName(className);
			} catch (ClassNotFoundException e) {
				logger.warn(e.getMessage());
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Tests if all persistent (not transient) getter have an according setter.
	 * FIXME Having matched getter / setter pairs is not strictly necessary if we 
	 * use field-level hibernate annotations, and is perhaps even desirable (i.e. 
	 * prevent the use of set() methods on *-to-Many fields by enforcing use of add*() 
	 * and remove*())
	 */
	@Test 
	@Ignore
	public final void testPersistentGetterSetterPair() {
		//
		Annotation annotation = clazzToTest.getAnnotation(Entity.class);
		if (annotation != null){
			Method[] methods = clazzToTest.getDeclaredMethods();
			List<String> strMethods = new ArrayList<String>(); 
			for (Method method : methods){
				strMethods.add(method.getName());
			}		
			for (Method method : methods){
				if (Modifier.isStatic( method.getModifiers())){
					continue;
				}
				String getMethodName = method.getName();
				try {
					if ( ( getMethodName.startsWith("get") || getMethodName.startsWith("is") )
							&& method.getAnnotation(Transient.class) == null){
						String setMethodName = null;
						if ( getMethodName.startsWith("get")){
							setMethodName = "s" + getMethodName.substring(1);
						}else if ( getMethodName.startsWith("is")){
							setMethodName = "set" + getMethodName.substring(2);
						}else{
							logger.error("Unknown getter method start");
							fail();
						}
						Class params = method.getReturnType();
						Method setMethod = clazzToTest.getDeclaredMethod(setMethodName, params);
						if (setMethod == null){fail();}
					}else{
						//no setter - do nothing
					}
				} catch (SecurityException e) {
					logger.info(e.getMessage());
				} catch (Exception e) {
					String warning = "Missing setter for getter - a non transient getter method should also have a setter: " + getMethodName;
					logger.warn(warning);
					if (! (clazzToTest == (Class)NonViralName.class && getMethodName.equals("getCitation") ) ){
						fail(warning);
					}
				}

			}
		}
		
	}

}
