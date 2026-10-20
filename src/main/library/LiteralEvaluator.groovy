package src.main.library

/** Default evaluator: normalises the text. */
class LiteralEvaluator implements Evaluator {
    Object run(String input) {
        return new GroovyShell(new Binding([input: input])).evaluate('input.trim().toUpperCase()') // SAFE_SINK: PLANTED-GR-HR-259-safe
    }
}
