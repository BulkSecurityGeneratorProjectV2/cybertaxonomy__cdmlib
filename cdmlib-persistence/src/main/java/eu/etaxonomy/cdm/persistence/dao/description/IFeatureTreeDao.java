/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermTreeNode;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author a.mueller
 * @since 10.07.2008
 */
public interface IFeatureTreeDao extends IIdentifiableDao<TermTree> {

    public List<TermTree> list();

    public UUID DefaultFeatureTreeUuid = UUID.fromString("ac8d4e58-926d-4f81-ac77-cebdd295df7c");

    /**
     * Loads nodes and the nodes child nodes recursively
     * @param nodes
     * @param nodePaths
     */
    public void deepLoadNodes(List<TermTreeNode> nodes, List<String> nodePaths);

    public <S extends TermTree> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern);
}
