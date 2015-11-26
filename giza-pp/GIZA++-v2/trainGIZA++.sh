#! /bin/csh

if( $# != 3 ) then

 echo Usage: trainGIZA++.sh vcb1 vcb2 snt
 echo " "
 echo Performs a training of word classes and a standard GIZA training.

else

    snt2plain.out $1 $2 $3 PLAIN

    mkcls -m2 -pPLAIN1.txt -c50 -V$1.classes opt >& mkcls1.log
    rm PLAIN1.txt
    mkcls -m2 -pPLAIN2.txt -c50 -V$2.classes opt >& mkcls2.log
    rm PLAIN2.txt
    GIZA++ -S $1 -T $2 -C $3 -p0 0.98 -o GIZA++ >& GIZA++.log

endif
