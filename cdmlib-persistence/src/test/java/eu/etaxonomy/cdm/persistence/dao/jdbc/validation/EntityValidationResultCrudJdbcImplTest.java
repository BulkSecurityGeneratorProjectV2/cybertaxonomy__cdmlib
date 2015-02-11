// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.jdbc.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;

import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author ayco_holleman
 * @date 20 jan. 2015
 *
 */
@DataSet
public class EntityValidationResultCrudJdbcImplTest extends CdmIntegrationTest {

    private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
    private static final String SYNONYM_RELATIONSHIP = "eu.etaxonomy.cdm.model.taxon.SynonymRelationship";
    private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#EntityValidationResultCrudJdbcImpl()}
     * .
     */
    @SuppressWarnings("unused")
    @Test
    public void test_EntityValidationResultCrudJdbcImpl() {
        new EntityValidationResultCrudJdbcImpl();
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#EntityValidationResultCrudJdbcImpl (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @SuppressWarnings("unused")
    @Test
    public void test_EntityValidationResultCrudJdbcImplI_CdmDataSource() {
        new EntityValidationResultCrudJdbcImpl(dataSource);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#saveValidationResult (eu.etaxonomy.cdm.model.common.CdmBase, java.util.Set, eu.etaxonomy.cdm.model.validation.CRUDEventType, Class)}
     * .
     */
    @Test
    public void test_SaveValidationResult_Set_T_CRUDEventType() {
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        ValidatorFactory factory = config.buildValidatorFactory();

        // This is the bean that is going to be tested
        Employee emp = new Employee();
        emp.setId(1);
        UUID uuid = emp.getUuid();
        // ERROR 1 (should be JOHN)
        emp.setFirstName("john");
        // This is an error (should be SMITH), but it is a Level-3
        // validation error, so the error should be ignored
        emp.setLastName("smith");

        // This is an @Valid bean on the Employee class, so Level-2
        // validation errors on the Company object should also be
        // listed.
        Company comp = new Company();
        // ERROR 2 (should be GOOGLE)
        comp.setName("Google");
        emp.setCompany(comp);

        Set<ConstraintViolation<Employee>> errors = factory.getValidator().validate(emp, Level2.class);
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl(dataSource);
        dao.saveValidationResult(emp, errors, CRUDEventType.NONE, null);

        EntityValidationResult result = dao.getValidationResult(emp.getClass().getName(), emp.getId());
        assertNotNull(result);
        assertEquals("Unexpected UUID", result.getValidatedEntityUuid(), uuid);
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());
        Set<EntityConstraintViolation> violations = result.getEntityConstraintViolations();
        List<EntityConstraintViolation> list = new ArrayList<EntityConstraintViolation>(violations);
        Collections.sort(list, new Comparator<EntityConstraintViolation>() {
            @Override
            public int compare(EntityConstraintViolation o1, EntityConstraintViolation o2) {
                return o1.getPropertyPath().toString().compareTo(o2.getPropertyPath().toString());
            }
        });
        assertEquals("Unexpected propertypath", list.get(0).getPropertyPath().toString(), "company.name");
        assertEquals("Unexpected propertypath", list.get(1).getPropertyPath().toString(), "firstName");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#deleteValidationResult (java.lang.String, int)}
     * .
     */
    @Test
    @ExpectedDataSet
    public void test_DeleteValidationResult() {
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl(dataSource);
        dao.deleteValidationResult(SYNONYM_RELATIONSHIP, 200);
        EntityValidationResult result = dao.getValidationResult(SYNONYM_RELATIONSHIP, 200);
        assertTrue(result == null);
    }

    @Test
    public void testGetEntityValidationResult() {
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl(dataSource);
        EntityValidationResult result;

        result = dao.getValidationResult(MEDIA, 100);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 1, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(SYNONYM_RELATIONSHIP, 200);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 2, result.getId());
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(GATHERING_EVENT, 300);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 3, result.getId());
        assertEquals("Unexpected number of constraint violations", 3, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(GATHERING_EVENT, 301);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 4, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        // Test we get a null back
        result = dao.getValidationResult("Foo Bar", 100);
        assertNull(result);
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#setDatasource (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @Test
    public void testSetDatasource() {
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl();
        dao.setDatasource(dataSource);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
    }

}
