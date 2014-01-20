/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.annotations.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;

/**
 *
 * @author etsai
 */
public class ReaderWrapper {
    private final Object readerObj;
    private final HashMap<String, Method> annotatedMethods;
    
    public ReaderWrapper(Class readerClass, Connection conn) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        readerObj= readerClass.getConstructor(new Class<?>[] {Connection.class}).newInstance(conn);
        annotatedMethods= new HashMap<>();
        for(Method it: readerClass.getMethods()) {
            if (it.isAnnotationPresent(Query.class)) {
                String name= ((Query)it.getAnnotation(Query.class)).name();
                if (annotatedMethods.containsKey(name)) {
                    throw new RuntimeException(String.format("Duplicate query name found: %s", name));
                }
                annotatedMethods.put(name, it);
            }
        }
    }
    
    public Object executeQuery(String name, Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!annotatedMethods.containsKey(name)) {
            throw new RuntimeException(String.format("Query name '%s' not found", name));
        }
        return annotatedMethods.get(name).invoke(readerObj, args);
    }
    public Object getReaderObject() {
        return readerObj;
    }
}
