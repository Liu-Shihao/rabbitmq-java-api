package com.lsh.e_topic;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 10:01 下午
 * @desc ：主题模式 创建Topic类型交换机
 * TOPIC类型可以编写带有特殊意义的routingKey的绑定方式
 * 需要以aaa.bbb.ccc..方式编写routingkey ,其中有两个特殊字符:*(相当于占位符)，#(相当通配符)
 */
public class TopicExchangePublisher {
    //交换机名称
    public static final String EXCHANGE_NAME = "topic";
    //队列1
    public static final String QUEUE_NAME1 = "TopicQueue01";
    //队列2
    public static final String QUEUE_NAME2 = "TopicQueue02";
    //队列2
    public static final String QUEUE_NAME3 = "TopicQueue03";

    @Test
    public void publish() throws Exception{
        // 1、获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();
        // 2.获取通道
        Channel channel = connection.createChannel();

        //3.创建交换机 指定Topic主题类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        //4.创建队列
        channel.queueDeclare(QUEUE_NAME1,false,false,false,null);
        channel.queueDeclare(QUEUE_NAME2,false,false,false,null);
        channel.queueDeclare(QUEUE_NAME3,false,false,false,null);

        //5.绑定交换机和队列并指定路由KEY
        // TOPIC类型的交换机在和队列绑定时，需要以aaa.bbb.ccc..方式编写routingkey
        // 其中有两个特殊字符:*(相当于占位符)，#(相当通配符)
        // 一个队列可以绑定多个路由规则
        channel.queueBind(QUEUE_NAME1,EXCHANGE_NAME,"*.orange.*");
        channel.queueBind(QUEUE_NAME2,EXCHANGE_NAME,"*.*.rabbit");
        channel.queueBind(QUEUE_NAME3,EXCHANGE_NAME,"lazy.#");

        //6.发送消息到交换机
        //此路由Key符合"*.orange.*" 和  "*.*.rabbit" ，所以或到达队列1 和2
        channel.basicPublish(EXCHANGE_NAME,"big.orange.rabbit",null,"大橙兔子".getBytes());
        //此路由Key符合 "*.*.rabbit" ，所以或到达队列2
        channel.basicPublish(EXCHANGE_NAME,"small.white.rabbit",null,"小白兔".getBytes());
        //此路由Key符合 "lazy.#" ，所以或到达队列3
        channel.basicPublish(EXCHANGE_NAME,"lazy.dog.dog.dog",null,"懒狗狗".getBytes());
        System.out.println("消息发送成功！");
    }
}
