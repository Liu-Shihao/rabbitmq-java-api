package com.lsh.springboot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/17 10:05 上午
 * @desc ：构造延时交换机
 * 消息设置延时时间后，发送到延时交换机，在延时指定时间之后，消息才会发送到队列中
 */
@Configuration
public class DelayedExchangeConfig {

    public static final String DELAYED_EXCHANGE_NAME = "boot-delayed-exchange";

    public static final String DELAYED_QUEUE_NAME = "boot-delayed-queue";

    public static final String DELAYED_ROUTING_KEY = "*.delayed.*";

    //普通队列
    @Bean
    public Queue delayedQueue(){
        return QueueBuilder.durable(DELAYED_QUEUE_NAME).build();
    }

    /**
     * 构造延时交换机
     * 1、构造arguments参数 指定交换机类型x-delayed-type为topic
     * 2、指定type为x-delayed-message类型
     */
    @Bean
    public Exchange delayedExchange(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type","topic");
        CustomExchange customExchange = new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, arguments);
        return customExchange;
    }

    @Bean
    public Binding delayedBinding(Queue delayedQueue,Exchange delayedExchange){
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}
