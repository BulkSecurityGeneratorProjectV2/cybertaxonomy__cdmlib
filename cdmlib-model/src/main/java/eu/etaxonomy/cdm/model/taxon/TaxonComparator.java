/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

import org.apache.log4j.Logger;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class makes available a method to compare two {@link TaxonBase taxa} by
 * comparing the publication dates of the corresponding 
 * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names}.
 *
 * @author a.mueller
 * @created 11.06.2008
 * @version 1.0
 */
public class TaxonComparator implements Comparator<TaxonBase>, Serializable {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonComparator.class);

    /**
     * Returns an integer generated by comparing fist the nomenclatural status and then the 
     * {@link eu.etaxonomy.cdm.model.name.INomenclaturalReference#getYear() publication years}
     * of both {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names} 
     * used in the given {@link TaxonBase taxa}.
     * If 1 name has status of type nom.inval. or nom.nudum the name is but to the end in a
     * list (returns +1 for a status in taxon1 and -1 for a status in taxon2). If both do have
     * no status or the same status, the publication date is taken for comparison.
     * Nom. nudum is handled as more "severe" status then nom.inval.
     *  
     * Returns a negative value if the publication year corresponding to the
     * first given taxon precedes the publication year corresponding to the
     * second given taxon. Returns a positive value if the contrary is true and
     * 0 if both publication years and the date, when they are created, are identical. 
     * In case one of the publication
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

        
        //set to end if a taxon has nomenclatural status "nom. inval." or "nom. nud."
        int statusCompareWeight = 0;
        statusCompareWeight += computeStatusCompareWeight(taxonBase1);
        statusCompareWeight -= computeStatusCompareWeight(taxonBase2);
        
        if (statusCompareWeight != 0){
        	return Integer.signum(statusCompareWeight);
        }
        
        //dates
        Integer intDate1 = getIntegerDate(taxonBase1);
        Integer intDate2 = getIntegerDate(taxonBase2);

        if (intDate1 == null && intDate2 == null){
            result = 0;
        }else if (intDate1 == null){
            return 1;
        }else if (intDate2 == null){
            return -1;
        }else{
            result = intDate1.compareTo(intDate2);
        }

        if (result == 0){
            TaxonNameBase<?,?> taxName1 = taxonBase1.getName();
            TaxonNameBase<?,?> taxName2 = taxonBase2.getName();

            return taxName1.compareTo(taxName2);

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
            TaxonNameBase<?,?> taxName1 = taxonBase1.getName();
            TaxonNameBase<?,?> taxName2 = taxonBase2.getName();

            return taxName1.compareTo(taxName2);

        }

        return result;

    }


	/**
	 * @param taxonBase1
	 * @param statusCompareWeight
	 * @return
	 */
	private int computeStatusCompareWeight(TaxonBase<?> taxonBase) {
		int result = 0;
		if (taxonBase == null || taxonBase.getName() == null || taxonBase.getName().getStatus() == null){
			return 0;
		}
		Set<NomenclaturalStatus> status1 = taxonBase.getName().getStatus();
        for (NomenclaturalStatus nomStatus1 : status1){
            if (nomStatus1.getType() != null){
            	if (nomStatus1.getType().equals(NomenclaturalStatusType.INVALID())){
            		result += 1;
            	}else if(nomStatus1.getType().equals(NomenclaturalStatusType.NUDUM())){
            		result += 2;
                }
            }
        }
		return result;
	}


    private Integer getIntegerDate(TaxonBase<?> taxonBase){
        Integer result;

        if (taxonBase == null){
            result = null;
        }else{
            TaxonNameBase<?,?> name = taxonBase.getName();
           if (name == null){
                result = null;
            }else{
                if (name instanceof ZoologicalName){

                    result = (((ZoologicalName)name).getPublicationYear());
                }else{
                    Reference<?> ref = (Reference<?>) name.getNomenclaturalReference();
                    if (ref == null){
                        result = null;
                    }else{
                        if (ref.getDatePublished() == null){
                        	Reference<?> inRef = ref.getInReference();
                        	if (inRef == null){
                                result = null;
                            }else{
                                if (inRef.getDatePublished() == null){
                                	result = null;
                                }else{
                                	result = ref.getInReference().getDatePublished().getStartYear();
                                }
                            }
                        }else{
                            result = ref.getDatePublished().getStartYear();
                        }
                    }
                }
            }
        }

        return result;
    }


}
