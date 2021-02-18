package com.zyzx.service.api.socket.branch;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zyzx.service.api.socket.model.PacketSocket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 描述:
 *  handler
 *
 *  extends
 *      SimpleChannelInboundHandler<String>
 *      改为
 *      ChannelInboundHandlerAdapter
 *
 * @author yly
 * @create 2021-01-18 16:07
 */


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {


    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

    private final AttributeKey<PacketSocket> firstPacket = AttributeKey.valueOf("firstPacket");
    private final AttributeKey<Integer> lossConnectCount = AttributeKey.valueOf("lossConnectCount");

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            Attribute<PacketSocket> packetAttribute = ctx.channel().attr(firstPacket);
            PacketSocket p = packetAttribute.get();
            String deviceId = "";
            if(p!=null){
                deviceId = p.getId();
            }

            switch (event.state()){
                case READER_IDLE:

                    Attribute<Integer> count = ctx.channel().attr(lossConnectCount);
                    int lossCount = count.get();
                    log.info("设备："+deviceId+"  **已经"+(lossCount+1)*35+"秒未收到客户端的消息了！"+"***state"+ event.state());
                    lossCount++;
                    count.set(lossCount);
                    if (lossCount>3){
                        log.info("关闭这个不活跃通道！");
                        ctx.channel().close();
                    }
                    break;
                case WRITER_IDLE:
                    log.info("设备："+deviceId+"30秒未发送");
                    break;
                case ALL_IDLE:
                    log.info("设备："+deviceId+"30秒未通讯");
                    break;
                default:break;
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        log.info("设备握手" + incoming.remoteAddress() + " in");

        // 记录所有连接
        SocketIO.channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Attribute<PacketSocket> packetAttribute = ctx.channel().attr(firstPacket);
        PacketSocket p = packetAttribute.get();
        if(p!=null){
            // 离线
//            deviceProcessor.updateDeviceOnlineState(p.getDeviceId(),0,ctx.channel().id());
        }
        log.info("退出 : " + ctx.channel().remoteAddress() + " active !"+(p!=null?"设备id"+p.getId():""));
        SocketIO.channels.remove(ctx.channel());
        super.handlerRemoved(ctx);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("client->service"+msg.toString());

        // 每次收到信息重置该参数， 记录是否收到信息
        Attribute<Integer> count = ctx.channel().attr(lossConnectCount);
        count.set(0);

        // 数据包
        PacketSocket packet = JSONObject.toJavaObject(JSON.parseObject(msg.toString()), PacketSocket.class);

//        if(!MD5.checkSignatureDevice(packet.getSignature())){
//            packet.setResultAndMsg(EnumCheck.ERROR.toString(),"签名错误");
//            SocketIO.writeAndFlush(ctx.channel(),packet);
//            ctx.close();
//        }
//
//
//        String clazz = packet.getNamespace().split(":")[2];
//        Object obj = SpringContextHolder.getBean(clazz + packet.getVtype()+"_" + packet.getVersion());
//        Object invoke = obj.getClass().getDeclaredMethod(packet.getAction(), PacketSocket.class).invoke(obj, packet);
//        PacketSocket packetSocket = (PacketSocket)invoke;



        // 1在线  -1异常但不影响使用 -2强制离线
//        if(packetSocket.getState() == -2)ctx.close();

        // 返回值
        SocketIO.writeAndFlush(ctx.channel(),packet);


        // 在线
//        deviceProcessor.updateDeviceOnlineState(packet.getDeviceId(),1,ctx.channel().id());



    }



    /*
     *
     * 覆盖 channelActive 方法 在channel被启用的时候触发 (在建立连接的时候)
     *
     * channelActive 和 channelInActive
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("in : " + ctx.channel().remoteAddress() + " active !");
        super.channelActive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常start***********",cause);
        Attribute<PacketSocket> packetAttribute = ctx.channel().attr(firstPacket);
        PacketSocket p = packetAttribute.get();
        String deviceId;
//        if(p!=null && StringUtils.isNotBlank(deviceId=p.getDeviceId())){
//            deviceProcessor.updateDeviceOnlineState(deviceId,0,ctx.channel().id());
//        }

        super.exceptionCaught(ctx, cause);
        ctx.close();
    }



//    @Autowired
//    public void setDeviceProcessor(DeviceProcessor deviceProcessor) {
//        this.deviceProcessor = deviceProcessor;
//    }

}
