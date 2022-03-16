# 一、安装部署

# 二、通讯方式(操作RabbitMQ API)
结构Maven项目，导入Pom依赖：
```xml
    <dependency>
        <groupId>com.rabbitmq</groupId>
        <artifactId>amqp-client</artifactId>
        <version>5.9.0</version>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.1</version>
    </dependency>
```
## 2.1 "Hello World!"
![Hello World!](src/main/resources/imges/helloworld.png)

- 一个生产者
- 一个消费者
- 一个队列
- 使用默认交换机
- 默认路由（为队列名）
## 2.2 Work queues
![Hello World!](src/main/resources/imges/workqueue.png)

- 一个生产者
- 两个消费者（进行轮询消费，每个消息只会被成功的消费一次）
- 一个队列
- 默认交换机
- 默认路由

## 2.3 Publish/Subscribe
![Hello World!](src/main/resources/imges/PublishSubscribe.png)

发布/订阅模式（FANOUT分裂模式）

- 一个生产者
- 一个FUNOUT类型交换机（这种模式交换机和队列直接绑定，不需要RoutingKey）
- 两个队列
- 两个消费者
## 2.4 Routing
![Hello World!](src/main/resources/imges/Routing.png)

DIRECT直接模式

- 一个生产者
- 一个DIRECT模式交换机（交换机通过不同的RoutingKey绑定队列）
- 两个队列（一个队列可以绑定多个路由规则，RoutingKey如果没有对应的队列则会被丢弃）
- 两个消费者
## 2.5 Topics
![Hello World!](src/main/resources/imges/Topic.png)

TOPIC主题模式

- 一个生产者
- 一个TOPIC类型交换机（通过不同的路由规则绑定队列）
- 两个队列
- 两个消费者

（注意：需要以aaa.bbb.ccc..方式编写routingkey ,其中有两个特殊字符:*(相当于占位符)，#(相当通配符)）
## 2.6 RPC
![Hello World!](src/main/resources/imges/RPC.png)

Client/Server RPC模式，通过队列进行解耦

Client 发送请求消息到达 请求消息队列，并且监听 响应消息队列

Server 监听请求消息队列，并且回复响应信息 到响应消息队列

整个流程需要携带两个参数：
1. replyTo： 告知Server将相应信息放到哪个队列 （指定响应队列）
2. correlationId：告知Server发送相应消息时，需要携带位置标示来告知Client响应的信息（响应消息对应请求消息）

# 三、RabbitMQ整合Spring
## 3.1 导入依赖
```pom
     <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
    </parent>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
```
## 3.2 application.yml配置RabbitMQ信息
```yml
spring:
  rabbitmq:
    host: 172.16.98.100
    port: 5672
    username: admin
    password: admin
    virtual-host: /
```
## 3.3 配置类声明交换机、队列和绑定
```Java
package com.lsh.springboot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/15 11:36 下午
 * @desc ：配置类声明交换机、队列和绑定
 */
@Configuration
public class RabbitMQConfig {

    /**Topic类型交换机*/
    public static final String TOPIC_EXCHANGE_NAME = "boot-exchange";

    /**Queue队列名*/
    public static final String QUEUE_NAME = "boot-queue";

    /**RoutingKey 路由Key*/
    public static final String ROUTING_KEY = "*.black.*";

    /**
     * 声明交换机: 在SpringBoot项目中，直接通过ExchangeBuilder来构造交换机
     * @return org.springframework.amqp.core.Exchange
     */
    @Bean
    public Exchange exchange(){
        // => channel.DeclareExchange
        Exchange exchange = ExchangeBuilder.topicExchange(TOPIC_EXCHANGE_NAME).build();
        return exchange;
    }

    /**
     * 声明队列：在SpringBoot中，通过QueueBuilder.durable(队列名)来构造队列
     * @return
     */
    @Bean
    public Queue queue(){
        Queue queue = QueueBuilder.durable(QUEUE_NAME).build();
        return queue;
    }

    /**
     * 声明绑定：在SpringBoot项目中，通过BindingBuilder.bind(队列).to(交换机).with(路由Key)构造绑定
     * @param exchange 交换机
     * @param queue    队列
     * @return
     */
    @Bean
    public Binding binding(Exchange exchange,Queue queue){
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
        return binding;
    }

}

```
## 3.4 生产消息
在SpringBoot项目直接通过RabbitTemplate对象进行操作
```java
package com.lsh.springboot;

import com.lsh.springboot.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
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
}

```

## 3.5 监听消息
```java
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

```
# 四、消息可靠性
## 4.1 确保消息到达交换机(confirms机制)
## 4.2 确保消息从交换机路由到达队列(return机制)
## 4.3 确保消息持久化（消息持久化）
```java
package com.lsh.g_confirms;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 2:49 下午
 * @desc ：开启消息确认机制
 * 1. 确保消息到达交换机 confirms机制
 *  1.开启confirms: channel.confirmSelect();
 *  2.增加异步回调：channel.addConfirmListener();
 * 2.确保消息从交换机路由到达队列 return机制
 */
public class ConfirmsPublisher {
    public static final String QUEUE_NAME = "confirms";

    @Test
    public void publisher() throws Exception {
        // 1.获取连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.构建Channel
        Channel channel = connection.createChannel();

        //3.构建队列 注意此处的durable参数只是控制队列持久化，并不能控制消息持久化
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //4.开启confirms
        channel.confirmSelect();

        //5.设置confirms的异步回调
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                //消息成功发送到交换机 success
                System.out.println("消息成功发送到交换机!");
            }
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                //消息未成功发送到交换机 fail
                System.out.println("消息未成功发送到交换机!");
            }
        });

        //6.设置Return回调，确认消息是否到达队列，需要在发送消息时，设置mandatory参数为true开启Return机制
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //只有消息没有到达指定队列时，才会触发此函数(例如RoutingKey不对导致消息没有投递到对应队列，就会触发该回调函数)
                System.out.println("消息没有到达指定队列!");
            }
        });

        //7.开启消息持久化，如果没有开启消息持久化。如果MQ重启，则消息会丢失
        AMQP.BasicProperties pop = new AMQP.BasicProperties()
                .builder()
                //设置deliveryMode为2表示开启消息持久化，MQ重启后消息不会消失
                .deliveryMode(2)
                .build();

        String message = "Confirms Messaage!";
        //8.发送消息  注意:此处需要设置mandatory参数为true，才能开启Return机制
        channel.basicPublish("","confirms",true,pop,message.getBytes());
        System.out.println("消息发送成功!");
        //read方法阻塞，查看WEB可视化界面的客户端连接数
        System.in.read();

    }
}

```
## 4.4 确保消息正常被消费（手动ack）
```java
package com.lsh.g_confirms;

import com.lsh.RabbitMQConnectionUtil;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ：LiuShihao
 * @date ：Created in 2022/3/16 3:41 下午
 * @desc ：监听队列消息，关闭自动ack，手动ack
 */
public class ConfirmsConsumer {

    public static final String QUEUE_NAME = "confirms";

    @Test
    public void consumer() throws Exception {
        //1.获得连接对象
        Connection connection = RabbitMQConnectionUtil.getConnection();

        //2.获得Channel
        final Channel channel = connection.createChannel();

        //3.声明队列，持久化队列 durable为true
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //4.设置CallBack函数
        DefaultConsumer callback = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("监听到队列消息:" + new String(body, "UTF-8"));

                //手动ack
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        //5.监听队列，关闭自动ack
        channel.basicConsume(QUEUE_NAME,false,callback);
        System.out.println("开始监听队列");
        System.in.read();
    }
}
```
## 4.5 SpringBoot项目实现消息可靠性
1. 在application.yml配置文件中通过配置spring.rabbitmq.publisher-confirm-type为correlated开启confirms机制
2. 在rabbitTemplate.setConfirmCallback()设置confirms机制的回调函数
3. application.yml配置spring.rabbitmq.publisher-returns为true开启Return机制。
4. 通过rabbitTemplate.setReturnCallback（）方法这是Return机制的回调函数。
5. 设置消息持久化
```java
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
```
# 五、死信队列&延时交换机
## 5.1 死信
当消息被消费者拒绝，并且requeue设置为false（消息不重新投递回队列）

# 、集群高可用