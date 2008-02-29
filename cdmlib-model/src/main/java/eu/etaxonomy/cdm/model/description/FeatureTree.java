/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * Feature trees arrange concepts and features/characters. They may also be used to
 * define flat char. subsets for filtering purposes. 
 * A feature tree is build out of feature nodes, which can be hierarchihcally organised
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@Entity
public class FeatureTree extends TermBase {
	static Logger logger = Logger.getLogger(FeatureTree.class);
	private Set<FeatureNode> nodes = new HashSet<FeatureNode>();
	private FeatureNode root;
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<FeatureNode> getNodes() {
		return nodes;
	}
	public void setNodes(Set<FeatureNode> nodes) {
		this.nodes = nodes;
	}

	@ManyToOne
	public FeatureNode getRoot() {
		return root;
	}
	public void setRoot(FeatureNode root) {
		this.root = root;
	}

}