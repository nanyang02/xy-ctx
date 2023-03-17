//package com.xy.web;
//
//import com.xy.stereotype.Component;
//import org.eclipse.jetty.server.Connector;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.server.handler.AbstractHandler;
//import org.eclipse.jetty.servlet.ServletHandler;
//
////import org.eclipse.jetty.util.thread.QueuedThreadPool;
//
///**
// * Class <code>JettyServer</code>
// *
// * @author yangnan 2022/11/21 10:38
// * @since 1.8
// */
//@Component
//public class JettyServer {
//
//    private Server server;
//
//    void initServer(int port) {
//        server = new Server(8080);
//
//        // Create a ServerConnector to accept connections from clients.
////        Connector connector = new ServerConnector(server);
//
//        // Add the Connector to the Server
////        server.addConnector(connector);
//
//        // Set a simple Handler to handle requests/responses.
//        // server.setHandler(new JettyDispatcher());
//    }
//
//    public Server setHandler(AbstractHandler handler) {
//        server.setHandler(handler);
//        return server;
//    }
//
//    // Embedding Servlets
//    public Server embeddingServlets() {
//        // The ServletHandler is a dead simple way to create a context handler
//        // that is backed by an instance of a Servlet.
//        // This handler then needs to be registered with the Server object.
//        ServletHandler handler = new ServletHandler();
//        server.setHandler(handler);
//
//        // Passing in the class for the Servlet allows jetty to instantiate an
//        // instance of that Servlet and mount it on a given context path.
//
//        // IMPORTANT:
//        // This is a raw Servlet, not a Servlet that has been configured
//        // through a web.xml @WebServlet annotation, or anything similar.
//        handler.addServletWithMapping(DispatcherServlet.class, "/*");
//
//        return server;
//    }
//
//    public void runServer(int port) throws Exception {
//        // init port
//        initServer(port);
//        // set one simple handler
////        setHandler(new JettySimpleHandler());
//        // supply servlet deal with get post request
//        embeddingServlets();
//        // Start the Server so it starts accepting connections from clients.
//        server.start();
//    }
//
//    public static void main(String[] args) throws Exception {
//        JettyServer server = new JettyServer();
//        server.runServer(8080);
//    }
//
//}
