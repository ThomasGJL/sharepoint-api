require('dotenv').config();

var request = require('request');
const logger = require("./logger");

var accessSettings = {
    tokenUrl: "https://accounts.accesscontrol.windows.net/f260df36-bc43-424c-8f44-c85226657b01/tokens/OAuth/2/",
    grant_type: "client_credentials",
    client_id: process.env.sharepoint_client_id,
    client_secret: process.env.sharepoint_client_secret,
    resource: "00000003-0000-0ff1-ce00-000000000000/kyndryl.sharepoint.com@f260df36-bc43-424c-8f44-c85226657b01",
};

var pdfFiles = []
var pdfFile = {}

function getAccessToken() {

    let responseContent = new Promise((resolve, reject) => {
        request.post({
            url: accessSettings.tokenUrl,
            form: {
                grant_type: accessSettings.grant_type,
                client_id: accessSettings.client_id,
                client_secret: accessSettings.client_secret,
                resource: accessSettings.resource,
            }
        }, function(error, response) {
            logger.info(`SharePoint Oauth...`)
            if (!error && response.statusCode == 200) {
                var responseContent = JSON.parse(response.body);
                //logger.info(responseContent.token_type)
                //logger.info(responseContent.access_token)
                resolve(responseContent)
            } else{
                logger.error(`Error: ${error}`)
                reject(`Error: ${error}`)
            }
        })
    })
    return responseContent
}

function getPageInfo(pageurl, authorization, cnum) {

    let responsePageFiles = new Promise((resolve, reject) => {
        request.get({
            url: pageurl,
            headers: {
                Accept: "application/json;odata=verbose",
                Authorization: authorization,
            },
        }, async function (error, response, body) {
            if (!error && response.statusCode == 200) {
                var responseContent = JSON.parse(response.body);
                //logger.info(responseContent)
                var responseResults = responseContent.d.results
                //logger.info(responseResults)
                responseResults.forEach(ff => {
                    if (ff.__metadata.type == "MS.FileServices.File" && ff.Name != ".DS_Store") {
                        //logger.info(ff.Name)
                        if (ff.Name.substr(0, 7) == cnum) {
                            //logger.info(`file name is: ${ff.Name}`)
                            pdfFiles.push(ff)
                        }
                    }
                })
                if (responseContent.d.__next) {
                    var nextpageurl = responseContent.d.__next
                    //logger.info(`nextpageurl is ${nextpageurl}`)
                    await getPageInfo(nextpageurl, authorization, cnum)
                }
                //logger.info(`files num: ${pdfFiles.length}`)
                resolve(pdfFiles)
            } else {
                logger.error(`Error: ${error}`)
                reject(`Error: ${error}`)
            }
        })
    })
    return responsePageFiles
}


function getPageFile(pageurl, authorization, fileId) {

    let responsePageFile = new Promise((resolve, reject) => {
        request.get({
            url: pageurl,
            headers: {
                Accept: "application/json;odata=verbose",
                Authorization: authorization,
            },
        }, async function (error, response, body) {
            logger.info(`SharePoint get_file_info...`)
            if (!error && response.statusCode == 200) {
                var responseContent = JSON.parse(response.body);
                //logger.info(responseContent)
                var responseResults = responseContent.d.results
                //logger.info(responseResults)
                responseResults.forEach(ff => {
                    if (ff.__metadata.type == "MS.FileServices.File" && ff.Name != ".DS_Store") {
                        if (ff.Id == fileId) {
                            //logger.info(ff)
                            pdfFile = ff;
                        }
                    }
                })
                if (responseContent.d.__next) {
                    var nextpageurl = responseContent.d.__next
                    //logger.info(`nextpageurl is ${nextpageurl}`)
                    await getPageFile(nextpageurl, authorization, fileId)
                }
                resolve(pdfFile)
            } else {
                logger.error(`Error: ${error}`)
                reject(`Error: ${error}`)
            }
        })
    })
    return responsePageFile
}


async function filter_items(cnum) {

    pdfFiles = []

    var accessToken = await getAccessToken();
    //logger.info(accessToken)
    var authorization = accessToken.token_type + " " + accessToken.access_token;
    //logger.info(authorization)
    var apiPath = "https://kyndryl.sharepoint.com" + process.env.sharepoint_api_path + "/_api/files";

    var responsePageFiles = await getPageInfo(apiPath, authorization, cnum);

    logger.info(`responsePageFiles: ${responsePageFiles.length}`)

    return responsePageFiles
}



async function get_file(fileId) {

    var accessToken = await getAccessToken();
    //logger.info(accessToken)
    var authorization = accessToken.token_type + " " + accessToken.access_token;
    //logger.info(authorization)
    var apiPath = "https://kyndryl.sharepoint.com" + process.env.sharepoint_api_path + "/_api/files";

    var responseFile = await getPageFile(apiPath, authorization, fileId);

    return responseFile
}

module.exports = { getAccessToken, filter_items, get_file };
