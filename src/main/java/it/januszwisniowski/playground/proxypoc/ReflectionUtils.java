package it.januszwisniowski.playground.proxypoc;

import com.google.common.base.Suppliers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ReflectionUtils {

    public <T> T deferredInstance(Supplier<T> instanceSupplier, Class<T> tClass) {
        requireNonNull(instanceSupplier);
        requireNonNull(tClass);

        Supplier<T> supplier = Suppliers.memoize(instanceSupplier::get)::get;
        InvocationHandler handler = (proxy, method, args) -> method.invoke(supplier.get(), args);

        return getProxy(handler, tClass);
    }

    public <T> T stubInstance(String unsupportedMessage, Class<T> tClass) {
        requireNonNull(unsupportedMessage);
        requireNonNull(tClass);

        InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
            throw new UnsupportedOperationException(unsupportedMessage);
        };

        return getProxy(handler, tClass);
    }

    private <T> T getProxy(InvocationHandler handler, Class<T> tClass) {
        requireNonNull(handler);
        requireNonNull(tClass);

        T proxy;

        try {
            proxy = (T)
                    Proxy
                            .getProxyClass(tClass.getClassLoader(), tClass)
                            .getConstructor(InvocationHandler.class)
                            .newInstance(handler);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return proxy;
    }
}
