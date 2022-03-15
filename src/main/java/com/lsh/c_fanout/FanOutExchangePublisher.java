package com.lsh.c_fanout;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 9:29 下午
 * @desc ：发布订阅模式  publish/subscribe
 * 自行构建交换机并绑定指定队列（FANOUT类型）
 * FANOUT类型交换机Exchange与队列Queue是直接绑定，不需要routingKey
 */
public class FanOutExchangePublisher {

    //交换机名称
    public static final String EXCHANGE_NAME = "pubsub";

    //队列1
    public static final String QUEUE_NAME2 = "subscribe02";

    //队列2
    public static final String QUEUE_NAME1 = "subscribe01";

    @Test
    public void publish() throws Exception{
        //1.获得链接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建channel
        Channel channel = connection.createChannel();

        //3.构建交换机 指定交换机类型为FANOUT
        // 交换机类型：（ DIRECT("direct"), FANOUT("fanout"), TOPIC("topic"), HEADERS("headers");）
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        //4.构建队列(队列名，是否持久，是否排他，是否自动删除，参数)
        channel.queueDeclare(QUEUE_NAME1,false,false,false,null);
        channel.queueDeclare(QUEUE_NAME2,false,false,false,null);

        //5.绑定交换机和队列，使用的是FANOUT类型的交换机，绑定方式是直接绑定，所以routingKey写和不写都是一样的
        channel.queueBind(QUEUE_NAME1,EXCHANGE_NAME,"");
        channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"");

        //6.发送消息到交换机 此处的routingKey没有用
        channel.basicPublish(EXCHANGE_NAME,"",null,"Publish/Subscribe".getBytes());
        System.out.println("消息发送成功！");
    }
}
