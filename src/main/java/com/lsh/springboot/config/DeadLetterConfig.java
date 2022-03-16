package com.lsh.springboot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 9:08 下午
 * @desc ：构建死信队列
 * 两个普通队列
 * 两个交换机
 * 两个路由
 */
@Configuration
public class DeadLetterConfig {
    /**普通交换机*/
    public static final String NORMAL_EXCHANGE = "normal-exchange";

    /**普通队列*/
    public static final String NORMAL_QUEUE = "normal-queue";

    /**普通队列路由*/
    public static final String NORMAL_ROUTING_KEY = "normal.#";

    /**死信交换机*/
    public static final String DEAD_EXCHANGE = "dead-exchange";

    /**死信队列*/
    public static final String DEAD_QUEUE = "dead-queue";

    /**死信队列路由*/
    public static final String DEAD_ROUTING_KEY = "dead.#";

    @Bean
    public Exchange normalExchange(){
        return ExchangeBuilder.topicExchange(NORMAL_EXCHANGE).build();
    }

    /**
     * 在普通队列设置死信交换机和死信路由
     * deadLetterExchange   设置死信交换机
     * deadLetterRoutingKey 设置死信路由
     * 设置普通队列的ttl，如果队列中消息过期则会进入死信队列
     * maxLength 设置队列最大长度 ，如果队列中的消息达到最大长度，此时再进入队列的消息则会被丢弃或者进入死信队列
     * @return
     */
    @Bean
    public Queue normalQueue(){
        return QueueBuilder.durable(NORMAL_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey("dead.abd")
                .ttl(5000)
                .maxLength(1)
                .build();
    }

    @Bean
    public Binding normalBinding(Exchange normalExchange,Queue normalQueue){
        return BindingBuilder.bind(normalQueue).to(normalExchange).with(NORMAL_ROUTING_KEY).noargs();
    }
    @Bean
    public  Exchange deadExchange(){
        return ExchangeBuilder.topicExchange(DEAD_EXCHANGE).build();
    }

    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding deadBinding(Exchange deadExchange,Queue deadQueue){
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(DEAD_ROUTING_KEY).noargs();
    }



}
