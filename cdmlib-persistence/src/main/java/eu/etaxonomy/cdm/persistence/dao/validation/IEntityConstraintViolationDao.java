package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.List;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.validation.Severity;

/**
 * A DAO for accessing the error tables populated as a consequence of entity validation
 * errors. See {@link IEntityValidationResultDao} for more info.
 * 
 * @author ayco_holleman
 * 
 */
public interface IEntityConstraintViolationDao extends ICdmEntityDao<EntityConstraintViolation> {

	/**
	 * Get all constraint violations for all validated entities of the specified type. The
	 * constraint violations are sorted according to the type and id of the validated
	 * entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations();


	/**
	 * Get all constraint violations for all entities of the specified type. The
	 * constraint violations are sorted according to the type and id of the validated
	 * entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass);


	/**
	 * Get all constraint violations of the specified severity for all entities of the
	 * specified type. The constraint violations are sorted according to the type and id
	 * of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity);


	/**
	 * Get all constraint violations of the specified severity. The constraint violations
	 * are sorted according to the type and id of the validated entities.
	 * 
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(Severity severity);
}
