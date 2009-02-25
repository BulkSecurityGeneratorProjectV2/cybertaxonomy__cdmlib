                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        /**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Abstract superclass implementing human annotations and machine markers to be assigned to CDM objects.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatableEntity", propOrder = {
    "markers",
    "annotations"
})
@MappedSuperclass
public abstract class AnnotatableEntity extends VersionableEntity {
	private static final long serialVersionUID = 9151211842542443102L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AnnotatableEntity.class);

	@XmlElementWrapper(name = "Markers")
	@XmlElement(name = "Marker")
	@OneToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected Set<Marker> markers = new HashSet<Marker>();
	
	@XmlElementWrapper(name = "Annotations")
	@XmlElement(name = "Annotation")
	@OneToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected Set<Annotation> annotations = new HashSet<Annotation>();
	
	protected AnnotatableEntity() {
		super();
	}

//*************** MARKER **********************************************
	
	
	public Set<Marker> getMarkers(){
		return this.markers;
	}
	public void addMarker(Marker marker){
		if (marker != null){
			marker.setMarkedObj(this);
			markers.add(marker);
		}
	}
	public void removeMarker(Marker marker){
		marker.setMarkedObj(null);
	}

//*************** ANNOTATIONS **********************************************
	
	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	public void addAnnotation(Annotation annotation){
		if (annotation != null){
			annotation.setAnnotatedObj(this);
			annotations.add(annotation);
		}
	}
	
	public void removeAnnotation(Annotation annotation){
		this.annotations.remove(annotation);
		annotation.setAnnotatedObj(null);
	}
	
//********************** CLONE *****************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		AnnotatableEntity result = (AnnotatableEntity)super.clone();
		
		//Annotations
		result.annotations = new HashSet<Annotation>();
		for (Annotation annotation : this.annotations ){
			Annotation newAnnotation = (Annotation)annotation.clone();
			result.addAnnotation(newAnnotation);
		}
		
		//Markers
		result.markers = new HashSet<Marker>();
		for (Marker marker : this.markers ){
			Marker newMarker = (Marker)marker.clone();
			result.addMarker(newMarker);
		}
		
		//no changes to: -
		return result;
	}	
}
