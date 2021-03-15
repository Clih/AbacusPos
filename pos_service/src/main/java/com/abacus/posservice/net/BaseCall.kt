package com.abacus.posservice.net

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse


public interface BaseCall {

    @Throws(Exception::class)
    fun run(
        fullHttpRequest: FullHttpRequest?,
        fullHttpResponse: FullHttpResponse?
    )
}