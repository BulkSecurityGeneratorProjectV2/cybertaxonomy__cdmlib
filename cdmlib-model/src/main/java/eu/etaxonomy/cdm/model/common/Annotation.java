/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import eu.etaxonomy.cdm.model.agent.Person;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.net.MalformedURLException;
import java.net.URL;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Annotation", propOrder = {
    "commentator",
    "annotatedObj",
    "annotationType",
    "linkbackUrl"
})
@Entity
//@Audited
public class Annotation extends LanguageStringBase implements Cloneable {
	private static final long serialVersionUID = -4484677078599520233L;
	private static final Logger logger = Logger.getLogger(Annotation.class);
	
	
	/**
	 * Factory method.
	 * @param text
	 * @param lang
	 * @return
	 */
	public static Annotation NewInstance(String text, Language lang){
		return new Annotation(text, lang);
	}
	
	/**
	 * Factory method. Using default language.
	 * @param text
	 * @return
	 */
	public static Annotation NewDefaultLanguageInstance(String text){
		return new Annotation(text, Language.DEFAULT());
	}
	
	private Annotation(){
		super();
	}
	
	/**
	 * Constructor
	 * @param text
	 * @param lang
	 */
	protected Annotation(String text, Language language) {
		super(text, language);
	}
	
	
	//Human annotation
	@XmlElement(name = "Commentator")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Person commentator;
	
	@XmlElement(name = "AnnotatedObject")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private AnnotatableEntity annotatedObj;
	
    @XmlElement(name = "AnnotationType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private AnnotationType annotationType;
	
	// for external annotations/comments the URL of these can be set.
	// should be useful to implement trackback, pingback or linkback:
	// http://en.wikipedia.org/wiki/Linkback
	@XmlElement(name = "LinkbackURL")
	private URL linkbackUrl;
	
	/**
	 * Currently envers does not support @Any
	 * @return
	 */
	@Any(metaDef = "CdmBase",
	    	 metaColumn=@Column(name = "annotatedObj_type"),
	    	 fetch = FetchType.LAZY,
	    	 optional = false)
	@JoinColumn(name = "annotatedObj_id")
//	@NotAudited
	public AnnotatableEntity getAnnotatedObj() {
		return annotatedObj;
	}
	public void setAnnotatedObj(AnnotatableEntity newAnnotatedObj) {
		this.annotatedObj = newAnnotatedObj;		
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Person getCommentator(){
		return this.commentator;
	}
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}
	
	@Transient
	public URL getLinkbackUrl() {
		return linkbackUrl;
	}
	public void setLinkbackUrl(URL linkbackUrl) {
		this.linkbackUrl = linkbackUrl;
	}
	
	/**
	 * private get/set methods for Hibernate that allows us to save the URL as strings
	 * @return
	 */
	private String getLinkbackUrlStr() {
		if (linkbackUrl == null){
			return null;
		}
		return linkbackUrl.toString();
	}
	private void setLinkbackUrlStr(String linkbackUrlString) {
		if (linkbackUrlString == null){
			this.linkbackUrl = null;
		}else{
			try {
				this.linkbackUrl = new URL(linkbackUrlString);
			} catch (MalformedURLException e) { //can't be thrown as otherwise Hibernate throws PropertyAccessExceptioin
				logger.warn("Runtime error occurred in setLinkbackUrlStr");
				e.printStackTrace();
			}
		}
	}
	
	
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		Annotation result = (Annotation)super.clone();
		//no changes to: type, flag
		return result;
	}
	
	/**
	 * Clones this annotation and sets the clone's annotated object to 'annotatedObject'
	 * @see java.lang.Object#clone()
	 */
	public Annotation clone(AnnotatableEntity annotatedObject) throws CloneNotSupportedException{
		Annotation result = (Annotation)clone();
		result.setAnnotatedObj(annotatedObject);
		return result;
	}
}