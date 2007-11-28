package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class NonOrderedTermBase extends DefinedTermBase {

	public NonOrderedTermBase(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

}
