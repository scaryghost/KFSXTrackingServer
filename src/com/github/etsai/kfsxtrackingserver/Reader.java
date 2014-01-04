/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.annotations.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashSet;

/**
 *
 * @author etsai
 */
public class Reader {
    private final Object readerObj;
    private final HashSet<Method> annotatedMethods;
    
    public Reader(Class readerClass, Connection conn) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        readerObj= readerClass.getConstructor(new Class<?>[] {Connection.class}).newInstance(conn);
        annotatedMethods= new HashSet<>();
        for(Method it: readerClass.getMethods()) {
            if (it.isAnnotationPresent(Query.class)) {
                annotatedMethods.add(it);
            }
        }
    }
    
    public Object executeQuery(String name, Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for(Method it: annotatedMethods) {
            if(((Query)it.getAnnotation(Query.class)).name().equals(name)) {
                return it.invoke(readerObj, args);
            }
        }
        return null;
    }
}
