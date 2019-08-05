/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @since 06.01.2017
 */
public abstract class ForSubtreeConfiguratorBase implements Serializable{

    private static final long serialVersionUID = 2756961021157678305L;

    private UUID subtreeUuid;
    private boolean includeAcceptedTaxa = true;
    private boolean includeSynonyms = true;
    private boolean includeSharedTaxa = true;
    private IProgressMonitor monitor;

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    protected ForSubtreeConfiguratorBase(UUID subtreeUuid, IProgressMonitor monitor) {
        this.subtreeUuid = subtreeUuid;
        this.monitor = monitor;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    protected ForSubtreeConfiguratorBase(UUID subtreeUuid) {
//        super(null);
        this.subtreeUuid = subtreeUuid;
    }

    public UUID getSubtreeUuid() {
        return subtreeUuid;
    }
    public void setSubtreeUuid(UUID subtreeUuid) {
        this.subtreeUuid = subtreeUuid;
    }


    public boolean isIncludeSynonyms() {
        return includeSynonyms;
    }
    public void setIncludeSynonyms(boolean includeSynonyms) {
        this.includeSynonyms = includeSynonyms;
    }

    public boolean isIncludeAcceptedTaxa() {
        return includeAcceptedTaxa;
    }
    public void setIncludeAcceptedTaxa(boolean includeAcceptedTaxa) {
        this.includeAcceptedTaxa = includeAcceptedTaxa;
    }

    public boolean isIncludeSharedTaxa() {
        return includeSharedTaxa;
    }
    public void setIncludeSharedTaxa(boolean includeSharedTaxa) {
        this.includeSharedTaxa = includeSharedTaxa;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }



}
