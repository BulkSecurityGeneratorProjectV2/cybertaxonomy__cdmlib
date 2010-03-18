/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * This class makes available a method to compare two {@link TaxonBase taxa} by
 * comparing the publication dates of the corresponding {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names}.
 * 
 * @author a.mueller
 * @created 11.06.2008
 * @version 1.0
 */
public class TaxonComparator implements Comparator<TaxonBase> {
	private static final Logger logger = Logger.getLogger(TaxonComparator.class);

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	/** 
	 * Returns an integer generated by comparing the {@link eu.etaxonomy.cdm.model.name.INomenclaturalReference#getYear() publication years}
	 * of both {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names} used in the given {@link TaxonBase taxa}.
	 * Returns a negative value if the publication year corresponding to the
	 * first given taxon precedes the publication year corresponding to the
	 * second given taxon. Returns a positive value if the contrary is true and
	 * 0 if both publication years and the date, when they are created, are identical. In case one of the publication
	 * years is "null" and the other is not, the "empty" publication year will
	 * be considered to be always preceded by the "not null" publication year.
	 * If both publication years are "null" the creation date is used for the comparison
	 * 
	 *  
	 * @see		java.lang.String#compareTo(String)
	 * @see		java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(TaxonBase taxonBase1, TaxonBase taxonBase2) {
		int result;
		String date1 = getDate(taxonBase1);;
		String date2 = getDate(taxonBase2);
		if (date1 == null && date2 == null){
			result = 0;
		}else if (date1 == null){
			return 1;
		}else if (date2 == null){
			return -1;
		}else{
				result = date1.compareTo(date2);
		}
		if (result == 0){
			DateTime date11 = taxonBase1.getCreated();
			DateTime date12 = taxonBase2.getCreated();
			if (date11 == null && date12 == null) {
				return 0;
			}
			if (date11 == null) {
				return 1;
			}
			if (date12 == null) {
				return -1;
			}
			result = date11.compareTo(date12);
		}

		//for ticket #393 if the publication year is the same, the order is alphabetically
		
		if (result == 0){
			TaxonNameBase taxName1 = taxonBase1.getName();
			TaxonNameBase taxName2 = taxonBase2.getName();
			
			return taxName1.compareTo(taxName2);
			
		}
		
		return result;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private String getDate(TaxonBase taxonBase){
		String result = null;
		if (taxonBase == null){
			result = null;
		}else{
			TaxonNameBase name = taxonBase.getName();
			if (name == null){
				result = null;
			}else{
				if (name instanceof ZoologicalName){
					
					result = String.valueOf(((ZoologicalName)name).getPublicationYear());
				}else{
					 INomenclaturalReference ref = name.getNomenclaturalReference();
					if (ref == null){
						result = null;
					}else{
						result = ref.getYear();
					}
				}
			}
		}
		if (result != null){
			result = result.trim();
		}
		if ("".equals(result)){
			result = null;
		}
		return result;
	}
	
}
