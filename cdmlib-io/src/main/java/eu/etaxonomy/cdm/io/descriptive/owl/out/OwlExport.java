/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component
public class OwlExport extends CdmExportBase<OwlExportConfigurator, OwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    private Property propHasSubStructure;
    private Property propHasRootNode;
    private Property propUuid;
    private Property propLabel;
    private Property propIsA;
    private Property propType;
    private Property propDescription;

    @Override
    protected boolean doCheck(OwlExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(OwlExportState state) {

        TransactionStatus txStatus = startTransaction(true);

        FeatureTree featureTree = state.getConfig().getFeatureTree();
        featureTree = getFeatureTreeService().load(featureTree.getUuid());

        FeatureNode rootNode = featureTree.getRoot();

        Model model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRootNode = model.createProperty(OwlConstants.PROPERTY_HAS_ROOT_NODE);
        propUuid = model.createProperty(OwlConstants.PROPERTY_UUID);
        propLabel = model.createProperty(OwlConstants.PROPERTY_LABEL);
        propIsA = model.createProperty(OwlConstants.PROPERTY_IS_A);
        propType = model.createProperty(OwlConstants.PROPERTY_TYPE);
        propDescription = model.createProperty(OwlConstants.PROPERTY_DESCRIPTION);

        Resource resourceRootNode = model.createResource(OwlConstants.RESOURCE_NODE + rootNode.getUuid().toString())
                .addProperty(propIsA, OwlConstants.NODE)
                .addProperty(propUuid, rootNode.getUuid().toString())
                .addProperty(propIsA, OwlConstants.NODE);

        model.createResource(OwlConstants.RESOURCE_FEATURE_TREE+featureTree.getUuid().toString())
                .addProperty(propUuid, featureTree.getUuid().toString())
                .addProperty(propLabel, featureTree.getTitleCache())
                .addProperty(propHasRootNode, resourceRootNode)
                .addProperty(propIsA, OwlConstants.TREE)
                .addProperty(propType, featureTree.getTermType().getKey());

        addChildNode(rootNode, resourceRootNode, model);

        exportStream = new ByteArrayOutputStream();
        model.write(exportStream);
        state.getResult().addExportData(getByteArray());

        commitTransaction(txStatus);
    }

    private void addChildNode(FeatureNode node, Resource resourceNode, Model model){
        List<FeatureNode> childNodes = node.getChildNodes();
        for (FeatureNode child : childNodes) {
            DefinedTermBase term = child.getTerm();
            Resource childResourceNode = model.createResource(OwlConstants.RESOURCE_NODE+term.getUuid().toString())
                    .addProperty(propUuid, term.getUuid().toString())
                    .addProperty(propLabel, term.getLabel())
                    .addProperty(propIsA, OwlConstants.NODE)
                    .addProperty(propType, term.getTermType().getKey());
            if(term.getDescription()!=null){
                childResourceNode.addProperty(propDescription, term.getDescription());
            }
            resourceNode.addProperty(propHasSubStructure, childResourceNode);
            addChildNode(child, childResourceNode, model);
        }
    }

    @Override
    protected boolean isIgnore(OwlExportState state) {
        return false;
    }

}
