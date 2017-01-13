package org.hyperfit;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Method;

/**
 * Created by btilford on 1/13/17.
 */
public interface DefaultMethodInvoker<T> {

    Object invoke(Class<T> targetType, Method method, Object[] args);

}
