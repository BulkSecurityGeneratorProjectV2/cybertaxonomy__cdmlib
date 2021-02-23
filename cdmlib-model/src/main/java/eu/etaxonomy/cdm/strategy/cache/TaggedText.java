/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @author m.doering
 * @since 11.12.2007
 */
public class TaggedText implements Serializable{

	private static final long serialVersionUID = -3553949743902449813L;

    private String text;
	private TagEnum type;
	private TypedEntityReference<?> entityReference;


	public static TaggedText NewWhitespaceInstance(){
		return new TaggedText(TagEnum.separator, " ");
	}

    /**
     * @see TagEnum#separator
     */
    public static TaggedText NewSeparatorInstance(String separator){
        return new TaggedText(TagEnum.separator, separator);
    }

    /**
     * @see TagEnum#postSeparator
     */
    public static TaggedText NewPostSeparatorInstance(String separator){
        return new TaggedText(TagEnum.postSeparator, separator);
    }

	public static TaggedText NewInstance(TagEnum type, String text){
	    return new TaggedText(type, text);
	}


    public TaggedText() {
		super();
	}

	public TaggedText(TagEnum type, String text, TypedEntityReference<?> entityReference) {
        super();
        this.text = text;
        this.type = type;
        this.entityReference = entityReference;
    }

	public TaggedText(TagEnum type, String text) {
		this(type, text, null);
	}

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public TagEnum getType() {
        return type;
    }
    public void setType(TagEnum type) {
        this.type = type;
    }

    public TypedEntityReference<?> getEntityReference() {
        return entityReference;
    }
    public void setEntityReference(TypedEntityReference<?> entityReference) {
        this.entityReference = entityReference;
    }

// **************************** TO STRING ***********************************/

	@Override
	public String toString(){
		String result = CdmUtils.concat(":", type.toString(), text);
		if (StringUtils.isBlank(result)){
			return super.toString();
		}else{
			return result;
		}
	}
}