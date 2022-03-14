package com.lsh.work;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 2:07 下午
 * @desc ：监听队列 进行消费
 */
public class twoConsumer {
    public static final String QUEUE_NAME = "work";

    @Test
    public void consumer01() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //4.监听队列
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费者01获取到消息:" + new String(body, "UTF-8"));
            }
        };
        //
        channel.basicConsume(QUEUE_NAME,true,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
    @Test
    public void consumer02() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //4.监听队列
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费者02获取到消息:" + new String(body, "UTF-8"));
            }
        };
        //
        channel.basicConsume(QUEUE_NAME,true,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
}
