package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.validation.Level2;

@DataSet
public class EntityValidationResultDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
    private static final String SYNONYM_RELATIONSHIP = "eu.etaxonomy.cdm.model.taxon.SynonymRelationship";
    private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

    @SpringBeanByType
    private IEntityValidationResultDao dao;

    @Test
    public void init() {
        assertNotNull("Expecting an instance of IEntityValidationResultDao", dao);
    }

    @Test
    public void testSaveValidationResult() {

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
        dao.saveValidationResult(emp, errors, CRUDEventType.NONE, null);

        EntityValidationResult result = dao.getValidationResult(Employee.class.getName(), 1);
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

    @Test
    @ExpectedDataSet
    // @Ignore //FIXME unignore entity validation result dao delete test
    public void testDeleteValidationResult() {
        dao.deleteValidationResult(SYNONYM_RELATIONSHIP, 200);

        commitAndStartNewTransaction(null);

        List<EntityValidationResult> results = dao.getEntityValidationResults(SYNONYM_RELATIONSHIP);
        assertEquals("Unexpected number of validation results", 0, results.size());
    }

    @Test
    public void testGetEntityValidationResult() {
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

    @Test
    public void testGetEntityValidationResults_String() {
        List<EntityValidationResult> results;

        results = dao.getEntityValidationResults(MEDIA);
        assertEquals("Unexpected number of validation results", 1, results.size());
        assertEquals("Unexpected number of error", 1, results.get(0).getEntityConstraintViolations().size());

        results = dao.getEntityValidationResults(SYNONYM_RELATIONSHIP);
        assertEquals("Unexpected number of validation results", 1, results.size());
        assertEquals("Unexpected number of error", 2, results.get(0).getEntityConstraintViolations().size());

        results = dao.getEntityValidationResults(GATHERING_EVENT);
        assertEquals("Unexpected number of validation results", 2, results.size());
        assertEquals("Unexpected number of error", 3, results.get(0).getEntityConstraintViolations().size());
        assertEquals("Unexpected number of error", 1, results.get(1).getEntityConstraintViolations().size());

        results = dao.getEntityValidationResults("foo.bar");
        assertEquals("Unexpected number of validation results", 0, results.size());
    }

    @Test
    public void testGetEntitiesViolatingConstraint_String() {
        List<EntityValidationResult> results;

        results = dao.getEntitiesViolatingConstraint("com.example.NameValidator");
        assertEquals("Unexpected number of validation results", 1, results.size());

        results = dao.getEntitiesViolatingConstraint("com.example.DistanceToGroundValidator");
        assertEquals("Unexpected number of validation results", 1, results.size());

        results = dao.getEntitiesViolatingConstraint("com.example.CountryValidator");
        assertEquals("Unexpected number of validation results", 2, results.size());

        results = dao.getEntitiesViolatingConstraint("foo.bar");
        assertEquals("Unexpected number of validation results", 0, results.size());
    }

    @Test
    public void testGetEntityValidationResults_String_Severity() {
        List<EntityValidationResult> results;

        results = dao.getValidationResults(MEDIA, Severity.NOTICE);
        assertEquals("Unexpected number of validation results", 0, results.size());
        results = dao.getValidationResults(MEDIA, Severity.WARNING);
        assertEquals("Unexpected number of validation results", 0, results.size());
        results = dao.getValidationResults(MEDIA, Severity.ERROR);
        assertEquals("Unexpected number of validation results", 1, results.size());
        assertEquals("Unexpected number of validation results", 1, results.iterator().next()
                .getEntityConstraintViolations().size());
        assertEquals("Unexpected severity", Severity.ERROR, results.iterator().next().getEntityConstraintViolations()
                .iterator().next().getSeverity());

        results = dao.getValidationResults(SYNONYM_RELATIONSHIP, Severity.NOTICE);
        assertEquals("Unexpected number of validation results", 0, results.size());
        results = dao.getValidationResults(SYNONYM_RELATIONSHIP, Severity.WARNING);
        assertEquals("Unexpected number of validation results", 1, results.size());
        results = dao.getValidationResults(SYNONYM_RELATIONSHIP, Severity.ERROR);
        assertEquals("Unexpected number of validation results", 1, results.size());

        results = dao.getValidationResults(GATHERING_EVENT, Severity.NOTICE);
        assertEquals("Unexpected number of validation results", 1, results.size());
        results = dao.getValidationResults(GATHERING_EVENT, Severity.WARNING);
        assertEquals("Unexpected number of validation results", 1, results.size());
        results = dao.getValidationResults(GATHERING_EVENT, Severity.ERROR);
        assertEquals("Unexpected number of validation results", 2, results.size());

        results = dao.getValidationResults("foo.bar", Severity.ERROR);
        assertEquals("Unexpected number of validation results", 0, results.size());
    }

    @Test
    public void testGetEntityValidationResults_Severity() {
        List<EntityValidationResult> results;
        results = dao.getValidationResults(Severity.NOTICE);
        assertEquals("Unexpected number of validation results", 1, results.size());
        results = dao.getValidationResults(Severity.WARNING);
        assertEquals("Unexpected number of validation results", 2, results.size());
        results = dao.getValidationResults(Severity.ERROR);
        assertEquals("Unexpected number of validation results", 4, results.size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }

}
