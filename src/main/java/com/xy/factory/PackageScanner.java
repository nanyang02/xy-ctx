package com.xy.factory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 包扫描的核心是将所有的class文件获取到进行处理
 */
public abstract class PackageScanner {

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
        String rootPackage = packageName;
        // dot -> /
        packageName = packageName.replace('.', '/');
        URL url = Thread.currentThread().getContextClassLoader().getResource(packageName);
        //by Url get standard resource marks
        try {
            assert url != null;
            URI uri = url.toURI();
            File root = new File(uri);
            deal(rootPackage, root);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}