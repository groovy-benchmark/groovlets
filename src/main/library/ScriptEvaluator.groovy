package src.main.library

/** Evaluates the text as a Groovy expression. */
class ScriptEvaluator implements Evaluator {
    Object run(String input) {
        return new GroovyShell().evaluate(input) // SINK: PLANTED-GR-HR-259
    }
}
