/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * @author a.mueller
 *
 */
public class TimePeriodParserTest {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TimePeriodParserTest.class);

//	private TimePeriod onlyStartYear;
//	private TimePeriod onlyEndYear;
//	private TimePeriod startAndEndYear;
//	private TimePeriod noStartAndEndYear;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
//		onlyStartYear = TimePeriod.NewInstance(1922);
//		onlyEndYear = TimePeriod.NewInstance(null, 1857);
//		startAndEndYear = TimePeriod.NewInstance(1931, 1957);
//		Integer start = null;
//		Integer end = null;
//		noStartAndEndYear = TimePeriod.NewInstance(start, end);
	}


//************************ TESTS ******************************************


	@Test
	public void testParseSingleDateString() {
		String strDate = "1756";
		Partial date = TimePeriodParser.parseSingleDate(strDate);
		assertNotNull(date);
		Assert.assertEquals(Integer.parseInt(strDate), date.get(DateTimeFieldType.year()));
		try {
			date.get(DateTimeFieldType.monthOfYear());
			assertFalse(true); //should not be reached
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
		try {
			date.get(DateTimeFieldType.dayOfMonth());
			assertFalse(true); //should not be reached
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
		//to be continued
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#parseString(java.lang.String)}.
	 */
	@Test
	public void testParseStringString() {
		String strTimePeriod = "1756";
		TimePeriod tp1 = TimePeriodParser.parseString(strTimePeriod);
		assertNotNull(tp1);
		Assert.assertEquals(strTimePeriod, tp1.getYear());
		Assert.assertEquals(strTimePeriod, String.valueOf(tp1.getStartYear()));
		assertNull(tp1.getEnd());
		assertNull(tp1.getStartMonth());
		strTimePeriod = "1756-88";
		tp1 = TimePeriodParser.parseString(strTimePeriod);
		assertNotNull(tp1);
		Assert.assertEquals("1756-1788", tp1.getYear());
		Assert.assertEquals("1756", String.valueOf(tp1.getStartYear()));
		Assert.assertEquals("1788", String.valueOf(tp1.getEndYear()));
		assertNull(tp1.getEndMonth());
		assertNull(tp1.getStartMonth());
		//unparsable
		String strUnparsablePeriod = "wef 1809-78";
		TimePeriod tpUnparsable = TimePeriodParser.parseString(strUnparsablePeriod);
		assertNotNull(tpUnparsable);
		Assert.assertEquals(strUnparsablePeriod, tpUnparsable.getFreeText());

		//"1806"[1807]
		String strCorrectedPeriod = "\"1806\"[1807]";
		TimePeriod tpcorrected = TimePeriodParser.parseString(strCorrectedPeriod);
		assertNotNull(tpcorrected);
		Assert.assertEquals(strCorrectedPeriod, tpcorrected.getFreeText());
		Assert.assertEquals("1807", tpcorrected.getYear());

	      //„1806‟[1807]
        String strCorrectedEnPeriod = UTF8.QUOT_DBL_LOW9 + "1806"+UTF8.QUOT_DBL_HIGH_REV9+"[1807]";
        TimePeriod tpcorrectedEn = TimePeriodParser.parseString(strCorrectedEnPeriod);
        assertNotNull(tpcorrectedEn);
        Assert.assertEquals(strCorrectedEnPeriod, tpcorrectedEn.getFreeText());
        Assert.assertEquals("1807", tpcorrectedEn.getYear());


		//fl. 1806
		String strFlPeriod = "fl.  1806?";
		TimePeriod tpFl = TimePeriodParser.parseString(strFlPeriod);
		assertNotNull(tpFl);
		Assert.assertEquals(strFlPeriod, tpFl.getFreeText());
		Assert.assertEquals("1806", tpFl.getYear());

		String strCPeriod = "c.  1806-1810";
		TimePeriod tpC = TimePeriodParser.parseString(strCPeriod);
		assertNotNull(tpC);
		Assert.assertEquals(strCPeriod, tpC.getFreeText());
		Assert.assertEquals(Integer.valueOf(1806), tpC.getStartYear());
		Assert.assertEquals(Integer.valueOf(1810), tpC.getEndYear());
		Assert.assertEquals("1806-1810", tpC.getYear());

		//1.1.2011
		String strDotDate = "1.2.2011";
		TimePeriod tp = TimePeriodParser.parseString(strDotDate);
		assertNotNull(tp);
		Assert.assertEquals(strDotDate, tp.toString());
		Assert.assertEquals("2011", tp.getYear());
		Assert.assertEquals(Integer.valueOf(2), tp.getStartMonth());
		Assert.assertEquals(Integer.valueOf(1), tp.getStartDay());

		strDotDate = "31.03.2012";
		tp = TimePeriodParser.parseString(strDotDate);
		assertNotNull(tp);
		Assert.assertEquals("31.3.2012", tp.toString());
		Assert.assertEquals("2012", tp.getYear());
		Assert.assertEquals(Integer.valueOf(3), tp.getStartMonth());
		Assert.assertEquals(Integer.valueOf(31), tp.getStartDay());

		strDotDate = "00.04.2013";
		tp = TimePeriodParser.parseString(strDotDate);
		assertNotNull(tp);
		Assert.assertEquals("4.2013", tp.toString());
		Assert.assertEquals("2013", tp.getYear());
		Assert.assertEquals(Integer.valueOf(4), tp.getStartMonth());
		Assert.assertEquals(null, tp.getStartDay());

		strDotDate = "13.00.2014";
		tp = TimePeriodParser.parseString(strDotDate);
		assertNotNull(tp);
		Assert.assertEquals("13.xx.2014", tp.toString());
		Assert.assertEquals("2014", tp.getYear());
		Assert.assertEquals(null, tp.getStartMonth());
		Assert.assertEquals(Integer.valueOf(13), tp.getStartDay());

		strDotDate = "31.12.2015 - 02.01.2016";
		tp = TimePeriodParser.parseString(strDotDate);
		assertNotNull(tp);
		Assert.assertEquals("31.12.2015-2.1.2016", tp.toString());
		Assert.assertEquals("2015-2016", tp.getYear());
		Assert.assertEquals(Integer.valueOf(2015), tp.getStartYear());
		Assert.assertEquals(Integer.valueOf(12), tp.getStartMonth());
		Assert.assertEquals(Integer.valueOf(31), tp.getStartDay());
		Assert.assertEquals(Integer.valueOf(2016), tp.getEndYear());
		Assert.assertEquals(Integer.valueOf(1), tp.getEndMonth());
		Assert.assertEquals(Integer.valueOf(2), tp.getEndDay());

	}

	@Test
	public void testSlashPattern() {

        String strSlashDate = "31/12/2015 - 2/1/2016";
        TimePeriod tp = TimePeriodParser.parseString(strSlashDate);
        assertNotNull(tp);
        Assert.assertEquals("31.12.2015-2.1.2016", tp.toString());
        Assert.assertEquals("2015-2016", tp.getYear());
        Assert.assertEquals(Integer.valueOf(2015), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(12), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(31), tp.getStartDay());
        Assert.assertEquals(Integer.valueOf(2016), tp.getEndYear());
        Assert.assertEquals(Integer.valueOf(1), tp.getEndMonth());
        Assert.assertEquals(Integer.valueOf(2), tp.getEndDay());

        strSlashDate = "1/12/2015 - 2/1/2016";
        tp = TimePeriodParser.parseString(strSlashDate);
        assertNotNull(tp);
        Assert.assertEquals("1.12.2015-2.1.2016", tp.toString());
        Assert.assertEquals("2015-2016", tp.getYear());
        Assert.assertEquals(Integer.valueOf(2015), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(12), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(1), tp.getStartDay());
        Assert.assertEquals(Integer.valueOf(2016), tp.getEndYear());
        Assert.assertEquals(Integer.valueOf(1), tp.getEndMonth());
        Assert.assertEquals(Integer.valueOf(2), tp.getEndDay());
	}

	@Test
	public void testParseDateWithMonths() {
	    String strDate = "24 Aug. 1957";
	    TimePeriod tp = TimePeriodParser.parseString(strDate);
        assertNotNull(tp);
        Assert.assertEquals("24.8.1957", tp.toString());
        Assert.assertEquals("1957", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(8), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(24), tp.getStartDay());

        String strSingleDay = "8 March 1957";
        tp = TimePeriodParser.parseString(strSingleDay);
        assertNotNull(tp);
        Assert.assertEquals("8.3.1957", tp.toString());
        Assert.assertEquals("1957", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(3), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(8), tp.getStartDay());

        String strNoSpace = "26.Apr.2003";
        tp = TimePeriodParser.parseString(strNoSpace);
        assertNotNull(tp);
        Assert.assertEquals("26.4.2003", tp.toString());
        Assert.assertEquals("2003", tp.getYear());
        Assert.assertEquals(Integer.valueOf(2003), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(4), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(26), tp.getStartDay());

        String strMissingDay = "Feb. 1894";
        tp = TimePeriodParser.parseString(strMissingDay);
        assertNotNull(tp);
        Assert.assertEquals("2.1894", tp.toString());
        Assert.assertEquals("1894", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1894), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(2), tp.getStartMonth());
        Assert.assertEquals(null, tp.getStartDay());

	}


    @Test
    public void testParseVerbatim() {
        String strDate = "1957 [\"1958\"]";
        VerbatimTimePeriod tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals(strDate, tp.toString());
        Assert.assertEquals("1957", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
//        Assert.assertEquals(Integer.valueOf(8), tp.getStartMonth());
//        Assert.assertEquals(Integer.valueOf(24), tp.getStartDay());
        Assert.assertEquals("1958", tp.getVerbatimDate());

        strDate = "1947 publ. 1948";
        tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals("1947 [\"1948\"]", tp.toString());
        Assert.assertEquals("1947", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1947), tp.getStartYear());
        Assert.assertEquals("1948", tp.getVerbatimDate());

        strDate = "\"1884-1885\" [1886]";
        tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals("1886 [\"1884-1885\"]", tp.toString());
        Assert.assertEquals("1886", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1886), tp.getStartYear());
        Assert.assertEquals("1884-1885", tp.getVerbatimDate());

        //unparsable date part should generally not be parsed
        strDate = "1957a [\"1958\"]";
        tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals(strDate, tp.toString());
        Assert.assertEquals("", tp.getYear());
        Assert.assertEquals(null, tp.getStartYear());
        Assert.assertEquals("1958", tp.getVerbatimDate());
        Assert.assertEquals(strDate, tp.getFreeText());

        //English quotation
        strDate = "1957 ["+UTF8.QUOT_DBL_LOW9+"1958"+UTF8.QUOT_DBL_RIGHT+"]";
        tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals("1957 [\"1958\"]", tp.toString());
        Assert.assertEquals("1957", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertEquals("1958", tp.getVerbatimDate());

        //invalid verbatim marker
        strDate = "1947 publa 1948";
        tp = TimePeriodParser.parseStringVerbatim(strDate);
        assertNotNull(tp);
        Assert.assertEquals("1947 publa 1948", tp.toString());
        Assert.assertEquals("", tp.getYear());
        Assert.assertEquals(null, tp.getStartYear());
        Assert.assertEquals(null, tp.getVerbatimDate());
    }

    @Test
    public void testParseContinued() {
        String strDate = "01.12.1957+";
        TimePeriod tp = TimePeriodParser.parseString(strDate);
        Assert.assertTrue(tp.isContinued());
        Assert.assertEquals("1.12.1957+", tp.toString());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(12), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(1), tp.getStartDay());
        Assert.assertNull(tp.getEnd());

        strDate = "1957+";
        tp = TimePeriodParser.parseString(strDate);
        Assert.assertTrue(tp.isContinued());
        Assert.assertEquals("1957+", tp.toString());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertNull(tp.getStartMonth());
        Assert.assertNull(tp.getStartDay());
        Assert.assertNull(tp.getEnd());

        strDate = "24 Aug. 1957+";
        tp = TimePeriodParser.parseString(strDate);
        Assert.assertEquals("24.8.1957+", tp.toString());
        Assert.assertTrue(tp.isContinued());
        Assert.assertEquals("1957+", tp.getYear());
        Assert.assertEquals(Integer.valueOf(1957), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(8), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(24), tp.getStartDay());

        String strSlashDate = "31/12/2015+";
        tp = TimePeriodParser.parseString(strSlashDate);
        Assert.assertEquals("31.12.2015+", tp.toString());
        Assert.assertTrue(tp.isContinued());
        Assert.assertEquals("2015+", tp.getYear());
        Assert.assertEquals(Integer.valueOf(2015), tp.getStartYear());
        Assert.assertEquals(Integer.valueOf(12), tp.getStartMonth());
        Assert.assertEquals(Integer.valueOf(31), tp.getStartDay());
        Assert.assertNull(tp.getEnd());

    }


}
