#!/bin/sh

# Small shell script to start up pooka from the build directory.

cd ../Build
MAIL_CP=../Imported/mail.jar
MBOX_CP=../Imported/mbox.jar
ACT_CP=../Imported/activation.jar
SEC_CP=../Imported/jsse.jar:../Imported/jnet.jar:../Import/jcert.jar
LNF_CP=../Imported/kunststoff.jar

CP=$MAIL_CP:$MBOX_CP:$ACT_CP:$SEC_CP:$LNF_CP:.
java -cp $CP net.suberic.pooka.Pooka

