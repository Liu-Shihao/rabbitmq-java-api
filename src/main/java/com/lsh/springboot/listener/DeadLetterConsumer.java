package com.lsh.springboot.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 9:27 下午
 * @desc ：
 */
@Component
public class DeadLetterConsumer {

    /**
     * 1.监听普通队列消息，拒绝或者不ack消息，并且requeue为false禁止重新投递队列，则消息会进入死信队列
     * @param msg
     * @param channel
     * @param message
     * @throws IOException
     */
//    @RabbitListener(queues = DeadLetterConfig.NORMAL_QUEUE)
    public void consumer1(String msg, Channel channel, Message message) throws IOException {
        System.out.println("监听到普通队列消息："+msg);
        //拒绝消息或者不ack确认消息
        //设置拒绝消息，并禁止重新投递队列requeue=false
//        channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        // requeue如果为true则重新排队，如果为false则被丢弃或者进入死信队列
        channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
    }

    /**
     * 2.通过设置TTL使消息进入死信队列
     * 消息设置TTL被投递到普通队列，此时不监听普通队列，等待消息过期，进入死信队列，监听死信队列，从而达到延时的目的
     * 注意：不监听普通队列，而是监听死信队列
     * @param msg
     * @param channel
     * @param message
     * @throws IOException
     */
//    @RabbitListener(queues = DeadLetterConfig.DEAD_QUEUE)
    public void consumer2(String msg, Channel channel, Message message) throws IOException {
        System.out.println("监听到死信队列消息："+msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
