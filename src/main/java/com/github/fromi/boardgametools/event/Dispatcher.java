package com.github.fromi.boardgametools.event;

import org.springframework.data.annotation.Transient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Dispatcher implements Observable {
    @Transient
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void observe(Object observer) {
        Method observerMethod = null;
        for (Method method : observer.getClass().getDeclaredMethods()) {
            if (method.getReturnType().equals(Void.TYPE) && method.getParameterCount() == 1) {
                if (observerMethod == null) {
                    observerMethod = method;
                } else {
                    throw new IllegalArgumentException("Cannot register object, at least 2 eligible methods found for event handling");
                }
            }
        }
        if (observerMethod == null) {
            throw new IllegalArgumentException("Cannot register object, no eligible method found for event handling");
        }
        observers.add(new Observer(observer, observerMethod));
        propagatedObservables.forEach(observable -> observable.observe(observer));
    }

    protected void dispatch(Object event) {
        observers.forEach(observer -> observer.handle(event));
    }

    @Transient
    private final List<Observable> propagatedObservables = new ArrayList<>();

    protected void propagate(Observable observable) {
        if (observable != null) {
            propagatedObservables.add(observable);
            observers.forEach(observable::observe);
        }
    }

    protected void propagate(Observable... observables) {
        for (Observable observable : observables) {
            propagate(observable);
        }
    }

    protected void propagate(Collection<? extends Observable> observables) {
        observables.forEach(this::propagate);
    }

    private static class Observer {

        private final Object object;
        private final Method method;
        private final Class<?> type;

        private Observer(Object object, Method method) {
            this.object = object;
            this.method = method;
            type = method.getParameterTypes()[0];
        }

        private void handle(Object event) {
            if (type.isAssignableFrom(event.getClass())) {
                method.setAccessible(true);
                try {
                    method.invoke(object, event);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                } finally {
                    method.setAccessible(false);
                }
            }
        }
    }
}
