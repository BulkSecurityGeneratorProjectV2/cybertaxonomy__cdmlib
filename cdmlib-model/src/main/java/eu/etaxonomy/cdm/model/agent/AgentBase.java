/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * The upmost (abstract) class for agents such as persons, teams or institutions.
 * An agent is a conscious entity which can take decisions, act and create
 * according to its own knowledge and goals and which may be approached.
 * Agents can be authors for nomenclatural or bibliographical references as well
 * as creators of pictures or field collectors or administrators of collections.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Agent", propOrder = {
		"contact"
})
@Entity
@Audited
@Table(appliesTo="AgentBase", indexes = { @Index(name = "agentTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class AgentBase<S extends IIdentifiableEntityCacheStrategy> extends IdentifiableMediaEntity<S>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7732768617469448829L;
	
	@XmlElement(name = "Contact")
    @Embedded
    private Contact contact;
	
	/** 
	 * Returns the {@link Contact contact} of <i>this</i> person.
	 * The contact contains several ways to approach <i>this</i> person.
	 *
	 * @see 	Contact
	 */
	public Contact getContact(){
		return this.contact;
	}
	/**
	 * @see  #getContact()
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}
}
