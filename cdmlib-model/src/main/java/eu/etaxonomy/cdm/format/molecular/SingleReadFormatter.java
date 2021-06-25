/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.molecular;

import eu.etaxonomy.cdm.format.AbstractCdmFormatter;
import eu.etaxonomy.cdm.model.molecular.SingleRead;

/**
 * @author pplitzner
 * @since Nov 30, 2015
 *
 */
public class SingleReadFormatter extends AbstractCdmFormatter {

    public SingleReadFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        SingleRead singleRead = (SingleRead)object;
        if(singleRead.getPrimer()!=null){
            formatKeyMap.put(FormatKey.SINGLE_READ_PRIMER, singleRead.getPrimer().getLabel());
        }
        if(singleRead.getPherogram()!=null){
            formatKeyMap.put(FormatKey.SINGLE_READ_PHEROGRAM_TITLE_CACHE, singleRead.getPherogram().getTitleCache());
        }
        if(singleRead.getAmplificationResult()!=null &&
                singleRead.getAmplificationResult().getAmplification()!=null){
            formatKeyMap.put(FormatKey.AMPLIFICATION_LABEL, singleRead.getAmplificationResult().getAmplification().getLabelCache());
        }
    }

}
