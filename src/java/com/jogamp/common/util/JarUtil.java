/**
 * Copyright 2011 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package com.jogamp.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.jogamp.common.os.NativeLibrary;

import jogamp.common.Debug;

public class JarUtil {
    private static final boolean DEBUG = Debug.debug("JarUtil");

    /**
     * @param clazzBinName com.jogamp.common.util.cache.TempJarCache 
     * @param cl
     * @return gluegen-rt.jar
     * @throws IOException
     * @see {@link IOUtil#getClassURL(String, ClassLoader)}
     */
    public static String getJarName(String clazzBinName, ClassLoader cl) throws IOException {
        URL url = IOUtil.getClassURL(clazzBinName, cl);
        if(null != url) {
            String urlS = url.toExternalForm();
            // from 
            //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/com/jogamp/common/util/cache/TempJarCache.class
            // to
            //   gluegen-rt.jar
            urlS = urlS.substring(0, urlS.lastIndexOf('!')); // exclude !/
            return urlS.substring(urlS.lastIndexOf('/')+1); // just the jar name
        }
        return null;
    }

    /**
     * @param clazzBinName com.jogamp.common.util.cache.TempJarCache 
     * @param cl
     * @return jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/
     * @throws IOException
     * @see {@link IOUtil#getClassURL(String, ClassLoader)}
     */
    public static URL getJarURL(String clazzBinName, ClassLoader cl) throws IOException {
        URL url = IOUtil.getClassURL(clazzBinName, cl);
        if(null != url) {
            String urlS = url.toExternalForm();
            // from 
            //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/com/jogamp/common/util/cache/TempJarCache.class
            // to
            //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/
            urlS = urlS.substring(0, urlS.lastIndexOf('!')+2); // include !/
            return new URL(urlS);
        }
        return null;
    }

    /**
     *  
     * @param jarURL jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/com/jogamp/common/util/cache/TempJarCache.class
     * @return file:/usr/local/projects/JOGL/gluegen/build-x86_64/
     * @throws IOException
     */
    public static URL getJarURLDirname(URL jarURL) throws IOException {
        String urlS = jarURL.toExternalForm();
        // from 
        //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/com/jogamp/common/util/cache/TempJarCache.class
        // to
        //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar
        urlS = urlS.substring(0, urlS.lastIndexOf('!')); // exclude !/
        
        // from 
        //   jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar
        // to
        //   file:/usr/local/projects/JOGL/gluegen/build-x86_64/        
        urlS = urlS.substring(4, urlS.lastIndexOf('/')+1); // include / exclude jar:
        return new URL(urlS);
    }
            
    /**
     * 
     * @param baseUrl file:/usr/local/projects/JOGL/gluegen/build-x86_64/
     * @param jarFileName gluegen-rt.jar
     * @return jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/
     * @throws IOException
     */
    public static URL getJarURL(URL baseUrl, String jarFileName) throws IOException {
        return new URL("jar:"+baseUrl.toExternalForm()+jarFileName+"!/");
    }
    
    /**
     * 
     * @param clazzBinName com.jogamp.common.util.cache.TempJarCache 
     * @param cl domain 
     * @return JarFile containing the named class within the given ClassLoader
     * @throws IOException
     * @see {@link #getJarURL(String, ClassLoader)}
     */
    public static JarFile getJarFile(String clazzBinName, ClassLoader cl) throws IOException {
            return getJarFile(getJarURL(clazzBinName, cl), cl);
    }

    /**
     * 
     * @param jarURL jar:file:/usr/local/projects/JOGL/gluegen/build-x86_64/gluegen-rt.jar!/
     * @param cl domain
     * @return JarFile as named by URL within the given ClassLoader
     * @throws IOException
     */
    public static JarFile getJarFile(URL jarUrl, ClassLoader cl) throws IOException {
        if(null != jarUrl) {
            URLConnection urlc = jarUrl.openConnection();
            if(urlc instanceof JarURLConnection) {
                JarURLConnection jarConnection = (JarURLConnection)jarUrl.openConnection();
                JarFile jarFile = jarConnection.getJarFile();
                return jarFile;
            }
        }
        return null;
    }
    
    /**
     * Return a map from native-lib-base-name to entry-name.
     */
    public static Map<String, String> getNativeLibNames(JarFile jarFile) {
        if (DEBUG) {
            System.err.println("JarUtil: getNativeLibNames: "+jarFile);
        }

        Map<String,String> nameMap = new HashMap<String, String>();
        Enumeration<JarEntry> entries = jarFile.entries();
        
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();
            final String baseName =  NativeLibrary.isValidNativeLibraryName(entryName, false);
            
            if(null != baseName) {
                nameMap.put(baseName, entryName);
            }
        }

        return nameMap;
    }

    /**
     * Extract the files of the given jar file.
     * <p>
     * If <code>extractNativeLibraries</code> is true,
     * native libraries are added to the given <code>nativeLibMap</code>
     * with the base name to temp file location.<br>
     * A file is identified as a native library,
     * if it's name complies with the running platform's native library naming scheme.<br>
     * Root entries are favored over non root entries in case of naming collisions.<br>
     * Example on a Unix like machine:<br>
     * <pre>
     *   mylib.jar!/sub1/libsour.so   -> sour  (mapped, unique name)  
     *   mylib.jar!/sub1/libsweet.so           (dropped, root entry favored)
     *   mylib.jar!/libsweet.so       -> sweet (mapped, root entry favored)
     *   mylib.jar!/sweet.dll         ->       (dropped, not a unix library name)
     * </pre>
     * </p>
     * <p>
     * In order to be compatible with Java Web Start, we need
     * to extract all root entries from the jar file.<br>
     * In this case, set all flags to true <code>extractNativeLibraries </code>.
     * <code>extractClassFiles</code>, <code>extractOtherFiles</code>.
     * </p>
     * 
     * @param dest
     * @param nativeLibMap
     * @param jarFile
     * @param deepDirectoryTraversal
     * @param extractNativeLibraries
     * @param extractClassFiles
     * @param extractOtherFiles
     * @return
     * @throws IOException
     */
    public static final int extract(File dest, Map<String, String> nativeLibMap, 
                                    JarFile jarFile,
                                    boolean extractNativeLibraries,
                                    boolean extractClassFiles,
                                    boolean extractOtherFiles) throws IOException {

        if (DEBUG) {
            System.err.println("JarUtil: extract: "+jarFile.getName()+" -> "+dest+
                               ", extractNativeLibraries "+extractNativeLibraries+
                               ", extractClassFiles "+extractClassFiles+
                               ", extractOtherFiles "+extractOtherFiles);
        }
        int num = 0;

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryName = entry.getName();

            // Match entries with correct prefix and suffix (ignoring case)
            final String libBaseName = NativeLibrary.isValidNativeLibraryName(entryName, false);
            final boolean isNativeLib = null != libBaseName;
            if(isNativeLib && !extractNativeLibraries) {
                if (DEBUG) {
                    System.err.println("JarUtil: JarEntry : " + entryName + " native-lib skipped");
                }
                continue;
            }
            
            final boolean isClassFile = entryName.endsWith(".class");
            if(isClassFile && !extractClassFiles) {
                if (DEBUG) {
                    System.err.println("JarUtil: JarEntry : " + entryName + " class-file skipped");
                }
                continue;
            }
            
            if(!isNativeLib && !isClassFile && !extractOtherFiles) {
                if (DEBUG) {
                    System.err.println("JarUtil: JarEntry : " + entryName + " other-file skipped");
                }
                continue;
            }

            final boolean isDir = entryName.endsWith("/");
            
            final boolean isRootEntry = entryName.indexOf('/') == -1 && 
                                        entryName.indexOf(File.separatorChar) == -1;
            
            if (DEBUG) {
                System.err.println("JarUtil: JarEntry : isNativeLib " + isNativeLib + 
                                   ", isClassFile " + isClassFile + ", isDir " + isDir +
                                   ", isRootEntry " + isRootEntry );
            }
            
            final File destFile = new File(dest, entryName);
            if(isDir) {
                if (DEBUG) {
                    System.err.println("JarUtil: MKDIR: " + entryName + " -> " + destFile );
                }
                destFile.mkdir();
            } else {
                final File destFolder = new File(destFile.getParent());
                if(!destFolder.exists()) {
                    if (DEBUG) {
                        System.err.println("JarUtil: MKDIR (parent): " + entryName + " -> " + destFolder );
                    }                    
                    destFolder.mkdir();
                }
                final InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                final OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
                int numBytes = -1; 
                try {
                    numBytes = IOUtil.copyStream2Stream(in, out, -1);
                } finally {
                    in.close();
                    out.close();
                }
                boolean addedAsNativeLib = false;
                if (numBytes>0) {
                    num++;
                    if (isNativeLib && ( isRootEntry || !nativeLibMap.containsKey(libBaseName) ) ) {                    
                        nativeLibMap.put(libBaseName, destFile.getAbsolutePath());
                        addedAsNativeLib = true;
                    }
                }
                if (DEBUG) {
                    System.err.println("JarUtil: EXTRACT["+num+"]: [" + libBaseName + " -> ] " + entryName + " -> " + destFile + ": "+numBytes+" bytes, addedAsNativeLib: "+addedAsNativeLib);
                }
            }
        }
        return num;
    }

    /**
     * Validate the certificates for each native Lib in the jar file.
     * Throws an IOException if any certificate is not valid.
     * <pre>
        Certificate[] rootCerts = Something.class.getProtectionDomain().
                                        getCodeSource().getCertificates();
       </pre>
     */
    public static final void validateCertificates(Certificate[] rootCerts, JarFile jarFile) 
            throws IOException, SecurityException {

        if (DEBUG) {
            System.err.println("JarUtil: validateCertificates: "+jarFile.getName());
        }

        if (rootCerts == null || rootCerts.length == 0) {
            throw new IllegalArgumentException("Null certificates passed");
        }

        byte[] buf = new byte[1024];
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if( ! entry.isDirectory() && ! entry.getName().startsWith("META-INF/") ) {
                // only validate non META-INF and non directories
                validateCertificate(rootCerts, jarFile, entry, buf);
            }
        }
    }

    /**
     * Check the certificates with the ones in the jar file
     * (all must match).
     */
    private static final void validateCertificate(Certificate[] rootCerts, 
            JarFile jar, JarEntry entry, byte[] buf) throws IOException, SecurityException {

        if (DEBUG) {
            System.err.println("JarUtil: validate JarEntry : " + entry.getName());
        }

        // API states that we must read all of the data from the entry's
        // InputStream in order to be able to get its certificates

        InputStream is = jar.getInputStream(entry);
        while (is.read(buf) > 0) { }
        is.close();

        // Get the certificates for the JAR entry
        Certificate[] nativeCerts = entry.getCertificates();
        if (nativeCerts == null || nativeCerts.length == 0) {
            throw new SecurityException("no certificate for " + entry.getName() + " in " + jar.getName());
        }

        int checked = 0;
        for (int i = 0; i < rootCerts.length; i++) {
            for (int j = 0; j < nativeCerts.length; j++) {
                if (nativeCerts[j].equals(rootCerts[i])){
                    checked++;
                    break;
                }
            }
        }
        if( checked != rootCerts.length ) {
            throw new SecurityException("not all certificates match, only "+checked+" out of "+rootCerts.length+" for " + entry.getName() + " in " + jar.getName());
        }
    }
}
