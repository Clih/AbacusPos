package com.abacus.posservice.net;


import com.abacus.posservice.api.UserService;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import com.abacus.posservice.bean.ResultBean;
import android.util.Log;
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "ServerHandler";

    ScheduledExecutorService defaultEventExecutor = Executors
            .newScheduledThreadPool(1); //1

    //    static DefaultEventExecutor defaultEventExecutor = new DefaultEventExecutor();
    static public HashMap<String, BaseCall> router = new HashMap<>();
    static PushService pushService = new PushService();

    static {
        router.put("/user", new UserService());
    }

    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        defaultEventExecutor.execute(new Runnable() {
            @Override
            public void run() {
                handleHttpRequest(ctx, msg);
            }
        });
    }


    private void handleHttpRequest(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        String path = queryStringDecoder.path();
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

        boolean isKeepAlive = HttpUtil.isKeepAlive(request);
        HttpUtil.setKeepAlive(response, isKeepAlive);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.SERVER, "Abacus");

        if (path.equals("/push")) {
            try {
                pushService.addUUID(ctx, msg, request, response);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() + " ---- " +HttpResponseStatus.INTERNAL_SERVER_ERROR.toString());
                ByteBufUtil.writeUtf8(response.content(),
                        new ResultBean<Object>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), new Object(),
                                HttpResponseStatus.INTERNAL_SERVER_ERROR.toString() + ex.toString()).toString());
            } finally {
                ReferenceCountUtil.release(msg);
            }
        } else {
            ChannelFuture channelFuture = null;
            try {
                BaseCall call = router.get(path);
                if (call != null) {
                    call.run(request, response);
                } else {
                    ByteBufUtil.writeUtf8(response.content(),
                            new ResultBean<Object>(HttpResponseStatus.NOT_FOUND.code(), new Object(),
                                    HttpResponseStatus.NOT_FOUND.toString()).toString());
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() + " ---- " + HttpResponseStatus.INTERNAL_SERVER_ERROR.toString());
                ByteBufUtil.writeUtf8(response.content(),
                        new ResultBean<Object>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), new Object(),
                                HttpResponseStatus.INTERNAL_SERVER_ERROR.toString() + ex.toString()).toString());
            } finally {
                if (isKeepAlive) {
                    HttpUtil.setContentLength(response, response.content().readableBytes());
                }
                channelFuture = ctx.channel().writeAndFlush(response);
                if (!isKeepAlive && channelFuture != null) {
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
                ReferenceCountUtil.release(msg);
            }
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e == IdleStateEvent.READER_IDLE_STATE_EVENT) {
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG,ctx.channel().toString() + " channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ScheduledFuture scheduledFuture = ctx.channel().attr(PushService.HEART_BEAT).get();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        Log.i(TAG, ctx.channel().toString() + " Inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.e(TAG, cause.getMessage() + " ---- " + ctx.channel().toString());
        ctx.channel().close();
    }
}
