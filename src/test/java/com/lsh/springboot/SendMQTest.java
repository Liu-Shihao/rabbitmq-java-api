package com.lsh.springboot;

import com.lsh.springboot.config.DeadLetterConfig;
import com.lsh.springboot.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/15 11:58 下午
 * @desc ：
 * 在SpringBoot项目中，通过rabbitTemplate.convertAndSend（）生产消息
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SendMQTest {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void send(){
        //交换机、路由Key、消息内容
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME,"little.black.rabbit","小黑兔");
        System.out.println("消息已发送");
    }
    /**
     * 生产消息
     */
    @Test
    public void sendAndMsgProperties(){
        //交换机、路由Key、消息内容、MessageProperties（传递Msg信息：包括CorrelationId、ReplyTo等）
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, "little.black.rabbit", "小黑兔", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                //设置唯一标识
                messageProperties.setCorrelationId("123");
                //设置响应队列
//                messageProperties.setReplyTo();
                return message;
            }
        });
        System.out.println("消息已发送");
    }

    /**
     * 通过rabbitTemplate.setConfirmCallback()开启confirms机制
     */
    @Test
    public void sendWithConfirms(){

        //1.设置Confirms机制
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack){
                    System.out.println("消息已送达交换机！");
                }else {
                    System.out.println("消息未到达交换机！");
                }
            }
        });

        //2.设置Return机制
        //注意：低版本使用setReturnCallback（）方法；在高版本中该方法被弃用，使用setReturnsCallback()方法
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey)  {
                String msg = new String(message.getBody());
                System.out.println("消息未成功投递到队列："+msg);
            }
        });
        //注意 ：只用SpringBoot项目投递消息时，不需要在设置mandatory参数为true

        //3.开启消息持久化
        //发送消息 设置消息持久化 message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        rabbitTemplate.convertAndSend("", "confirmss", "SpringBoot Confirms Message!", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // MessageDeliveryMode枚举类：
                // NON_PERSISTENT 表示不持久化 ；PERSISTENT表示持久化
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            }
        });
    }

    /**
     * 通过消费者拒绝消费或者Nack使消息进入死信队列
     * 或者普通队列设置了ttl
     */
    @Test
    public void sendNormalQueue(){
        rabbitTemplate.convertAndSend(DeadLetterConfig.NORMAL_EXCHANGE,"normal.nack.reject","normal——message");
        System.out.println("消息已发送");
    }

    /**
     * 通过设置TTL消息存活时间，使消息进入死信队列
     */
    @Test
    public void sendDeadLetterQueueAndSetTTL(){
        rabbitTemplate.convertAndSend(DeadLetterConfig.NORMAL_EXCHANGE, "normal.ttl", "通过设置TTL消息存活时间，使消息进入死信队列", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //设置消息存活时间
                message.getMessageProperties().setExpiration("5000");
                return message;
            }
        });
        System.out.println("消息已发送");
    }

}
