/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedTermBase", propOrder = {
    "orderIndex"
})
@XmlRootElement(name = "OrderedTermBase")
@Entity
@Audited
public abstract class OrderedTermBase<T extends OrderedTermBase> extends DefinedTermBase<T> implements Comparable<T> {
	private static final long serialVersionUID = 8000797926720467399L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OrderedTermBase.class);
	
	//Order index, value < 1 means that this Term is not in order yet
	@XmlElement(name = "OrderIndex")
	protected int orderIndex;
	
	public OrderedTermBase() {
		super();
	}
	public OrderedTermBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(T orderedTerm) {
		int orderThat;
		int orderThis;
		try {
			orderThat = orderedTerm.orderIndex;//OLD: this.getVocabulary().getTerms().indexOf(orderedTerm);
			orderThis = orderIndex; //OLD: this.getVocabulary().getTerms().indexOf(this);
		} catch (RuntimeException e) {
			throw e;
		}
		if (orderThis > orderThat){
			return -1;
		}else if (orderThis < orderThat){
			return 1;
		}else {
			return 0;
		}
	}
	
	/**
	 * If this term is lower than the parameter term, true is returned, else false.
	 * If the parameter term is null, an Exception is thrown.
	 * @param orderedTerm
	 * @return boolean result of the comparison
	 */
	public boolean isLower(T orderedTerm){
		return (this.compareTo(orderedTerm) < 0 );
	}

	
	/**
	 * If this term is higher than the parameter term, true is returned, else false.
	 * If the parameter term is null, an Exception is thrown.
	 * @param orderedTerm
	 * @return boolean result of the comparison
	 */
	public boolean isHigher(T orderedTerm){
		return (this.compareTo(orderedTerm) > 0 );
	}
	
	
	/** To be used only by OrderedTermVocabulary*/
	@Deprecated
	protected boolean decreaseIndex(OrderedTermVocabulary<T> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex--;
			return true;
		}else{
			return false;
		}
	}
	
	/** To be used only by OrderedTermVocabulary*/
	@Deprecated
	protected boolean incrementIndex(OrderedTermVocabulary<T> vocabulary){
		if (vocabulary.indexChangeAllowed(this) == true){
			orderIndex++;
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean equals(Object object){
		if(this == object)
			return true;
		if((object == null) || (!OrderedTermBase.class.isAssignableFrom(object.getClass()))) {
			return false;
		}else{
			OrderedTermBase orderedTermBase = (OrderedTermBase)object;
			if (orderedTermBase.getUuid().equals(this.getUuid())){
				return true;
			}else{
				return false;
			}
		}
	}
}