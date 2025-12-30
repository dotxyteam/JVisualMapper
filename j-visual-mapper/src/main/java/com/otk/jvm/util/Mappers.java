package com.otk.jvm.util;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
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

	public static final BiFunction<Class<?>, Method, Function<Object[], Object>> MAP_STRUCT_FALLBACK_HANDLER = (
			mapperInterface, method) -> {
		try {
			return createMapStructHandler(mapperInterface, method);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	};

	/**
	 * @param <M>                   The mapper interface.
	 * @param mapperInterface       The interface containing the mappings methods.
	 * @param defaultHandlerFactory The function that will be used to create a
	 *                              default mapper.
	 * @return A proxy implementing the provided interface using {@link Mapper}
	 *         instances (specified via the {@link MappingsResource} annotation), or
	 *         using by default the MapStruct (https://mapstruct.org/)
	 *         implementation.
	 */
	@SuppressWarnings("unchecked")
	public static <M> M getMapper(Class<M> mapperInterface,
			BiFunction<Class<?>, Method, Function<Object[], Object>> defaultHandlerFactory) {
		if (!mapperInterface.isInterface()) {
			throw new IllegalStateException("'" + mapperInterface + "' is not an interface");

		}
		return (M) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface },
				getInvocationHandler(mapperInterface, defaultHandlerFactory));
	}

	private static InvocationHandler getInvocationHandler(Class<?> mapperInterface,
			BiFunction<Class<?>, Method, Function<Object[], Object>> defaultHandlerFactory) {
		return new InvocationHandler() {

			Map<Method, Function<Object[], Object>> internalHandlerCache = new HashMap<Method, Function<Object[], Object>>();

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Function<Object[], Object> mappingsHandler = internalHandlerCache.get(method);
				if (mappingsHandler == null) {
					mappingsHandler = createHandler(mapperInterface, method, defaultHandlerFactory);
					internalHandlerCache.put(method, mappingsHandler);
				}
				try {
					return mappingsHandler.apply(args);
				} catch (MappingErroprWrapper e) {
					throw e.getCause();
				}
			}

		};
	}

	private static Function<Object[], Object> createHandler(Class<?> mapperInterface, Method method,
			BiFunction<Class<?>, Method, Function<Object[], Object>> defaultHandlerFactory) throws Exception {
		if (method.getAnnotation(MappingsResource.class) != null) {
			return createStandardHandler(mapperInterface, method);
		}
		if (defaultHandlerFactory == null) {
			throw new IllegalStateException(
					"Cannot implement mapping method: '" + method + "': no fallback mappings handler factory found");
		}
		try {
			return defaultHandlerFactory.apply(mapperInterface, method);
		} catch (RuntimeException e) {
			throw new IllegalStateException(
					"Cannot implement mapping method: '" + method + "': fallback mappings handler creation failed", e);
		}
	}

	private static Function<Object[], Object> createStandardHandler(Class<?> mapperInterface, Method method)
			throws IOException {
		MappingsResource resourceAnnotation = method.getAnnotation(MappingsResource.class);
		if (resourceAnnotation == null) {
			throw new IllegalStateException("Invalid mapping method: '" + method + "': '"
					+ MappingsResource.class.getName() + "' annotation not found");
		}
		if (method.getParameterCount() != 1) {
			throw new IllegalStateException("Invalid mapping method: '" + method
					+ "': Unexpected number of parameters: " + method.getParameterCount() + ": Expected: 1 parameter");
		}
		Class<?> sourceClass = method.getParameterTypes()[0];
		if (method.getReturnType() == void.class) {
			throw new IllegalStateException("Invalid mapping method: '" + method + "': Unexpected 'void' return type");
		}
		Class<?> targetClass = method.getReturnType();
		return new Function<Object[], Object>() {

			Mapper mapper = Mapper.get(sourceClass, targetClass, mapperInterface, resourceAnnotation.location());

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

	private static Function<Object[], Object> createMapStructHandler(Class<?> mapperInterface, Method method)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			InvocationTargetException {
		Class<?> mappersFactoryClass = Class.forName("org.mapstruct.factory.Mappers");
		Method mappersFactoryMethod = mappersFactoryClass.getMethod("getMapper", Class.class);
		return new Function<Object[], Object>() {

			Object mapper = mappersFactoryMethod.invoke(null, mapperInterface);

			@Override
			public Object apply(Object[] args) {
				try {
					return method.invoke(mapper, args);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new MappingErroprWrapper(e);
				} catch (InvocationTargetException e) {
					throw new MappingErroprWrapper(e.getTargetException());
				}
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
