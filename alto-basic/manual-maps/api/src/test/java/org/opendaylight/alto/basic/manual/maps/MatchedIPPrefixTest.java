/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.manual.maps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatchedIPPrefixTest {

    @Test
    public void match() throws Exception {
        final String ip1 = "192.168.0.0/32";
        final String ip2 = "192.168.0.0/30";
        boolean matched = new MatchedIPPrefix(ip2).match(new MatchedIPPrefix(ip1));
        assertEquals(matched, true);
    }
}