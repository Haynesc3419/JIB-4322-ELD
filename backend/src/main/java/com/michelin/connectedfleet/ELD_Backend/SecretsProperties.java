package com.michelin.connectedfleet.ELD_Backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:secrets.properties")
public class SecretsProperties {

}
