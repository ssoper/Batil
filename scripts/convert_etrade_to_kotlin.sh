#!/usr/bin/env bash

# Convert E*TRADE API documentation directly into a Kotlin model
#
# Example
# Copy and paste the API documentation into a file called quote_details.txt. Use content
# from https://apisb.etrade.com/docs/api/market/api-quote-v1.html#/definitions/AllQuoteDetails
# as an example.
#
# Input:
# Property	Type	Description	Possible Values
# adjustedFlag	boolean	Indicates whether an option has been adjusted due to a corporate action (for example, a dividend or stock split)
# annualDividend	number (double)	Cash amount paid per share over the past year
# askExchange	string	Code for the exchange reporting the ask price
#
# Run the script
# ./convert_etrade_to_kotlin.sh quote_details.txt
#
# Output:
# val adjustedFlag: Boolean  // Indicates whether an option has been adjusted due to a corporate action (for example, a dividend or stock split)
# val annualDividend: Float  // Cash amount paid per share over the past year
# val askExchange: String    // Code for the exchange reporting the ask price
#

dir="/tmp/convert_api_docs_to_kotlin"
tmp="$dir/convert.kt"
charCount="$dir/charCount"
result="$dir/result.kt"
declaration="__D__"

# Setup
rm -rf $dir
mkdir $dir

# Remove column headers if they exist
sed $'/^Property.*/d' $1 > $tmp

# Remove trailing tabs
sed -i.bak $'s/\t$//' $tmp

# Prepend val
sed -i.bak 's/^/val /' $tmp

# Convert types to Kotlin equivalents
sed -i.bak $'s/\tnumber.*\(\t\)/: Float?'$declaration'/' $tmp
sed -i.bak $'s/\tinteger.*\(\t\)/: Int?'$declaration'/' $tmp
sed -i.bak $'s/\tstring.*\(\t\)/: String?'$declaration'/' $tmp
sed -i.bak $'s/\tboolean.*\(\t\)/: Boolean?'$declaration'/' $tmp
sed -i.bak $'s/\t\(.*\)\(\t\)/: UNKNOWN'$declaration'/' $tmp

# Prepend // to description
sed -i.bak $'s/\t/, \/\/ /2' $tmp
sed -i.bak $'s/'$declaration'/, \/\/ /' $tmp

# Get largest character position of all comments
while read line; do
  match="${line%%,*}"
  if [ "$match" != "$line" ]; then
    echo ${#match} >> $charCount
  fi
done < "$tmp"
sort -r -o $charCount $charCount
minChar=$(head -n 1 "$charCount")

# Normalize spacing of comments
while read line; do
  match="${line%%,*}"
  if [ "${#match}" -lt "$minChar" ]; then
    spaces=$(( $minChar - ${#match} ))
    echo -n "${match}," >> $result
    echo -n "$(seq -s " " $spaces | tr -d '[:digit:]')" >> $result
    echo "${line#*,}" >> $result
  else
    echo "$line" >> $result
  fi
done < "$tmp"

cat $result
