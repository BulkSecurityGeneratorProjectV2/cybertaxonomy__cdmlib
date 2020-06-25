/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.in;

import java.net.URI;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;

/**
 * Configurator base class for taxon fact excel imports.
 *
 * @author a.mueller
 * @since 28.05.2020
 */
public abstract class FactExcelImportConfiguratorBase
        extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = 1649010514975388511L;

    protected FactExcelImportConfiguratorBase(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

}