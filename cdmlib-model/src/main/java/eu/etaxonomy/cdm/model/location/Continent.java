/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Continent")
@XmlRootElement(name = "Continent")
@Entity
@Audited
public class Continent extends NamedArea {
	private static final long serialVersionUID = 4650684072484353151L;
	private static final Logger logger = Logger.getLogger(Continent.class);

	private static final UUID uuidEurope = UUID.fromString("3b69f979-408c-4080-b573-0ad78a315610");
	private static final UUID uuidAfrica = UUID.fromString("c204c529-d8d2-458f-b939-96f0ebd2cbe8");
	private static final UUID uuidAsiaTemperate = UUID.fromString("7f4f4f89-3b4c-475d-929f-144109bd8457");
	private static final UUID uuidAsiaTropical = UUID.fromString("f8039275-d2c0-4753-a1ab-0336642a1499");
	private static final UUID uuidNAmerica = UUID.fromString("81d8aca3-ddd7-4537-9f2b-5327c95b6e28");
	private static final UUID uuidSAmerica = UUID.fromString("12b861c9-c922-498c-8b1a-62afc26d19e3");
	private static final UUID uuidAustralasia = UUID.fromString("a2afdb9a-04a0-434c-9e75-d07dbeb86526");
	private static final UUID uuidPacific = UUID.fromString("c57adcff-5213-45f0-a5f0-97a9f5c0f1fe");
	private static final UUID uuidAntarctica = UUID.fromString("71fd9ab7-9b07-4eb6-8e54-c519aff56728");
	private static Continent PACIFIC;
	private static Continent AUSTRALASIA;
	private static Continent SOUTH_AMERICA;
	private static Continent ANTARCTICA;
	private static Continent NORTH_AMERICA;
	private static Continent ASIA_TROPICAL;
	private static Continent ASIA_TEMPERATE;
	private static Continent AFRICA;
	private static Continent EUROPE;

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(){
		logger.debug("NewInstance of Continent");
		return new Continent();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(String term, String label, String labelAbbrev){
		return new Continent(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public Continent() {
		super();
	}
	public Continent(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	public static final Continent EUROPE(){
		return EUROPE;
	}

	public static final Continent AFRICA(){
		return AFRICA;
	}

	public static final Continent ASIA_TEMPERATE(){
		return ASIA_TEMPERATE;
	}

	public static final Continent ASIA_TROPICAL(){
		return ASIA_TROPICAL;
	}

	public static final Continent NORTH_AMERICA(){
		return NORTH_AMERICA;
	}

	public static final Continent ANTARCTICA(){
		return ANTARCTICA;
	}

	public static final Continent SOUTH_AMERICA(){
		return SOUTH_AMERICA;
	}

	public static final Continent AUSTRALASIA(){
		return AUSTRALASIA;
	}
	
	public static final Continent PACIFIC(){
		return PACIFIC;
	}
	@Override
	public NamedArea readCsvLine(Class<NamedArea> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		try {
			Continent newInstance = Continent.class.newInstance();
		    return DefinedTermBase.readCsvLine(newInstance, csvLine, Language.ENGLISH());
		} catch (Exception e) {
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
		}
		
	    return null;
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
		Continent.AFRICA = (Continent)termVocabulary.findTermByUuid(Continent.uuidAfrica);
		Continent.ANTARCTICA = (Continent)termVocabulary.findTermByUuid(Continent.uuidAntarctica);
		Continent.ASIA_TEMPERATE = (Continent)termVocabulary.findTermByUuid(Continent.uuidAsiaTemperate);
		Continent.ASIA_TROPICAL = (Continent)termVocabulary.findTermByUuid(Continent.uuidAsiaTropical);
		Continent.AUSTRALASIA = (Continent)termVocabulary.findTermByUuid(Continent.uuidAustralasia);
		Continent.EUROPE = (Continent)termVocabulary.findTermByUuid(Continent.uuidEurope);
		Continent.NORTH_AMERICA = (Continent)termVocabulary.findTermByUuid(Continent.uuidNAmerica);
		Continent.PACIFIC = (Continent)termVocabulary.findTermByUuid(Continent.uuidPacific);
		Continent.SOUTH_AMERICA = (Continent)termVocabulary.findTermByUuid(Continent.uuidSAmerica);
	}

}