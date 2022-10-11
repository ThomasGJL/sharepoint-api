package sharepoint

import (
	"encoding/json"
	log "github.com/sirupsen/logrus"
	"github.com/spf13/viper"
	"github.com/xuanbo/requests"
	"net/url"
	vc "sharepoint-api-golang/config/viper"
)

type TokenResData struct {
	Token_type   string `json:"token_type"`
	Expires_in   string `json:"expires_in"`
	Not_before   string `json:"not_before"`
	Expires_on   string `json:"expires_on"`
	Resource     string `json:"resource"`
	Access_token string `json:"access_token"`
}

func GetAccessToken() TokenResData {

	vc.InitLocalConfigFile()

	tokenUrl := viper.GetString("tokenUrl")
	grant_type := viper.GetString("grant_type")
	client_id := viper.GetString("client_id")
	client_secret := viper.GetString("client_secret")
	resource := viper.GetString("resource")

	log.Info("grant_type: ", grant_type)

	resContent, err := requests.Post(tokenUrl).
		Form(url.Values{
			"grant_type":    {grant_type},
			"client_id":     {client_id},
			"client_secret": {client_secret},
			"resource":      {resource},
		}).
		Send().
		Text()
	if err != nil {
		panic(err)
	}
	//log.Info(resContent)

	var data TokenResData
	if err := json.Unmarshal([]byte(resContent), &data); err == nil {
		//log.Info(data.Token_type)
		//log.Info(data.Access_token)
	} else {
		log.Info(err)
	}
	return data
}
