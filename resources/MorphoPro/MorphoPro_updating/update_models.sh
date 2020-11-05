#! /bin/sh

LANG=$1
CSV=resources/eng/morphodictionary.csv
MORPHOASFDIR=models/english/
MORPHOASFFILE=english-utf8.fsa

if [ $LANG = "ITA" ]; then
	CSV=resources/ita/morphodictionary.csv
	MORPHOASFDIR=models/italian/
	MORPHOASFFILE=italian-utf8.fsa
fi

SOLABEL=`uname -s`

echo "creating an addon morphological automata for Italian"
bin/$SOLABEL/fstmc -v -f ../../../conf/$CSV addon.asf
bin/$SOLABEL/fstmerge $MORPHOASFDIR$MORPHOASFFILE,addon.asf merge.asf
\rm addon.asf

if [ -e "merge.asf" ]; then
echo "moving the new model into MorphoPro directory..."
mv merge.asf ../models/$MORPHOASFFILE
echo "DONE!"
fi

