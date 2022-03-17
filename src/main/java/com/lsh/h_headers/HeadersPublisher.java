package com.lsh.h_headers;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/17 4:14 下午
 * @desc ：Headers类型交换机
 */
public class HeadersPublisher {

    public static final String EXCHANGE_NAME = "headers-exchange";
    public static final String QUEUE_NAME = "headers-queue";


    @Test
    public void publisher() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建Headers类型交换机，创建队列并绑定
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);
         Map<String, Object> arguments = new HashMap<>();
         //x-match 设置为all：表示所有条件都满足才能路由，any：表示有一个条件满足就可以路由
        arguments.put("x-match","all");
//        arguments.put("x-match","any");
        arguments.put("name","jack");
        arguments.put("age","23");
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);

        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,"",arguments);

        HashMap<String, Object> headers = new HashMap<>();
        headers.put("name","jack");
        headers.put("age","23");
        //4.发送消息
        String msg = "这是Headers类型消息";
        AMQP.BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .headers(headers)
                .build();
        channel.basicPublish(EXCHANGE_NAME,"",properties,msg.getBytes());
        System.out.println("消息已发送");
        System.in.read();
    }
}
