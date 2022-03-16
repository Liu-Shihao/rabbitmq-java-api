package com.lsh.g_confirms;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 2:49 下午
 * @desc ：开启消息确认机制
 * 1. 确保消息到达交换机 confirms机制
 *  1.开启confirms: channel.confirmSelect();
 *  2.增加异步回调：channel.addConfirmListener();
 * 2.确保消息从交换机路由到达队列 return机制
 */
public class ConfirmsPublisher {

    public static final String QUEUE_NAME = "confirms";

    @Test
    public void publisher() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建队列 注意此处的durable参数只是控制队列持久化，并不能控制消息持久化
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //4.开启confirms
        channel.confirmSelect();

        //5.设置confirms的异步回调
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                //消息成功发送到交换机 success
                System.out.println("消息成功发送到交换机!");
            }
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                //消息未成功发送到交换机 fail
                System.out.println("消息未成功发送到交换机!");
            }
        });

        //6.设置Return回调，确认消息是否到达队列，需要在发送消息时，设置mandatory参数为true开启Return机制
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //只有消息没有到达指定队列时，才会触发此函数(例如RoutingKey不对导致消息没有投递到对应队列，就会触发该回调函数)
                System.out.println("消息没有到达指定队列!");
            }
        });

        //7.开启消息持久化，如果没有开启消息持久化。如果MQ重启，则消息会丢失
        AMQP.BasicProperties pop = new AMQP.BasicProperties()
                .builder()
                //设置deliveryMode为2表示开启消息持久化，MQ重启后消息不会消失
                .deliveryMode(2)
                .build();

        String message = "Confirms Messaage!";
        //8.发送消息  注意:此处需要设置mandatory参数为true，才能开启Return机制
        channel.basicPublish("","confirms",true,pop,message.getBytes());
        System.out.println("消息发送成功!");
        //read方法阻塞，查看WEB可视化界面的客户端连接数
        System.in.read();

    }
}
