html.html {
    head {
        title "${data.node} Services"
        link rel:'stylesheet', href:'/style/main.css'
    }
    body {
        div(class:'header') {
            h1 'The Language Applications Grid'
            h2 'An open framework for interoperable NLP web services'
        }
        div(class:'content') {
            h1 "LAPPS services on the ${data.node} node"
            p "Total Services Registered: ${data.totalCount}"
            table {
                thead {
                    th 'ID'
                    th 'Name'
                    th 'Metadata'
                }
                tbody {
                    data.elements.each { e->
                        //String u = "http://api.lappsgrid.org/metadata?id=${e.serviceId}"
                        String u = "/metadata?id=${e.serviceId}"
                        String handler = "window.open('$u', '_self')"
                        tr {
                            td e.serviceId
                            td e.serviceName
                            td {
                                button(onclick:"$handler", 'View')
                            }
                        }
                    }
                }
            }
            String year = new Date().format('yyyy')
            p class:'copyright', "Copyright $year The Language Applications Grid."
        }
    }
}
