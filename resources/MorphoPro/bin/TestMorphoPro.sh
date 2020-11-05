#! /bin/tcsh -f

### TestMorphoPro         ###
### V1.2.0, 2007          ###

### TestMorphoPro Version ###
set VERSION = "1.2.0, 2007"

### Do not modify ###
set MORPHOPRO_DIR = $TEXTPRO/MorphoPro

$MORPHOPRO_DIR/bin/MorphoPro.sh -v

echo ""
echo "testing ..."

$MORPHOPRO_DIR/bin/MorphoPro.sh -m a -l ENG -o $MORPHOPRO_DIR/test/example-eng-01.tst $MORPHOPRO_DIR/test/example-eng-01.txt

echo

set diff_eng = "eng-model"
set diff_eng = `cmp $MORPHOPRO_DIR/test/example-eng-01.tst $MORPHOPRO_DIR/test/example-eng-01-old.mor | cut -f1 -d' '`
if ( $diff_eng == "" ) then
  if (! -z $MORPHOPRO_DIR/test/example-eng-01.tst) then
    echo "================"
    echo "All tests passed"
    echo "================"
  endif
endif
else
  echo "================"
  echo "TEST FAILED!"
  echo "================"
endif

rm $MORPHOPRO_DIR/test/*.tst
