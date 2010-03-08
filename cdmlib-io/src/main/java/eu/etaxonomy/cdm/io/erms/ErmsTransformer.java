// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class ErmsTransformer implements IDbImportTransformer{
	private static final Logger logger = Logger.getLogger(ErmsTransformer.class);
	
	public static final int SOURCE_USE_ORIGINAL_DESCRIPTION = 1;
	public static final int SOURCE_USE_BASIS_OF_RECORD = 2;
	public static final int SOURCE_USE_ADDITIONAL_SOURCE = 3;
	public static final int SOURCE_USE_SOURCE_OF_SYNONYMY = 4;
	public static final int SOURCE_USE_REDESCRIPTION = 5;
	public static final int SOURCE_USE_NEW_COMBINATION_REFERENCE = 6;
	public static final int SOURCE_USE_STATUS_SOURCE = 7;
	public static final int SOURCE_USE_EMENDATION = 8;
	
	
	public static NomenclaturalCode kingdomId2NomCode(Integer kingdomId){
		switch (kingdomId){
			case 1: return null;
			case 2: return NomenclaturalCode.ICZN;  //Animalia
			case 3: return NomenclaturalCode.ICBN;  //Plantae
			case 4: return NomenclaturalCode.ICBN;  //Fungi
			case 5: return NomenclaturalCode.ICZN ;  //Protozoa
			case 6: return NomenclaturalCode.ICNB ;  //Bacteria
			case 7: return NomenclaturalCode.ICBN;  //Chromista
			case 147415: return NomenclaturalCode.ICNB;  //Monera
			default: return null;
	
		}
	
	}
	
	public NameTypeDesignationStatus transformNameTypeDesignationStatus(Object statusId){
		if (statusId == null){
			return null;
		}
		Integer intDesignationId = (Integer)statusId;
		switch (intDesignationId){
			case 1: return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
			case 2: return NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION();
			case 3: return NameTypeDesignationStatus.MONOTYPY();
			default: 
				String warning = "Unknown name type designation status id " + statusId;
				logger.warn(warning);
				throw new IllegalArgumentException(warning);
		}
	}

	public Language languageByErmsAbbrev(String ermsAbbrev){
		if (CdmUtils.isEmpty(ermsAbbrev)){return null;
		}else if (ermsAbbrev.equals("af")){return Language.AFRIKAANS();
		}else if (ermsAbbrev.equals("al")){return Language.ALEUT();
		}else if (ermsAbbrev.equals("ar")){return Language.ARABIC();
		}else if (ermsAbbrev.equals("as")){return Language.ASSAMESE();
//		}else if (ermsAbbrev.equals("au")){return Language.AUNS();  //??
		}else if (ermsAbbrev.equals("az")){return Language.AZERBAIJANI();
		}else if (ermsAbbrev.equals("ba")){return Language.BASQUE();
		}else if (ermsAbbrev.equals("be")){return Language.BELORUSSIAN();
		}else if (ermsAbbrev.equals("bg")){return Language.BULGARIAN();
		}else if (ermsAbbrev.equals("bn")){return Language.BENGALI();
		}else if (ermsAbbrev.equals("br")){return Language.BRETON();
		}else if (ermsAbbrev.equals("bu")){return Language.BURMESE();
		}else if (ermsAbbrev.equals("ca")){return Language.CATALAN_VALENCIAN();  //??? (Catalan)
		}else if (ermsAbbrev.equals("ce")){return Language.CEBUANO();
		}else if (ermsAbbrev.equals("ch")){return Language.CHINESE();
//		}else if (ermsAbbrev.equals("cl")){return Language.CHUKCHI(); // (LOURAVETLANY)(); //iso639-3: ckt //also known as Luoravetlan, Chukot and Chukcha is a Palaeosiberian language spoken by Chukchi people in the easternmost extremity of Siberia, mainly in Chukotka Autonomous Okrug.
		}else if (ermsAbbrev.equals("cr")){return Language.CROATIAN();
		}else if (ermsAbbrev.equals("cs")){return Language.CZECH();
		}else if (ermsAbbrev.equals("da")){return Language.DANISH();
		}else if (ermsAbbrev.equals("de")){return Language.GERMAN();
//		}else if (ermsAbbrev.equals("ec")){return Language.ENGLISH-CANADIAN();  //no iso
		}else if (ermsAbbrev.equals("ee")){return Language.ESTONIAN();
//		}else if (ermsAbbrev.equals("ek")){return Language.EVEN-KAMCHATKA(); //iso639-3: eve    Lamut, Ewen, Eben, Orich, Ilqan; Russian: ???�????? ???�?, earlier also ????????? ???�?) is a Tungusic language spoken by the Evens in Siberia
		}else if (ermsAbbrev.equals("en")){return Language.ENGLISH();
		}else if (ermsAbbrev.equals("ep")){return Language.ESPERANTO();
		}else if (ermsAbbrev.equals("es")){return Language.SPANISH_CATALAN();
//		}else if (ermsAbbrev.equals("eu")){return Language.ENGLISH-UNITED STATES();  no iso //ENGLISH();
//		}else if (ermsAbbrev.equals("ev")){return Language.EVENKI();   iso: evn  //languages of Tungusic family 
		}else if (ermsAbbrev.equals("fa")){return Language.PERSIAN(); 
//		}else if (ermsAbbrev.equals("fc")){return Language.FRENCH-CANADIAN();   no iso  //FRENCH();
		}else if (ermsAbbrev.equals("fi")){return Language.FINNISH();
		}else if (ermsAbbrev.equals("fj")){return Language.FIJIAN();
		}else if (ermsAbbrev.equals("fl")){return Language.DUTCH_FLEMISH();
		}else if (ermsAbbrev.equals("fo")){return Language.FAROESE();
		}else if (ermsAbbrev.equals("fr")){return Language.FRENCH();
		}else if (ermsAbbrev.equals("ga")){return Language.GAELIC_SCOTTISH_GAELIC();  //??
		}else if (ermsAbbrev.equals("ge")){return Language.KALAALLISUT_GREENLANDIC(); // GREENLANDIC
		}else if (ermsAbbrev.equals("gl")){return Language.GALICIAN();
		}else if (ermsAbbrev.equals("gr")){return Language.GREEK_MODERN(); //(Greek)
//		}else if (ermsAbbrev.equals("gu")){return Language.GUARAYO();     //GUARANI() ??
//		}else if (ermsAbbrev.equals("ha")){return Language.HASSANYA(); Hassaniyya Arabic  ios 639-3: mey
		}else if (ermsAbbrev.equals("he")){return Language.HEBREW();
		}else if (ermsAbbrev.equals("hi")){return Language.HINDI();
		}else if (ermsAbbrev.equals("hu")){return Language.HUNGARIAN();
		}else if (ermsAbbrev.equals("hw")){return Language.HAWAIIAN();
		}else if (ermsAbbrev.equals("hy")){return Language.ARMENIAN();
		}else if (ermsAbbrev.equals("in")){return Language.INDONESIAN();
		}else if (ermsAbbrev.equals("iq")){return Language.INUPIAQ();
		}else if (ermsAbbrev.equals("ir")){return Language.IRISH();
		}else if (ermsAbbrev.equals("is")){return Language.ICELANDIC();
		}else if (ermsAbbrev.equals("it")){return Language.ITALIAN();
		}else if (ermsAbbrev.equals("ja")){return Language.JAPANESE();
//		}else if (ermsAbbrev.equals("ji")){return Language.JIVARA();   		//??
//		}else if (ermsAbbrev.equals("ka")){return Language.KAMCHADAL();   iso 639-3:itl //Itelmen, formerly also known as Kamchadal, is a language belonging to the Chukotko-Kamchatkan family traditionally spoken in the Kamchatka Peninsula.    
		}else if (ermsAbbrev.equals("ko")){return Language.KOREAN();
//		}else if (ermsAbbrev.equals("kr")){return Language.KORYAK();    //iso639-3: kpy
		}else if (ermsAbbrev.equals("la")){return Language.LATIN();
		}else if (ermsAbbrev.equals("li")){return Language.LITHUANIAN();
//		}else if (ermsAbbrev.equals("lp")){return Language.LAPP();      //??
		}else if (ermsAbbrev.equals("lv")){return Language.LATVIAN();
		}else if (ermsAbbrev.equals("ma")){return Language.MACEDONIAN();
//		}else if (ermsAbbrev.equals("mh")){return Language.MAHR();   //Marathi ; Mari ??
//		}else if (ermsAbbrev.equals("mk")){return Language.MAKAH (QWIQWIDICCIAT)();  //iso639-3: myh
		}else if (ermsAbbrev.equals("ml")){return Language.MALAY();
//		}else if (ermsAbbrev.equals("ne")){return Language.NENETS();   iso639-3 yrk; iso639-2: mis
		}else if (ermsAbbrev.equals("nl")){return Language.DUTCH_FLEMISH();
		}else if (ermsAbbrev.equals("no")){return Language.NORWEGIAN();
		}else if (ermsAbbrev.equals("np")){return Language.NEPALI();
//		}else if (ermsAbbrev.equals("os")){return Language.OSTYAK();   //Ostyak on its own or in combination, can refer, especially in older literature, to several Siberian peoples and languages:
		//																Khanty language (kca; 639-2: fiu); Ket language(ket); Selkup language(sel; 639-2: sel)
//		}else if (ermsAbbrev.equals("pi")){return Language.PIRAYAGUARA();  //??
		}else if (ermsAbbrev.equals("pl")){return Language.POLISH();
		}else if (ermsAbbrev.equals("pt")){return Language.PORTUGUESE();
		}else if (ermsAbbrev.equals("ro")){return Language.ROMANIAN();
		}else if (ermsAbbrev.equals("ru")){return Language.RUSSIAN();
		}else if (ermsAbbrev.equals("sc")){return Language.SCOTS();
		}else if (ermsAbbrev.equals("sd")){return Language.SINDHI();
//		}else if (ermsAbbrev.equals("sh")){return Language.SERBO_CROATIAN();  //hbs
		}else if (ermsAbbrev.equals("si")){return Language.SINHALA_SINHALESE();
		}else if (ermsAbbrev.equals("sk")){return Language.SLOVAK();
		}else if (ermsAbbrev.equals("sn")){return Language.SLOVENIAN();
		}else if (ermsAbbrev.equals("sr")){return Language.SERBIAN();
		}else if (ermsAbbrev.equals("st")){return Language.SRANAN_TONGO();
		}else if (ermsAbbrev.equals("sv")){return Language.SWEDISH();
		}else if (ermsAbbrev.equals("sw")){return Language.SWAHILI();
		}else if (ermsAbbrev.equals("ta")){return Language.TAMIL();
		}else if (ermsAbbrev.equals("te")){return Language.TELUGU();
		}else if (ermsAbbrev.equals("tg")){return Language.TAGALOG();
		}else if (ermsAbbrev.equals("th")){return Language.THAI();
//		}else if (ermsAbbrev.equals("tm")){return Language.TAMUL();			//??
		}else if (ermsAbbrev.equals("tr")){return Language.TURKISH();
		}else if (ermsAbbrev.equals("tu")){return Language.TUPIS();
		}else if (ermsAbbrev.equals("uk")){return Language.UKRAINIAN();
		}else if (ermsAbbrev.equals("ur")){return Language.URDU();
		}else if (ermsAbbrev.equals("vi")){return Language.VIETNAMESE();
		}else if (ermsAbbrev.equals("we")){return Language.WELSH();
		}else if (ermsAbbrev.equals("wo")){return Language.WOLOF();
		}else if (ermsAbbrev.equals("ya")){return Language.YAKUT();
		}else if (ermsAbbrev.equals("yp")){return Language.YUPIKS();
//		}else if (ermsAbbrev.equals("yu")){return Language.YUKAGIR();  639-2: mis;  639-3 yux (Southern Yukaghir)- ykg(Tundra Yukaghir)
		}else{
			String warning = "Unknown language abbreviation " + ermsAbbrev;
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		
		
		
	}
	
}
