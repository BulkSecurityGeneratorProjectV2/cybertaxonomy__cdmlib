/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author a.kohlbecker
 * @since Jun 12, 2017
 *
 */
public class TypedEntityReference<T> extends EntityReference {


    private Class<T> type;

    /**
     * @param uuid
     * @param label
     */
    public TypedEntityReference(Class<T> type, UUID uuid, String label) {
        super(uuid, label);
        this.type = type;
    }

    public TypedEntityReference(Class<T> type, UUID uuid) {
        super(uuid, null);
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(uuid)
                .appendSuper(type.hashCode())
                .hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        try {
            TypedEntityReference other = (TypedEntityReference) obj;
            return uuid.equals(other.uuid) && type.equals(other.type);

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString(){
        return type.getSimpleName() + "#" + uuid;

    }

}