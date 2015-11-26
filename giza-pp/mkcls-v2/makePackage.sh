/*

Copyright (C) 1997,1998,1999,2000,2001  Franz Josef Och

mkcls - a program for making word classes .

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, 
USA.

*/
#! /bin/csh

setenv VERSION `date +%Y-%m-%d`
rm -rf mkcls-v2

mkdir mkcls-v2
foreach i ( Array.h FixedArray.h FlexArray.h GDAOptimization.C GDAOptimization.h HCOptimization.C HCOptimization.h IterOptimization.C IterOptimization.h KategProblem.C KategProblem.h KategProblemKBC.C KategProblemKBC.h KategProblemTest.C KategProblemTest.h KategProblemWBC.C KategProblemWBC.h MSBOptimization.C MSBOptimization.h MYOptimization.C MYOptimization.h Optimization.C Optimization.h PopOptimization.C PopOptimization.h Problem.C Problem.h ProblemTest.C ProblemTest.h RRTOptimization.C RRTOptimization.h SAOptimization.C SAOptimization.h StatVar.C StatVar.h TAOptimization.C TAOptimization.h general.C general.h makePackage.sh mkcls.C my.h myassert.h myleda.h mystl.h )
    cat $i | filterIfdef.out NO_LIGHT_GIZA | filterIfdefInverse.out DEBUG | filterIfdefInverse.out DEBUG_TRICKY_IBM3 | filterIfdefInverse.out VDEBUG | stripcmt | addHead.out -file header > mkcls-v2/$i
end

cp Makefile.simple mkcls-v2/Makefile
cp ../giza++/GNU.GPL mkcls-v2
cp ../giza++/LICENSE mkcls-v2
cp README mkcls-v2

tar cf - mkcls-v2 | gzip -9 > mkcls.$VERSION.tar.gz

cd mkcls-v2
gmake -k 
cd ..

