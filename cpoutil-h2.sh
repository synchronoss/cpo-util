#!/bin/sh
#
# Copyright (C) 2003-2012 David E. Berry, Michael A. Bellomo
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
#
# A copy of the GNU Lesser General Public License may also be found at
# http://www.gnu.org/licenses/lgpl.txt
#

# start h2
nohup java -cp ~/dbjars/h2-2.4.240.jar org.h2.tools.Server -user sa -tcp -tcpPort 9092 > h2.log 2>&1 &

# initialize the database
java -cp ~/dbjars/h2-2.4.240.jar org.h2.tools.RunScript -url jdbc:h2:tcp://localhost:9092/~/cpo -user sa -script /Users/dberry/project/cpo/api/cpo-jdbc/src/test/resources/h2/initDB.sql

# start cpoutil
mvn exec:java -Dexec.mainClass="org.synchronoss.cpo.util.CpoUtil" -e

# stop h2
java -cp ~/dbjars/h2-2.4.240.jar org.h2.tools.Server -tcpShutdown tcp://localhost:9092/~/cpo
