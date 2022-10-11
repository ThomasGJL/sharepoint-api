package viper

import (
	"fmt"
	log "github.com/sirupsen/logrus"
	"github.com/spf13/viper"
)

const fileName = "application"

// InitLocalConfigFile 加载本地配置文件
func InitLocalConfigFile() {
	log.Info("start Init Local ConfigFile")
	viper.SetConfigName(fileName)
	viper.SetConfigType("yaml")
	viper.AddConfigPath("./")
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("load Local ConfigFile failed: %s \n", err))
	}
	log.Info("Init Local ConfigFile done")
}
