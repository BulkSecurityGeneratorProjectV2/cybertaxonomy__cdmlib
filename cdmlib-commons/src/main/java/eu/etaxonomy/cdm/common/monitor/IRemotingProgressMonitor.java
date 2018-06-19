/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for progress monitor to be used in the context of
 * remoting (spring httpinvoker)
 *
 * @author cmathew
 * @since 14 Oct 2015
 *
 */
public interface IRemotingProgressMonitor extends IRestServiceProgressMonitor {

    /**
     * Returns the result of the monitored job
     * @return result of the monitored job
     */
    public Object getResult();

    /**
     * Sets the result of the monitored job
     * @param result of the monitored job
     */
    public void setResult(Serializable result);

    /**
     * Returns the reports generated by the monitored job
     * @return the reports generated by the monitored job
     */
    public List<String> getReports();

    /**
     * Adds a report generated by the monitored job
     * @param report generated by the monitored job
     */
    public void addReport(String report);



//    public RemotingProgressMonitorThread getThread();
    public boolean isMonitorThreadRunning();

}
