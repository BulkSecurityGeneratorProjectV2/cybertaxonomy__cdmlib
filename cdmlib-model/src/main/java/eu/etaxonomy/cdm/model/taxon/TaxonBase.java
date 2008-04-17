/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.*;

/**
 * {unique name within view/treatment}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@Entity
public abstract class TaxonBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(TaxonBase.class);
	//The assignment to the Taxon or to the Synonym class is not definitive
	private boolean isDoubtful;
	private TaxonNameBase name;
	// The concept reference
	private ReferenceBase sec;

	@Override
	public String generateTitle() {
		String title;
		if (name != null){
			title = name.getTitleCache() + " sec. ";
			if (sec != null){
				title += sec.getTitleCache();
			}else{
				title += "???";
			}
		}else{
			title = this.toString();
		}
		return title;
	}
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public TaxonNameBase getName(){
		return this.name;
	}
	public void setName(TaxonNameBase newName){
		try {
			//TODO make static for performance reasons
			Field taxonBaseField = TaxonNameBase.class.getDeclaredField("taxonBases");
			taxonBaseField.setAccessible(true);
			if(this.name == newName) return;
			if (name != null) { 
				Set<TaxonBase> taxonBases = (Set<TaxonBase>) taxonBaseField.get(name);
				taxonBases.remove(this);
			}
			if (newName != null) { 
				Set<TaxonBase> taxonBases = (Set<TaxonBase>) taxonBaseField.get(newName);
				taxonBases.add(this);
			}
			this.name = newName;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.getStackTrace();
		} 	
	}

	public boolean isDoubtful(){
		return this.isDoubtful;
	}
	public void setDoubtful(boolean isDoubtful){
		this.isDoubtful = isDoubtful;
	}

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public ReferenceBase getSec() {
		return sec;
	}

	public void setSec(ReferenceBase sec) {
		this.sec = sec;
	}
	
	@Transient
	public boolean isSaveable(){
		if (  (this.getName() == null)  ||  (this.getSec() == null)  ){
			return false;
		}else{
			return true;
		}
	}

}