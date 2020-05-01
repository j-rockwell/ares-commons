package com.playares.commons.connect.redis;

public interface JsonDocument<T> {
    /**
     * Updates the object with the provided json information
     * @param json Json information
     * @return JsonDocument
     */
    T fromDocument(String json);

    /**
     * Returns the Json form of this document
     * @return Json
     */
    String toDocument();
}
