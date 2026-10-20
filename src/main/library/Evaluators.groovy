package src.main.library

class Evaluators {
    static Evaluator forMode(String mode) {
        return (mode == 'script') ? new ScriptEvaluator() : new LiteralEvaluator()
    }
}
