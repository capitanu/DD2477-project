package com.ir22.booksrec;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class HTTPServer {
    public static void start() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/test", new TestHandler());
            server.createContext("/query", new Query());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class Query implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] content = new byte[1024 * 10]; // 10kB
            int byteRead = exchange.getRequestBody().read(content);
            ObjectMapper om = new ObjectMapper();
            QueryRequest request = om.readValue(new String(content), QueryRequest.class);

            // substitute with your business logic here
            System.out.println(request.query);
            System.out.println(Arrays.toString(request.titles));
            QueryResponse resp = new QueryResponse();
            resp.a = "test";
            resp.b = 2;

            OutputStream os = exchange.getResponseBody();
            String respString = om.writeValueAsString(resp);
            exchange.sendResponseHeaders(200, respString.length());
            os.write(respString.getBytes());
            os.close();
        }
    }
}

class QueryRequest {
    public String query;
    public String[] titles;
}

class QueryResponse {
    public String a;
    public int b;
}