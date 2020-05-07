package guru.sfg.beer.order.service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * https://github.com/vromero/activemq-artemis-docker
 * @author kas
 */
@Configuration
public class JmsConfig {


    @Bean
    public MessageConverter messageConverter() {
        // will convert POJO to JSON string
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        // for spring processing. Spring sets value of property equal to specific java class. Check @Payload in HelloListener
        // for @Payload HelloWorldMessage would be: _type = guru.springframework.sfgjms.model.HelloWorldMessage
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
