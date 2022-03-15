package com.lsh.d_direct;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 9:50 下午
 * @desc ：生产者:在绑定Exchange和Queue时，需要指定好routingKey，同时在发送消息时，也指定routingKey，只有routingKey一致时，才会把指定的消息路由到 指定的Queue
 */
public class DirectExchangePublisher {
    //交换机名称
    public static final String EXCHANGE_NAME = "routing";
    //队列1
    public static final String QUEUE_NAME1 = "routing01";
    //队列2
    public static final String QUEUE_NAME2 = "routing02";

    @Test
    public void publish() throws Exception{
        Connection connection = RabbitMQConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        //创建交换机 指定DIRECT直接类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        //创建队列
        channel.queueDeclare(QUEUE_NAME1,false,false,false,null);
        channel.queueDeclare(QUEUE_NAME2,false,false,false,null);

        //绑定交换机和队列并指定路由KEY
        channel.queueBind(QUEUE_NAME1,EXCHANGE_NAME,"ORANGE");
        //  将两个队列和一个交换机绑定
        channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"BLACK");
        channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"GREEN");

        //发送消息到交换机
        //此消息会到达队列1
        channel.basicPublish(EXCHANGE_NAME,"ORANGE",null,"橙子".getBytes());
        //此消息会到达队列2
        channel.basicPublish(EXCHANGE_NAME,"BLACK",null,"小黑狗".getBytes());
        //此消息没有对应的routingKey，所以会被丢弃
        channel.basicPublish(EXCHANGE_NAME,"WHITE",null,"小白兔".getBytes());
        System.out.println("消息发送成功！");
    }
}
