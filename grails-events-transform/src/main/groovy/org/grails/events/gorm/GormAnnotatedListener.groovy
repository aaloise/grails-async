package org.grails.events.gorm

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.ReflectionUtils

/**
 * Marks a class as a synchronous listener of GORM events
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
trait GormAnnotatedListener extends GormAnnotatedSubscriber {
    private static final Logger log = LoggerFactory.getLogger(GormAnnotatedListener)
    /**
     * Whether the listener supports the given event
     * @param event The event
     * @return True if it does
     */
    boolean supports(AbstractPersistenceEvent event) {
        getSubscribedEvents().contains(event.getClass())
    }
    /**
     * Dispatch the event to this listener
     * @param event
     */
    void dispatch(AbstractPersistenceEvent event) {
        for(method in getSubscribedMethods()) {
            if(method.parameterTypes[0].isInstance(event)) {
                try {
                    log.debug("Invoking method [{}] for event [{}]", method, event)
                    ReflectionUtils.invokeMethod(method, this, event)
                } catch (Throwable e) {
                    log.error("Error triggering event [$event] for listener [${method}]: $e.message", e)
                    throw e
                }
            }
        }
    }
}
