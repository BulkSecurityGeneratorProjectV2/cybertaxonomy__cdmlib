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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.kohlbecker
 * @since Jun 13, 2016
 */
public class TaxonNodeDto extends UuidAndTitleCache<ITaxonTreeNode> {

    private static final long serialVersionUID = -7169646913528213604L;

    /**
     * count of the direct taxonomic children
     */
    private int taxonomicChildrenCount = 0;

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
     * The status of the TaxonNode entity
     */
    private TaxonNodeStatus status;

    private Map<Language, String> statusNote = new HashMap<>();


    /**
     * The Rank.label value of the rank to which the associated TaxonName entity is assigned to.
     */
    private String rankLabel = null;
    private Integer rankOrderIndex = null;

    private TaxonStatus taxonStatus;

    private UUID classificationUUID = null;
    private UUID parentUUID = null;

    private String treeIndex = null;
    private Integer sortIndex = null;
    private Boolean taxonIsPublish = true;


    public TaxonNodeDto(ITaxonTreeNode taxonNode) {
        this(TaxonNode.class, taxonNode);
    }

    public TaxonNodeDto(UUID uuid, Integer id, String titleCache) {
        super(uuid, id, titleCache);
    }

    public TaxonNodeDto(UUID uuid, Integer id, String nameTitleCache, String taxonTitleCache) {
        super(uuid, id, nameTitleCache, taxonTitleCache);

    }
    public TaxonNodeDto(UUID uuid, Integer id, UUID taxonUuid, String treeIndex, String nameTitleCache, String taxonTitleCache, Integer rankOrderIndex, UUID parentUuid, Integer sortIndex, UUID classificationUuid, Boolean taxonIsPublished, TaxonNodeStatus status, List<LanguageString> statusNote){
    	this(uuid, id, treeIndex, nameTitleCache, taxonTitleCache, rankOrderIndex, parentUuid, sortIndex, classificationUuid);
    	this.status = status;
    	this.taxonIsPublish = taxonIsPublished;
    	for (LanguageString str: statusNote) {
    		this.statusNote.put(str.getLanguage(), str.getText());
    	}
	this.taxonUuid = taxonUuid;
    }

    public TaxonNodeDto(UUID uuid, Integer id, String treeIndex, String nameTitleCache, String taxonTitleCache, Integer rankOrderIndex, UUID parentUuid, Integer sortIndex, UUID classificationUuid) {
        super(uuid, id, nameTitleCache, taxonTitleCache);
        this.rankOrderIndex = rankOrderIndex;
        this.parentUUID = parentUuid;
        this.treeIndex = treeIndex;
        this.sortIndex = sortIndex;
        this.classificationUUID = classificationUuid;
    }

    public TaxonNodeDto(UUID uuid, Integer id, String titleCache, Integer rankOrderIndex) {
        super(uuid, id, titleCache);
        this.rankOrderIndex = rankOrderIndex;
    }

    public TaxonNodeDto(Class<? extends ITaxonTreeNode> type, ITaxonTreeNode taxonTreeNode) {
        super(type, taxonTreeNode.getUuid(), taxonTreeNode.getId(), null);

        Taxon taxon = null;
        TaxonNode taxonNode = null;
        Classification classification = null;

        //taxonNode, taxon, classification
        if (taxonTreeNode instanceof TaxonNode){
            taxonNode = (TaxonNode)taxonTreeNode;
            classification = taxonNode.getClassification();
            taxon = taxonNode.getTaxon();
        }else if (taxonTreeNode instanceof Classification){
            classification = (Classification) taxonTreeNode;
            taxonNode = classification.getRootNode();
            //taxon should always be null for rootnode therefore no assignment here
        }else{
            throw new IllegalStateException("Class not yet handled: " +  taxonTreeNode.getClass().getName());
        }

        //taxon or titleCache
        if (taxon != null){
            setTitleCache(taxon.getName() != null ? taxon.getName().getTitleCache() : taxon.getTitleCache());
            secUuid = taxon.getSec() != null ? taxon.getSec().getUuid() : null;
            taxonUuid = taxon.getUuid();
            taggedTitle = taxon.getName() != null? taxon.getName().getTaggedName() : taxon.getTaggedTitle();
            rankLabel = taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getLabel() : null;
            this.setAbbrevTitleCache(taxon.getTitleCache());
            rankOrderIndex =taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getOrderIndex() : null;
            taxonIsPublish = taxon.isPublish();
        }else{
            if (classification != null){
                setTitleCache(classification.getTitleCache());
            }
            rankOrderIndex = null;
        }
        taxonStatus = TaxonStatus.Accepted;

        //taxonNode
        taxonomicChildrenCount = taxonNode.getCountChildren();
        status = taxonNode.getStatus();

        for(Language lang : taxonNode.getStatusNote().keySet()) {
            statusNote.put(lang, taxonNode.getStatusNote(lang));
        }

        treeIndex = taxonNode.treeIndex();
        if(taxonNode.getParent() != null) {
            parentUUID = taxonNode.getParent().getUuid();
        } else {
            parentUUID = null;
        }

        sortIndex = taxonNode.getSortIndex();

        //classification
        if (classification != null){
            classificationUUID = classification.getUuid();
        }
    }

    public TaxonNodeDto(Synonym synonym, boolean isHomotypic) {
        super(null, synonym.getName().getTitleCache());

        taxonomicChildrenCount = 0;
        secUuid = synonym.getSec().getUuid();
        taxonUuid = synonym.getUuid();
//        setTitleCache(synonym.getName().getTitleCache());
        taggedTitle = synonym.getName().getTaggedName();

        rankLabel = synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getLabel() : null;
        rankOrderIndex =synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getOrderIndex() : null;
        taxonStatus = isHomotypic ? TaxonStatus.SynonymObjective : TaxonStatus.Synonym;
        classificationUUID = null;
    }

    public int getTaxonomicChildrenCount() {
        return taxonomicChildrenCount;
    }

    public UUID getSecUuid() {
        return secUuid;
    }

    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    public List<TaggedText> getTaggedTitle() {
        return taggedTitle;
    }

    public TaxonNodeStatus getStatus() {
        return status;
    }

    public boolean isUnplaced() {
        return status == null ? false : status.equals(TaxonNodeStatus.UNPLACED);
    }

    public boolean isExcluded() {
        return status == null ? false : status.equals(TaxonNodeStatus.EXCLUDED);
    }

    public boolean isDoubtful() {
        return status == null ? false : status.equals(TaxonNodeStatus.DOUBTFUL);
    }

    public String getRankLabel() {
        return rankLabel;
    }

    public TaxonStatus getTaxonStatus() {
        return taxonStatus;
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

    /**
     * Preliminary implementation. May not be exactly match
     * the real name cache.
     */
    public String getNameCache(){
        List<TaggedText> nameCacheTags = taggedTitle.stream()
                .filter(t->t.getType().isNameCachePart())
                .collect(Collectors.toList());
        return TaggedCacheHelper.createString(nameCacheTags, new HTMLTagRules());
    }

    public boolean isPublish(){
        return taxonIsPublish;
    }

    @Override
    public boolean equals(Object node2){
        if (node2 instanceof TaxonNodeDto){
            return this.getUuid().equals(((TaxonNodeDto)node2).getUuid());
        } else{
            return false;
        }
    }

    public Map<Language, String> getStatusNote() {
        return statusNote;
    }
}