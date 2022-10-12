package com.kyndryl.sharepoint;

import com.alibaba.fastjson.JSONObject;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SiteCollectionPage;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadResult;
import com.microsoft.graph.tasks.LargeFileUploadTask;
import net.schmizz.sshj.common.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphSharePoint {

    private static String CLIENT_ID = "<update-your-client-id>";
    private static String CLIENT_SECRET = "<update-your-app-secret>";
    private static String TENANT_GUID = "<update-your-app-tenant-guid>";

    private static List<String> SCOPES = Arrays.asList("https://graph.microsoft.com/.default");

    @Test
    public void listSite() throws IOException {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                .build();

        TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

        GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        //List site
        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new QueryOption("search", "testSite1"));

        SiteCollectionPage sites = graphClient.sites()
                .buildRequest( requestOptions )
                .get();

        Site site = sites.getCurrentPage().get(0);

        System.out.println(site.id + ":" + site.displayName);
    }

    @Test
    public void uploadFile() throws IOException {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                .build();

        TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

        GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        //Upload file
        FileInputStream fileInputStream = new FileInputStream("C:\\IdeaProjects\\Kyndryl-Concur-Backend-Application\\one-derive-api\\testFile1.txt");
        byte[] bytes = IOUtils.readFully(fileInputStream).toByteArray();


        DriveItem driveItem = graphClient.sites("4wpl6t.sharepoint.com,09254027-3b5c-4c52-9d75-831d9d9484d5,dc66b15a-b6fd-491c-b75f-fb387b688ded")
                .drive().root().itemWithPath("testFolder1/testFile1.txt")
                .content().buildRequest().put(bytes);

        System.out.println(driveItem.id + ":" + driveItem.name);
    }

    @Test
    public void createFolder() throws IOException {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                .build();

        TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

        GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        DriveItem driveItem = new DriveItem();
        driveItem.name = "testFolder1";
        Folder folder = new Folder();
        driveItem.folder = folder;
        driveItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));

        DriveItem folderDriveItem = graphClient
                .sites("4wpl6t.sharepoint.com,09254027-3b5c-4c52-9d75-831d9d9484d5,dc66b15a-b6fd-491c-b75f-fb387b688ded")
                .drive()
                .root()
                .children()
                .buildRequest()
                .post(driveItem);

        System.out.println(folderDriveItem.id + ":" + folderDriveItem.name);
    }

    @Test
    public void uploadLargeFile() throws IOException {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                .build();

        TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

        GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\TaoHu\\Desktop\\Commands.pdf");
        byte[] bytes = IOUtils.readFully(fileInputStream).toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

//        DriveItemUploadableProperties driveItemUploadableProperties = new DriveItemUploadableProperties();
//        driveItemUploadableProperties.name = "Commands.pdf";
//        driveItemUploadableProperties.description = "Test large file";
//        driveItemUploadableProperties.fileSize = (long) bytes.length;
//        DriveItemCreateUploadSessionParameterSet driveItemCreateUploadSessionParameterSet = DriveItemCreateUploadSessionParameterSet.newBuilder().withItem(driveItemUploadableProperties).build();

        UploadSession uploadSession = graphClient
                .sites("4wpl6t.sharepoint.com,09254027-3b5c-4c52-9d75-831d9d9484d5,dc66b15a-b6fd-491c-b75f-fb387b688ded")
                .drive().root()
                .itemWithPath("testFolder1/Commands.pdf")
                .createUploadSession(new DriveItemCreateUploadSessionParameterSet())
                .buildRequest()
                .post();

        LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<>(uploadSession,graphClient,byteArrayInputStream,bytes.length,DriveItem.class);
        IProgressCallback iProgressCallback = (current, max) -> System.out.println("Uploaded " + current + "bytes of total " + max + " bytes");

        LargeFileUploadResult<DriveItem> driveItemLargeFileUploadResult = largeFileUploadTask.upload(327680 * 8,null,iProgressCallback);

        System.out.println(JSONObject.toJSONString(driveItemLargeFileUploadResult.responseBody));
    }

    @Test
    public void testGetSiteId(){
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                .build();

        TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

        GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        String testSite1 = getSiteId(graphClient, "testSite1");

        System.out.println(testSite1);
    }

    public String getSiteId(GraphServiceClient graphClient, String siteName){
        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new QueryOption("search", siteName));

        SiteCollectionPage sites = graphClient.sites()
                .buildRequest( requestOptions )
                .get();


        String id = "";
        List<Site> currentPage = sites.getCurrentPage();

        while(id.length() == 0 && currentPage != null && currentPage.size() > 0) {
            List<Site> collect = currentPage.stream().filter(site -> site.name.equals(siteName)).collect(Collectors.toList());
            if(collect.size() == 1){
                id = collect.get(0).id;
                continue;
            }
            if(collect.size() > 1){
                throw new RuntimeException("Multiply site founds");
            }
        }

        return id;
    }
}
