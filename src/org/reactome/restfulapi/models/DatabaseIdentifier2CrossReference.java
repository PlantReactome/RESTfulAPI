package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DatabaseIdentifier2CrossReference generated by hbm2java
 */
@XmlRootElement
public class DatabaseIdentifier2CrossReference extends DatabaseObject {

    private long id;

    public DatabaseIdentifier2CrossReference() {
    }

    public DatabaseIdentifier2CrossReference(
            long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
