package com.github.fromi.boardgametools.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Transient;

public class Dispatcher implements Observable {
    @Transient
    private final Set<Observer> observers = new HashSet<>();

    @Override
    public void addObserver(Object observer) {
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
        propagatedObservables.forEach(observable -> observable.addObserver(observer));
    }

    protected void dispatch(Object event) {
        observers.forEach(observer -> observer.observe(event));
    }

    @Transient
    private final Set<Observable> propagatedObservables = new HashSet<>();

    protected void propagate(Observable observable) {
        if (observable != null) {
            propagatedObservables.add(observable);
            observers.forEach(observable::addObserver);
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

        private void observe(Object event) {
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
