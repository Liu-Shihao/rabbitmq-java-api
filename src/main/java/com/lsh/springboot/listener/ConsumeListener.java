package com.lsh.springboot.listener;

import com.lsh.springboot.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 2:21 下午
 * @desc ：在SpringBoot项目中监听消息,通过@RabbitListener(queues = "队列名") 注解监听队列
 * 在SpringBoot项目中，
 * 如果要关闭自动ack需要在application.yml文件中设置
 * spring.rabbitmq.listener.simple.acknowledge-mode为manual
 *
 */
@Component
public class ConsumeListener {
    /**
     *
     * @param msg  队列的消息
     * @param channel
     * @param message  包含消息的各种信息，如msg、DeliveryTag、CorrelationId、ReplyTo等信息
     * @throws IOException
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumer(String msg, Channel channel, Message message) throws IOException {
        System.out.println("队列的消息："+msg);
        String correlationId = message.getMessageProperties().getCorrelationId();
        System.out.println("唯一标识："+correlationId);
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
