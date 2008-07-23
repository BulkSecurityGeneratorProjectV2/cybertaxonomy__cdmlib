package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.Person;

public interface ICdmBase {

	/**
	 * Returns local unique identifier for the concrete subclass
	 * @return
	 */
	@Id
	@GeneratedValue(generator = "system-increment")
	public abstract int getId();

	/**
	 * Assigns a unique local ID to this object. 
	 * Because of the EJB3 @Id and @GeneratedValue annotation this id will be
	 * set automatically by the persistence framework when object is saved.
	 * @param id
	 */
	public abstract void setId(int id);

	@Transient
	public abstract UUID getUuid();

	public abstract void setUuid(UUID uuid);

	@Temporal(TemporalType.TIMESTAMP)
	@Basic(fetch = FetchType.LAZY)
	public abstract DateTime getCreated();

	/**
	 * Sets the timestamp this object was created. 
	 * Most databases cannot store milliseconds, so they are removed by this method.
	 * Caution: We are planning to replace the Calendar class with a different datetime representation which is more suitable for hibernate
	 * see {@link http://dev.e-taxonomy.eu/trac/ticket/247 TRAC ticket} 
	 * 
	 * @param created
	 */
	public abstract void setCreated(DateTime created);

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract Person getCreatedBy();

	public abstract void setCreatedBy(Person createdBy);

}