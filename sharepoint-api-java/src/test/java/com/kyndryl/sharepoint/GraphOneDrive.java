package com.kyndryl.sharepoint;

import com.alibaba.fastjson.JSONObject;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCreateUploadSessionParameterSet;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadResult;
import com.microsoft.graph.tasks.LargeFileUploadTask;
import net.schmizz.sshj.common.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class GraphOneDrive {
    private static String CLIENT_ID = "<update-your-client-id>";
    private static List<String> SCOPES = Arrays.asList("https://graph.microsoft.com/Files.ReadWrite.All");
    private static String username = "<update-your-ms-account>";
    private static String password = "<update-your-ms-account-psw>";

    @Test
    public void createFolder(){
        final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId(CLIENT_ID)
                .username(username)
                .password(password)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(SCOPES, usernamePasswordCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        DriveItem driveItem = new DriveItem();
        driveItem.name = "testFolder2";
        Folder folder = new Folder();
        driveItem.folder = folder;
        driveItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));

        DriveItem post = graphClient.me().drive().root().children()
                .buildRequest()
                .post(driveItem);


        System.out.println(post);

    }

    @Test
    public void uploadFile() throws IOException {
        final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId(CLIENT_ID)
                .username(username)
                .password(password)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(SCOPES, usernamePasswordCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        FileInputStream fileInputStream = new FileInputStream("C:\\IdeaProjects\\Kyndryl-Concur-Backend-Application\\one-derive-api\\testFile1.txt");
        byte[] bytes = IOUtils.readFully(fileInputStream).toByteArray();

        DriveItem driveItem = graphClient.me().drive().root()
                .itemWithPath("testFolder1/testFile1.txt_2022-04-30")
                .content()
                .buildRequest()
                .put(bytes);

        System.out.println(driveItem.id + ":" + driveItem.name);

    }

    @Test
    public void downloadFile() throws IOException {
        final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId(CLIENT_ID)
                .username(username)
                .password(password)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(SCOPES, usernamePasswordCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        InputStream inputStream = graphClient.me().drive().root().itemWithPath("testFolder1/testFile1.txt")
                .content()
                .buildRequest()
                .get();

        byte[] bytes = IOUtils.readFully(inputStream).toByteArray();

        System.out.println(new String(bytes));

    }

    @Test
    public void uploadLargeFile() throws IOException {
        final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId(CLIENT_ID)
                .username(username)
                .password(password)
                .build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(SCOPES, usernamePasswordCredential);

        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\TaoHu\\Desktop\\Commands.pdf");
        byte[] bytes = IOUtils.readFully(fileInputStream).toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

//        DriveItemUploadableProperties driveItemUploadableProperties = new DriveItemUploadableProperties();
//        driveItemUploadableProperties.name = "Commands.pdf";
//        driveItemUploadableProperties.description = "Test large file";
//        driveItemUploadableProperties.fileSize = (long) bytes.length;
//        DriveItemCreateUploadSessionParameterSet driveItemCreateUploadSessionParameterSet = DriveItemCreateUploadSessionParameterSet.newBuilder().withItem(driveItemUploadableProperties).build();

        UploadSession uploadSession = graphClient.me().drive().root()
                .itemWithPath("testFolder1/Commands.pdf")
                .createUploadSession(new DriveItemCreateUploadSessionParameterSet())
                .buildRequest()
                .post();

        LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<>(uploadSession,graphClient,byteArrayInputStream,bytes.length,DriveItem.class);
        IProgressCallback iProgressCallback = (current,max) -> System.out.println("Uploaded " + current + "bytes of total " + max + " bytes");

        LargeFileUploadResult<DriveItem> driveItemLargeFileUploadResult = largeFileUploadTask.upload(327680 * 8,null,iProgressCallback);

        System.out.println(JSONObject.toJSONString(driveItemLargeFileUploadResult.responseBody));
    }
}
