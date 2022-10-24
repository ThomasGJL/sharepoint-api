package com.kyndryl.sharepoint.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.kyndryl.sharepoint.api.SharepointApiService;
import com.kyndryl.sharepoint.common.property.ApiProperties;
import com.kyndryl.sharepoint.entity.OauthToken;
import com.kyndryl.sharepoint.util.CommonInputStreamResource;
import com.kyndryl.sharepoint.util.PropertiesUtil;
import com.sun.mail.iap.ByteArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SharepointApiServiceImpl implements SharepointApiService {

    private OauthToken oauthToken;

    private ApiProperties apiProperties;

    private String siteUrl = PropertiesUtil.getProperties("config", "sharepoint.api.siteUrl");

    //get information from system env
    private ApiProperties GetAllProperties() {

        apiProperties = new ApiProperties();

        String tokenUrl = PropertiesUtil.getProperties("config", "sharepoint.api.tokenUrl");
        String grant_type = PropertiesUtil.getProperties("config", "sharepoint.api.grant_type");
        String clientId = PropertiesUtil.getProperties("config", "sharepoint.api.clientId");
        String clientSecret = PropertiesUtil.getProperties("config", "sharepoint.api.clientSecret");
        String resource = PropertiesUtil.getProperties("config", "sharepoint.api.resource");

        apiProperties.setTokenUrl(tokenUrl);
        apiProperties.setGrant_type(grant_type);
        apiProperties.setClientId(clientId);
        apiProperties.setClientSecret(clientSecret);
        apiProperties.setResource(resource);

        return apiProperties;
    }

    /***
     public ApiProperties GetAllProperties() {

     apiProperties = new ApiProperties();

     String tokenUrl = System.getenv("sharepoint.api.tokenUrl");
     String grant_type = System.getenv("sharepoint.api.grant_type");
     String clientId = System.getenv("sharepoint.api.clientId");
     String clientSecret = System.getenv("sharepoint.api.clientSecret");
     String resource = System.getenv("sharepoint.api.resource");

     apiProperties.setTokenUrl(tokenUrl);
     apiProperties.setGrant_type(grant_type);
     apiProperties.setClientId(clientId);
     apiProperties.setClientSecret(clientSecret);
     apiProperties.setResource(resource);

     return apiProperties;
     }
     ***/

    private OauthToken getAccessToken() {

        GetAllProperties();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", apiProperties.getGrant_type());
        requestBody.add("client_id", apiProperties.getClientId());
        requestBody.add("client_secret", apiProperties.getClientSecret());
        requestBody.add("resource", apiProperties.getResource());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

        //Prepare URL
        String url = UriComponentsBuilder
                .fromHttpUrl(apiProperties.getTokenUrl())
                .build()
                .encode()
                .toString();

        try {
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            RestTemplate restTemplate = new RestTemplate();
            //ResponseEntity<String> exchange = restTemplate.postForEntity(url, httpEntity, String.class);
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            //log.info("exchange========: " + exchange);

            //Get Result
            String body = exchange.getBody();
            JSONObject resultRemote = JSONObject.parseObject(body);

            String tokenType = resultRemote.getString("token_type");
            String expiresIn = resultRemote.getString("expires_in");
            String notBefore = resultRemote.getString("not_before");
            String expiresOn = resultRemote.getString("expires_on");
            String resource = resultRemote.getString("resource");
            String accessToken = resultRemote.getString("access_token");

            oauthToken = new OauthToken();

            oauthToken.setToken_type(tokenType);
            oauthToken.setExpires_in(expiresIn);
            oauthToken.setNot_before(notBefore);
            oauthToken.setExpires_on(expiresOn);
            oauthToken.setResource(resource);
            oauthToken.setAccess_token(accessToken);

        } catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            this.oauthToken.setError(responseBodyAsString);
            log.error("getAccessToken exception: {}", oauthToken.getError());
        } catch (Exception e) {
            this.oauthToken.setError(e.getMessage());
            log.error("getAccessToken exception: {}", oauthToken.getError());
        }

        return oauthToken;
    }


    //only first page,
    public JSONObject getAllFolderAndFiles() {

        oauthToken = getAccessToken();
        String authorization = oauthToken.getToken_type() + " " + oauthToken.getAccess_token();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", authorization);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

        //Prepare URL
        String url = UriComponentsBuilder
                .fromHttpUrl(siteUrl + "/_api/files")
                .build()
                .encode()
                .toString();

        JSONObject allfolderfiles = new JSONObject();

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

            //log.info("exchange========: " + exchange);

            allfolderfiles = JSONObject.parseObject(exchange.getBody());

        } catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            this.oauthToken.setError(responseBodyAsString);
            log.error("getAllFolderAndFiles exception: {}", oauthToken.getError());
        } catch (Exception e) {
            this.oauthToken.setError(e.getMessage());
            log.error("getAllFolderAndFiles exception: {}", oauthToken.getError());
        }

        return allfolderfiles;
    }

    /**
     *  download file
     * */
    public void downloadFile(String localFile, String remoteFile) {

        oauthToken = getAccessToken();
        String authorization = oauthToken.getToken_type() + " " + oauthToken.getAccess_token();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.MULTIPART_FORM_DATA_VALUE);
        headers.add("Authorization", authorization);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

        String remoteUrl = "";
        remoteUrl = siteUrl + "/_api/web/GetFileByServerRelativeUrl('";
        remoteUrl = remoteUrl + remoteFile + "')/$value";

        //Prepare URL
        String url = UriComponentsBuilder
                .fromHttpUrl(remoteUrl)
                .build()
                .encode()
                .toString();

        //log.info("remoteUrl========: " + remoteUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

            File file = new File(localFile);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = exchange.getBody().getBytes(StandardCharsets.ISO_8859_1);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(buffer);
            bos.flush();
            fos.close();

            log.info("download finish");

        } catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            this.oauthToken.setError(responseBodyAsString);
            log.error("downloadFile exception: {}", oauthToken.getError());
        } catch (Exception e) {
            this.oauthToken.setError(e.getMessage());
            log.error("downloadFile exception: {}", oauthToken.getError());
        }

    }

    /**
     *  upload file
     * */
    public void uploadFile(String localFile, String remoteFolder) throws IOException {

        oauthToken = getAccessToken();
        String authorization = oauthToken.getToken_type() + " " + oauthToken.getAccess_token();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.MULTIPART_FORM_DATA_VALUE);
        headers.add("Authorization", authorization);

        File file = new File(localFile);
        FileInputStream fileInputStream = new FileInputStream(file);

        String fileName = file.getName();
        long fileLength = file.length();

        log.info("fileName========" + fileName);

        //FileSystemResource fileSystemResource = new FileSystemResource(file);
        //MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();


        CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(fileInputStream, fileName, fileLength);
        //requestBody.add("file", commonInputStreamResource);

        //HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
        HttpEntity<Object> httpEntity = new HttpEntity<>(commonInputStreamResource, headers);

        String postUrl = "";
        postUrl = siteUrl + "/_api/web/GetFolderByServerRelativeUrl('";
        postUrl = postUrl + remoteFolder + "')/files/add(overwrite=true,url='" + fileName + "')";

        //Prepare URL
        String url = UriComponentsBuilder
                .fromHttpUrl(postUrl)
                .build()
                .encode()
                .toString();

        //log.info("remoteFolder========: " + remoteFolder);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            log.info("upload finish");

        } catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            this.oauthToken.setError(responseBodyAsString);
            log.error("uploadFile exception: {}", oauthToken.getError());
        } catch (Exception e) {
            this.oauthToken.setError(e.getMessage());
            log.error("uploadFile exception: {}", oauthToken.getError());
        }


    }

    public Boolean folderExsit(String remoteFolder) {

        boolean folderEx = false;

        oauthToken = getAccessToken();
        String authorization = oauthToken.getToken_type() + " " + oauthToken.getAccess_token();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", authorization);

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        String remoteUrl = "";
        remoteUrl = siteUrl + "/_api/web/GetFolderByServerRelativeUrl('";
        remoteUrl = remoteUrl + remoteFolder + "')";

        //Prepare URL
        String url = UriComponentsBuilder
                .fromHttpUrl(remoteUrl)
                .build()
                .encode()
                .toString();

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

            if (exchange.getStatusCodeValue() == 200) {
                folderEx = true;
            }

        } catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            this.oauthToken.setError(responseBodyAsString);
            log.error("folderExsit exception: {}", oauthToken.getError());
        } catch (Exception e) {
            this.oauthToken.setError(e.getMessage());
            log.error("folderExsit exception: {}", oauthToken.getError());
        }

        log.info("folderEx======== {}", folderEx);

        return folderEx;
    }

}
