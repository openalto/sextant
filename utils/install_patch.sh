##############################################################################
# Copyright (c) 2020 OpenALTO. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
##############################################################################
#!/bin/bash

cd $(dirname $0)
KARAF_ROOT=$1

cp -r alto/* $KARAF_ROOT/system/org/opendaylight/alto/
