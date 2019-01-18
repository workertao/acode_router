package com.bjx.router.processor;

import com.bjx.annotation.BjxRouter;
import com.bjx.annotation.bean.RouteMeta;
import com.bjx.router.data.Config;
import com.bjx.router.utils.Utils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


import static javax.lang.model.element.Modifier.PUBLIC;
/**
 * Created by yt on 2019/1/14.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.bjx.annotation.BjxRouter"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BjxRouterProcessor extends AbstractProcessor {
    //LOG打印
    private Messager messager;

    //生成文件
    private Filer filer;

    //存放分组和路由数据
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();

    //根路由 <key,value> 对应 <group,apt生成对应的类文件>
    private Map<String, String> rootMap = new HashMap<>();

    //节点工具类 (类、函数、属性都是节点)
    private Elements elementUtils;

    //gradle配置参数，获取moduleName
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "BjxRouterProcessor init");
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Config.ARGUMENTS_NAME);
        }
        messager.printMessage(Diagnostic.Kind.NOTE, ("RouteProcessor Parmaters:" + moduleName));
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set Processor Parmaters.");
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "BjxRouterProcessor process");
//        for (TypeElement typeElement:set){
        //获取所有被BjxRouter注解的元素
        Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(BjxRouter.class);
        if (routeElements == null || routeElements.size() == 0) {
            return false;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "routeElements.size   " + routeElements.size());
        for (Element element : routeElements) {
            //路由信息
            // 使用Route注解的类信息
            TypeMirror tm = element.asType();
            //获取BjxRouter
            BjxRouter bjxRouter = element.getAnnotation(BjxRouter.class);
            //将基本信息构造到RouteMeta中
            RouteMeta routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, element, bjxRouter);
            //将路由地址归类
            classifyOfGroup(routeMeta);
            messager.printMessage(Diagnostic.Kind.NOTE, "BjxRouterProcessor -------" + groupMap.size());
        }
        TypeElement iRouteGroup = elementUtils.getTypeElement(Config.IROUTE_GROUP);
        TypeElement iRouteRoot = elementUtils.getTypeElement(Config.IROUTE_ROOT);
        //构造路由表
        createGroupFile(iRouteGroup);
        //构造根部路由表
        createRootFile(iRouteRoot, iRouteGroup);
        return false;
    }

    //创建路由根部表
    private void createRootFile(TypeElement iRouteRoot, TypeElement iRouteGroup) {
        //类型 Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                )
        );

        //参数 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec rootParamSpec = ParameterSpec.builder(routes, "rootMap")
                .build();
        //函数 public void loadInfo(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder
                ("loadRootMap")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(rootParamSpec);

        //函数体
        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            methodSpec.addStatement("rootMap.put($S, $T.class)", entry
                    .getKey(), ClassName.get(Config.APT_PACKAGE_NAME, entry.getValue()));
        }
        //生成 $Root$类
        String rootClassName = Config.APT_ROOT_CLASS_NAME + moduleName;
        try {
            JavaFile.builder(Config.APT_PACKAGE_NAME,
                    TypeSpec.classBuilder(rootClassName)
                            .addSuperinterface(ClassName.get(iRouteRoot))
                            .addModifiers(PUBLIC)
                            .addMethod(methodSpec.build())
                            .build()
            ).build().writeTo(filer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //创建路由表，分组类的路由信息
    private void createGroupFile(TypeElement iRouteGroup) {
        //构造方法代码
        for (Map.Entry<String, List<RouteMeta>> entry : groupMap.entrySet()) {
            //构造参数 （Map<String,RouteMeta>）
            ParameterizedTypeName params = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouteMeta.class));
            //构造参数（Map<String,RouteMeta> groupMap）
            ParameterSpec parameterSpec = ParameterSpec.builder(params, "groupMap").build();

            //生成方法
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("loadGroupMap")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(parameterSpec);
//                .addStatement("int a = 10")
            List<RouteMeta> routeMetas = entry.getValue();
            for (RouteMeta routeMeta : routeMetas) {
                // 组装函数体:
                methodSpec.addStatement(
                        "groupMap.put($S, $T.build($T.$L,$T.class, $S, $S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase());
            }
            //构造类
            String className = Config.APT_GROUP_CLASS_NAME + entry.getKey();
            TypeSpec clazz = TypeSpec.classBuilder(className)
                    .addSuperinterface(ClassName.get(iRouteGroup))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec.build())
                    .build();
            try {
                JavaFile.builder(Config.APT_PACKAGE_NAME, clazz).build().writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //将路由分组添加到根部路由表中
            rootMap.put(entry.getKey(), className);
        }
    }


    /**
     * 将路由地址归类
     *
     * @param routeMeta 路由信息实体
     *                  最后数据格式如下：
     *                  group:   Agroup    Bgroup
     *                  path ：  A1        B1
     *                  path ：  A2        B2
     *                  path ：  ...       ...
     */
    private void classifyOfGroup(RouteMeta routeMeta) {
        if (checkRouterPath(routeMeta)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Group Info, Group Name = " + routeMeta.getGroup() + ", Path = " +
                    routeMeta.getPath());
            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
            //如果未记录分组则创建
            if (Utils.isEmpty(routeMetas)) {
                List<RouteMeta> routeMetaSet = new ArrayList<>();
                routeMetaSet.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetaSet);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "Group Info Error: " + routeMeta.getPath());
        }
    }

    //校验路由地址
    private boolean checkRouterPath(RouteMeta routeMeta) {
        //获取路由地址
        String path = routeMeta.getPath();
        //获取路由分组
        String group = routeMeta.getGroup();
        //校验路由地址格式
        if (Utils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }
        //校验分组，如果没有设置分组，则取出地址中第一个“/”到第二个“/”的字符作为分组名称
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (Utils.isEmpty(defaultGroup)) {
                return false;
            }
            routeMeta.setGroup(defaultGroup);
            return true;
        }
        return true;
    }
}
