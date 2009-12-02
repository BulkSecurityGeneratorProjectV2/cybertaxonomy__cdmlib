// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.ITreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;


/**
 * @author n.hoffmann
 * @created Sep 21, 2009
 * @version 1.0
 */
public interface ITaxonTreeService extends IIdentifiableEntityService<TaxonomicTree> {

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public TaxonNode getTaxonNodeByUuid(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public ITreeNode getTreeNodeByUuid(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public TaxonomicTree getTaxonomicTreeByUuid(UUID uuid);
	
	/**
	 * 
	 * @param taxon
	 * @param taxonomicTreeUuid
	 * @param propertyPaths
	 * @return
	 * @deprecated use loadTaxonNode(TaxonNode taxonNode, ...) instead
	 * if you have a taxonomicTree and a taxon that is in it, you should also have the according taxonNode
	 */
	public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID taxonomicTreeUuid, List<String> propertyPaths);
	
	/**
	 * 
	 * @param taxonNode
	 * @param propertyPaths
	 * @return
	 */
	public TaxonNode loadTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);
	
	/**
	 * Loads all TaxonNodes of the specified tree for a given Rank.
	 * If a branch does not contain a TaxonNode with a TaxonName at the given
	 * Rank the node associated with the next lower Rank is taken as root node.
	 * If the <code>rank</code> is null the absolute root nodes will be returned.
	 *
	 * @param taxonomicTree
	 * @param rank may be null
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadRankSpecificRootNodes(TaxonomicTree taxonomicTree, Rank rank, List<String> propertyPaths);

	/**
	 * @param taxonNode
	 * @param baseRank
	 *            specifies the root level of the taxonomic tree, may be null.
	 *            Nodes of this rank or in case this rank does not exist in the
	 *            current branch the next lower rank is taken as root node for
	 *            this rank henceforth called the <b>base node</b>.
	 * @param propertyPaths
	 *            the initialization strategy for the returned TaxonNode
	 *            instances.
	 * @return the path of nodes from the <b>base node</b> to the node of the
	 *         specified taxon.
	 */
	public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, Rank baseRank, List<String> propertyPaths);
	
	/**
	 * Although this method seems to be a redundant alternative to {@link #loadChildNodesOfTaxonNode(TaxonNode, List)} it is an important 
	 * alternative from which web services benefit. Without this method the web service controller method, which operates outside of the 
	 * transaction, would have to initialize the full taxon tree with all nodes of the taxon. 
	 * This would be rather slow compared to using this method. 
	 * @param taxon
	 * @param taxonomicTree
	 *            the taxonomic tree to be used
	 * @param baseRank
	 *            specifies the root level of the taxonomic tree, may be null.
	 *            Nodes of this rank or in case this rank does not exist in the
	 *            current branch the next lower rank is taken as as root node for
	 *            this rank henceforth called the <b>base node</b>.
	 * @param propertyPaths
	 *            the initialization strategy for the returned TaxonNode
	 *            instances.
	 * @return the path of nodes from the <b>base node</b> to the node of the specified
	 *         taxon.
	 */
	public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, TaxonomicTree taxonomicTree, Rank baseRank, List<String> propertyPaths);
		
	
	
	/**
	 * Although this method seems to be a redundant alternative to {@link #loadChildNodesOfTaxonNode(TaxonNode, List)} it is an important 
	 * alternative from which web services benefit. Without this method the web service controller method, which operates outside of the 
	 * transaction, would have to initialize the full taxon tree with all nodes of the taxon. 
	 * This would be rather slow compared to using this method. 
	 * @param taxon
	 * @param taxonomicTree
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxon(Taxon taxon, TaxonomicTree taxonomicTree, List<String> propertyPaths);
	
	/**
	 * 
	 * @param taxon
	 * @param taxonomicTree
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);
	
	/**
	 * 
	 * @param taxonomicTree
	 * @return
	 */
	public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByTaxonomicTree(TaxonomicTree taxonomicTree);
	
	/**
	 * @param taxon
	 * @param taxTree
	 * @param propertyPaths
	 * @param size
	 * @param height
	 * @param widthOrDuration
	 * @param mimeTypes
	 * @return
	 *  @deprecated use getAllMediaForChildNodes(TaxonNode taxonNode, ...) instead
	 * if you have a taxonomicTree and a taxon that is in it, you should also have the according taxonNode
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(Taxon taxon, TaxonomicTree taxTree, List<String> propertyPaths, Integer size, Integer height, Integer widthOrDuration, String[] mimeTypes);
	
	/**
	 * 
	 * @param taxonNode
	 * @param propertyPaths
	 * @param size
	 * @param height
	 * @param widthOrDuration
	 * @param mimeTypes
	 * @return
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(TaxonNode taxonNode, List<String> propertyPaths, Integer size, Integer height, Integer widthOrDuration, String[] mimeTypes);
	
	/**
	 * 
	 * @param taxonNode
	 * @return
	 */
	public UUID removeTaxonNode(TaxonNode taxonNode);
	
	/**
	 * 
	 * @param taxonNode
	 * @return
	 */
	public UUID saveTaxonNode(TaxonNode taxonNode);
	
	/**
	 * 
	 * @param taxonNodeCollection
	 * @return
	 */
	public Map<UUID, TaxonNode> saveTaxonNodeAll(Collection<TaxonNode> taxonNodeCollection);
	
	/**
	 * 
	 * @param treeNode
	 * @return
	 */
	public UUID removeTreeNode(ITreeNode treeNode);
	
	/**
	 * 
	 * @param treeNode
	 * @return
	 */
	public UUID saveTreeNode(ITreeNode treeNode);

}
