package com.lsh.g_confirms;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 3:41 下午
 * @desc ：监听队列消息，关闭自动ack，手动ack
 */
public class ConfirmsConsumer {

    public static final String QUEUE_NAME = "confirms";

    @Test
    public void consumer() throws Exception {
        //1.获得连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.获得Channel
        final Channel channel = connection.createChannel();

        //3.声明队列，持久化队列 durable为true
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //4.设置CallBack函数
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("监听到队列消息:" + new String(body, "UTF-8"));

                //手动ack
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        //5.监听队列，关闭自动ack
        channel.basicConsume(QUEUE_NAME,false,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
}
