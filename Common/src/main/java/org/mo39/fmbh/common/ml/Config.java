package org.mo39.fmbh.common.ml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// https://github.com/scala/bug/issues/10134
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

  String description() default "";

}





