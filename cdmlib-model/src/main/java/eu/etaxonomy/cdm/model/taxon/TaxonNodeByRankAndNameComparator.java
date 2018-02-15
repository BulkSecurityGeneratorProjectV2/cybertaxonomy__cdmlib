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
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author k.luther
 * @date 18.03.2010
 *
 */
public class TaxonNodeByRankAndNameComparator implements Serializable, Comparator<TaxonNode> {
	private static final long serialVersionUID = 2596641007876609704L;

	@Override
    public int compare(TaxonNode node1, TaxonNode node2) {

	    boolean node1Excluded = node1.isExcluded();
	    boolean node2Excluded = node2.isExcluded();
	    boolean node1Unplaced = node1.isUnplaced();
	    boolean node2Unplaced = node2.isUnplaced();


		if (node1.getUuid().equals(node2.getUuid())){
			return 0;
		}
		//They should both be put to the end (first unplaced then excluded)
		if (node2Excluded && !node1Excluded){
		    return -1;
		}
		if (node2Unplaced && !(node1Unplaced || node1Excluded)){
		    return -1;
		}

		if (node1Excluded && !node2Excluded){
            return 1;
        }
        if (node1Unplaced && !(node2Unplaced || node2Excluded)){
            return 1;
        }

        if (node1Unplaced && node2Excluded){
            return -1;
        }
        if (node2Unplaced && node1Excluded){
            return 1;
        }


		TaxonBase<?> taxon1 = node1.getTaxon();
		TaxonBase<?> taxon2 = node2.getTaxon();

		TaxonName name1 = (taxon1 == null) ? null : taxon1.getName();
		TaxonName name2 = (taxon2 == null) ? null : taxon2.getName();

		Rank rankTax1 = (name1 == null) ? null : name1.getRank();
		Rank rankTax2 = (name2 == null) ? null : name2.getRank();

		//first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
		if (rankTax1 == null && rankTax2 != null){
			return 1;
		}else if(rankTax2 == null && rankTax1 != null){
			return -1;
		}else if (rankTax1 != null && rankTax1.isHigher(rankTax2)){
			return -1;
		}else if (rankTax1 == null && rankTax2 == null || rankTax1.equals(rankTax2)) {
			if (name1 != null && name2 != null){
				//same rank, order by name
				int result = name1.compareToName(name2);
				if (result == 0){
					return getTaxonUuid(taxon1, node1).compareTo(getTaxonUuid(taxon2, node2));
				}else{
					return result;
				}
			}else {
				//this is maybe not 100% correct, we need to compare name cases, but it is a very rare case
				return getTaxonTitle(taxon1, node1).compareTo(getTaxonTitle(taxon2, node2));
			}
		}else{
			//rankTax2.isHigher(rankTax1)
			return 1;
		}
	}

    /**
     * @param taxon1
     * @return
     */
    public String getTaxonTitle(TaxonBase<?> taxon, TaxonNode node) {
        return (taxon == null) ? node.getUuid().toString(): taxon.getTitleCache();
    }

    /**
     * @param taxon
     * @param node
     * @return
     */
    private UUID getTaxonUuid(TaxonBase<?> taxon, TaxonNode node) {
        return (taxon == null) ? node.getUuid(): taxon.getUuid();
    }



}
