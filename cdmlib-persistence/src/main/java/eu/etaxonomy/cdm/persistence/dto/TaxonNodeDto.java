/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.kohlbecker
 * @since Jun 13, 2016
 *
 */
public class TaxonNodeDto extends UuidAndTitleCache<ITaxonTreeNode> {

    private static final long serialVersionUID = -7169646913528213604L;

    /**
     * count of the direct taxonomic children
     */
    private final int taxonomicChildrenCount;

    /**
     * The UUID of the associated secundum reference
     */
    private UUID secUuid = null;

    /**
     * The uuid of the associated Taxon entity
     */
    private UUID taxonUuid = null;

    /**
     * the taggedTitle of the associated TaxonName entity
     */
    private List<TaggedText> taggedTitle = new ArrayList<>();

    /**
     * The unplaced flag of the Taxon entity
     */
    private final boolean unplaced;

    /**
     * The excluded flag of the Taxon entity
     */
    private final boolean excluded;

    /**
     * The doubtful flag of the TaxonNode entity
     */
    private final boolean doubtful;

    /**
     * The Rank.label value of the rank to which the associated TaxonName entity is assigned to.
     */
    private String rankLabel = null;
    private Integer rankOrderIndex;

    private final TaxonStatus status;

    private final UUID classificationUUID;
    private final UUID parentUUID;

    private final String treeIndex;
    private final Integer sortIndex;

    /**
     * @param taxonNode
     */
    public TaxonNodeDto(TaxonNode taxonNode) {
        this(null, taxonNode);
    }

    public TaxonNodeDto(Class type, TaxonNode taxonNode) {
        super(type, taxonNode.getUuid(), taxonNode.getId(), null);
        Taxon taxon = taxonNode.getTaxon();
        if (taxon != null){
            setTitleCache(taxon.getName() != null ? taxon.getName().getTitleCache() : taxon.getTitleCache());
            secUuid = taxon.getSec() != null ? taxon.getSec().getUuid() : null;
            taxonUuid = taxon.getUuid();
            taggedTitle = taxon.getName() != null? taxon.getName().getTaggedName() : taxon.getTaggedTitle();
            rankLabel = taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getLabel() : null;
            this.setAbbrevTitleCache(taxon.getTitleCache());
            rankOrderIndex =taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getOrderIndex() : null;

        }else{
            setTitleCache(taxonNode.getClassification().getTitleCache());
            rankOrderIndex = null;
        }
        taxonomicChildrenCount = taxonNode.getCountChildren();
        unplaced = taxonNode.isUnplaced();
        excluded = taxonNode.isExcluded();
        doubtful = taxonNode.isDoubtful();

        status = TaxonStatus.Accepted;
        classificationUUID = taxonNode.getClassification().getUuid();
        treeIndex = taxonNode.treeIndex();
        parentUUID = taxonNode.getParent() == null? null:taxonNode.getParent().getUuid();
        sortIndex = taxonNode.getSortIndex();
    }

    /**
     * @param taxonNode
     */
    public TaxonNodeDto(Synonym synonym, boolean isHomotypic) {
        super(null, synonym.getName().getTitleCache());

        taxonomicChildrenCount = 0;
        secUuid = synonym.getSec().getUuid();
        taxonUuid = synonym.getUuid();
//        setTitleCache(synonym.getName().getTitleCache());
        taggedTitle = synonym.getName().getTaggedName();
        unplaced = false;
        excluded = false;
        doubtful = false;
        rankLabel = synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getLabel() : null;
        rankOrderIndex =synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getOrderIndex() : null;
        status = isHomotypic ? TaxonStatus.SynonymObjective : TaxonStatus.Synonym;
        classificationUUID = null;
        treeIndex = null;
        sortIndex = null;
        parentUUID = null;

    }



    /**
     * @return the taxonomicChildrenCount
     */
    public int getTaxonomicChildrenCount() {
        return taxonomicChildrenCount;
    }

    /**
     * @return the secUuid
     */
    public UUID getSecUuid() {
        return secUuid;
    }

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }


    /**
     * @return the taggedTitle
     */
    public List<TaggedText> getTaggedTitle() {
        return taggedTitle;
    }

    /**
     * @return the unplaced
     */
    public boolean isUnplaced() {
        return unplaced;
    }

    /**
     * @return the excluded
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * @return the doubtful
     */
    public boolean isDoubtful() {
        return doubtful;
    }
    /**
     * @return the rankLabel
     */
    public String getRankLabel() {
        return rankLabel;
    }

    /**
     * @return the status
     */
    public TaxonStatus getStatus() {
        return status;
    }

    public UUID getClassificationUUID() {
        return classificationUUID;
    }

    public String getTreeIndex() {
        return treeIndex;
    }

    public UUID getParentUUID() {
        return parentUUID;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public Integer getRankOrderIndex() {
        return rankOrderIndex;
    }

    public String getTaxonTitleCache(){
        return getAbbrevTitleCache();
    }

    public String getNameTitleCache(){
        return getTitleCache();
    }


    @Override
    public boolean equals(Object node2){
        if (node2 instanceof TaxonNodeDto){
            return this.getUuid().equals(((TaxonNodeDto)node2).getUuid());
        } else{
            return false;
        }


    }


}
