/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.AbstractStringComparator;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * Comparator that compares two TaxonNode instances by the titleCache of their referenced names.
 * @author a.kohlbecker
 * @since 24.06.2009
 *
 */
public class TaxonNodeByNameComparator extends AbstractStringComparator<TaxonNode> implements Comparator<TaxonNode>, ITaxonNodeComparator<TaxonNode> {

    private static final String HYBRID_SIGN = UTF8.HYBRID.toString();

    private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparator.class);

    private boolean ignoreHybridSign = true;
    private boolean sortInfraGenericFirst = true;

    @Override
    public int compare(TaxonNode node1, TaxonNode node2) {
        if (node1 == null && node2 == null) {
            return 0;
        } else if (node1 == null) {
            return 1;
        } else if (node2 == null) {
            return -1;
        }

        if (node1.equals(node2)){
            return 0;
        }
        int nodeResult = compareNodes(node1, node2);
        if (nodeResult != 0){
            return nodeResult;
        }
        return compareNames(node1, node2);
    }


    /**
     * @param node1
     * @param node2
     * @return
     */
    protected int compareNames(TaxonNode node1, TaxonNode node2) {
        String titleCache1 = createSortableTitleCache(node1);
        String titleCache2 = createSortableTitleCache(node2);

        if(isIgnoreHybridSign()) {
            if (logger.isTraceEnabled()){logger.trace("ignoring Hybrid Signs: " + HYBRID_SIGN);}
            titleCache1 = titleCache1.replace(HYBRID_SIGN, "");
            titleCache2 = titleCache2.replace(HYBRID_SIGN, "");
        }

        titleCache1 = applySubstitutionRules(titleCache1);
        titleCache2 = applySubstitutionRules(titleCache2);

        // 1
        StringTokenizer s2 = new StringTokenizer(titleCache1, "\"");
        if (s2.countTokens()>0){
            titleCache1 = "";
        }
        while(s2.hasMoreTokens()){
            titleCache1 += s2.nextToken();
        }

        // 2
        s2 = new StringTokenizer(titleCache2, "\"");
        if (s2.countTokens()>0){
            titleCache2 = "";
        }

        while(s2.hasMoreTokens()){
            titleCache2 += s2.nextToken();
        }

        int result = titleCache1.compareTo(titleCache2);
        if (result != 0){
        	return result;
        }else{
        	return node1.getUuid().compareTo(node2.getUuid());
        }
    }


    /**
     * @param node1
     * @param node2
     */
    protected int compareNodes(TaxonNode node1, TaxonNode node2) {


        boolean node1Excluded = node1.isExcluded();
        boolean node2Excluded = node2.isExcluded();
        boolean node1Unplaced = node1.isUnplaced();
        boolean node2Unplaced = node2.isUnplaced();

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
        return 0;
    }


    private String createSortableTitleCache(TaxonNode taxonNode) {

        String titleCache = null;
        if(taxonNode.getTaxon() != null && taxonNode.getTaxon().getName() != null ){
            TaxonName name = HibernateProxyHelper.deproxy(taxonNode.getTaxon().getName(), TaxonName.class);

            if (name.isNonViral()){
                if (logger.isTraceEnabled()){logger.trace(name + " isNonViralName");}
                INonViralName nonViralName = name;
                if (nonViralName.getGenusOrUninomial() != null){
                    titleCache = nonViralName.getGenusOrUninomial();
                    if ((name.isSpecies() || name.isInfraSpecific()) && nonViralName.getSpecificEpithet() != null){
                        titleCache = titleCache + " " + nonViralName.getSpecificEpithet();
                    }
                	if (name.isInfraSpecific() && nonViralName.getSpecificEpithet() != null
                			&& nonViralName.getInfraSpecificEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isInfraSpecific");}
                		    titleCache = titleCache + " " + nonViralName.getInfraSpecificEpithet();
                		if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
                			titleCache = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
                		}
                	}
                	if (name.isInfraGeneric() && nonViralName.getInfraGenericEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isInfraGeneric");}
                		titleCache = titleCache + " " + nonViralName.getInfraGenericEpithet();
                	}
                	if (nonViralName.isSpeciesAggregate() && nonViralName.getSpecificEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isSpeciesAggregate");}
                		titleCache = nonViralName.getGenusOrUninomial() + " " + nonViralName.getSpecificEpithet();
                	}
                }

            }
            if (titleCache == null){
                if (logger.isTraceEnabled()){logger.trace("titleCache still null, using name.getTitleCache()");}
                titleCache = name.getTitleCache();
            }
        }
        if (titleCache == null){
            if (logger.isTraceEnabled()){logger.trace("titleCache still null, using taxonNode id");}
            titleCache = String.valueOf(taxonNode.getId());
        }
        if (logger.isTraceEnabled()){logger.trace("SortableTitleCache: " + titleCache);}
//        System.out.println(titleCache);
        return titleCache;
    }


    @Override
    public boolean isIgnoreHybridSign() {
        return ignoreHybridSign;
    }

    @Override
    public void setIgnoreHybridSign(boolean ignore) {
        this.ignoreHybridSign = ignore;
    }

    public boolean isSortInfraGenericFirst() {
        return sortInfraGenericFirst;
    }

    public void setSortInfraGenericFirst(boolean infraGenericFirst) {
        this.sortInfraGenericFirst = infraGenericFirst;
    }

}
