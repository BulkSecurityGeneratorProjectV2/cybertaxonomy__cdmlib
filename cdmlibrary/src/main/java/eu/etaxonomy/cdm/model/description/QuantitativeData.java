/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public class QuantitativeData extends FeatureBase {
	static Logger logger = Logger.getLogger(QuantitativeData.class);
	private MeasurementUnit unit;
	private ArrayList statisticalValues;

	public ArrayList getStatisticalValues(){
		return this.statisticalValues;
	}

	/**
	 * 
	 * @param statisticalValues    statisticalValues
	 */
	public void setStatisticalValues(ArrayList statisticalValues){
		this.statisticalValues = statisticalValues;
	}

	public MeasurementUnit getUnit(){
		return this.unit;
	}

	/**
	 * 
	 * @param unit    unit
	 */
	public void setUnit(MeasurementUnit unit){
		this.unit = unit;
	}

	@Transient
	public float getMin(){
		return 0;
	}

	@Transient
	public float getMax(){
		return 0;
	}

	@Transient
	public float getTypicalLowerBoundary(){
		return 0;
	}

	@Transient
	public float getTypicalUpperBoundary(){
		return 0;
	}

}