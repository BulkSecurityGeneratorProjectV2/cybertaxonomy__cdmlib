package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.dozer.util.mapping.CustomFieldMapperIF;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;
import net.sf.dozer.util.mapping.converters.CustomConverter;
import net.sf.dozer.util.mapping.converters.CustomConverterBase;
import net.sf.dozer.util.mapping.event.DozerEventListener;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * extended version of S�ren Chittka's DozerBeanMapperFactoryBean, allowing other
 * properties to be set.
 * @author S�ren Chittka
 */
public class DozerBeanMapperFactoryBean implements FactoryBean, InitializingBean {

	  private DozerBeanMapper beanMapper;
	  private Resource[] mappingFiles;
	  private List<CustomConverter> customConverters;
	  private Map<String,CustomConverterBase> customConvertersWithId;
	  private List<DozerEventListener> eventListeners;
	  private Map<String, BeanFactory> factories;
	  private CustomFieldMapperIF customFieldMapper;
	  

	  public final void setMappingFiles(final Resource[] mappingFiles) {
	    this.mappingFiles = mappingFiles;
	  }

	  public final void setCustomConverters(final List<CustomConverter> customConverters) {
	    this.customConverters = customConverters;
	  }

	  public final void setEventListeners(final List<DozerEventListener> eventListeners) {
	    this.eventListeners = eventListeners;
	  }

	  public final void setFactories(final Map<String, BeanFactory> factories) {
	    this.factories = factories;
	  }
	  
	  public final void setCustomFieldMapper(final CustomFieldMapperIF customFieldMapper) {
		  this.customFieldMapper = customFieldMapper;
	  }
	  
	  public final void setCustomConvertersWithId(final Map<String,CustomConverterBase> customConvertersWithId) {
		  this.customConvertersWithId = customConvertersWithId;
	  }

	  // ==================================================================================================================================
	  // interface 'FactoryBean'
	  // ==================================================================================================================================
	  public final Object getObject() throws Exception {
	    return this.beanMapper;
	  }
	  public final Class<MapperIF> getObjectType() {
	    return MapperIF.class;
	  }
	  public final boolean isSingleton() {
	    return true;
	  }

	  // ==================================================================================================================================
	  // interface 'InitializingBean'
	  // ==================================================================================================================================
	  public final void afterPropertiesSet() throws Exception {
	    this.beanMapper = new DozerBeanMapper();

	    if (this.mappingFiles != null) {
	      final List<String> mappings = new ArrayList<String>(this.mappingFiles.length);
	      for (Resource mappingFile : this.mappingFiles) {
	        mappings.add(mappingFile.getURL().toString());
	      }
	      this.beanMapper.setMappingFiles(mappings);
	    }
	    if (this.customConverters != null) {
	      this.beanMapper.setCustomConverters(this.customConverters);
	    }
	    if (this.eventListeners != null) {
	      this.beanMapper.setEventListeners(this.eventListeners);
	    }
	    if (this.factories != null) {
	      this.beanMapper.setFactories(this.factories);
	    }
	    
	    if(this.customFieldMapper != null) {
	    	this.beanMapper.setCustomFieldMapper(customFieldMapper);
	    }
	    
	    if(this.customConvertersWithId != null) {
	    	this.beanMapper.setCustomConvertersWithId(customConvertersWithId);
	    }
	  }

	}