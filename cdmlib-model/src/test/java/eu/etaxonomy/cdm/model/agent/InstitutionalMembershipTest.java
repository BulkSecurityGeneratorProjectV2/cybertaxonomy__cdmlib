/**
 * 
 */
package eu.etaxonomy.cdm.model.agent;

import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.Partial;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.babadshanjan
 *
 */
public class InstitutionalMembershipTest {

	private InstitutionalMembership mship;

	@Before
	public void onSetUp() throws Exception {
		
		mship = InstitutionalMembership.NewInstance();
		
		mship.setPerson(new Person("Steve", "Miller", "Mil."));
		GregorianCalendar joined = new GregorianCalendar(1967, 4, 23);
		GregorianCalendar resigned = new GregorianCalendar(1999, 0, 10);
		mship.setPeriod(TimePeriod.NewInstance(joined, resigned));
		mship.setInstitute(Institution.NewInstance());
		mship.setDepartment("Biodiversity");
		mship.setRole("Head");
	}

	@Test
	public void testMembershipInit() {
		Assert.assertNotNull(mship);
	}
}
