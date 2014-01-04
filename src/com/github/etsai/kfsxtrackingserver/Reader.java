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
public class Reader {
    private final Object readerObj;
    private final HashMap<String, Method> annotatedMethods;
    
    public Reader(Class readerClass, Connection conn) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        readerObj= readerClass.getConstructor(new Class<?>[] {Connection.class}).newInstance(conn);
        annotatedMethods= new HashMap<>();
        for(Method it: readerClass.getMethods()) {
            if (it.isAnnotationPresent(Query.class)) {
                annotatedMethods.put(((Query)it.getAnnotation(Query.class)).name(), it);
            }
        }
    }
    
    public Object executeQuery(String name, Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (annotatedMethods.containsKey(name)) {
            annotatedMethods.get(name).invoke(readerObj, args);
        }
        return null;
    }
}
