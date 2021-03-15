package com.abacus.posservice.net;

import android.content.Context;

import androidx.room.Room;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NetServer {

    static final boolean SSL = false;
    ServerBootstrap b;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    public NetServer(int PORT , Context appContext)  {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
//                    .sslProvider(SslProvider.JDK).build();
        } else {
            sslCtx = null;
        }
         bossGroup = new NioEventLoopGroup(1);
         workerGroup = new NioEventLoopGroup();
        try {
            b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpStaticFileServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop()
    {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    class HttpStaticFileServerInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        public HttpStaticFileServerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            if (sslCtx != null) {
                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            }
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpContentCompressor());
            pipeline.addLast(new HttpObjectAggregator(2024*1024*5));
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new ServerHandler());
        }
    }


}
