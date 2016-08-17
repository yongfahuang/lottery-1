package me.ele.micservice.routes;

import java.lang.annotation.*;

/**
 * Created by frankliu on 15/9/2.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Controller {
    String controllerKey();
    String viewPath() default "";
}
