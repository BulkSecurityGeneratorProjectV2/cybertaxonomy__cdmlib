/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author k.luther
 * @since 19.11.2018
 *
 */
public enum PublishEnum {

    Publish("Publish", "Publish"),
    NotPublish("NotPublish", "Not Publish"),
    InheritFromParent("InheritFromParent", "Inherit from Parent");


    String label;
    String key;

    private PublishEnum(String key, String label){
        this.label = label;
        this.key = key;
    }

    public String getLabel(){
        return label;
    }

    public String getKey(){
        return key;
    }
}