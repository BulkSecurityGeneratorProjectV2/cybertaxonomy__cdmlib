/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;

/**
 * @author a.kohlbecker
 * @since Mar 20, 2020
 */
public class SortableTaxonNodeQueryResult {

    protected UUID taxonNodeUuid;
    protected Integer taxonNodeId;
    protected String treeIndex;
    protected UUID taxonUuid;
    protected String taxonTitleCache;
    protected String nameTitleCache;
    protected Rank nameRank = Rank.UNKNOWN_RANK();
    protected UUID parentNodeUuid;
    protected Integer sortIndex;
    protected UUID classificationUuid;
    protected boolean taxonIsPublish = true;
    protected TaxonNodeStatus status;
 


    private Map<Language, String> statusNote = new HashMap<>();


    /**Is this the reason
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache, String nameTitleCache,
            Rank nameRank, UUID parentNodeUuid, Integer sortIndex, UUID classificationUuid,  Boolean taxonIsPublished, TaxonNodeStatus status) {
        this.taxonNodeUuid = taxonNodeUuid;
        this.taxonNodeId = taxonNodeId;
        this.treeIndex = treeIndex;
        this.taxonUuid = taxonUuid;
        this.taxonTitleCache = taxonTitleCache;
        this.nameTitleCache = nameTitleCache;
        if(nameRank != null){
            this.nameRank = nameRank;
        }
        this.parentNodeUuid = parentNodeUuid;
        this.sortIndex = sortIndex;
        this.classificationUuid = classificationUuid;
        this.taxonIsPublish = taxonIsPublished;
        
    }

    /**
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache,
            Rank nameRank, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, parentNodeUuid, null, null, null, null);
    }


    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache,
            Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, null, null, null, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache,
            Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, nameRank, null, null, null, null, null);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, UUID taxonUuid, String taxonTitleCache, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, null, taxonUuid, taxonTitleCache, null, parentNodeUuid);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, parentNodeUuid);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, UUID taxonUuid, String taxonTitleCache) {
        this(taxonNodeUuid, taxonNodeId, null, taxonUuid, taxonTitleCache, null, null);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, null);
    }

    public UUID getTaxonNodeUuid() {
        return taxonNodeUuid;
    }
    public void setTaxonNodeUuid(UUID taxonNodeUuid) {
        this.taxonNodeUuid = taxonNodeUuid;
    }

    public String getTreeIndex() {
        return treeIndex;
    }
    
    public void setTreeIndex(String treeIndex) {
        this.treeIndex = treeIndex;
    }
    
    public Integer getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(Integer sortIndex) {
		this.sortIndex = sortIndex;
	}
	
    public UUID getTaxonUuid() {
        return taxonUuid;
    }
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    /**
     * @return the parentNodeUuid
     */
    public UUID getParentNodeUuid() {
        return parentNodeUuid;
    }

    /**
     * @param parentNodeUuid the parentNodeUuid to set
     */
    public void setParentNodeUuid(UUID parentNodeUuid) {
        this.parentNodeUuid = parentNodeUuid;
    }
    /**
     * @return the parentNodeUuid
     */
    public UUID getClassificationUuid() {
        return classificationUuid;
    }

    /**
     * @param parentNodeUuid the parentNodeUuid to set
     */
    public void setClassificationUuid(UUID classificationUuid) {
        this.classificationUuid = classificationUuid;
    }

    
    public boolean isTaxonIsPublish() {
		return taxonIsPublish;
	}

	public void setTaxonIsPublish(boolean taxonIsPublish) {
		this.taxonIsPublish = taxonIsPublish;
	}

	public Map<Language, String> getStatusNote() {
		return statusNote;
	}

	public void setStatusNote(Map<Language, String> statusNote) {
		this.statusNote = statusNote;
	}

	public Integer getTaxonNodeId() {
        return taxonNodeId;
    }
    public void setTaxonNodeId(Integer taxonNodeId) {
        this.taxonNodeId = taxonNodeId;
    }
    public String getTaxonTitleCache() {
        return taxonTitleCache;
    }
    public void setTaxonTitleCache(String taxonTitleCache) {
        this.taxonTitleCache = taxonTitleCache;
    }
    public Rank getNameRank() {
        return nameRank;
    }
    public void setNameRank(Rank nameRank) {
        this.nameRank = nameRank;
    }

    public String getNameTitleCache() {
        return nameTitleCache;
    }
    public void setNameTitleCache(String nameTitleCache) {
        this.nameTitleCache = nameTitleCache;
    }

	public TaxonNodeStatus getStatus() {
		return status;
	}

	public void setStatus(TaxonNodeStatus status) {
		this.status = status;
	}


}
