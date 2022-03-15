package com.lsh.c_fanout;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 2:07 下午
 * @desc ：
 */
public class TwoConsumer {
    //队列1
    public static final String QUEUE_NAME2 = "subscribe02";
    //队列2
    public static final String QUEUE_NAME1 = "subscribe01";

    @Test
    public void consumer01() throws Exception {
        Connection connection = RabbitMQConnectionUtil.getConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.basicQos(1);
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费者01获取到消息:" + new String(body, "UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(QUEUE_NAME1,false,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }

    @Test
    public void consumer02() throws Exception {
        Connection connection = RabbitMQConnectionUtil.getConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel.basicQos(1);
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费者02获取到消息:" + new String(body, "UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(QUEUE_NAME2,false,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
}
