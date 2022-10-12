package com.kyndryl.sharepoint.api;


import com.alibaba.fastjson.JSONObject;
import com.kyndryl.sharepoint.entity.OauthToken;

import java.io.IOException;

public interface SharepointApiService {

    //OauthToken getAccessToken();

    JSONObject getAllFolderAndFiles();

    void downloadFile(String localFile, String remoteFile);

    void uploadFile(String localFile, String remoteFolder) throws IOException;

    Boolean folderExsit(String remoteFolder);

}
