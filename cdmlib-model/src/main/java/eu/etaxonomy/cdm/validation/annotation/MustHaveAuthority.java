package eu.etaxonomy.cdm.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.etaxonomy.cdm.validation.constraint.MustHaveAuthorityValidator;

@Target( { TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = MustHaveAuthorityValidator.class)
@Documented
public @interface MustHaveAuthority {
String message() default "{eu.etaxonomy.cdm.validation.annotation.MustHaveAuthority.message}";
Class<? extends Payload>[] payload() default {};
Class<?>[] groups() default {};
}
