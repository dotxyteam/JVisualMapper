package com.otk.jvm.util;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.otk.jesb.solution.Plan.ExecutionError;
import com.otk.jvm.Mapper;
import com.otk.jvm.annotation.MappingsResource;

/**
 * 
 * Utility class for {@link Mapper}.
 * 
 * @author olitank
 *
 */
public class Mappers {

	/**
	 * @param <M>             The mapper interface.
	 * @param mapperInterface The interface containing the mappings methods.
	 * @return A proxy implementing the provided interface using {@link Mapper}
	 *         instances (specified via the {@link MappingsResource} annotation), or
	 *         using by default the MapStruct (https://mapstruct.org/)
	 *         implementation.
	 */
	@SuppressWarnings("unchecked")
	public static <M> M getMapper(Class<M> mapperInterface) {
		if (!mapperInterface.isInterface()) {
			throw new IllegalStateException("'" + mapperInterface + "' is not an interface");

		}
		return (M) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface },
				getInvocationHandler(mapperInterface));
	}

	private static InvocationHandler getInvocationHandler(Class<?> mapperInterface) {
		return new InvocationHandler() {

			Map<Method, Function<Object[], Object>> internalHandlerCache = new HashMap<Method, Function<Object[], Object>>();

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Function<Object[], Object> internalHandler = internalHandlerCache.get(method);
				if (internalHandler == null) {
					internalHandler = createInternalHandler(method);
					internalHandlerCache.put(method, internalHandler);
				}
				try {
					return internalHandler.apply(args);
				} catch (MappingErroprWrapper e) {
					throw e.getCause();
				}
			}

			Function<Object[], Object> createInternalHandler(Method method) throws IOException {
				MappingsResource resourceAnnotation = method.getAnnotation(MappingsResource.class);
				if (resourceAnnotation == null) {
					if (mapperInterface.getAnnotation(org.mapstruct.Mapper.class) != null) {
						return new Function<Object[], Object>() {

							Object mapper = org.mapstruct.factory.Mappers.getMapper(mapperInterface);

							@Override
							public Object apply(Object[] args) {
								try {
									return method.invoke(mapper, args);
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									throw new MappingErroprWrapper(e);
								}
							}
						};
					}
					throw new IllegalStateException(
							"'" + MappingsResource.class.getName() + "' annotation not found on '" + method + "'");
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
				return new Function<Object[], Object>() {

					Mapper mapper = Mapper.get(sourceClass, targetClass, mapperInterface,
							resourceAnnotation.location());

					@Override
					public Object apply(Object[] args) {
						try {
							return mapper.map(args[0]);
						} catch (ExecutionError e) {
							throw new MappingErroprWrapper(e);
						}
					}
				};

			}
		};
	}

	private static class MappingErroprWrapper extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public MappingErroprWrapper(Throwable cause) {
			super(cause);
		}

	}

}
