package com.bitsim.vrpc.server

import io.vertx.core.Vertx
import io.vertx.core.net.NetServer

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
object VertTcpServer  {
    private val vertx: Vertx = Vertx.vertx()
    private val server: NetServer = vertx.createNetServer()

    fun start(port: Int) {
        server
            .connectHandler(VertxTcpServerSocketHandler)
            .listen(port) { result ->
                if (result.succeeded()) {
                    println("TCP server started on port 8888")
                } else {
                    println("Failed to start TCP server: ${result.cause().message}")
                }
            }
    }



    fun stop() {
        server.close { result ->
            if (result.succeeded()) {
                println("TCP server stopped")
            } else {
                println("Failed to stop TCP server: ${result.cause().message}")
            }
        }
    }
}
