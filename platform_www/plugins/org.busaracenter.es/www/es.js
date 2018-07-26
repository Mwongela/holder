cordova.define("org.busaracenter.es._es", function(require, exports, module) {
var exec = require('cordova/exec');

var PLUGIN_NAME = "ES";

var _es = {

    addRecord: function(successCb, errorCb, record) {

        console.log(record);
        exec(successCb, errorCb, PLUGIN_NAME, "addRecord", [record]);
    },

    getSessionDetails: function(successCb, errorCb, record) {

        console.log(record);
        exec(successCb, errorCb, PLUGIN_NAME, "getSessionDetails", [record]);
    }

};

module.exports = _es;
});
