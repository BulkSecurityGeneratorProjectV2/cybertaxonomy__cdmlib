/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * This voter always returns {@link #ACCESS_GRANTED}.
 * It is needed as default voter when using the {@link UnanimousBased}
 * @author andreas kohlbecker
 * @since Sep 4, 2012
 */
public class GrantAlwaysVoter extends CdmPermissionVoter {

    @Override
    public int vote(Authentication authentication, TargetEntityStates object, Collection<ConfigAttribute> attributes) {
        return ACCESS_GRANTED;
    }

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return CdmBase.class;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        return false;
    }
}
