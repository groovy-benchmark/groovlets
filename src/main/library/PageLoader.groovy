package src.main.library

import groovy.text.Template

/** Loads page templates by name (no external dependencies). */
interface PageLoader {
    Template load(String name)
}
