package io.github.pengxianggui.config;

import cn.hutool.json.JSONNull;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
public class MessageConverter {

    public static ObjectReader reader;
    public static ObjectWriter writer;

    @Bean("ObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_NULL).
                serializerByType(LocalDateTime.class, new LocalDateTimeSerializeConverter()).
                deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializeConverter()).
                build();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

        this.reader = objectMapper.reader();
        this.writer = objectMapper.writer();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(JSONNull.class, new JsonSerializer<JSONNull>() {
            @Override
            public void serialize(JSONNull jsonNull, JsonGenerator jsonGenerator
                    , SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeNull();
            }
        });
        simpleModule.addDeserializer(JSONNull.class, new JsonDeserializer<JSONNull>() {
            @Override
            public JSONNull deserialize(JsonParser jsonParser
                    , DeserializationContext deserializationContext) {
                return null;
            }
        });
        return objectMapper.registerModule(simpleModule);
    }

    @Bean("MappingJackson2HttpMessageConverter")
    public MappingJackson2HttpMessageConverter javaSerializationConverter(@Qualifier(value = "ObjectMapper") ObjectMapper build) {
        return new MappingJackson2HttpMessageConverter(build);
    }

}

class LocalDateTimeSerializeConverter extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}

class LocalDateTimeDeserializeConverter extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonParser.getLongValue()), ZoneId.systemDefault());
    }
}

