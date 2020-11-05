********************************
How to use the MorphoPro
********************************
Usage:
fstan -a -f <input file> -o <output file> <language model>

NOTE! The input file must be one token per line.


Example:
fstan -a -f text.txt -o text-morpho.txt italian.asf

Input file format (one token for each line):

Incidente
sulla
A22
coinvolge
il
sig
.
Valdelli
.
Traffico
interrotto
dalle
tre
.


Output file format.
Each line is composed of token and the possible morphological analyzes (the fields is separated by a space character):

Incidente 
sulla su~su+prep/la~det+art+f+sing 
A22 
coinvolge coinvolgere+v+indic+pres+nil+3+sing 
il det+art+m+sing 
sig 
. full_stop+punc 
Valdelli 
. full_stop+punc 
Traffico 
interrotto interrompere+v+part+pass+m+nil+sing interrotto+adj+m+sing+pst 
dalle da'~dare+v+imp+pres+nil+2+sing/le~pro+pron+accdat+f+3+plur da~da+prep/le~det+art+f+plur 
tre tre+adj+_+_+pst+num 
a.m. 
fino fino+adj+m+sing+pst fino+prep fino+adv 
alle a~a+prep/le~det+art+f+plur 
22 
. full_stop+punc 
00 
causa causare+v+indic+pres+nil+3+sing causare+v+imp+pres+nil+2+sing causa+n+f+sing 
sbombero 
automezzi automezzo+n+m+plur 

