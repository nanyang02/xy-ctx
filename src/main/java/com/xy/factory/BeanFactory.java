package com.xy.factory;

import com.xy.beans.BeanDefine;
import com.xy.beans.BeanGetter;
import com.xy.beans.BeansException;
import com.xy.context.ApplicationContext;
import com.xy.context.BeanConfigure;
import com.xy.context.annotation.*;
import com.xy.stereotype.Component;
import com.xy.stereotype.ComponentScan;
import com.xy.stereotype.Controller;
import com.xy.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 先不做三级缓存,也就是不用AOP增强,采用二级缓存的方式来实现一般的服务类的创建和支持
 * <p>
 * BeanFactory 提供简单的bean的管理. 通过直接注册单例对象或者注册bean的定义对象来实现bean的实例化.
 */
public class BeanFactory {

    // bean容器的上下文
    private ApplicationContext applicationContext;

    // (1个半成品的bean(二级缓存)，1成品的bean{一级缓存})
    // AOP的循环代理 -> 解决代理对象的提前暴露的问题:
    //  正常,将对象进行提前暴露就没有什么问题,但是,如果是一个代理对象则不行
    //      代理对象需要进行处理,有两种情况: 1 已经是代理对象,则直接进行返回, 如果不是但还是需要代理增强,则需要.
    // 本质上讲: 代理对象和实际对象是并存的. 但是最终在缓存中的,要么是真实对象,要么是代理对象, 都有.

    public BeanFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    //一级缓存，成品bean
    Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    //二级缓存，半成品bean
    Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    //三级缓存, bean工厂(代理生成,spring中放 ObjectFactory, 用于生成代理对象) 用于进行对象的增强的处理(比如使用AOP的实现)
    Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    Map<String, BeanDefine> definitions = new HashMap<>(128);

    @SuppressWarnings("all")
    private static <T> T cast(Object o) {
        return (T) o;
    }

    public List<BeanDefine> getDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    /**
     * 对象实例化
     *
     * @param c   字节码
     * @param <T> 对象类型
     * @return 对象
     */
    private static <T> T instance(Class<T> c) throws InstantiationException {
        T o = null;
        try {
            o = c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            throw new InstantiationException("can't instantiation, may be because of dependency inject obj is null, you can replace implements InitializingBean to resolve it");
        }
        return o;
    }

    /**
     * 获取Bean实例对象
     *
     * @param c   对象的字节码
     * @param <T> 需要的类型
     * @return 对象
     */
    public <T> T getBean(Class<T> c) {
        return getBean(c.getName());
    }

    /**
     * 获取Bean实例对象
     *
     * @param name 名称
     * @param <T>  需要的类型
     * @return 对象
     */
    public <T> T getBean(String name) {
        return singletonObjects.containsKey(name) ? cast(singletonObjects.get(name)) :

                singletonFactories.containsKey(name) ? cast(singletonFactories.get(name).getObject()) :

                        definitions.containsKey(name) ? regBeanByDefineAndGet(definitions.get(name), name) :
                                null;
    }

    public <T> T regBeanByDefineAndGet(BeanDefine def, String alias) {
        // 如果原型对象，则放入三级缓存中去创建
        if (def.isPrototype()) {
            singletonFactories.put(alias, cast(doCreateBean(def)));
        } else {
            regSingletonBean(def);
        }

        return getBean(alias);
    }

    public void regSingletonBean(BeanDefine define) {

        // 1 检查一级缓存,如果有这个bean则对其别名,接口进行检查,如果没有则直接映射起来,然后直接返回
        if (define.hasTargetAddMapping(singletonObjects)) return;

        // 2 创建一个bean
        Object bean = doCreateBean(define);

        // 3 添加到一级缓存中
        define.addCache(singletonObjects, bean);
    }

    /**
     * 注册bean实例对象
     *
     * @param b bean 对象
     */
    public void regSingletonBean(Object b) {
        if (null == b) throw new RuntimeException("Warning : BeanFactory.regSingletonBean(obj) -> 'obj' can't null !");
        // 如果有则直接存储
        if (singletonObjects.containsKey(b.getClass().getName())) return;
        if (earlySingletonObjects.containsKey(b.getClass().getName())) return;

        initialBean(b);
        singletonObjects.put(b.getClass().getName(), b);
    }

    public void regSingletonBean(Object b, String alias) {

        if (null == b || null == alias)
            throw new RuntimeException("Warning : BeanFactory.regSingletonBean(obj, alias) -> 'obj' and 'alias' can't null ! Class<" + (null == b ? null : b.getClass().getName()) + "> bean. Alias<" + alias + "> .");

        // 如果有则直接存储
        if (singletonObjects.containsKey(b.getClass().getName()) && !singletonObjects.containsKey(alias)) {
            singletonObjects.put(alias, singletonObjects.get(b.getClass().getName()));
            return;
        }

        if (earlySingletonObjects.containsKey(b.getClass().getName())) {
            if (earlySingletonObjects.containsKey(alias)) {
                singletonObjects.put(alias, earlySingletonObjects.get(alias));
            } else {
                singletonObjects.put(alias, earlySingletonObjects.get(b.getClass().getName()));
            }
            return;
        }

        initialBean(b);
        singletonObjects.put(b.getClass().getName(), b);
        singletonObjects.put(alias, b);
    }

    /**
     * 直接注册代理对象,不用单独的处理,比如mybatis给的mapper就是代理对象
     *
     * @param proxy 代理的bean对象
     * @param name  注册名称
     */
    public void regProxyBean(Object proxy, String name) {
        singletonObjects.put(name, proxy);
    }

    /**
     * 加载bean的配置创建对象
     */
    public void loadConfigBean(final Object obj) {
        Class<?> targetClass = obj.getClass();
        for (Class<?> c : targetClass.getInterfaces()) {
            // 获取到配置需要添加到容器中的bean的创建和初始化
            boolean has = c.isAssignableFrom(BeanConfigure.class);
            if (has) {
                Method[] methods = targetClass.getMethods();
                // 先生成所有的BeanDefine,不然无法进行迭代根据BeanDefine来创建依赖对象
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(Bean.class)) {
                        continue;
                    }
                    BeanDefine bd = new BeanDefine()
                            // 设置configBean
                            .setBeanConfig(obj)
                            // 设置method
                            .setCreateBeanMethod(method);

                    // 设置了别名
                    if (method.isAnnotationPresent(Qualifier.class)) {
                        bd.setAlias(method.getAnnotation(Qualifier.class).value());
                    }
                    // 如果返回类型和创建的实际对象不是一样的class,则认定为接口类型的
                    Class<?> retClass = method.getReturnType();
                    // 返回的是接口类型, bd 中添加一下接口的class
                    if (retClass.isInterface()) {
                        bd.setInterfaceClass(retClass);
                    }

                    definitions.put(retClass.getName(), bd);

                    if (bd.hasAlias()) {
                        definitions.put(bd.getAlias(), bd);
                    }

                    if (bd.hasName()) {
                        definitions.put(bd.getName(), bd);
                    }

                }
                // 此处将所有的 define 都初始化完毕了, 开始进行bean的创建和初始化
                for (BeanDefine bd : definitions.values()) {
                    if (bd.isPrototype()) continue;

                    // 注册单例
                    regSingletonBean(bd);
                }
            }
        }
    }

    /**
     * 创建Bean的核心
     *
     * @param define
     * @return
     */
    private Object doCreateBean(final BeanDefine define) {
        // 通过@Bean创建的bean的创建
        if (define.getCreateBeanMethod() != null) {
            try {
                // 通过调用接口来实现对象的获取
                define.getCreateBeanMethod().setAccessible(true); // 采用匿名内部类的方式创建的接口,必须设置方法的可访问属性
                Object bean = define.getCreateBeanMethod().invoke(define.getCreateBeanConfig());
                define.setTargetClass(bean.getClass());
                // 提前放入二级缓存中
                define.addCache(earlySingletonObjects, bean);

                // 初始化处理
                return beanInitProcess(bean);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        if (define.isPrototype()) {
            return (ObjectFactory) () -> {
                try {
                    // 初始化处理
                    Object bean = instance(define.getTargetClass());
                    return beanInitProcess(bean);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
                return null;
            };
        } else {
            // 通过class直接创建bean
            if (null != define.getTargetClass()) {
                try {
                    Object bean = instance(define.getTargetClass());
                    // 提前暴露二级缓存
                    define.addCache(earlySingletonObjects, bean);
                    // 初始化处理
                    return beanInitProcess(bean);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private Object beanInitProcess(Object bean) {
        // 提供ctx上下文
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
        }

        // 初始化
        initialBean(bean);

        if (bean instanceof InitializingBean) {
            try {
                ((InitializingBean) bean).afterPropertiesSet();
            } catch (Exception e) {
                throw new BeansException(e.getMessage(), e);
            }
        }

        return bean;
    }

    private void parentPropInitial(Object bean, Class<?> c) {

        Class aClass = (Class) c.getGenericSuperclass();

        // 如果不是接口,并且不是超类,就尝试进行填充属性
        if (!aClass.isInterface() && !aClass.getName().equals(Object.class.getName())) {
            // 填充父类的字段属性
            beanPropInitial(bean, aClass.getDeclaredFields());
            // 继续迭代
            parentPropInitial(bean, aClass);
        }
    }

    private Object initialBean(final Object bean) {
        // 处理父对象中的注入
        parentPropInitial(bean, bean.getClass());
        // 填充属性
        beanPropInitial(bean, bean.getClass().getDeclaredFields());
        return bean;
    }

    enum DiType {
        Autowired, BeanGetter;

        static boolean isAutowired(DiType diType) {
            return diType.ordinal() == Autowired.ordinal();
        }

        static boolean isBeanGetter(DiType diType) {
            return diType.ordinal() == BeanGetter.ordinal();
        }
    }

    private void beanPropInitial(Object bean, Field[] declaredFields) {

        // 提前注册依赖，不然BeanGetter获取的时候出现问题
        registDependecyFields(declaredFields);

        // 支持实现了BeanGetter接口的注入和AutoWire注入
        for (Field fd : declaredFields) {

            // BeanGetter
            if (fd.getType().isAssignableFrom(BeanGetter.class)) {
                propInitial(bean, fd, DiType.BeanGetter);
            }

            // Autowired Or Dependency
            if (null != fd.getAnnotation(Autowired.class)
                    || null != fd.getAnnotation(Dependency.class)) {
                propInitial(bean, fd, DiType.Autowired);
            }
        }
    }

    private void registDependecyFields(Field[] declaredFields) {
        for (Field fd : declaredFields) {
            registDependencyField(fd);
        }
    }

    private void registDependencyField(Field fd) {
        // ext Dependency(impl)
        Dependency dependency = fd.getAnnotation(Dependency.class);
        if (null != dependency) {
            // 注册实例
            Class<?> impl = dependency.value();
            if (void.class.isAssignableFrom(impl)) {
                impl = fd.getType();
            }
            if (impl.isInterface()) {
                throw new RuntimeException("Dependency Must A interface Impl Instance. At Field: " + fd.getName());
            }
            if (fd.getType().isInterface()) {
                regBeanDefinition(impl, fd.getType());
            } else {
                regBeanDefinition(impl);
            }
        }
    }

    private void propInitial(Object bean, Field fd, DiType diType) {

        // 填充属性
        try {
            boolean isBg = DiType.isBeanGetter(diType);
            boolean isAw = DiType.isAutowired(diType);
            String signature = isBg ? getBgSignature(fd) : isAw ? getAutowiredSignature(fd) : null;

            // 如果有别名指定，优先使用
            Qualifier qualifier = fd.getAnnotation(Qualifier.class);
            if (null != qualifier && qualifier.value().length() > 0) {
                signature = qualifier.value();
            }

            Object diBean = singletonObjects.getOrDefault(signature, null);
            if (null == diBean) {
                // 从二级缓存中获取半成品
                diBean = earlySingletonObjects.getOrDefault(signature, null);
                // 如果没有bean的define
                if (null == diBean) {

                    if (definitions.containsKey(signature)) {
                        BeanDefine define = definitions.get(signature);

                        // 此处注意，如果直接创建是不合理的，因为解析的签名可能是接口呢
                        if (define.isInterface()) {
                            diBean = singletonObjects.getOrDefault(define.getName(), null);
                            if (null == diBean) {
                                diBean = earlySingletonObjects.getOrDefault(define.getName(), null);
                            }
                        }

                        // 再做判定，如果还是null再创建不迟
                        if (diBean == null) diBean = doCreateBean(define);

                        if (!(diBean instanceof ObjectFactory)) {
                            // 放到一级缓存中去
                            define.addCache(singletonObjects, diBean);
                        }
                    } else {
                        // 如果没有匹配到则失败
                        throw new RuntimeException("No BeanDefine For " + signature);
                    }
                }
            }
            fd.setAccessible(true);
            if (isBg) {
                try {
                    BeanGetter<Object> o1 = new BeanGetter<Object>() {
                        private Object o;

                        @Override
                        public Object get() {
                            return o;
                        }

                        @Override
                        public void set(Object t) {
                            o = t;
                        }
                    };
                    o1.set(getDiBean(diBean));
                    fd.set(bean, o1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (isAw) {
                fd.set(bean, getDiBean(diBean));
            }
        } catch (
                IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Dependency injection Class: " + fd.getName() + " Fail. ");
        }

    }

    private Object getDiBean(Object bean) {
        if (bean instanceof ObjectFactory) {
            return ((ObjectFactory) bean).getObject();
        } else {
            return bean;
        }
    }

    public void beanPropInitial(Object bean) {
        beanPropInitial(bean, bean.getClass().getDeclaredFields());
    }

    public String getBgSignature(Field fd) {
        // 获取定义的签名,然后从里面摘取出对应的数据的类型
        String signature = fd.getGenericType().getTypeName();
        signature = signature.substring(signature.indexOf("<") + 1, signature.length() - 1);
        return signature;
    }

    public String getAutowiredSignature(Field fd) {
        return fd.getType().getName();
    }

    public void regBeanDefinition(Class<?> beanClass, String alias) {
        Scope scope = beanClass.getAnnotation(Scope.class);
        if (definitions.containsKey(alias)) {
            throw new RuntimeException("Has Exists Alias [" + alias + "], please change alias for bean [" + beanClass.getName() + "]");
        }

        if (definitions.containsKey(beanClass.getName())) {
            definitions.put(alias, definitions.get(beanClass.getName()));
        } else {
            definitions.put(alias, new BeanDefine(beanClass)
                    .setAlias(alias)
                    .setPrototype(null != scope && scope.value().ordinal() == ScopeType.prototype.ordinal()));
        }
    }

    public void regBeanDefinition(Class<?> beanClass, Class<?> interfaceClass) {
        Scope scope = beanClass.getAnnotation(Scope.class);
        if (definitions.containsKey(interfaceClass.getName())) return;
        if (definitions.containsKey(beanClass.getName())) {
            BeanDefine beanDefine = definitions.get(beanClass.getName());
            beanDefine.setInterfaceClass(interfaceClass);
            definitions.put(interfaceClass.getName(), beanDefine);
        } else {
            definitions.put(interfaceClass.getName(), new BeanDefine(beanClass)
                    .setInterfaceClass(interfaceClass)
                    .setPrototype(null != scope && scope.value().ordinal() == ScopeType.prototype.ordinal()));
        }
    }

    public void regBeanDefinition(Class<?> beanClass) {
        Scope scope = beanClass.getAnnotation(Scope.class);
        if (definitions.containsKey(beanClass.getName())) return;
        definitions.put(beanClass.getName(), new BeanDefine(beanClass)
                .setPrototype(null != scope && scope.value().ordinal() == ScopeType.prototype.ordinal()));
    }

    public void doScan(String path) {

        PackageScanner scanner = new PackageScanner() {
            @Override
            public void dealClass(Class<?> klass) {
                /*
                 * 不考虑注解加在接口上的情况，大部分不会这样的，需要时再扩展
                 * */
                boolean isComponent = null != klass.getAnnotation(Component.class);
                boolean isController = null != klass.getAnnotation(Controller.class);
                boolean isService = null != klass.getAnnotation(Service.class);

                if (isComponent) {
                    String alias = klass.getAnnotation(Component.class).value();
                    if (alias.length() > 0) {
                        regBeanDefinition(klass, alias);
                    } else {
                        regBeanDefinition(klass);
                    }
                } else if (isController) {
                    String alias = klass.getAnnotation(Controller.class).value();
                    if (alias.length() > 0) {
                        regBeanDefinition(klass, alias);
                    } else {
                        regBeanDefinition(klass);
                    }
                } else if (isService) {
                    Service service = klass.getAnnotation(Service.class);
                    String alias = service.value();
                    boolean mapperInterface = service.ignoreSupper();

                    if (alias.length() > 1) {
                        regBeanDefinition(klass, alias);
                    } else {
                        regBeanDefinition(klass);
                    }

                    // if service -> serviceImpl
                    if (!mapperInterface && klass.getInterfaces().length > 0) {
                        for (Class<?> anInterface : klass.getInterfaces()) {
                            // 前缀相同的则建立关联
                            if (klass.getSimpleName().startsWith(anInterface.getSimpleName())
                                    || klass.getSimpleName().endsWith(anInterface.getSimpleName())) {
                                regBeanDefinition(klass, anInterface);
                            }
                        }
                    }
                }
            }
        };

        scanner.packageScanner(path);
    }

    public void scan(Class<?> c) {
        ComponentScan scan = c.getAnnotation(ComponentScan.class);
        if (null != scan) {
            String value = scan.value();
            if (value.length() > 0) {
                doScan(value);
            } else {
                String[] packages = scan.packages();
                if (packages.length > 0) {
                    for (String pkg : packages) {
                        doScan(pkg);
                    }
                }
            }
        }

        // scan Dependency
        registDependecyFields(c.getDeclaredFields());

    }

    public void onlyScan(String pkg) {
        doScan(pkg);
    }
}
