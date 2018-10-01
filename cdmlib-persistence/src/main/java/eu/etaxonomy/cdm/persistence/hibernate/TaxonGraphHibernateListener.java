/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
public class TaxonGraphHibernateListener implements PostInsertEventListener, PostUpdateEventListener {

    private static final long serialVersionUID = 5062518307839173935L;

    private static TaxonGraphHibernateListener instance;

    private ITaxonGraphDao taxonGraphDao;

    public void setTaxonGraphDao(ITaxonGraphDao taxonGraphDao){
        this.taxonGraphDao = taxonGraphDao;
    }

    private String[] NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private String[] NOMREF_PROP = new String[]{"nomenclaturalReference"};


    private int checkStateChange(PostUpdateEvent event, String[] propertyNamesToCheck){

        String[] propertyNames = event.getPersister().getPropertyNames();
        Object[] oldState = event.getOldState();
        Object[] state = event.getState();

        int propsCheckedCnt = 0;
        for(int i = 0; i < propertyNames.length; i++){
            if(ArrayUtils.contains(propertyNamesToCheck, propertyNames[i])){
                propsCheckedCnt++;
                if(!oldState[i].equals(state[i])){
                    return i;
                }
                if(propsCheckedCnt == propertyNamesToCheck.length){
                    return -1;
                }
            }
        }
        // this execption should be raised during the unit tests already and thus will never occur in production
        throw new RuntimeException("TaxonName class misses at least one property of: " + ArrayUtils.toString(propertyNamesToCheck));
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if(taxonGraphDao == null){
            return;
        }
        try {
            if(event.getEntity() instanceof TaxonName){
                if(checkStateChange(event, NAMEPARTS_OR_RANK_PROPS) > -1){
                    taxonGraphDao.onNameOrRankChange((TaxonName) event.getEntity());
                }
                int changedNomRefIndex = checkStateChange(event, NOMREF_PROP);
                if(changedNomRefIndex > -1){
                    taxonGraphDao.onNomReferenceChange((TaxonName) event.getEntity(), (Reference)event.getOldState()[changedNomRefIndex]);
                }
            }
        } catch (TaxonGraphException e) {
            Logger.getLogger(this.getClass()).error(e);
        }

    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if(taxonGraphDao == null){
            return;
        }
        try {
            if(event.getEntity() instanceof TaxonName){
                taxonGraphDao.onNewTaxonName((TaxonName) event.getEntity());
            }
        } catch (TaxonGraphException e) {
            Logger.getLogger(this.getClass()).error(e);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

    /**
     * @return
     */
    public static TaxonGraphHibernateListener instance() {
        if(instance == null){
            instance = new TaxonGraphHibernateListener();
        }
        return instance;
    }

}
