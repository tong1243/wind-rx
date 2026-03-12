package com.wut.screendbmongorx.Config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import javax.annotation.Nonnull;

@Configuration
public class MongoConverterConfig extends AbstractMongoClientConfiguration {
    @Override
    @Nonnull
    protected String getDatabaseName() {
        return "radar";
    }

    @Override
    @Nonnull
    public MappingMongoConverter mappingMongoConverter(
            @Nonnull MongoDatabaseFactory databaseFactory,
            @Nonnull MongoCustomConversions customConversions,
            @Nonnull MongoMappingContext mappingContext) {
        MappingMongoConverter mappingMongoConverter = super.mappingMongoConverter(databaseFactory, customConversions, mappingContext);
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return mappingMongoConverter;
    }

}
