package com.llewkcor.ares.commons.promise;

public interface Promise<T> {
    /**
     * Object callback
     * @param t Object instance
     */
    void ready(T t);
}
