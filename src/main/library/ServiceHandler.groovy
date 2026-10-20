package src.main.library

@GrabConfig(systemClassLoader=true)
@Grab("net.servicegrid:jp.go.nict.langrid.client:1.0.5")
import jp.go.nict.langrid.client.RequestAttributes
@Grab("net.servicegrid:jp.go.nict.langrid.client.soap:1.0.5")
import jp.go.nict.langrid.client.soap.SoapClientFactory
@Grab('org.langrid:jp.go.nict.langrid.service.management_1_2:1.0.10')
import jp.go.nict.langrid.service_1_2.foundation.MatchingCondition
import jp.go.nict.langrid.service_1_2.foundation.Order
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceEntrySearchResult
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceManagementService

class ServiceHandler {

    String url
    String username
    String password
    String node

    def params
    def headers
    def out
    def response
    def html

    Map searchTerms

    public ServiceHandler(def parent) {
        params = parent.params
        headers = parent.headers
        out = parent.out
        response = parent.response
        html = parent.html
        searchTerms = [
                id:'serviceId',
                name: 'serviceName',
                desc: 'serviceDescription',
                type: 'serviceType',
                domain: 'serviceTypeDomain'
        ]
    }

    void handle() {

        // filter, field and contains post-process the result and are not search terms.
        def searchParams = params.findAll { name, value -> !(name in ['filter', 'field', 'contains']) }
        def conditions = new MatchingCondition[searchParams.size()]
        int i = 0
        searchParams.each { name,value ->
            name = searchTerms[name] ?: name
            conditions[i] = new MatchingCondition(name, value, "PARTIAL")
            ++i
        }

        def order = [] as Order[]
        SoapClientFactory f = new SoapClientFactory();
        ServiceManagementService s = f.create(
                ServiceManagementService.class,
                new URL("$url/services/ServiceManagement")
        );
        RequestAttributes attr = (RequestAttributes)s;
        attr.setUserId(username);
        attr.setPassword(password);

        // The data that will be rendered as JSON or passed to the html template.
        Map result = [:]
        result.url = url
        result.totalCount = 0
        result.elements = []

        // The ServiceManager only allows us to fetch metadata for 100 services at a
        // time. So we have to be prepared to page through the entire list if more than
        // 100.
        int PAGE_SIZE = 100
        int count = 0
        ServiceEntrySearchResult more = s.searchServices(count, PAGE_SIZE, conditions, order, "ALL");
        while (more.elements.length > 0) {
            result.elements.addAll more.elements
            count += more.elements.length
            more = s.searchServices(count, PAGE_SIZE, conditions, order, "ALL")
        }
        result.totalCount = count
        if (params.filter) {
            result.elements = new ServiceFilter().applyFilter(result.elements, params.filter)
        }
        else if (params.field) {
            result.elements = new ServiceFilter().applyFieldFilter(result.elements, params.field, params.contains ?: '')
        }
        if (headers.Accept == '*/*' || headers.Accept?.contains('application/json')) {
            response.status = 200
            response.contentType = 'application/json'
            out.println new groovy.json.JsonBuilder(result).toPrettyString()
        }
        else if (headers.Accept?.contains('text/html')) {
            File template = new File('src/main/templates/services.gsp')
            result.node = node
            Binding binding = new Binding()
            binding.html = html
            binding.data = result
            def shell = new GroovyShell(binding)
            def script = shell.parse(template)
            script.run()
        }
        else {
            response.status = 406
            response.addHeader('Accept', 'application/json, text/html')
            out.println "Please request application/json or text/html"
            out.println "Sent Accept: ${headers.Accept}"
        }

    }
}
