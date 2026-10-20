package src.main.library

/**
 * @author Keith Suderman
 */
class LoggerFactory {

    static org.slf4j.Logger getLogger() {
        return getLogger('default-logger')
    }

    static org.slf4j.Logger getLogger(Class theClass) {
        return getLogger(theClass.name)
    }

    static org.slf4j.Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name)
    }
}
