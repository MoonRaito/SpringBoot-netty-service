package com.zyzx.service.api.socket.branch;

import com.alibaba.fastjson.JSONObject;
import com.zyzx.service.api.socket.model.PacketSocket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * 描述:
 *  基本参数
 *
 * @author JSKJ
 * @create 2018-10-19 20:13
 */
public class SocketIO {
    private static final Logger log = LoggerFactory.getLogger(SocketIO.class);


    // 所有在线设备 deviceId + channelId
    public static Map<String, ChannelId> mapChannelIds = new HashMap<>();

    // 所有在线通道
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static void writeAndFlush(Channel ctx, final PacketSocket packetSocket){
        // 返回值
        final String order = JSONObject.toJSONString(packetSocket)+"\r\n";
        ctx.writeAndFlush(order,ctx.newPromise().addListener(
                new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            log.info("service->client:success  "+ order);
                        }else {
                            log.error("service->client:error"+ order
                                    +" send data to client exception occur: ", channelFuture.cause());
                        }
                    }
                }
        ));
    }


    public static void writeAndFlush(String deviceId, final PacketSocket packetSocket){
        if(!mapChannelIds.containsKey(deviceId))return;
        if(channels.isEmpty())return;
        Channel ctx = channels.find(mapChannelIds.get(deviceId));
        if(ctx==null||!ctx.isOpen())return;
        writeAndFlush(ctx,packetSocket);
    }
}



















