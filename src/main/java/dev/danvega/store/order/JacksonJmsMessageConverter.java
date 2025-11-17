package dev.danvega.store.order;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JacksonJmsMessageConverter implements MessageConverter {

    private final JsonMapper jsonMapper;

    public JacksonJmsMessageConverter() {
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException {
        try {
            String json = jsonMapper.writeValueAsString(object);
            TextMessage message = session.createTextMessage(json);
            message.setStringProperty("_type", object.getClass().getName());
            return message;
        } catch (Exception e) {
            throw new JMSException("Failed to convert to JSON: " + e.getMessage());
        }
    }

    @Override
    public Object fromMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            try {
                String json = textMessage.getText();
                String className = message.getStringProperty("_type");
                Class<?> clazz = Class.forName(className);
                return jsonMapper.readValue(json, clazz);
            } catch (Exception e) {
                throw new JMSException("Failed to parse JSON: " + e.getMessage());
            }
        }
        throw new JMSException("Only TextMessage is supported");
    }
}
