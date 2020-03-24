package com.llewkcor.ares.commons.connect.mongodb;

import org.bson.Document;

public interface MongoDocument<T> {
    /**
     * Creates a new instance of this object from a MongoDB Document
     * @param document MongoDB Document
     * @return T
     */
    T fromDocument(Document document);

    /**
     * Converts this object instance to a MongoDB Document
     * @return MongoDB Document
     */
    Document toDocument();
}
