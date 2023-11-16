package io.github.binarybeing.hotcat.plugin.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.binarybeing.hotcat.plugin.server.controller.AbstractController;
import io.github.binarybeing.hotcat.plugin.server.controller.ControllerContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.server.dto.StreamResponse;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
@SuppressWarnings("AlibabaThreadPoolCreation")
public class Server {
    public static final Server INSTANCE = new Server();
    private HttpServer httpServer;
    private int port = 17122;

    public void start() {
        ControllerContext.start();
        ExecutorService threadPool = Executors.newFixedThreadPool(20);
        Pair<Integer, HttpServer> serverPair = doCreateServer(port);
        if (serverPair == null) {
            LogUtils.addLog("server start failed");
            return;
        }
        httpServer = serverPair.getRight();
        httpServer.setExecutor(threadPool);
        port = serverPair.getLeft();
        httpServer.createContext("/api", exchange -> {
            AbstractController controller = ControllerContext.get(exchange.getRequestURI().getPath());
            if (controller == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }
            Response response;
            try {
                Request request = Request.formExchange(exchange);
                response = controller.handleRequest(request);
            } catch (Exception e) {
                LogUtils.addLog(exchange.getRequestURI().getPath()+" error: " + e.getMessage());
                response = Response.error(e.getMessage());
            }
            if (response instanceof StreamResponse) {
                streamResp((StreamResponse) response, exchange);
            }

            resp(new Gson().toJson(response), exchange);
        });
        httpServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            httpServer.stop(0);
        }));
    }

    private Pair<Integer, HttpServer> doCreateServer(int port) {
        try {
            return Pair.of(port, HttpServer.create(new InetSocketAddress("127.0.0.1", port), 10));
        } catch (BindException e) {
            LogUtils.addLog("port " + port + " is in use, try to use port " + (port + 1));
            return doCreateServer(port + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private void resp(String res, HttpExchange exchange) throws IOException {
        byte[] respContents = res.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, respContents.length);
        exchange.getResponseBody().write(respContents);
        exchange.close();
    }

    private void streamResp(StreamResponse response, HttpExchange exchange) throws IOException{
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        // set stream response
        exchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
        exchange.sendResponseHeaders(200, 0);
        while (!response.getIsEnd()) {
            try {
                String s = response.getQueue().poll(1, TimeUnit.SECONDS);
                if (StringUtils.isNotBlank(s)) {
                    byte[] respContents = s.getBytes("UTF-8");
                    // 16进制长度
                    exchange.getResponseBody().write(respContents);
                    exchange.getResponseBody().write("\r\n".getBytes());
                    exchange.getResponseBody().flush();
                }
            } catch (Exception e) {
                LogUtils.addLog("streamResp error: " + e.getMessage());
                response.end();
            }
        }
        exchange.close();
    }

    public int getPort() {
        return port;
    }
}
