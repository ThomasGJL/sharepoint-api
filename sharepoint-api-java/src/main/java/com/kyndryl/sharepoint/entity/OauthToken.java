package com.kyndryl.sharepoint.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthToken {

    private String token_type;
    private String expires_in;
    private String not_before;
    private String expires_on;
    private String resource;
    private String access_token;

    private String error;
}
