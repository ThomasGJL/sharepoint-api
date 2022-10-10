import logging
import requests
import os
from dotenv import find_dotenv, load_dotenv

logging.basicConfig(format='%(asctime)s %(message)s', level=logging.INFO)

class Sharepoint:

    def getAuthToken(self):

        #BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        #logging.info(BASE_DIR)
        load_dotenv(find_dotenv('.env'))

        authToken = {}

        #logging.info("siteUrl===" + siteUrl)

        authToken['siteUrl'] = os.getenv('sharepoint.api.siteUrl')
        authToken['tokenUrl'] = os.getenv('sharepoint.api.tokenUrl')
        authToken['grant_type'] = os.getenv('sharepoint.api.grant_type')
        authToken['clientId'] = os.getenv('sharepoint.api.clientId')
        authToken['clientSecret'] = os.getenv('sharepoint.api.clientSecret')
        authToken['resource'] = os.getenv('sharepoint.api.resource')

        return authToken


    def getAccessToken(self):

        authToken = self.getAuthToken()

        url = authToken['tokenUrl']

        data = {
            "grant_type": authToken['grant_type'],
            "client_id": authToken['clientId'],
            "client_secret": authToken['clientSecret'],
            "resource": authToken['resource']
        }

        response = requests.post(url, data=data)
        #logging.info(response.text)
        responseDict = eval(response.text)
        if 'error' not in responseDict:
            return responseDict












