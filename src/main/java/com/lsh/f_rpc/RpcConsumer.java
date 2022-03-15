package com.lsh.f_rpc;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/15 4:45 下午
 * @desc ：RabbitMQ RPC模式  Server端代码
 * client：向 rpc_publisher 队列发送请求消息，并监听 rpc_consumer 响应队列的消息
 * server：监听rpc_publisher队列的消息，并向rpc_consumer发送响应消息
 */
public class RpcConsumer {

    //client端发出消息  队列
    public static final String QUEUE_PUBLISHER = "rpc_publisher";
    //server端发出响应 队列
    public static final String QUEUE_CONSUMER = "rpc_consumer";
    @Test
    public void consumer01() throws Exception {
        // 1.获得链接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();
        // 2.获得channel
        final Channel channel = connection.createChannel();
        //3.声明队列
        channel.queueDeclare(QUEUE_PUBLISHER, false, false, false, null);
        channel.queueDeclare(QUEUE_CONSUMER, false, false, false, null);
        //4. 监听消息
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("服务端获取到消息:" + new String(body, "UTF-8"));
                String resp = new Date()+":获取到了client发出的请求，这里是响应的信息";
                //获取响应队列
                String respQueueName = properties.getReplyTo();
                //获取UUID
                String uuid = properties.getCorrelationId();
                AMQP.BasicProperties props = new AMQP.BasicProperties()
                        .builder()
                        .correlationId(uuid)
                        .build();

                //将响应信息发送到响应队列
                channel.basicPublish("",respQueueName,props,resp.getBytes());
                //手动确认
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(QUEUE_PUBLISHER,false,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
}
