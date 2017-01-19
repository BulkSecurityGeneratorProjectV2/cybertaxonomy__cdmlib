/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * @author k.luther
 * @date 08.12.2016
 *
 */
public class CacheUpdaterWrapper extends CdmImportBase<CacheUpdaterConfigurator, DefaultImportState<CacheUpdaterConfigurator>> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(DefaultImportState<CacheUpdaterConfigurator> state) {
        CacheUpdaterConfigurator config = state.getConfig();
        CacheUpdater updater;

       // CaseType caseType = CaseType.caseTypeOfDatasource(config.getDestination());
        IProgressMonitor  monitor = DefaultProgressMonitor.NewInstance();
        updater = new CacheUpdater();
        updater.invoke(state);

        return;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(DefaultImportState<CacheUpdaterConfigurator> state) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(DefaultImportState<CacheUpdaterConfigurator> state) {
        // TODO Auto-generated method stub
        return false;
    }

}
