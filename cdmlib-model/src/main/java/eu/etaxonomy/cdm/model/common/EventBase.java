package eu.etaxonomy.cdm.model.common;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventBase", propOrder = {
    "timeperiod",
    "actor",
    "description"
})
@XmlRootElement(name = "EventBase")
@MappedSuperclass
public abstract class EventBase extends AnnotatableEntity implements IEvent {
	
	static Logger logger = Logger.getLogger(EventBase.class);

	@XmlElement(name = "TimePeriod")
	private TimePeriod timeperiod = new TimePeriod();
	
	@XmlElement(name = "Actor")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Agent actor;
	
	@XmlElement(name = "Description")
	private String description;
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#getTimeperiod()
	 */
	public TimePeriod getTimeperiod() {
		return timeperiod;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#setTimeperiod(eu.etaxonomy.cdm.model.common.TimePeriod)
	 */
	public void setTimeperiod(TimePeriod timeperiod) {
		this.timeperiod = timeperiod;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#getActor()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getActor() {
		return actor;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#setActor(eu.etaxonomy.cdm.model.agent.Agent)
	 */
	public void setActor(Agent actor) {
		this.actor = actor;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.occurrence.IEvent#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
}
