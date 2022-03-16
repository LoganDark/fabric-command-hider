#!/bin/bash

if [[ "$#" -ne 1 || ! "$1" =~ ^([a-z]+-)*[a-z]+$ ]]; then
	printf 'Usage: rebrand.sh new-mod-name\n'
	exit 0
fi

MODID="$1"
PKGNAME="$(printf "$MODID" | sed 's/-//g')"
CLASSNAME="$(printf "$MODID" | sed 's/\(-\|^\)\([a-z]\)/\U\2/g')"

printf 'New mod ID = %s\n' "$MODID"
printf 'New package name = %s\n' "$PKGNAME"
printf 'New class name = %s\n' "$CLASSNAME"
printf '\n'

read -r -p 'Are you sure? [y/N] ' RESPONSE

if [[ ! "$RESPONSE" =~ ^[yY][eE][sS]|[yY]$ ]]; then
	printf 'Aborting.\n'
	exit 1
fi

printf "$MODID" > ./.idea/.name

function replace() {
	FILE="$1"
	FROM="$2"
	TO="$3"

	printf '%s: %s -> %s\n' "$FILE" "$FROM" "$TO"

	sed -i "s/$FROM/$TO/g" "$FILE"
}

function move() {
	FROM="$1"
	TO="$2"

	printf '%s -> %s\n' "$FROM" "$TO"

	mv "$FROM" "$TO"
}

replace ./.idea/.name barebones "$MODID"
replace .idea/modules/barebones.iml barebones "$MODID"

move ./.idea/modules/barebones.iml \
     ./.idea/modules/"$MODID".iml
move ./.idea/modules/barebones.main.iml \
     ./.idea/modules/"$MODID".main.iml
move ./.idea/modules/barebones.test.iml \
     ./.idea/modules/"$MODID".test.iml

replace ./.idea/runConfigurations/Minecraft_Client.xml barebones "$MODID"
replace ./.idea/runConfigurations/Minecraft_Server.xml barebones "$MODID"

replace ./gradle.properties barebones "$MODID"
replace ./settings.gradle barebones "$MODID"

find ./src/main/{java,kotlin} -type f -print0 | xargs -0 sed -i "s/barebones/$PKGNAME/g"
find ./src/main/{java,kotlin} -type f -print0 | xargs -0 sed -i "s/Barebones/$CLASSNAME/g"

move ./src/main/kotlin/net/logandark/barebones/Barebones.kt \
     ./src/main/kotlin/net/logandark/barebones/"$CLASSNAME".kt
move ./src/main/java/net/logandark/barebones \
     ./src/main/java/net/logandark/"$PKGNAME"
move ./src/main/kotlin/net/logandark/barebones \
     ./src/main/kotlin/net/logandark/"$PKGNAME"

replace ./src/main/resources/fabric.mod.json barebones "$MODID"
replace ./src/main/resources/fabric.mod.json net\\.logandark\\."$MODID" net.logandark."$PKGNAME"
replace ./src/main/resources/fabric.mod.json Barebones "$CLASSNAME"
replace ./src/main/resources/barebones.mixins.json barebones "$PKGNAME"

move ./src/main/resources/assets/barebones \
     ./src/main/resources/assets/"$MODID"
move ./src/main/resources/barebones.mixins.json \
     ./src/main/resources/"$MODID".mixins.json

rm rebrand.sh
