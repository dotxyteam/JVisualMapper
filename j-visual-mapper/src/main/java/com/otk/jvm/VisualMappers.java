package com.otk.jvm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mapstruct.factory.Mappers;

import com.otk.jvm.annotation.VisualMappingsResource;

public class VisualMappers {

	@SuppressWarnings("unchecked")
	public static <M> M getMapper(Class<M> mapperInterface) {
		return (M) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface },
				getInvocationHandler(mapperInterface));
	}

	private static InvocationHandler getInvocationHandler(Class<?> mapperInterface) {
		return new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				VisualMappingsResource resourceAnnotation = method.getAnnotation(VisualMappingsResource.class);
				if (resourceAnnotation == null) {
					if (mapperInterface.getAnnotation(org.mapstruct.Mapper.class) != null) {
						return method.invoke(Mappers.getMapper(mapperInterface), args);
					}
					throw new IllegalStateException("'" + VisualMappingsResource.class.getName()
							+ "' annotation not found on '" + method + "'");
				}
				if (method.getParameterCount() != 1) {
					throw new IllegalStateException(
							"Invalid mapping method: '" + method + "': Unexpected number of parameters: "
									+ method.getParameterCount() + ": Expected: 1 parameter");
				}
				Class<?> sourceClass = method.getParameterTypes()[0];
				if (method.getReturnType() == void.class) {
					throw new IllegalStateException(
							"Invalid mapping method: '" + method + "': Unexpected 'void' return type");
				}
				Class<?> targetClass = method.getReturnType();
				Mapper mapper = Mapper.get(sourceClass, targetClass, mapperInterface, resourceAnnotation.location());
				Object source = args[0];
				Object target = mapper.map(source);
				return target;
			}
		};
	}

}
