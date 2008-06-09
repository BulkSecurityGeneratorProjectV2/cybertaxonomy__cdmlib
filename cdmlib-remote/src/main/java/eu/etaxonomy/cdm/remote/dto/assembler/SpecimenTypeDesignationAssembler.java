/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaInstance;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.remote.dto.BaseSTO;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.IdentifiedString;
import eu.etaxonomy.cdm.remote.dto.SpecimenSTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenTypeDesignationSTO;

/**
 * @author a.kohlbecker
 * @created 05.06.2008
 * @version 1.0
 */
@Component
public class SpecimenTypeDesignationAssembler extends AssemblerBase<SpecimenTypeDesignationSTO, BaseTO, SpecimenTypeDesignation> {
	private static Logger logger = Logger
			.getLogger(SpecimenTypeDesignationAssembler.class);
	
	@Autowired
	private SpecimenAssembler specimenAssembler;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public SpecimenTypeDesignationSTO getSTO(SpecimenTypeDesignation specimenTypeDesignation, Enumeration<Locale> locales) {
		SpecimenTypeDesignationSTO specimenTypeDesignationSTO = new SpecimenTypeDesignationSTO();
		specimenTypeDesignationSTO.setUuid(specimenTypeDesignation.getUuid().toString());
		specimenTypeDesignationSTO.setStatus(
				new IdentifiedString(
						specimenTypeDesignation.getTypeStatus().getLabel(),
						specimenTypeDesignation.getTypeStatus().getUuid().toString()));
		specimenTypeDesignationSTO.setTypeSpecimen(specimenAssembler.getSTO(specimenTypeDesignation, locales));
		return specimenTypeDesignationSTO;
	}
	
	/**
	 * @param specimenTypeDesignations
	 * @param locales
	 * @return
	 */
	public List<SpecimenTypeDesignationSTO> getSTOs(Set<SpecimenTypeDesignation> specimenTypeDesignations, Enumeration<Locale> locales){
		List<SpecimenTypeDesignationSTO> specimenTypeDesignationSTOs = new ArrayList<SpecimenTypeDesignationSTO>(specimenTypeDesignations.size());
		for(SpecimenTypeDesignation specimenTypeDesignation : specimenTypeDesignations){
			specimenTypeDesignationSTOs.add(getSTO(specimenTypeDesignation, locales));
		}
		return specimenTypeDesignationSTOs;
	}

	/** 
	 * Method inherited from {@link AssemblerBase} but not implemented since no SpecimenTypeDesignationTO class exists.
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	public BaseTO getTO(SpecimenTypeDesignation cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}
}
