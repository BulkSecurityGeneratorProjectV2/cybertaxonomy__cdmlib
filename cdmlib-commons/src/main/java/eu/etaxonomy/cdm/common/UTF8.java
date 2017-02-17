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
	NO_BREAK_SPACE("\u00A0"),
	POLISH_L("\u0142"),
	SMALL_A_ACUTE("\u00E1"),
	RIGHT_SINGLE_QUOT("\u2019"), // Right single quotation mark
	ENGLISH_QUOT_START("\u201c"),  //Left English quotation mark
	ENGLISH_QUOT_END("\u201d"),  //Right English quotation mark
    ENGLISH_QUOT_START_LOW("\u201e"),  //Left English quotation mark Low
	ENGLISH_QUOT_END_HIGH("\u201f"),  //Right English quotation mark -‟-
	ACUTE_ACCENT("\u00B4"),     //Acute Accent, looks a bit similar to th single quotation mark
	BLACK_CIRCLE("\u25CF"),       //Black circle, symbol for endemic

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
