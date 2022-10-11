var winston = require('winston');
const { createLogger, format, transports } = require('winston');

    var logger = winston.createLogger({ format: format.combine(
            format.timestamp({
                format: 'YYYY-MM-DD HH:mm:ss.ms'
            }),
            format.colorize(),
            format.printf(info => `${info.timestamp} ${info.level}: ${info.message}`+(info.splat!==undefined?`${info.splat}`:" "))
        ), 
        transports: [
            new (winston.transports.Console)
           ]
    });
    module.exports = logger;