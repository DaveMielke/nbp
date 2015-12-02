programName="${0##*/}"

writeLine() {
  local line="${1}"

  echo >&2 -e "${line}"
}

writeCharacters() {
  local characters="${1}"

  writeLine "${characters}\\c"
}

rewriteLine() {
  local line="${1}"

  writeCharacters "\\r${line}\\e[K\e[${#line}D"
}

clearLine() {
  rewriteLine ""
}

programMessage() {
  local message="${1}"

  [ -z "${message}" ] || writeLine "${programName}: ${message}"
}

askUser() {
  local prompt="${1}"
  shift 1

  read -p "${prompt}" -- "${@}" || {
    echo >&2 ""
    programMessage "end of file"
    exit 1
  }
  
  ! REPLY="$(expr "${REPLY}" : ' *\(.*\)')" ||
  REPLY="$(expr "${REPLY}" : '\(.*[^ ]\) *$')"
}

confirmAction() {
  local action="${1}"

  while true
  do
    askUser "${action}? "

    case "${REPLY}"
    in
      yes|ye|y) return 0;;
      no|n) return 1;;
      *) programMessage "unrecognized confirmation: ${REPLY}";;
    esac
  done
}

isVariable() {
  local variable="${1}"

  local first="a-zA-Z_"
  local rest="${first}0-9"

  [ "$(expr "${variable}" : "[${first}][${rest}]*\$")" -eq 0 ] && return 1
  return 0
}

getVariable() {
  eval 'echo ${'"${1}"'}'
}

setVariable() {
  eval "${1}='${2}'"
}

