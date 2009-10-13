/**
 * 
 */
package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * 
 * @author ben.clark
 *
 */
@SuppressWarnings("unused")
public class ReferenceValidationTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(ReferenceValidationTest.class);
	
	@SpringBeanByType
	private Validator validator;
	
	private Book book;
	
	@Before
	public void setUp() {
		book = Book.NewInstance();
		book.setTitleCache("Lorem ipsum");
	}
	
	
/****************** TESTS *****************************/

	/**
	 * Test validation at the second level with a valid reference
	 */
	@Test
	public final void testLevel2ValidationWithValidBook() {
        Set<ConstraintViolation<Book>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithValidISBN() {
		book.setIsbn("ISBN 1-919795-99-5");
        Set<ConstraintViolation<Book>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithInValidISBN() {
		book.setIsbn("ISBN 1-9197954-99-5");
        Set<ConstraintViolation<Book>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse("There should be a constraint violation as this book has an invalid ISBN number",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithValidUri() {
		book.setUri("http://www.e-taxonomy.eu");
        Set<ConstraintViolation<Book>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithInValidUri() {
		book.setUri("http://www.e-\taxonomy.eu");
        Set<ConstraintViolation<Book>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse("There should be a constraint violation as this book has an invalid URI",constraintViolations.isEmpty());
	}
}