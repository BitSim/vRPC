package com.bitsim.vrpc.wrapper

import com.bitsim.vrpc.constant.TcpMessageConstant
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.parsetools.RecordParser

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
class VertBufferWrapper(handler: Handler<Buffer>): Handler<Buffer> {
    private val recordParser: RecordParser
    private var flag: Boolean = true
    private var result: Buffer = Buffer.buffer()

    init {
        recordParser = customRecordParser(handler)
    }

    override fun handle(p0: Buffer) {
        recordParser.handle(p0)
    }

    private fun customRecordParser(handler: Handler<Buffer>): RecordParser {
        val parser = RecordParser.newFixed(TcpMessageConstant.HEADER_MESSAGE_LEN)
        parser.setOutput { buffer ->
            if (flag)
                flag = buffer.getInt(TcpMessageConstant.MESSAGE_LEN_INDEX)
                    .also { result.appendBuffer(buffer); parser.fixedSizeMode(it) }.let { false }
            else
                flag = true.also {
                    handler.handle(result.appendBuffer(buffer)); result =
                    Buffer.buffer(); parser.fixedSizeMode(TcpMessageConstant.HEADER_MESSAGE_LEN)
                }
        }
        return parser
    }

}

