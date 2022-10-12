package com.kyndryl.sharepoint.common.property;


import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApiProperties {

    private String tokenUrl;
    private String grant_type;
    private String clientId;
    private String clientSecret;
    private String resource;

}
