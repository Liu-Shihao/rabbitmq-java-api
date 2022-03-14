package com.lsh;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/14 12:22 下午
 * @desc ：RabbitMQ 连接工具类
 * 获取RabbitMQ连接Connection
 */
public class RabbitMQConnectionUtil {

    public static final String RABBIT_HOST = "172.16.98.100";
    public static final int RABBIT_PORT = 5672;
    public static final String RABBIT_USERNAME = "admin";
    public static final String RABBIT_PWD = "admin";
    public static final String RABBIT_VIRTUAL_HOST = "/";

    /**
     * 获取连接对象
     * @return
     * @throws Exception
     */
    public static Connection getConnection () throws Exception{
        //1. 创建Connection工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();

        //2. 设置RabbitMQ的连接信息
        connectionFactory.setHost(RABBIT_HOST);
        connectionFactory.setPort(RABBIT_PORT);
        connectionFactory.setUsername(RABBIT_USERNAME);
        connectionFactory.setPassword(RABBIT_PWD);
        connectionFactory.setVirtualHost(RABBIT_VIRTUAL_HOST);

        //3. 返回连接对象
        Connection connection = connectionFactory.newConnection();
        return connection;
    }


}
