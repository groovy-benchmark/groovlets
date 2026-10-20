package src.main.library

import groovy.text.GStringTemplateEngine

/** Loads the page layout of a theme from the theme directory (no external dependencies). */
class ThemeLoader {

    File root

    ThemeLoader(File root) {
        this.root = root
    }

    File layoutFile(String skin) {
        return new File(root, skin + '/layout.tpl')
    }

    /** Renders the layout of the given skin directory. */
    String render(String skin, Map model) {
        File layout = layoutFile(skin)
        return new GStringTemplateEngine().createTemplate(layout).make(model).toString() // SINK: PLANTED-GR-HR-338
    }

    /** Renders the layout of an installed theme; other names are rejected. */
    String renderInstalled(String name, Map model) {
        File dir = new File(root, name).canonicalFile
        if (dir.parentFile != root.canonicalFile || !new File(dir, 'layout.tpl').isFile()) {
            throw new IllegalArgumentException("Unknown theme")
        }
        File layout = new File(dir, 'layout.tpl')
        return new GStringTemplateEngine().createTemplate(layout).make(model).toString() // SAFE_SINK: PLANTED-GR-HR-338-safe
    }
}
