#!/bin/sh

OUTPUT_PATH=$1
if [ -z $OUTPUT_PATH ]; then
	echo "Usage : $0 output_path" 
	exit
fi
if [ -d $OUTPUT_PATH ];then
	read -P "所输入的目录已存在,是否覆盖 :(y/n)" yn
	if [ "$yn" != "Y" ] && [ "$yn" != "y" ]; then
		echo "build终止.."
		exit
	else
		rm -rf $OUTPUT_PATH
	fi
fi

mkdir $OUTPUT_PATH


PROJECT_PATH=$(cd `dirname $0`; pwd)
JAR_PATH=$PROJECT_PATH/libs
SRC_PATH=$PROJECT_PATH/src
RESOURCE_PATH=$PROJECT_PATH/resources

find $SRC_PATH -name *.java > $SRC_PATH/sources.list

cp -r $RESOURCE_PATH $OUTPUT_PATH

javac -d $OUTPUT_PATH -classpath $OUTPUT_PATH/libs/*.jar @$SRC_PATH/sources.list

rm $SRC_PATH/sources.list