package com.github.etsai.kfsxtrackingserver;

import java.io.File;
import java.io.IOException;
import java.lang.ClassLoader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

public class JarClassLoader extends URLClassLoader {
    private static final String manifestPath= "META-INF/MANIFEST.MF";

    public JarClassLoader() {
        super(new URL[] {});
    }
    public JarClassLoader(ClassLoader loader) {
        super(new URL[] {}, (ClassLoader)loader);
    }
    
    public void addJar(File jarPath) {
        try {
            JarFile jarFile= new JarFile(jarPath);

            addURL(new URL(String.format("jar:file:%s!/", jarPath)));
            Manifest manifest= jarFile.getManifest();
            if (manifest != null) {
                for(String cp: manifest.getMainAttributes().getValue("Class-Path").split(" ")) {
                    addURL(new URL(String.format("jar:file:%s!/", cp)));
                }
            }
        } catch (IOException ex) {
            Common.logger.log(Level.SEVERE, String.format("Error adding jar: %s", jarPath), ex);
        }
    }
}
