package org.grails.async.factory.future

import grails.async.Promise
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.grails.async.factory.BoundPromise

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

/**
 * A child promise of a {@link FutureTaskPromise}
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@PackageScope
class FutureTaskChildPromise<T> implements Promise<T> {
    final Promise<T> parent
    final Closure<T> callable
    private Collection<FutureTaskChildPromise> failureCallbacks = new ConcurrentLinkedQueue<>()
    private Collection<FutureTaskChildPromise> successCallbacks = new ConcurrentLinkedQueue<>()

    private Promise<T> bound = null
    FutureTaskChildPromise(Promise<T> parent, Closure<T> callable) {
        this.parent = parent
        this.callable = callable
    }

    @Override
    Promise<T> accept(T value) {
        bound = new BoundPromise<T>(callable.call(value))
        return bound
    }

    @Override
    Promise<T> onComplete(Closure callable) {
        def newPromise = new FutureTaskChildPromise(this, callable)
        successCallbacks.add(newPromise)
        return newPromise
    }

    @Override
    Promise<T> onError(Closure callable) {
        def newPromise = new FutureTaskChildPromise(this, callable)
        failureCallbacks.add(newPromise)
        return newPromise

    }

    @Override
    Promise<T> then(Closure callable) {
        return onComplete(callable)
    }

    @Override
    boolean cancel(boolean mayInterruptIfRunning) {
        return false
    }

    @Override
    boolean isCancelled() {
        return false
    }

    @Override
    boolean isDone() {
        return bound != null
    }

    @Override
    T get() throws InterruptedException, ExecutionException {
        if(bound != null) {
            return bound.get()
        }
        else {
            if(parent instanceof FutureTaskPromise) {

                def value = parent.get()
                if(bound == null) {
                    def v = callable.call(value)
                    bound = new BoundPromise<>(v)
                }
                return bound.get()
            }
            else {
                def v = callable.call(parent.get())
                bound = new BoundPromise<>(v)
                return v
            }
        }
    }

    @Override
    T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(bound != null) {
            return bound.get()
        }
        else {
            if(parent instanceof FutureTaskPromise) {
                def value = parent.get(timeout, unit)
                if(bound == null) {
                    def v = callable.call(value)
                    bound = new BoundPromise<>(v)
                }
                return bound.get()
            }
            else {
                def v = callable.call(parent.get(timeout, unit))
                bound = new BoundPromise<>(v)
                return v
            }
        }
    }
}
