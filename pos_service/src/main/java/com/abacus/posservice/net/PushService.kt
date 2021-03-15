package com.abacus.posservice.net

import android.util.Log
import com.abacus.posservice.bean.ResultBean
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.util.AttributeKey
import io.netty.util.concurrent.ScheduledFuture
import java.util.*
import java.util.concurrent.TimeUnit

public class PushService {
    companion object {
        val TAG: String = "PushService"
        @JvmField
        val HEART_BEAT =
            AttributeKey.valueOf<ScheduledFuture<*>>("HeartBeat")
    }

    val CHANNEL_UUID =
        AttributeKey.valueOf<String>("CHANNEL_UUID")
    var hashtable =
        Hashtable<String, Channel>()
    var byteBuf = ByteBufAllocator.DEFAULT.buffer()


    fun PushService() {
        byteBuf.writeInt(0)
    }


    fun addUUID(
        ctx: ChannelHandlerContext,
        msg: Any?,
        request: FullHttpRequest,
        response: FullHttpResponse
    ) {
        val queryStringDecoder = QueryStringDecoder(request.uri())
        val uuid = queryStringDecoder.parameters()["uid"].toString()
        hashtable[uuid] = ctx.channel()
        val cur_uuid = ctx.channel().attr(CHANNEL_UUID).get()
        if (cur_uuid != null) {
        }
        ctx.channel().attr(CHANNEL_UUID).set(uuid)
        response.content().writeInt(0)
        val len = ByteBufUtil.writeUtf8(
            response.content(),
            ResultBean<Any>(Any()).toString()
        )
        response.content().writerIndex(0)
        response.content().writeInt(len)
        response.content().writerIndex(len + 4)
        //        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        ctx.pipeline().remove(HttpContentCompressor::class.java)
        ctx.pipeline().writeAndFlush(response)
            .addListener { ctx.pipeline().remove(HttpServerCodec::class.java) }
        val scheduledFuture =
            ctx.channel().attr(HEART_BEAT).get()
        scheduledFuture?.cancel(true)
        ctx.channel().attr(HEART_BEAT)
            .set(ctx.channel().eventLoop().scheduleAtFixedRate({
                byteBuf.retain()
                ctx.writeAndFlush(byteBuf)
                Log.i(TAG, "writeAndFlush")
            }, 10, 10, TimeUnit.SECONDS))
    }
}