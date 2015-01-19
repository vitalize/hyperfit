package org.hyperfit.methodinfo;

import org.hyperfit.annotation.FirstLink;
import org.hyperfit.resource.HyperResource;
import org.hyperfit.annotation.Data;
import org.hyperfit.annotation.Link;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to store resource metadata
 *
 * @author Carlos Perez
 */
@ToString
@EqualsAndHashCode
public class MethodInfo {

    private static final Set<String> HYPER_RESOURCE_METHODS;

    static {
        Method[] methods = HyperResource.class.getMethods();

        HYPER_RESOURCE_METHODS = new HashSet<String>(methods.length);

        for (Method m : methods) {
            HYPER_RESOURCE_METHODS.add(m.getName());
        }
    }

    //method types. Depending on the type the behavior can vary.
    public enum MethodType {

        EQUALS("equals"),
        HASH_CODE("hashCode"),
        TO_STRING("toString"),
        GET_LINK("getLink"),
        GET_LINKS("getLinks"),
        FROM_HYPER_RESOURCE_CLASS(HYPER_RESOURCE_METHODS);

        private Object nameObj;

        MethodType(Object nameObj) {
            this.nameObj = nameObj;
        }

        public boolean is(String o) {
            return (nameObj instanceof Set ? ((Set) nameObj).contains(o) : o.equals(nameObj));
        }
    }

    private MethodType methodType;
    private final Class<?> returnType;
    private final Type genericReturnType;

    private final Link linkAnnotation;
    private final FirstLink firstLinkAnnotation;
    private final Data dataAnnotation;
    private final Annotation[][] parameterAnnotations;


    /**
     * Builds it from a method
     *
     * @param method
     */
    public MethodInfo(Method method) {

        this.parameterAnnotations = method.getParameterAnnotations();
        //the only way to get the number of arguments from Method :S
        int argsLength = parameterAnnotations.length;

        String methodName = method.getName();

        this.methodType = null;
        if (argsLength == 1) {
            if (MethodType.EQUALS.is(methodName)) {
                this.methodType = MethodType.EQUALS;
            }
        } else if (argsLength == 0) {
            if (MethodType.TO_STRING.is(methodName)) {
                this.methodType = MethodType.TO_STRING;
            } else if (MethodType.HASH_CODE.is(methodName)) {
                this.methodType = MethodType.HASH_CODE;
            }
        }

        if (this.methodType == null) {
            if (MethodType.GET_LINK.is(methodName)) {
                this.methodType = MethodType.GET_LINK;
            } else if (MethodType.GET_LINKS.is(methodName)) {
                this.methodType = MethodType.GET_LINKS;
            } else if (MethodType.FROM_HYPER_RESOURCE_CLASS.is(methodName)) {
                this.methodType = MethodType.FROM_HYPER_RESOURCE_CLASS;
            }
        }

        this.returnType = method.getReturnType();
        this.genericReturnType = method.getGenericReturnType();

        this.dataAnnotation = method.getAnnotation(Data.class);
        this.linkAnnotation = method.getAnnotation(Link.class);
        this.firstLinkAnnotation = method.getAnnotation(FirstLink.class);

    }

    public MethodType getMethodType() {
        return methodType;
    }

    public Type getGenericReturnType() {
        return genericReturnType;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Data getDataAnnotation() {
        return dataAnnotation;
    }

    public Link getLinkAnnotation() {
        return linkAnnotation;
    }

    public FirstLink getFirstLinkAnnotation() {
        return firstLinkAnnotation;
    }

    public Annotation[][] getParameterAnnotations() {
        return parameterAnnotations;
    }

}