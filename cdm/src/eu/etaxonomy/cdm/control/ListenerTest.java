/**
 * 
 */
package eu.etaxonomy.cdm.control;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public class ListenerTest implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger(ListenerTest.class);
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String str = " Changed from " + evt.getOldValue() + " to " + evt.getNewValue();
		logger.info("Jippie es gibt Veränderung: " + str);

	}

}
