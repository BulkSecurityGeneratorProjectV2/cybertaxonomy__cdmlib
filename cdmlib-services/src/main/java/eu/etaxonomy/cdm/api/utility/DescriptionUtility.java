/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;

/**
 * @author a.kohlbecker
 * @since Apr 18, 2013
 */
public class DescriptionUtility {

    private static final Logger logger = Logger.getLogger(DescriptionUtility.class);

    /**
     * <b>NOTE: To avoid LayzyLoadingExceptions this method must be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes
     * The following rules are respected during the filtering:
     * <ol>
     * <li><b>Marked area filter</b>: Skip distributions for areas having a {@code TRUE} {@link Marker}
     * with one of the specified {@link MarkerType}s. Existing sub-areas of a marked area must also be marked
     * with the same marker type, otherwise the marked area acts as a <b>fallback area</b> for the sub areas.
     * An area is a <b>fallback area</b> if it is marked to be hidden and if it has at least one of
     * sub area which is not marked to be hidden. The fallback area will be show if there is no {@link Distribution}
     * for any of the non hidden sub-areas. For more detailed discussion on fallback areas see
     * https://dev.e-taxonomy.eu/trac/ticket/4408</li>
     * <li><b>Prefer computed rule</b>:Computed distributions are preferred over entered or imported elements.
     * (Computed description elements are identified by the {@link
     * MarkerType.COMPUTED()}). This means if a entered or imported status
     * information exist for the same area for which computed data is available,
     * the computed data has to be given preference over other data.
     * see parameter <code>preferComputed</code></li>
     * <li><b>Status order preference rule</b>: In case of multiple distribution
     * status ({@link PresenceAbsenceTermBase}) for the same area the status
     * with the highest order is preferred, see
     * {@link OrderedTermBase#compareTo(OrderedTermBase)}. This rule is
     * optional, see parameter <code>statusOrderPreference</code></li>
     * <li><b>Sub area preference rule</b>: If there is an area with a <i>direct
     * sub area</i> and both areas have the same status only the
     * information on the sub area should be reported, whereas the super area
     * should be ignored. This rule is optional, see parameter
     * <code>subAreaPreference</code>. Can be run separately from the other filters.
     * This rule affects any distribution,
     * that is to computed and edited equally. For more details see
     * {@link https://dev.e-taxonomy.eu/trac/ticket/5050})</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param hiddenAreaMarkerTypes
     *            distributions where the area has a {@link Marker} with one of the specified {@link MarkerType}s will
     *            be skipped or acts as fall back area. For more details see <b>Marked area filter</b> above.
     * @param preferComputed
     *            Computed distributions for the same area will be preferred over edited distributions.
     *            <b>This parameter should always be set to <code>true</code>.</b>
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            This rule can be run separately from the other filters.
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true
     * @return the filtered collection of distribution elements.
     */
    public static Set<Distribution> filterDistributions(Collection<Distribution> distributions,
            Set<MarkerType> hiddenAreaMarkerTypes, boolean preferComputed, boolean statusOrderPreference,
            boolean subAreaPreference) {

        Map<NamedArea, Set<Distribution>> filteredDistributions = new HashMap<>(100); // start with a big map from the beginning!

        // sort Distributions by the area
        for(Distribution distribution : distributions){
            NamedArea area = distribution.getArea();
            if(area == null) {
                logger.debug("skipping distribution with NULL area");
                continue;
            }

            if(!filteredDistributions.containsKey(area)){
                filteredDistributions.put(area, new HashSet<>());
            }
            filteredDistributions.get(area).add(distribution);
        }

        // -------------------------------------------------------------------
        // 1) skip distributions having an area with markers matching hideMarkedAreas
        //    but keep distributions for fallback areas
        if(hiddenAreaMarkerTypes != null && !hiddenAreaMarkerTypes.isEmpty()) {
            Set<NamedArea> areasHiddenByMarker = new HashSet<>();
            for(NamedArea area : filteredDistributions.keySet()) {
                if(checkAreaMarkedHidden(hiddenAreaMarkerTypes, area)) {
                    boolean showAsFallbackArea = false;
                    // if at least one sub area is not hidden by a marker
                    // this area is a fall-back area for this sub area
                    for(NamedArea subArea : area.getIncludes()) {
                        if (!areasHiddenByMarker.contains(subArea) && checkAreaMarkedHidden(hiddenAreaMarkerTypes, subArea)) {
                            if(filteredDistributions.containsKey(subArea)) {
                                areasHiddenByMarker.add(subArea);
                            }
                        }
                        // if this sub-area is not marked to be hidden
                        // the parent area must be visible if there is no
                        // data for the sub-area
                        boolean subAreaVisible = filteredDistributions.containsKey(subArea)
                                && !areasHiddenByMarker.contains(subArea);
                        showAsFallbackArea = !subAreaVisible || showAsFallbackArea;
                    }
                    if (!showAsFallbackArea) {
                        // this area does not need to be shown as
                        // fall-back for another area so it will be hidden.
                        areasHiddenByMarker.add(area);
                    }
                }
            }
            for(NamedArea area :areasHiddenByMarker) {
                filteredDistributions.remove(area);
            }
        }
        // -------------------------------------------------------------------


        // -------------------------------------------------------------------
        // 2) remove not computed distributions for areas for which computed
        //    distributions exists
        //
        if(preferComputed) {
            Map<NamedArea, Set<Distribution>> computedDistributions = new HashMap<>(distributions.size());
            Map<NamedArea, Set<Distribution>> otherDistributions = new HashMap<>(distributions.size());
            // separate computed and edited Distributions
            for (NamedArea area : filteredDistributions.keySet()) {
                for (Distribution distribution : filteredDistributions.get(area)) {
                    // this is only required for rule 1
                    if(isAggregated(distribution)){
                        if(!computedDistributions.containsKey(area)){
                            computedDistributions.put(area, new HashSet<>());
                        }
                        computedDistributions.get(area).add(distribution);
                    } else {
                        if(!otherDistributions.containsKey(area)){
                            otherDistributions.put(area, new HashSet<>());
                        }
                        otherDistributions.get(area).add(distribution);
                    }
                }
            }
            for(NamedArea keyComputed : computedDistributions.keySet()){
                otherDistributions.remove(keyComputed);
            }
            // combine computed and non computed Distributions again
            filteredDistributions.clear();
            for(NamedArea key : computedDistributions.keySet()){
                if(!filteredDistributions.containsKey(key)) {
                    filteredDistributions.put(key, new HashSet<>());
                }
                filteredDistributions.get(key).addAll(computedDistributions.get(key));
            }
            for(NamedArea key : otherDistributions.keySet()){
                if(!filteredDistributions.containsKey(key)) {
                    filteredDistributions.put(key, new HashSet<>());
                }
                filteredDistributions.get(key).addAll(otherDistributions.get(key));
            }
        }
        // -------------------------------------------------------------------


        // -------------------------------------------------------------------
        // 3) statusOrderPreference
        if (statusOrderPreference) {
            Map<NamedArea, Set<Distribution>> tmpMap = new HashMap<>(filteredDistributions.size());
            for(NamedArea key : filteredDistributions.keySet()){
                tmpMap.put(key, byHighestOrderPresenceAbsenceTerm(filteredDistributions.get(key)));
            }
            filteredDistributions = tmpMap;
        }
        // -------------------------------------------------------------------


        // -------------------------------------------------------------------
        // 4) Sub area preference rule
        if(subAreaPreference){
            Set<NamedArea> removeCandidatesArea = new HashSet<>();
            for(NamedArea key : filteredDistributions.keySet()){
                if(removeCandidatesArea.contains(key)){
                    continue;
                }
                if(key.getPartOf() != null && filteredDistributions.containsKey(key.getPartOf())){
                    removeCandidatesArea.add(key.getPartOf());
                }
            }
            for(NamedArea removeKey : removeCandidatesArea){
                filteredDistributions.remove(removeKey);
            }
         }
        // -------------------------------------------------------------------

        return valuesOfAllInnerSets(filteredDistributions.values());
    }

    private static boolean isAggregated(Distribution distribution) {
        if (distribution.hasMarker(MarkerType.COMPUTED(), true)){
            return true;
        }else{
            DescriptionBase<?> desc = distribution.getInDescription();
            if (desc != null && desc.isAggregatedDistribution()){
                return true;
            }
        }
        return false;
    }

    public static boolean checkAreaMarkedHidden(Set<MarkerType> hiddenAreaMarkerTypes, NamedArea area) {
        if(hiddenAreaMarkerTypes != null) {
            for(MarkerType markerType : hiddenAreaMarkerTypes){
                if(area.hasMarker(markerType, true)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Orders the given Distribution elements in a hierarchical structure.
     * This method will not filter out any of the Distribution elements.
     * @param termDao
     * @param omitLevels
     * @param hiddenAreaMarkerTypes
     *      Areas not associated to a Distribution in the {@code distList} are detected as fall back area
     *      if they are having a {@link Marker} with one of the specified {@link MarkerType}s. Areas identified as such
     *      are omitted from the hierarchy and the sub areas are moving one level up.
     *      For more details on fall back areas see <b>Marked area filter</b> of
     *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
     * @param distributionOrder
     * @param distList
     * @return
     */
    public static DistributionTree orderDistributions(IDefinedTermDao termDao,
            Set<NamedAreaLevel> omitLevels,
            Collection<Distribution> distributions,
            Set<MarkerType> hiddenAreaMarkerTypes,
            DistributionOrder distributionOrder) {

        DistributionTree tree = new DistributionTree(termDao);

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}
        //order by areas
        tree.orderAsTree(distributions, omitLevels, hiddenAreaMarkerTypes);
        tree.recursiveSortChildren(distributionOrder); // FIXME respect current locale for sorting
        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return tree;
    }

    /**
     * Implements the Status order preference filter for a given set to Distributions.
     * The distributions should all be for the same area.
     * The method returns a site of distributions since multiple Distributions
     * with the same status are possible. For example if the same status has been
     * published in more than one literature references.
     *
     * @param distributions
     *
     * @return the set of distributions with the highest status
     */
    private static Set<Distribution> byHighestOrderPresenceAbsenceTerm(Set<Distribution> distributions){

        Set<Distribution> preferred = new HashSet<>();
        PresenceAbsenceTerm highestStatus = null;  //we need to leave generics here as for some reason highestStatus.compareTo later jumps into the wrong class for calling compareTo
        int compareResult;
        for (Distribution distribution : distributions) {
            if(highestStatus == null){
                highestStatus = distribution.getStatus();
                preferred.add(distribution);
            } else {
                if(distribution.getStatus() == null){
                    continue;
                } else {
                    compareResult = highestStatus.compareTo(distribution.getStatus());
                }
                if(compareResult < 0){
                    highestStatus = distribution.getStatus();
                    preferred.clear();
                    preferred.add(distribution);
                } else if(compareResult == 0) {
                    preferred.add(distribution);
                }
            }
        }

        return preferred;
    }

    private static <T extends CdmBase> Set<T> valuesOfAllInnerSets(Collection<Set<T>> collectionOfSets){
        Set<T> allValues = new HashSet<T>();
        for(Set<T> set : collectionOfSets){
            allValues.addAll(set);
        }
        return allValues;
    }

    /**
     * Provides a consistent string based key of the given NamedArea , see also
     * {@link #areaKey(Distribution)}
     *
     * @param area
     * @return the string representation of the NamedArea.uuid
     */
//    private static String areaKey(NamedArea area){
//        return String.valueOf(area.getUuid());
//    }

    /**
     * Provides a consistent string based key of the given NamedArea contained
     * in the given distribution, see also {@link #areaKey(Distribution)}
     *
     * @param distribution
     * @return the string representation of the NamedArea.uuid or
     *         <code>"NULL"</code> in case the Distribution had no NamedArea
     */
//    private static String areaKey(Distribution distribution){
//        StringBuilder keyBuilder = new StringBuilder();
//
//        if(distribution.getArea() != null){
//            keyBuilder.append(distribution.getArea().getUuid());
//        } else {
//            keyBuilder.append("NULL");
//        }
//
//        return keyBuilder.toString();
//    }

}
