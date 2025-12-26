package com.otk.jvm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.otk.jvm.Mapper;

/**
 * Allows you to recognize and locate the {@link Mapper} specification to use in
 * order to implement the target method.
 * 
 * @author olitank
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingsResource {

	String location();

}
