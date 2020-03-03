##############################################################################
# Copyright (c) 2020 OpenALTO. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
##############################################################################
#!/bin/bash

cd $(dirname $0)/..
PROJECT_ROOT=$(pwd)
KAR_ROOT=$PROJECT_ROOT/alto-karaf/target/assembly
ALTO_KAR_ROOT=$KAR_ROOT/system/org/opendaylight/alto/

generate_patch() {
    TMP_DIR=$(mktemp -d)
    TMP_PATCH_ROOT=$TMP_DIR/odl-alto-patch
    mkdir -p $TMP_PATCH_ROOT/alto
    cp -r $ALTO_KAR_ROOT $TMP_PATCH_ROOT/alto/
    cp $PROJECT_ROOT/utils/install_patch.sh $TMP_PATCH_ROOT/
    pushd $TMP_DIR
    zip -r odl-alto-patch odl-alto-patch
    mkdir -p $1
    echo "Putting patch archieve to $1"
    mv odl-alto-patch.zip $1/
    popd
    rm -rf $TMP_DIR
}

usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "    <dir>      directory to put the patch"
    echo "    help       display help information"
}

if [ -z $1 ]; then
    usage
    exit 0
fi

case $1 in
    help)
        usage
        ;;
    *)
        # Generate patch to directory
        if [ -d $ALTO_KAR_ROOT ]; then
            generate_patch $(realpath $1)
        else
            echo "$ALTO_KAR_ROOT not found"
            echo "Please build alto-karaf first"
            exit -1
        fi
esac
