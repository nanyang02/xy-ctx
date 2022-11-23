package com.xy.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 包扫描的核心是将所有的class文件获取到进行处理
 */
public abstract class PackageScanner {

    private static final Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    public abstract void dealClass(Class<?> klass);

    public PackageScanner() {
    }

    private void dealClass(String rootPackage, File file) {
        String fileName = file.getName();//得到一个文件的名字
        if (fileName.endsWith(".class")) {//如果这个文件的后缀名是.class
            fileName = fileName.replace(".class", "");//去掉后缀名
            try {
                Class<?> klass = Class.forName(rootPackage + "." + fileName);
                dealClass(klass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void deal(String rootPackage, File file) {
        // all files and folders
        File[] fds = file.listFiles();
        if (null == fds) return;
        for (File fd : fds) {
            if (fd.isDirectory()) {
                deal(rootPackage + "." + fd.getName(), fd);
            } else if (fd.isFile()) {
                dealClass(rootPackage, fd);
            }
        }
    }

    public void packageScanner(String packageName) {
//        String rootPackage = packageName;
//        // dot -> /
//        packageName = packageName.replace('.', '/');
//        URL url = Thread.currentThread().getContextClassLoader().getResource(packageName);
//        //by Url get standard resource marks
//        try {
//            assert url != null;
//            URI uri = url.toURI();
//            File root = new File(uri);
//            deal(rootPackage, root);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        Set<Class<?>> clzFromPkg = getClzFromPkg(packageName);
        //System.out.println(clzFromPkg);
        clzFromPkg.forEach(this::dealClass);
    }

    /**
     * 从当前已经加载过的里面查找，注意，还没有加载过的，不会出现在这里面，所以不能用于包扫描
     * @param packageName
     */
    public void packageScanner2(String packageName) {
        Field f;
        try {
            f = ClassLoader.class.getDeclaredField("classes");
            f.setAccessible(true);
            Vector<?> classes=(Vector<?>)f.get(ClassLoader.getSystemClassLoader());
            Iterator<?> iterator = classes.iterator();
            List<Class<?>> classList = new ArrayList<>();
            while (iterator.hasNext()) {
                Class<?> next = (Class<?>) iterator.next();
                if(next.getName().startsWith(packageName)) {
                    classList.add(next);
                }
            }
            classList.forEach(this::dealClass);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Class<?> loadClass(String fullClzName) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullClzName);
        } catch (ClassNotFoundException e) {
            logger.error("load class error! clz: {}, e:{}", fullClzName, e);
        }
        return null;
    }

    /**
     * 扫描包路径下的所有class文件
     *
     * @param pkgName 包名
     * @param jar     jar文件
     * @param classes 保存包路径下class的集合
     */
    private static void findClassesByJar(String pkgName, JarFile jar, Set<Class<?>> classes) {
        String pkgDir = pkgName.replace(".", "/");


        Enumeration<JarEntry> entry = jar.entries();

        JarEntry jarEntry;
        String name, className;
        Class<?> claze;
        while (entry.hasMoreElements()) {
            jarEntry = entry.nextElement();

            name = jarEntry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }


            if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
                // 非指定包路径， 非class文件
                continue;
            }


            // 去掉后面的".class", 将路径转为package格式
            className = name.substring(0, name.length() - 6);
            claze = loadClass(className.replace("/", "."));
            if (claze != null) {
                classes.add(claze);
            }
        }
    }

    /**
     * 扫描包路径下的所有class文件
     *
     * @param pkgName 包名
     * @param pkgPath 包对应的绝对地址
     * @param classes 保存包路径下class的集合
     */
    private static void findClassesByFile(String pkgName, String pkgPath, Set<Class<?>> classes) {
        File dir = new File(pkgPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }


        // 过滤获取目录，or class文件
        File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));


        if (dirfiles == null || dirfiles.length == 0) {
            return;
        }


        String className;
        Class clz;
        for (File f : dirfiles) {
            if (f.isDirectory()) {
                findClassesByFile(pkgName + "." + f.getName(),
                        pkgPath + "/" + f.getName(),
                        classes);
                continue;
            }


            // 获取类名，干掉 ".class" 后缀
            className = f.getName();
            className = className.substring(0, className.length() - 6);

            // 加载类
            clz = loadClass(pkgName + "." + className);
            if (clz != null) {
                classes.add(clz);
            }
        }
    }

    /**
     * 扫描包路径下所有的class文件
     *
     * @param pkg
     * @return
     */
    public static Set<Class<?>> getClzFromPkg(String pkg) {
        Set<Class<?>> classes = new LinkedHashSet<>();

        String pkgDirName = pkg.replace('.', '/');
        try {
            Enumeration<URL> urls = PackageScanner.class.getClassLoader().getResources(pkgDirName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {// 如果是以文件的形式保存在服务器上
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");// 获取包的物理路径
                    findClassesByFile(pkg, filePath, classes);
                } else if ("jar".equals(protocol)) {// 如果是jar包文件
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    findClassesByJar(pkg, jar, classes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }
}