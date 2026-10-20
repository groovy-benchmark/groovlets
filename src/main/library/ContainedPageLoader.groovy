package src.main.library

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/** Loads a template file, but only from inside the page directory. */
class ContainedPageLoader implements PageLoader {

    File root

    ContainedPageLoader(File root) {
        this.root = root
    }

    Template load(String name) {
        File file = new File(root, name).canonicalFile
        if (!file.toPath().startsWith(root.canonicalFile.toPath())) {
            throw new IllegalArgumentException("Page outside of the page directory")
        }
        return new SimpleTemplateEngine().createTemplate(file) // SAFE_SINK: PLANTED-GR-HR-339-safe
    }
}
