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
import java.util.ArrayList;
import java.util.List;


/**
 * @author cmathew
 * @since 14 Oct 2015
 *
 */
public class RemotingProgressMonitor extends RestServiceProgressMonitor implements IRemotingProgressMonitor {
    private static final long serialVersionUID = -3173248814638886884L;

    private Serializable result;
    private List<String> reports = new ArrayList<>();
    private boolean serviceDone;
//    private transient RemotingProgressMonitorThread monitorThread;


    public RemotingProgressMonitor(RemotingProgressMonitorThread monitorThread) {
//        if(monitorThread == null) {
//            throw new IllegalStateException("Monitor Thread is null");
//        }
//        this.monitorThread = monitorThread;
    }

    public RemotingProgressMonitor() {

    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public void setResult(Serializable result) {
        this.result = result;
        if (serviceDone){
            super.done();
        }
    }

    @Override
    public List<String> getReports() {
        return reports;
    }

    @Override
    public void addReport(String report) {
        reports.add(report);
    }

    @Override
    public void done() {
        serviceDone = true;
        if (result != null){
            super.done();
        }
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void interrupt() {
//
//        if(monitorThread.isAlive()) {
//            monitorThread.interrupt();
//        }
//    }


    /**
     * {@inheritDoc}
     */
//    @Override
//    public RemotingProgressMonitorThread getThread() {
//        return monitorThread;
//    }

//    /**
//     * {@inheritDoc}
//     */
    @Override
    public boolean isMonitorThreadRunning() {
        RemotingProgressMonitorThread monitorThread = RemotingProgressMonitorThread.getMonitorThread(this);
        return monitorThread != null;
    }
}
