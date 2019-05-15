/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.model.term.FeatureTree;


/**
 *
 * @author pplitzner
 * @since May 2, 2019
 *
 */
public class StructureTreeOwlExportConfigurator extends XmlExportConfiguratorBase<StructureTreeOwlExportState> {

    private static final long serialVersionUID = -4360021755279592592L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StructureTreeOwlExportConfigurator.class);

    private List<FeatureTree> featureTrees;


    private StructureTreeOwlExportConfigurator(ICdmDataSource source, File destinationFolder, List<FeatureTree> featureTrees) {
        super(destinationFolder, source, null);
        this.featureTrees = featureTrees;
    }

    public static StructureTreeOwlExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder, List<FeatureTree> featureTrees) {
        return new StructureTreeOwlExportConfigurator(source, destinationFolder, featureTrees);
    }

    @Override
    public StructureTreeOwlExportState getNewState() {
        return new StructureTreeOwlExportState(this);
    }

    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                StructureTreeOwlExport.class
        };
    }

    public List<FeatureTree> getFeatureTrees() {
        return featureTrees;
    }

}
