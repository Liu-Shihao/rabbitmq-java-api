package com.lsh.f_rpc;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/15 4:14 下午
 * @desc ：RabbitMQ RPC模式  Client端代码 RabbitMQ这种RPC模式一般使用的不多
 * 因为两个服务在交互时，可以尽量做到Client和Server的解耦，通过RabbitMQ进行解耦操作 需要让Client发送消息时，携带两个属性:
 * replyTo告知Server将相应信息放到哪个队列
 * correlationId告知Server发送相应消息时，需要携带位置标示来告知Client响应的信息
 *
 *
 * client：向 rpc_publisher 队列发送请求消息，并监听 rpc_consumer 响应队列的消息
 * server：监听rpc_publisher队列的消息，并向rpc_consumer发送响应消息
 *
 */
public class RpcPublisher {
    //client端发出消息  队列
    public static final String QUEUE_PUBLISHER = "rpc_publisher";
    //server端发出响应 队列
    public static final String QUEUE_CONSUMER = "rpc_consumer";

    @Test
    public void publisher()throws Exception{
        // 1、获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();
        // 2.获取通道
        final Channel channel = connection.createChannel();

        //3. 构建队列
        channel.queueDeclare(QUEUE_PUBLISHER,false,false,false,null);
        channel.queueDeclare(QUEUE_CONSUMER,false,false,false,null);

        //4. 发布消息
        String message = "Hello RPC!";
        final String uuid = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties()
                .builder()
                .replyTo(QUEUE_CONSUMER) //设置监听队列（即Server端的响应消息）
                .correlationId(uuid) //设置UUID
                .build();
        channel.basicPublish("",QUEUE_PUBLISHER,props,message.getBytes());
        System.out.println("消息发送成功！");
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //获取唯一标识ID
                String id = properties.getCorrelationId();
                if (id != null && id.equals(uuid)){
                    //说明是我们发送的请求消息
                    System.out.println("接收到服务端响应："+new String(body, "UTF-8"));
                }
                //手动ACK确认
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        //监听响应队列
        channel.basicConsume(QUEUE_CONSUMER,false,consumer);
        System.in.read();
    }
}
