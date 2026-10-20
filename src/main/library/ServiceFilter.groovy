package src.main.library

/** Post-filters for lists of service records (no external dependencies). */
class ServiceFilter {

    /** Elements matching a caller supplied predicate on the element {@code e}. */
    List applyFilter(List elements, String expression) {
        GroovyShell shell = new GroovyShell()
        Closure predicate = shell.evaluate("{ e -> ${expression} }") // SINK: PLANTED-GR-HR-258
        return elements.findAll(predicate)
    }

    private static final Map FILTER_PREDICATES = [
            id: '{ e -> e.serviceId?.contains(text) }',
            name: '{ e -> e.serviceName?.contains(text) }',
            type: '{ e -> e.serviceType?.contains(text) }',
            domain: '{ e -> e.serviceTypeDomain?.contains(text) }'
    ]

    /** Elements whose selected field contains the given text. */
    List applyFieldFilter(List elements, String field, String text) {
        String source = FILTER_PREDICATES[field]
        if (source == null) {
            return elements
        }
        GroovyShell shell = new GroovyShell(new Binding([text: text]))
        Closure predicate = shell.evaluate(source) // SAFE_SINK: PLANTED-GR-HR-258-safe
        return elements.findAll(predicate)
    }
}
