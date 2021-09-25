#!/usr/bin/env bash

# Convert a Kotlin model into dokka documentation
#
# val property: Type? // Hello World
# Becomes
# * @param[property] Hello World

dir="/tmp/convert_kotlin_to_dokka"
tmp="$dir/convert.kt"

# Setup
rm -rf $dir
mkdir $dir

cp $1 $tmp
sed -i.bak $'/@Json.*/d' $tmp
sed -i.bak -E 's/^[[:space:]]*va[l|r] ([[:alnum:]]+)[^\/]+\/\//* @param[\1]/i' $tmp
sed -i.bak -E '/^[[:space:]]*$/d' $tmp
cat $tmp
