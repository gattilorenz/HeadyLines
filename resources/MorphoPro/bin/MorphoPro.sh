#! /bin/tcsh -f

### MorphoPro    ###
### V1.3.1, 2007 ###

### How to use MorphoPro ###
### e.g.
### ./MorphoPro.sh ../test/example-eng-01.txt
### cat ../test/example-eng-01.txt | ./MorphoPro.sh

### input file format
### file: the input file must be in one-word-per-line lowercase format, i.e. each line contains one token (word, punctuation character or
### parenthesis). A space line signals the end of a sentence. '#' is a reserved character and can not be used as a token!

### Do not modify ###
set MORPHOPRO_DIR = $TEXTPRO/modules/MorphoPro
set MODEL_NAME = "" #current model name
set ENG_MODEL_NAME = "english-utf8.fsa" #The English model
set ITA_MODEL_NAME = "italian-utf8.fsa" #The Italian model

### Initialize Parameters ###
set outFileName = "" #use this FILE as output file
set inFileName = "" #use this FILE as input file
set language = "ENG" #the language [ENG|ITA]
set mode = "a" #mode of operation [ANALYSIS|SYNTHESIS]

setenv LC_ALL en_GB.UTF8

### Read Parameters ###
while ( {$1} != {} )

    if ( {$1} == "-h" || {$1} == "--help" ) then
        echo "MorphoPro"
        echo "Copyright (C) 2007 All rights reserved."
        echo ""
        echo "Usage: MorphoPro [options] file"
        echo "  -h, --help                        show this help and exit"
        echo "  -v, --version                     show the version and exit"
	echo "  -s, --system                      check the system and exit"
        echo "  -m, --mode=MODE                   mode of operation [a|s]  (default "$mode")"
        echo "  -l, --language=LANGUAGE           the language [ENG|ITA]  (default "$language")"
        echo "  -o, --output=FILE                 use FILE as output file"
        echo ""
        exit 0
    else if ( {$1} == "-v" || {$1} == "--version" ) then
        set mode = `echo '$version$' | $MORPHOPRO_DIR/bin/fstan $MORPHOPRO_DIR/models/$ENG_MODEL_NAME`
	echo "MorphoPro "$mode
        
        exit 0
    else if ( {$1} == "-s" || {$1} == "--system" ) then
        $MORPHOPRO_DIR/bin/TestMorphoPro.sh
        exit 0
    else if ( {$1} == "-m" || {$1} == "--mode" ) then
        shift
        set mode = $1
    else if ( {$1} == "-l" || {$1} == "--language" ) then
        shift
        set language = $1
    else if ( {$1} == "-o" || {$1} == "--output" ) then
        shift
        set outFileName = $1
    else if ( $# == 1 ) then
        set inFileName = $1
    else
        $MORPHOPRO_DIR/bin/MorphoPro.sh -h
        exit 0
    endif

    shift

end

### Check Parameters ###
if ( $inFileName != "" ) then
    if ( ! -f $inFileName ) then
      echo "Error: No such file or directory!"
    exit 1
    endif
endif
if ( $mode != "a" ) then
  if ( $mode != "s" ) then
    echo "Error: Unrecognized mode of operation "$mode"!"
    exit 1
  endif
endif
if ( $language != "ENG" ) then
  if ( $language != "ITA" ) then
    echo "Error: Unrecognized language option "$language"!"
    exit 1
  endif
endif

### Set Variables ###
if ( $language == "ENG") then
    set MODEL_NAME = $ENG_MODEL_NAME 
else if ( $language == "ITA" ) then
    set MODEL_NAME = $ITA_MODEL_NAME
endif
if ( $mode != "" ) then
    set mode = "-"$mode
endif

set sys = `uname -s`
set FSTANBIN = "fstan/linux_64"
if ( $sys == "Darwin" ) then
	set FSTANBIN = "fstan/x86_64"
endif 

### Run the System ###
# $TEXTPRO/bin/tolowercase $inFileName | $MORPHOPRO_DIR/bin/fstan $mode $outFileName $MORPHOPRO_DIR/models/$MODEL_NAME
#
# 2012-08-20: 
# awk is used to get lowercase tokens
if ( $inFileName != "" ) then
  if ( $outFileName != "" ) then
    awk '{if ($0 == "") printf("%s\n", "_EOS_"); else printf("%s\n", tolower($0));}' $inFileName | $MORPHOPRO_DIR/bin/$FSTANBIN/fstan $mode $MORPHOPRO_DIR/models/$MODEL_NAME | awk '{if ($0 == "_EOS_ ") printf("%s\n", ""); else printf("%s\n", $0);}' > $outFileName
  else
    awk '{if ($0 == "") printf("%s\n", "_EOS_"); else printf("%s\n", tolower($0));}' $inFileName | $MORPHOPRO_DIR/bin/$FSTANBIN/fstan $mode $MORPHOPRO_DIR/models/$MODEL_NAME | awk '{if ($0 == "_EOS_ ") printf("%s\n", ""); else printf("%s\n", $0);}'
  endif
else
  if ( $outFileName != "" ) then
    cat $_ | awk '{if ($0 == "") printf("%s\n", "_EOS_"); else printf("%s\n", tolower($0));}' | $MORPHOPRO_DIR/bin/$FSTANBIN/fstan $mode $MORPHOPRO_DIR/models/$MODEL_NAME | awk '{if ($0 == "_EOS_ ") printf("%s\n", ""); else printf("%s\n", $0);}' > $outFileName
  else
    cat $_ | awk '{if ($0 == "") printf("%s\n", "_EOS_"); else printf("%s\n", tolower($0));}' | $MORPHOPRO_DIR/bin/$FSTANBIN/fstan $mode $MORPHOPRO_DIR/models/$MODEL_NAME | awk '{if ($0 == "_EOS_ ") printf("%s\n", ""); else printf("%s\n", $0);}'
  endif
endif







