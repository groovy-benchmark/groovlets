#!/usr/bin/env groovy
@Grab('ch.qos.logback:logback-classic:1.2.3')
@Grab('org.slf4j:slf4j-simple:1.7.25')
import groovy.util.logging.Slf4j
@Grab(group='org.mortbay.jetty', module='jetty-embedded', version='6.1.26')
//import org.mortbay.jetty.Server
//import org.mortbay.jetty.handler.ResourceHandler
import org.mortbay.jetty.servlet.*
import groovy.servlet.*
import org.mortbay.jetty.Server
//import org.mortbay.jetty.servlet.Context

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

def startJetty() {
    def jetty = new Server(8888)

    // Use a DefaultServlet to serve static content from the /style directory.  The
    // DefaultServlet needs to be defined before the GroovyServlet since the
    // GroovyServlet redirects '/'
    def styleContext = new Context(jetty, '/style', Context.SESSIONS)
    styleContext.resourceBase = 'src/main/style'
    styleContext.addServlet(DefaultServlet, '/*')

    def context = new Context(jetty, '/', Context.SESSIONS)
    context.resourceBase = 'src/main/groovy'
    context.addServlet(GroovyServlet, '/*')
//    context.addFilter(ServicesFilter, '/services', 1)
//    context.addFilter(ServicesFilter, '/services/', 1)
    context.addFilter(RedirectFilter, '/', 1)
//    context.addFilter(NotFoundFilter, '/*', 1)

    jetty.start()
}
 
println "Starting Jetty, press Ctrl+C to stop."
startJetty()
return

/** Redirect everything to the /info page. */
class RedirectFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException { /* NOP */ }

    @Override
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest
        HttpServletResponse response = (HttpServletResponse) servletResponse
        response.sendRedirect('http://api.lappsgrid.org/info')
    }

    @Override
    void destroy() { /* NOP */ }
}

class ServicesFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException { /* NOP */ }

    @Override
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse
        response.sendRedirect('http://api.lappsgrid.org/services/index')
    }

    @Override
    void destroy() { /* NOP */ }
}

/**
 * Return a 404 for matching paths. This is used to block access to some items in the
 * resourceBase that we do not want served as Groovlets.
 */
class NotFoundFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException { /* NOP */ }

    @Override
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest
        if (accept(request.pathInfo)) {
            chain.doFilter(servletRequest, servletResponse)
        }
        else {
            HttpServletResponse response = (HttpServletResponse) servletResponse
            response.sendError(404)
        }
    }

    @Override
    void destroy() { /* NOP */ }

    private boolean accept(String path) {
//        if (path.startsWith('/templates')) return false
        if (path.startsWith('/.git')) return false
//        if (path.startsWith('/groovlets')) return false
        return true
    }
}