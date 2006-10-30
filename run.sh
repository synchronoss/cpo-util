#!/bin/sh
#/*
# *  Copyright (C) 2006  David E. Berry
# *
# *  This library is free software; you can redistribute it and/or
# *  modify it under the terms of the GNU Lesser General Public
# *  License as published by the Free Software Foundation; either
# *  version 2.1 of the License, or (at your option) any later version.
# *  
# *  This library is distributed in the hope that it will be useful,
# *  but WITHOUT ANY WARRANTY; without even the implied warranty of
# *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# *  Lesser General Public License for more details.
# *
# *  You should have received a copy of the GNU Lesser General Public
# *  License along with this library; if not, write to the Free Software
# *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
# *  
# *  A copy of the GNU Lesser General Public License may also be found at 
# *  http://www.gnu.org/licenses/lgpl.txt
# */


CLASSES=./lib/gnu-regexp-1.1.4.jar
CLASSES=./lib/mysql-connector-java-5.0.4-bin.jar:$CLASSES
CLASSES=./lib/log4j.jar:$CLASSES
CLASSES=./lib/systray4j.jar:$CLASSES
CLASSES=./lib/cpo.jar:$CLASSES
CLASSES=./lib/hsqldb.jar:$CLASSES
CLASSES=./lib/ojdbc14.jar:$CLASSES
CLASSES=./bin/utilscpo.jar:$CLASSES

echo "CLASSPATH=$CLASSES"

java -cp $CLASSES org.synchronoss.utils.cpo.CpoUtil

