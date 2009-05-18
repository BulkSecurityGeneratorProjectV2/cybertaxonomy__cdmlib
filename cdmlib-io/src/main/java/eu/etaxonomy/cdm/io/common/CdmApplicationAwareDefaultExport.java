/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.out.DbExportState;
import eu.etaxonomy.cdm.io.berlinModel.out.IoState;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

@Component("defaultExport")
public class CdmApplicationAwareDefaultExport<T extends IExportConfigurator> implements ICdmExport<T>, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(CdmApplicationAwareDefaultExport.class);

	protected ApplicationContext applicationContext;
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}


	//Constants
	final boolean OBLIGATORY = true; 
	final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	IService service = null;
	
	//different type of stores that are used by the known imports
	Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();

	public CdmApplicationAwareDefaultExport(){
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<TeamOrPersonBase>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.REF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<Specimen>(service));
	}
	
	
	public boolean invoke(IExportConfigurator config){
		if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
			doCheck(config);
			return doExport(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
			return doExport(config);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected <S extends IExportConfigurator> boolean doCheck(S  config){
		boolean result = true;
		System.out.println("Start checking Source ("+ config.getSourceNameString() + ") ...");
		
		//check
		if (config == null){
			logger.warn("CdmExportConfiguration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("CdmExportConfiguration is not valid");
			return false;
		}
		
		//do check for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmIO<S> cdmIo = (ICdmIO<S>)applicationContext.getBean(ioBeanName, ICdmIO.class);
				if (cdmIo != null){
					result &= cdmIo.check(config);
				}else{
					logger.error("cdmIO for class " + (ioClass == null ? "(null)" : ioClass.getSimpleName()) + " was null");
					result = false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					result = false;
			}
		}
		
		//return
		System.out.println("End checking Source ("+ config.getSourceNameString() + ") for export from Cdm");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected <S extends IExportConfigurator>  boolean doExport(S config){
		boolean result = true;
		//validate
		if (config == null){
			logger.warn("Configuration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			return false;
		}
			
		System.out.println("Start export from source '" + config.getSourceNameString() 
				+ "' to destination '" + config.getDestinationNameString() + "'");
		
		//do invoke for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmIO<S> cdmIo = (ICdmIO<S>)applicationContext.getBean(ioBeanName, ICdmIO.class);
				if (cdmIo != null){
					result &= cdmIo.invoke(config, stores);
//					IoState<S> state = null;
//					result &= cdmIo.invoke(state);
				}else{
					logger.error("cdmIO was null");
					result = false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					result = false;
			}
		}
		
		//do invoke for each class
//		for (String ioBean: config.getIoBeans()){
//			try {
//				ICdmIO<S> cdmIo = (ICdmIO<S>)applicationContext.getBean(ioBean, ICdmIO.class);
//				if (cdmIo != null){
//					result &= cdmIo.invoke(config, stores);
//				}else{
//					logger.error("cdmIO was null");
//					result = false;
//				}
//			} catch (Exception e) {
//					logger.error(e);
//					e.printStackTrace();
//					result = false;
//			}
//			
//		}
		
		
		System.out.println("End export from source '" + config.getSourceNameString() 
				+ "' to destination '" + config.getDestinationNameString() + "'");
		return result;
	}
	
	private String getComponentBeanName(Class<ICdmIO> ioClass){
		Component component = ioClass.getAnnotation(Component.class);
		String ioBean = component.value();
		if ("".equals(ioBean)){
			ioBean = ioClass.getSimpleName();
			ioBean = ioBean.substring(0, 1).toLowerCase() + ioBean.substring(1); //make camelcase
		}
		return ioBean;
	}
	
}
