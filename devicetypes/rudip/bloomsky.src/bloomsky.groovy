/**
*  Device: Bloomsky
*
*  This DTH requires the BloomSky (Connect) app (https://github.com/tslagle13/SmartThingsPersonal/blob/master/smartapps/tslagle13/bloomsky-connect.src/bloomsky-connect.groovy)
*
*  Version History:  
*
*  2.1.3 - 01/30/2016 - Bug Fixes
*  2.1.2 - 01/27/2016 - Added Unit options
*  2.1.1 - 01/25/2016 - Added STORM capabilities (testing)
*  2.1.0 - 01/15/2016 - UI updated (by RudiP)
*  2.0.3 - Updated BloomSky API references
*  2.0.2 - Fixed DST issue and display time in AM/PM
*  2.0.1 - Fixde issue with pollster
*  2.0 - Perfomance improvements. The DTH now updates almsot twice as fast!
*	 - Updated the "Rain" tile to a "Water Sensor" tile. You can use bloomsky in any water detection app now!
*	 - Removed the "It's Night Time" tile. It was sort of meaningless. 
* 
*  Version: 1.0.7 - Fixed trailing decimcals for android.
*  Version: 1.0.6 - fixed code halting issue with temp conversion introduced by 1.0.5 and fixed last updated display issue
*  Version: 1.0.5 - fixed celcius support, i had removed by accident - @thrash99er
*  Version: 1.0.4 - reincluded lx after bloomsky api change
*
*  Copyright 2016 Tim Slagle
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/

def getVersion() { return "2.1.3"}

metadata {
    definition (name: "Bloomsky", namespace: "RudiP", author: "Tim Slagle") {
        capability "Battery"
        capability "Illuminance Measurement"
        capability "Refresh"
        capability "Polling"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Ultraviolet Index"
        capability "Water Sensor"

        attribute "pressure", "number"
        attribute "lastUpdated", "string"
        attribute "deviceMode", "string"
        // STORM data
        attribute "rainRate", "number"
        attribute "rainDaily", "number"
        attribute "windSpeed", "number"
        attribute "windDirection", "string"
        attribute "windGust", "number"
        attribute "msgStorm", "string"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale:2) {
        multiAttributeTile(name: "main", type:"generic", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°', icon:"st.Weather.weather2", backgroundColors: getTempColors())
			}
            tileAttribute("device.lastUpdated", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Updated: ${currentValue}')
            }
            tileAttribute("device.ultravioletIndex", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: ""
                attributeState "VALUE_DOWN", action: ""
            }
        }

        carouselTile("cameraDetails", "device.image", width: 4, height: 4) { }
        valueTile("temperature", "device.temperature",  width: 2, height: 2) {
            state("default", label:'${currentValue}°', backgroundColors: getTempColors())
        }
        valueTile("uv", "device.ultravioletIndex", width: 2, height: 2) {
            state "default", label:'${currentValue} UV',
                backgroundColors:[
                    [value: 1, color: "#289500"],
                    [value: 3, color: "#F7e400"],
                    [value: 6, color: "#F85900"],
                    [value: 8, color: "#d80010"],
                    [value: 11, color: "#6B49C8"]
                ]
        }
        standardTile("water", "device.water", width: 2, height: 2) {
            state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
            state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label:'${currentValue}% humidity', unit:""
        }
        valueTile("pressure", "device.pressure", inactiveLabel: false, width: 2, height: 2) {
            state "pressure", label:'Pressure: ${currentValue}', unit:""
        }
        valueTile("light", "device.illuminance", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} Lux'
        }
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue}% Battery'
        }
        valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue}'
        }
        standardTile("deviceMode", "device.deviceMode", decoration: "flat", canChangeBackground: true, width: 2, height: 2) {
            state "Day", label: '${currentValue}', icon:"st.Weather.weather14"
            state "Night",  label: '${currentValue}', icon:"st.Weather.weather4"
            state "Default",  label: 'N/A', icon:"st.Weather.weather1"
        }
        standardTile("deviceType", "device.deviceType", decoration: "flat", width: 2, height: 2) {
            state "default", label:'Type: ${currentValue}'
        }
        valueTile("msgStorm", "device.msgStorm", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
            state "default", label:'${currentValue}'
        }

        standardTile("refresh", "device.weather", decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }
        main(["main"])
        details(["cameraDetails", "temperature", "uv",  "water", "humidity", "pressure", "deviceMode", "light", "lastUpdated", "msgStorm", "refresh", "battery", "deviceType"])
    }

    preferences {
        input "pressureMbar", "boolean", title: "Pressure in Mbar? (Default = inHg)", displayDuringSetup:false, defaultValue:false
        input "enableStorm", "boolean", title: "Enable STORM data?", displayDuringSetup:false, defaultValue:false
        input "windspeedKph", "boolean", title: "Wind Speed in Kph? (Default = Mph)", displayDuringSetup:false, defaultValue:false
        input "rainMm", "boolean", title: "Rain in mm? (Default = inches)", displayDuringSetup:false, defaultValue:false
        input "detailDebug", "boolean", title: "Enable Debug logging?", displayDuringSetup:false, defaultValue:false
    }
}    

def getTempColors() {
	def colorMap

    if (tempScale()=="C") {
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 33, color: "#d04e00"],
			[value: 36, color: "#bc2323"]
			]
	} else {
		colorMap = [
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 92, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
	}
}

def installed() {
    log.info "--- BloomSky - Version: ${getVersion()}"
    log.info "--- Device Created"
    state.debug = ("true" == detailDebug)
    refresh()
}

def updated() {
    log.info "--- BloomSky - Version: ${getVersion()}"
    log.info "--- Device Config Updated"
    state.debug = ("true" == detailDebug)

    // Check if STORM is enabled
    if (!("true" == enableStorm)) {
        sendEvent(name:"rainRate", value: 0.0, displayed:false)
        sendEvent(name:"rainDaily", value: 0.0, displayed:false)
        sendEvent(name:"windSpeed", value: 0.0, displayed:false)
        sendEvent(name:"windDirection", value: "N", displayed:false)
        sendEvent(name:"windGust", value: 0.0, displayed:false)
    }

    refresh()
}

def poll() {
    state.debug = ("true" == detailDebug)
    log.info("--- Device Poll")
    callAPI()
}

def refresh() {
    state.debug = ("true" == detailDebug)
    callAPI()
}


//call Bloomsky API and update device
private def callAPI() {
    log.info "--- Refreshing Bloomsky Device: ${device.label}"
    def data = [:]
    
    try {
        httpGet([
            //uri: "https://thirdpartyapi.appspot.com",    
            uri: "https://api.bloomsky.com",
            path: "/api/skydata/",
            requestContentType: "application/json",
            headers: ["Authorization": parent.getAPIKey()]
        ]) { resp ->

            // Get the Device Info of the Correct Bloom Sky
            def individualBloomSky = []
            // If you don't have Device ID specific get the first bloom sky only
            if(device.deviceNetworkId) {
                individualBloomSky = resp.getData().findAll{ it.DeviceID == device.deviceNetworkId }
                if (state.debug) log.debug "Found BloomSky ID: ${device.deviceNetworkId}"
            }
            else {
                individualBloomSky = resp.data[0]
                if (state.debug) log.debug "Using BloomSky ID: ${individualBloomSky.DeviceID}"
            }
            
            // Bloomsky (SKY1/SKY2) data
            data << individualBloomSky.Data
            if (data) {
                if (state.debug) log.debug "--- Getting Bloomsky (SKY1/SKY2) data"

                //itterate through hashmap pairs to update device. Used case statement because it was twice as fast.
                data.each {oldkey, datum->
                    def key = oldkey.toLowerCase() //bloomsky returns camel cased keys. Put to lowercase so dth can update correctly
                    if (state.debug) log.debug "${key}:${datum}"

                    switch(key) {
                        case "voltage": //update "battery"
                            sendEvent(name:"battery", value: getBattery(datum), unit: "%")
                        break;
                        case "ts": //update last update from bloomsky
                            def date = (datum * 1000L)
                            def df = new java.text.SimpleDateFormat("MMM dd hh:mm a")
                            df.setTimeZone(location.timeZone)
                            def lastUpdated = df.format(date)
                            sendEvent(name: "lastUpdated", value: lastUpdated)
                        break;
                        case  "rain": //check if it is raining or not
                            if (datum == true) {
                                sendEvent(name: "water", value: "wet")
                            } else {
                                sendEvent(name: "water", value: "dry")
                            }
                        break;
                        case "luminance": //update illuminance
                            sendEvent(name: "illuminance", value: datum, unit: "Lux")
                        break;
                        case "uvindex": //bloomsky does UV index! how cool is that!?
                            sendEvent(name: "ultravioletIndex", value: datum)
                        break;
                        case "pressure": 
                            def presValue = datum.toDouble().trunc(2)
                            def presUnit = "inHg"
                            if (("true" == pressureMbar)) {
                                presValue =  (presValue / 0.0295301).round(0).toInteger()
                                presUnit = "mbar"
                            } else {
                                presValue = presValue.trunc(1)
                            }
                            sendEvent(name:"${key}", value: presValue, unit: presUnit)
                        break;
                        case "humidity":
                            sendEvent(name: "${key}", value: datum, unit: "%")
                        break;
                        case "temperature": //temperature needs to be converted to celcius in some cases so we break it out on its own
                            def temp = getTemperature(datum).toDouble().trunc(1)
                            sendEvent(name:"${key}", value: temp, unit: tempScale())
                        break;
                        case "imageurl": //lastly update the image from bloomsky to the DTH
                            httpGet(datum) { it -> 
                                storeImage(getPictureName(), it.data)
                            }
                        break;
                        case "night": // Mode = Day or Night
                            def mode = "Day"
                            if (datum == true) {
                                mode = "Night"
                            }
                            sendEvent(name:"deviceMode", value: mode)
                        break;
                        case "devicetype": // Device Type: SKY1 or SKY2
                            sendEvent(name:"deviceType", value: datum)
                        break;

                        default: 
                            if (state.debug) log.debug "${key} is not being used"
                        break;
                    }
                }
            }
            data.clear()

            // Bloomsky (STORM) data
            data << individualBloomSky.Storm
            def rainRate = 0.0
            def rainDaily = 0.0
            def windSpeed = 0.0
            def windDirection = "N"
            def windGust = 0.0
            def msgStorm = "No STORM data"
            def rainUnit = "in"
            if (("true" == rainMm)) {
                rainUnit = "mm"
            }
            def windSpeedUnit = "mph"
            if (("true" == windspeedKph)) {
                windSpeedUnit = "kph"
            }

            if (data && ("true" == enableStorm)) {
                if (state.debug) log.debug "--- Getting Bloomsky (STORM) data"

                data.each {oldkey, datum->
                    def key = oldkey.toLowerCase() //bloomsky returns camel cased keys. Put to lowercase so dth can update correctly
                    if (state.debug) log.debug "${key}:${datum}"

                    switch(key) {
                        case "raindaily":
                            if (datum < 9000) {
                                rainDaily = datum.toDouble()
                                if (("true" == rainMm)) {
                                    rainDaily = rainDaily.trunc(1)
                                } else {
                                    rainDaily =  (rainDaily * 0.039370).round(1).trunc(1)
                                }
                            }
                            sendEvent(name:"rainDaily", value: rainDaily, unit: rainUnit)
                        break;
                        case "rainrate":
                            if (datum < 9000) {
                                rainRate = datum.toDouble()
                                if (("true" == rainMm)) {
                                    rainRate = rainRate.trunc(1)
                                } else {
                                    rainRate =  (rainRate * 0.039370).round(1).trunc(1)
                                }
                            }
                            sendEvent(name:"rainRate", value: rainRate, unit: rainUnit+"/h")
                        break;
                        case "sustainedwindspeed":
                            if (datum < 9000) {
                                windSpeed = datum.toDouble()
                                if (("true" == windspeedKph)) {
                                    windSpeed =  (windSpeed * 1.609344).round(1).trunc(1)
                                } else {
                                    windSpeed = windSpeed.trunc(1)
                                }
                            }
                            sendEvent(name:"windSpeed", value: windSpeed, unit: windSpeedUnit)
                        break;
                        case "winddirection":
                            if (datum < 9000) {
                                windDirection = datum.toString()
                            }
                            sendEvent(name:"windDirection", value: datum)
                        break;
                        case "windgust":
                            if (datum < 9000) {
                                windGust = datum.toDouble()
                                if (("true" == windspeedKph)) {
                                    windGust =  (windGust * 1.609344).round(1).trunc(1)
                                } else {
                                    windGust = windGust.trunc(1)
                                }
                            }
                            sendEvent(name:"windGust", value: windGust, unit: windSpeedUnit)
                        break;
                        default: 
                            if (state.debug) log.debug "${key} is not being used"
                        break;
                    }
                }
                //--- Create STORM data message
                msgStorm = "Wind: " + windSpeed.toString() + windSpeedUnit + "/" + windDirection + " | Rain Rate: " + rainRate.toString() + rainUnit + "/h - Daily: " + rainDaily.toString() + rainUnit

            } else {
                if (state.debug) log.debug "--- STORM data Disabled"
            }
            sendEvent(name: "msgStorm", value: msgStorm, displayed:false)

        }
    }
    //log exception gracefully
    catch (Exception e) {
        log.debug "Error: $e"
    }
    data.clear()
}

//convert temp to celcius if needed
def getTemperature(value) {
    //def cmdScale = getTemperatureScale()
    return convertTemperatureIfNeeded(value.toFloat(), "F", 4)
}

//create unique name for picture storage
private getPictureName() {
    def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
    "image" + "_$pictureUuid" + ".jpg"
}

def getBattery(v) {
    def result = 0
    def miliVolts = v
    // Minimum: 2480 - Maximum: 2620
    def minVolts = 2480
    def maxVolts = 2620
    if (miliVolts >= minVolts) {
        if (miliVolts > maxVolts) {
            result = 100
        } else {
            def pct = (miliVolts - minVolts) / (maxVolts - minVolts)
            result = Math.min(100, (int) pct * 100)
        }
    }

    return result
}

def tempScale() {
    //def tempScale = getTemperatureScale()
    def tempScaleUnit = "C"
    return tempScaleUnit
}
