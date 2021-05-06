/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @since 06.01.2017
 */
public class SecundumForSubtreeConfigurator extends ForSubtreeConfiguratorBase{

    private static final long serialVersionUID = -5181147074062916234L;

    private Reference newSecundum;
    private boolean overwriteExistingAccepted = true;
    private boolean overwriteExistingSynonyms = true;
    private boolean overwriteExistingRelations = true;
    private boolean emptySecundumDetail = true;

    public SecundumForSubtreeConfigurator(UUID subtreeUuid, Reference newSecundum, IRemotingProgressMonitor monitor) {
        super(subtreeUuid, monitor);
        this.newSecundum = newSecundum;
    }

    public SecundumForSubtreeConfigurator(UUID subtreeUuid) {
        super(subtreeUuid);
    }

    public Reference getNewSecundum() {
        return newSecundum;
    }
    public void setNewSecundum(Reference newSecundum) {
        this.newSecundum = newSecundum;
    }

    public boolean isEmptySecundumDetail() {
        return emptySecundumDetail;
    }
    public void setEmptySecundumDetail(boolean emptySecundumDetail) {
        this.emptySecundumDetail = emptySecundumDetail;
    }

    public boolean isOverwriteExistingAccepted() {
        return overwriteExistingAccepted;
    }
    public void setOverwriteExistingAccepted(boolean overwriteExistingAccepted) {
        this.overwriteExistingAccepted = overwriteExistingAccepted;
    }

    public boolean isOverwriteExistingSynonyms() {
        return overwriteExistingSynonyms;
    }
    public void setOverwriteExistingSynonyms(boolean overwriteExistingSynonyms) {
        this.overwriteExistingSynonyms = overwriteExistingSynonyms;
    }

    public boolean isOverwriteExistingRelations() {
        return overwriteExistingRelations;
    }
    public void setOverwriteExistingRelations(boolean overwriteExistingRelations) {
        this.overwriteExistingRelations = overwriteExistingRelations;
    }
}