package pubsub.demo

import grails.events.annotation.gorm.Listener
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.springframework.stereotype.Component

@Component
class BookChecks {

    @Listener
    void checkBook(PreInsertEvent event) {
        String title = event.getEntityAccess().getPropertyValue("title")
        if(title?.contains("Politics")) {
            println "Books about politics not allowed"
            event.cancel()
        }
    }

    @Listener
    void tagFunnyBooks(PreInsertEvent event) {
        String title = event.getEntityAccess().getPropertyValue("title")
        if(title?.contains("funny")) {
            event.getEntityAccess().setProperty("title", "Humor - ${title}".toString())
        }
    }
}
