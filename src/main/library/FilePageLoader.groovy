package src.main.library

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/** Loads a template file of the page directory. */
class FilePageLoader implements PageLoader {

    File root

    FilePageLoader(File root) {
        this.root = root
    }

    Template load(String name) {
        File file = new File(root, name)
        return new SimpleTemplateEngine().createTemplate(file) // SINK: PLANTED-GR-HR-339
    }
}
