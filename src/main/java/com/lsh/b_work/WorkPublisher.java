package com.lsh.b_work;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 12:29 下午
 * @desc ：生产者和Hello World的形式是一样的，都是将消息推送到默认交换机。
 */
public class WorkPublisher {

    public static final String QUEUE_NAME = "work";

    /**
     * 发送消息
     */
    @Test
    public void publisher() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //4.发送消息

        for (int i = 0; i < 10; i++) {
            //默认交换机 "" ； 默认路由为队列名
            String message = "Work Queue :"+i;
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        }
        System.out.println("消息发送成功!");
        //read方法阻塞，查看WEB可视化界面的客户端连接数
        System.in.read();
    }
}