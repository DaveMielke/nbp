package org.nbp.calculator;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface FunctionMethod {
  String summary();
}

public abstract class Operations {
  public abstract Class<? extends Function> getFunctionType ();
  public abstract Class<?> getArgumentType ();

  protected Operations () {
  }

  public final static String getSummary (Method method) {
    Annotation annotation = method.getAnnotation(FunctionMethod.class);
    if (annotation == null) return null;
    return ((FunctionMethod)annotation).summary();
  }
}
