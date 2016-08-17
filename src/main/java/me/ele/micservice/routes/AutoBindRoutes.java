package me.ele.micservice.routes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jfinal.config.Routes;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by frankliu on 15/9/2.
 */
public class AutoBindRoutes extends Routes {

    private static final Logger logger = LoggerFactory.getLogger(AutoBindRoutes.class);

    private static final String suffix = "Controller";

    private String scanPath = ".";

    public AutoBindRoutes(String scanPath) {
        this.scanPath = scanPath;
    }

    @Override
    public void config() {

        List<Class<? extends com.jfinal.core.Controller>> controllerClasses = null;
        try {
            controllerClasses = extraction(com.jfinal.core.Controller.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Controller controllerbind = null;
        for (Class controller : controllerClasses) {
            controllerbind = (Controller) controller.getAnnotation(Controller.class);
            if (controllerbind == null) {
                this.add(controllerKey(controller), controller);
                logger.debug("routes.add(" + controllerKey(controller) + ", " + controller.getName() + ")");
            } else if (StrKit.isBlank(controllerbind.viewPath())) {
                this.add(controllerbind.controllerKey(), controller);
                logger.debug("routes.add(" + controllerbind.controllerKey() + ", " + controller.getName() + ")");
            } else {
                this.add(controllerbind.controllerKey(), controller, controllerbind.viewPath());
                logger.debug("routes.add(" + controllerbind.controllerKey() + ", " + controller + "," + controllerbind.viewPath() + ")");
            }
        }

    }

    private String controllerKey(Class<com.jfinal.core.Controller> clazz) {
        Preconditions.checkArgument(clazz.getSimpleName().endsWith(suffix),
                " does not has a @Controller annotation and it's name is not end with " + suffix);
        String controllerKey = "/" + StrKit.firstCharToLowerCase(clazz.getSimpleName());
        controllerKey = controllerKey.substring(0, controllerKey.indexOf(suffix));
        return controllerKey;
    }

    private List<String> findFiles(String baseDirName, String targetFileName) {
        /**
         * 算法简述： 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件， 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 队列不空，重复上述操作，队列为空，程序结束，返回结果。
         */
        List<String> classFiles = Lists.newArrayList();
        String tempName = null;
        // 判断目录是否存在
        String classpath = getClass().getClassLoader().getResource("/").getPath();
        File baseDir = new File(classpath + baseDirName);
        if (baseDir.exists() && baseDir.isDirectory()) {
            String[] filelist = baseDir.list();
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(classpath + baseDirName + File.separator + filelist[i]);
                if (readfile.isDirectory()) {
                    classFiles.addAll(findFiles(baseDirName + File.separator + filelist[i], targetFileName));
                } else {
                    tempName = readfile.getName();
                    if (wildcardMatch(targetFileName, tempName)) {
                        String classname;
                        String tem = readfile.getAbsoluteFile().toString().replaceAll("\\\\", "/");
                        classname = tem.substring(tem.indexOf("/classes") + "/classes".length() + 1, tem.indexOf(".class"));
                        classFiles.add(classname.replaceAll("/", "."));
                    }
                }
            }
        }
        return classFiles;
    }

    private boolean wildcardMatch(String pattern, String str) {
        int patternLength = pattern.length();
        int strLength = str.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                // 通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1), str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                // 通配符问号?表示匹配任意一个字符
                strIndex++;
                if (strIndex > strLength) {
                    // 表示str中已经没有字符匹配?了。
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return strIndex == strLength;
    }

    private <T> List<Class<? extends T>> extraction(Class<T> clazz) throws ClassNotFoundException {

        List<String> classFileList = findFiles(scanPath, "*Controller.class");

        List<Class<? extends T>> classList = Lists.newArrayList();
        for (String classFile : classFileList) {
            Class<?> classInFile = Class.forName(classFile);
            if (clazz.isAssignableFrom(classInFile) && clazz != classInFile) {
                classList.add((Class<? extends T>) classInFile);
            }
        }

        return classList;
    }
}
