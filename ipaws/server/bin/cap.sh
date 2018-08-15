includeScriptLibraries xml files

defineEnumeration capUrgencyEnumeration Immediate Expected Future Past Unknown
defineEnumeration capSeverityEnumeration Extreme Severe Moderate Minor Unknown
defineEnumeration capCertaintyEnumeration Observed Likely Possible Unlikely Unknown

readonly capAlertElement="/ns1:alerts/alert"

capGetAlertCount() {
   local alerts="${1}"

   xmlEvaluate "${alerts}" "count(${capAlertElement})"
} && readonly -f capGetAlertCount

capGetAlertElement() {
   local alerts="${1}"
   local index="${2}"

   xmlEvaluate "${alerts}" "${capAlertElement}[${index}]"
} && readonly -f capGetAlertElement

capCacheAlertProperties() {
   local alert="${1}"

   capCacheAlertProperty "${alert}" type /alert/msgType
   capCacheAlertProperty "${alert}" references /alert/references

   capCacheAlertProperty "${alert}" status /alert/status
   capCacheAlertProperty "${alert}" scope /alert/scope

   capCacheAlertProperty "${alert}" sent /alert/sent
   capCacheAlertProperty "${alert}" sender /alert/sender

   capCacheAlertProperty "${alert}" effective /alert/info/effective
   capCacheAlertProperty "${alert}" expires /alert/info/expires

   capCacheAlertProperty "${alert}" urgency /alert/info/urgency
   capCacheAlertProperty "${alert}" severity /alert/info/severity
   capCacheAlertProperty "${alert}" certainty /alert/info/certainty

   capCacheAlertValues "${alert}" area /alert/info/area/geocode
   capCacheAlertValues "${alert}" event /alert/info/eventCode
} && readonly -f capCacheAlertProperties

capCacheAlertValues() {
   local alert="${1}"
   local property="${2}"
   local element="${3}"

   set -- $(xmlEvaluate "${alert}" "${element}/valueName/text()" | sort -u)
   local name

   for name
   do
      capCacheAlertProperty "${alert}" "${property}.${name}" "${element}[valueName/text()='${name}']/value"
   done
} && readonly -f capCacheAlertValues

capCacheAlertProperty() {
   local alert="${1}"
   local property="${2}"
   local element="${3}"

   capSetAlertProperty "${alert}" "${property}" "$(xmlEvaluate "${alert}" "${element}/text()")"
} && readonly -f capCacheAlertProperty

capSetAlertProperty() {
   local alert="${1}"
   local property="${2}"
   local value="${3}"

   setFileAttribute "${alert}" "NBP.CAP.alert.${property}" "${value}"
} && readonly -f capSetAlertProperty

capGetAlertProperty() {
   local alert="${1}"
   local property="${2}"

   getFileAttribute "${alert}" "NBP.CAP.alert.${property}"
} && readonly -f capGetAlertProperty

