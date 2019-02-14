/**
 *
 */
package eu.etaxonomy.cdm.common;

/**
 * This class is a constant holder for commonly used UTF-8 characters.
 *
 * @author a.mueller
 * @since 19.06.2013
 */
public enum UTF8 {


	EN_DASH("\u2013"),   // https://de.wikipedia.org/wiki/Halbgeviertstrich
	SPATIUM("\u202F"),   //very short non-breaking space
	EN_DASH_SPATIUM("\u202F\u2013\u202F"),
	HYBRID ("\u00D7"),   // hybrid sign
	SHARP_S("\u00DF"),
    a_UMLAUT("\u00E4"),   //small a umlaut, latin small letter a with diaeresis
	O_UMLAUT("\u00F6"),   //small o umlaut, latin small letter o with diaeresis
    U_UMLAUT("\u00FC"),   //small u umlaut, latin small letter u with diaeresis
    SMALL_O_WITH_STROKE("\u00F8"),   //"Danish" o
	NO_BREAK_SPACE("\u00A0"),
	POLISH_L("\u0142"),
	SMALL_A_ACUTE("\u00E1"),
    SMALL_O_ACUTE("\u00F3"),
    SMALL_E_ACUTE("\u00E9"),
    REGEX_NOT("U+005E"),   //Circumflex Accent, used in regular expression for negation (e.g not a: [^a]
    QUOT_SINGLE_RIGHT("\u2019"), // Right single quotation mark
	QUOT_SINGLE_HIGH_REV9("\u201b"), // Left high single quotation mark
    QUOT_DBL_LEFT("\u201c"),  //LEFT DOUBLE QUOTATION MARK Left English quotation mark
	QUOT_DBL_RIGHT("\u201d"),  //RIGHT DOUBLE QUOTATION MARK Right English quotation mark
    QUOT_DBL_LOW9("\u201e"),  //DOUBLE LOW-9 QUOTATION MARK  Left English quotation mark Low
	QUOT_DBL_HIGH_REV9("\u201f"),  //DOUBLE HIGH-REVERSED-9 QUOTATION MARK Right English quotation mark -‟-
	ACUTE_ACCENT("\u00B4"),     //Acute Accent, looks a bit similar to th single quotation mark
	BLACK_CIRCLE("\u25CF"),       //Black circle, symbol for endemic
	DEGREE_SIGN("\u00B0"),      //°
	NARROW_NO_BREAK("\u202F")
	;

	private String value;

	private UTF8(String value) {
		this.value = value;
	}

	@Override
    public String toString(){
		return value;
	}

}
