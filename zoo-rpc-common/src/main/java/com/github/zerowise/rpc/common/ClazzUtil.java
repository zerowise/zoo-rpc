package com.github.zerowise.rpc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 ** @createtime : 2018/10/23 3:17 PM
 **/
public class ClazzUtil {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String CLASS_SUFFIX = ".class";

    private static final Pattern INNER_PATTERN = java.util.regex.Pattern.compile("\\$(\\d+).", java.util.regex.Pattern.CASE_INSENSITIVE);

    public static Set<Class<?>> findCandidateComponents(String packageName) throws IOException {
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        Map<String, String> classMap = new HashMap<>(32);
        String path = packageName.replace(".", "/");
        Enumeration<URL> urls = findAllClassPathResources(path);
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                String file = URLDecoder.decode(url.getFile(), "UTF-8");
                File dir = new File(file);
                if (dir.isDirectory()) {
                    parseClassFile(dir, packageName, classMap);
                } else {
                    throw new IllegalArgumentException("file must be directory");
                }
            } else if ("jar".equals(protocol)) {
                parseJarFile(url, classMap);
            }
        }

        Set<Class<?>> set = new HashSet<>(classMap.size());

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        for (String key : classMap.keySet()) {
            String className = classMap.get(key);
            try {
                set.add(classloader.loadClass(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return set;
    }

    private static void parseClassFile(File dir, String packageName, Map<String, String> classMap) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                parseClassFile(file, packageName, classMap);
            }
        } else if (dir.getName().endsWith(CLASS_SUFFIX)) {
            String name = dir.getPath();
            name = name.substring(name.indexOf("classes") + 8).replace("\\", ".");
            addToClassMap(name, classMap);
        }
    }

    private static void parseJarFile(URL url, Map<String, String> classMap) throws IOException {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            if (name.endsWith(CLASS_SUFFIX)) {
                addToClassMap(name.replace("/", "."), classMap);
            }
        }
    }

    private static boolean addToClassMap(String name, Map<String, String> classMap) {

        if (INNER_PATTERN.matcher(name).find()) { //过滤掉匿名内部类
            System.out.println("anonymous inner class:" + name);
            return false;
        }
        System.out.println("class:" + name);
        if (name.indexOf("$") > 0) { //内部类
            System.out.println("inner class:" + name);
        }
        if (!classMap.containsKey(name)) {
            classMap.put(name, name.substring(0, name.length() - 6)); //去掉.class
        }
        return true;
    }

    private static Enumeration<URL> findAllClassPathResources(String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
        return urls;
    }
}